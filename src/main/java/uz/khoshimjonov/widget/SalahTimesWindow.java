package uz.khoshimjonov.widget;

import lombok.Getter;
import lombok.Setter;
import uz.khoshimjonov.service.ConfigurationManager;
import uz.khoshimjonov.service.HijriDate;
import uz.khoshimjonov.service.IslamicCalendar;
import uz.khoshimjonov.service.LanguageHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.List;
import java.util.Timer;

@Getter
@Setter
public class SalahTimesWindow extends JDialog {

    private static final Color BACKGROUND_PRIMARY = new Color(18, 18, 24);
    private static final Color BACKGROUND_CARD = new Color(38, 38, 48);
    private static final Color ACCENT_PRIMARY = new Color(34, 197, 94);
    private static final Color ACCENT_SECONDARY = new Color(16, 185, 129);
    private static final Color ACCENT_GOLD = new Color(251, 191, 36);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(55, 55, 65);
    private static final Color CURRENT_SALAH_BG = new Color(34, 197, 94, 25);
    private static final Color CURRENT_SALAH_BORDER = new Color(34, 197, 94, 100);
    private static final Color DANGER_COLOR = new Color(220, 38, 38);
    private static final Color DANGER_TEXT = new Color(248, 113, 113);

    private static final String FONT_FAMILY = "Segoe UI";
    private static final int FONT_SIZE_TITLE = 32;
    private static final int FONT_SIZE_LARGE = 20;
    private static final int FONT_SIZE_MEDIUM = 16;
    private static final int FONT_SIZE_NORMAL = 14;
    private static final int FONT_SIZE_SMALL = 12;
    private static final int FONT_SIZE_TINY = 11;

    private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    private AWTEventListener outsideClickListener;
    private WindowAdapter windowAdapter;
    private final Runnable onCloseCallback;
    private boolean isClosing = false;
    private Timer updateTimer;
    private final Map<String, LocalTime> timings;
    private final List<String> orderedKeys;
    private int currentSalahIndex = -1;
    private int nextSalahIndex = -1;
    private final String userLanguage;

    private JPanel contentPanel;
    private JLabel currentTimeLabel;
    private JLabel countdownValueLabel;
    private JLabel nextSalahInfoLabel;
    private JPanel prayerTimesCardPanel;
    private final Map<Integer, JLabel> remainingFromNowLabels = new HashMap<>();
    private final Map<Integer, JLabel> remainingFromPrevLabels = new HashMap<>();

    public SalahTimesWindow(Map<String, LocalTime> timings, Runnable onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
        this.timings = timings != null ? new LinkedHashMap<>(timings) : new LinkedHashMap<>();
        this.userLanguage = configurationManager.getUserLanguage();
        this.orderedKeys = new ArrayList<>(this.timings.keySet());

        determineCurrentAndNextSalah();

        setUndecorated(true);
        setMinimumSize(new Dimension(420, 640));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        setFocusableWindowState(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BACKGROUND_PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        contentPanel.add(createHeaderSection());
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(createCurrentTimeSection());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(createPrayerTimesSection());
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createUpcomingEventsSection());
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createButtonSection());

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel borderPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 24, 24));
                g2.dispose();
            }
        };
        borderPanel.setOpaque(false);
        borderPanel.add(mainPanel, BorderLayout.CENTER);

        getContentPane().setBackground(new Color(0, 0, 0, 0));
        getContentPane().add(borderPanel);
        pack();
        setPosition();
        installOutsideClickCloser();
        startUpdateTimer();
        setVisible(true);
        toFront();
        requestFocus();
    }

    private void applyRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private void determineCurrentAndNextSalah() {
        if (timings == null || timings.isEmpty() || orderedKeys.isEmpty()) {
            currentSalahIndex = -1;
            nextSalahIndex = -1;
            return;
        }

        LocalTime now = LocalTime.now();
        int size = orderedKeys.size();

        currentSalahIndex = -1;
        nextSalahIndex = -1;

        for (int i = 0; i < size; i++) {
            String key = orderedKeys.get(i);
            LocalTime salahTime = timings.get(key);

            if (salahTime != null && now.isBefore(salahTime)) {
                nextSalahIndex = i;
                currentSalahIndex = (i > 0) ? i - 1 : size - 1;
                return;
            }
        }

        currentSalahIndex = size - 1;
        nextSalahIndex = 0;
    }

    private JPanel createHeaderSection() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        HijriDate hijriDate = HijriDate.today();

        JPanel yearPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        yearPanel.setOpaque(false);

        JLabel decorLeft = new JLabel("<");
        decorLeft.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL));
        decorLeft.setForeground(ACCENT_GOLD);

        JLabel yearLabel = new JLabel(hijriDate.getYear() + " " + LanguageHelper.getText("hijriYearSuffix"));
        yearLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_LARGE));
        yearLabel.setForeground(TEXT_PRIMARY);

        JLabel decorRight = new JLabel(">");
        decorRight.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL));
        decorRight.setForeground(ACCENT_GOLD);

        yearPanel.add(decorLeft);
        yearPanel.add(yearLabel);
        yearPanel.add(decorRight);
        yearPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String monthName = hijriDate.getMonthName(userLanguage);
        JLabel dateLabel = new JLabel(hijriDate.getDay() + ", " + monthName);
        dateLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_LARGE));
        dateLabel.setForeground(TEXT_SECONDARY);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        LocalDate today = LocalDate.now();
        String gregorianDateStr = getDayOfWeekLocalized() + " " + formatGregorianDate(today);
        JLabel gregorianLabel = new JLabel(gregorianDateStr);
        gregorianLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL));
        gregorianLabel.setForeground(TEXT_MUTED);
        gregorianLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(yearPanel);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(dateLabel);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(gregorianLabel);

        String specialDay = hijriDate.getSpecialDay();
        if (specialDay != null) {
            JPanel specialPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
            specialPanel.setOpaque(false);

            JLabel specialLabel = new JLabel("* " + specialDay + " *");
            specialLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_SMALL));
            specialLabel.setForeground(ACCENT_GOLD);
            specialPanel.add(specialLabel);
            specialPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            headerPanel.add(Box.createVerticalStrut(2));
            headerPanel.add(specialPanel);
        }

        return headerPanel;
    }

    private String formatGregorianDate(LocalDate date) {
        Locale locale = getLocaleFromLanguage(userLanguage);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale);
        return date.format(formatter);
    }

    private String getDayOfWeekLocalized() {
        Locale locale = getLocaleFromLanguage(userLanguage);
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE", locale);
        String dayName = today.format(formatter);
        return dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
    }

    private Locale getLocaleFromLanguage(String language) {
        if (language == null) {
            return Locale.ENGLISH;
        }
        return switch (language.toLowerCase()) {
            case "ru" -> new Locale("ru", "RU");
            case "uz" -> new Locale("uz", "UZ");
            default -> Locale.ENGLISH;
        };
    }

    private JPanel createCurrentTimeSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        currentTimeLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        currentTimeLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_TITLE));
        currentTimeLabel.setForeground(ACCENT_PRIMARY);
        currentTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateCurrentTimeLabel();

        JPanel countdownPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        countdownPanel.setOpaque(false);

        if (nextSalahIndex >= 0 && !orderedKeys.isEmpty()) {
            String nextSalahName = orderedKeys.get(nextSalahIndex);

            nextSalahInfoLabel = createStyledLabel(
                    LanguageHelper.getText("nextPrayer") + ": " + nextSalahName + ", " + LanguageHelper.getText("inTime") + " ",
                    FONT_SIZE_MEDIUM, TEXT_SECONDARY, false
            );

            countdownValueLabel = createStyledLabel("", FONT_SIZE_MEDIUM, ACCENT_PRIMARY, true);
            updateCountdownLabel();

            countdownPanel.add(nextSalahInfoLabel);
            countdownPanel.add(countdownValueLabel);
        } else {
            JLabel noDataLabel = createStyledLabel(
                    LanguageHelper.getText("loadingTitle"),
                    FONT_SIZE_MEDIUM, TEXT_SECONDARY, false
            );
            countdownPanel.add(noDataLabel);
        }
        countdownPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(currentTimeLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(countdownPanel);

        return panel;
    }

    private JLabel createStyledLabel(String text, int fontSize, Color color, boolean bold) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        label.setFont(new Font(FONT_FAMILY, bold ? Font.BOLD : Font.PLAIN, fontSize));
        label.setForeground(color);
        return label;
    }

    private void updateCurrentTimeLabel() {
        if (currentTimeLabel != null) {
            LocalTime now = LocalTime.now();
            currentTimeLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void updateCountdownLabel() {
        if (countdownValueLabel != null && nextSalahIndex >= 0 && !orderedKeys.isEmpty()) {
            String nextKey = orderedKeys.get(nextSalahIndex);
            LocalTime nextTime = timings.get(nextKey);
            if (nextTime != null) {
                Duration remaining = calculateRemainingTime(LocalTime.now(), nextTime);
                countdownValueLabel.setText(formatDurationHHMMSS(remaining));
            }
        }
    }

    private JPanel createPrayerTimesSection() {
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setOpaque(false);
        containerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String address = configurationManager.getAddress();
        double latitude = configurationManager.getLatitude();
        double longitude = configurationManager.getLongitude();
        address = address == null || address.isEmpty() ? (latitude + ", " + longitude) : address;
        JLabel titleLabel = createStyledLabel(
                LanguageHelper.getText("prayerTimesTitle") + ": " + address,
                FONT_SIZE_MEDIUM, TEXT_SECONDARY, true
        );
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerPanel.add(titleLabel);
        containerPanel.add(Box.createVerticalStrut(10));

        prayerTimesCardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BACKGROUND_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        prayerTimesCardPanel.setLayout(new BoxLayout(prayerTimesCardPanel, BoxLayout.Y_AXIS));
        prayerTimesCardPanel.setOpaque(false);
        prayerTimesCardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        prayerTimesCardPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        buildPrayerTimesContent();

        containerPanel.add(prayerTimesCardPanel);
        return containerPanel;
    }

    private void buildPrayerTimesContent() {
        prayerTimesCardPanel.removeAll();
        remainingFromNowLabels.clear();
        remainingFromPrevLabels.clear();

        if (timings == null || timings.isEmpty()) {
            JLabel loading = createStyledLabel(
                    LanguageHelper.getText("loadingTitle"),
                    FONT_SIZE_MEDIUM, TEXT_SECONDARY, false
            );
            loading.setAlignmentX(Component.CENTER_ALIGNMENT);
            prayerTimesCardPanel.add(loading);
        } else {
            LocalTime now = LocalTime.now();
            int size = orderedKeys.size();

            for (int i = 0; i < size; i++) {
                String salahName = orderedKeys.get(i);
                LocalTime salahTime = timings.get(salahName);

                if (salahTime == null) continue;

                boolean isCurrent = (i == currentSalahIndex);
                boolean isNext = (i == nextSalahIndex);
                boolean isPast = now.isAfter(salahTime) && !isCurrent;

                JPanel rowPanel = createPrayerTimeRow(salahName, salahTime, i, isCurrent, isNext, isPast);
                prayerTimesCardPanel.add(rowPanel);

                if (i < size - 1) {
                    prayerTimesCardPanel.add(Box.createVerticalStrut(4));
                    JSeparator separator = new JSeparator() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            applyRenderingHints(g2);
                            g2.setColor(new Color(70, 70, 80, 120));
                            g2.fillRect(0, 0, getWidth(), 1);
                            g2.dispose();
                        }
                    };
                    separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    separator.setOpaque(false);
                    prayerTimesCardPanel.add(separator);
                    prayerTimesCardPanel.add(Box.createVerticalStrut(4));
                }
            }
        }

        prayerTimesCardPanel.revalidate();
        prayerTimesCardPanel.repaint();
    }

    private JPanel createPrayerTimeRow(String salahName, LocalTime time, int index,
                                       boolean isCurrent, boolean isNext, boolean isPast) {
        JPanel rowPanel = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                if (isCurrent) {
                    g2.setColor(CURRENT_SALAH_BG);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(CURRENT_SALAH_BORDER);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rowPanel.setOpaque(false);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        namePanel.setOpaque(false);

        JLabel nameLabel = createStyledLabel(
                salahName,
                FONT_SIZE_MEDIUM,
                isPast ? TEXT_MUTED : (isCurrent ? ACCENT_PRIMARY : TEXT_PRIMARY),
                isCurrent
        );

        namePanel.add(nameLabel);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        statusPanel.setOpaque(false);

        leftPanel.add(namePanel);
        if (isCurrent || isNext) {
            leftPanel.add(statusPanel);
        }

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        JLabel timeLabel = createStyledLabel(
                time.format(DateTimeFormatter.ofPattern("HH:mm")),
                FONT_SIZE_MEDIUM + 2,
                isPast ? TEXT_MUTED : (isCurrent ? ACCENT_PRIMARY : TEXT_PRIMARY),
                true
        );
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(timeLabel);

        if (!isPast && !isCurrent) {
            LocalTime now = LocalTime.now();

            Duration fromNow = calculateRemainingTime(now, time);
            JLabel fromNowLabel = createStyledLabel(
                    LanguageHelper.getText("inTime") + " " + formatDurationHHMMSS(fromNow),
                    FONT_SIZE_SMALL,
                    isNext ? ACCENT_SECONDARY : TEXT_MUTED,
                    false
            );
            fromNowLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            rightPanel.add(fromNowLabel);
            remainingFromNowLabels.put(index, fromNowLabel);

        }
        if (index > 0) {
            int prevIndex = index - 1;
            String prevName = orderedKeys.get(prevIndex);
            LocalTime prevTime = timings.get(prevName);

            if (prevTime != null) {
                Duration fromPrev = Duration.between(prevTime, time);
                if (!fromPrev.isNegative() && !fromPrev.isZero()) {
                    String fromPrevText = prevName + " -> " + salahName + " +" + formatDurationHHMMSS(fromPrev);
                    JLabel fromPrevLabel = createStyledLabel(
                            fromPrevText,
                            FONT_SIZE_SMALL,
                            TEXT_MUTED,
                            false
                    );
                    fromPrevLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
                    rightPanel.add(fromPrevLabel);
                    remainingFromPrevLabels.put(index, fromPrevLabel);
                }
            }
        }

        rowPanel.add(leftPanel, BorderLayout.WEST);
        rowPanel.add(rightPanel, BorderLayout.EAST);

        return rowPanel;
    }

    private void updateRemainingTimes() {
        if (timings == null || timings.isEmpty()) return;

        LocalTime now = LocalTime.now();

        for (Map.Entry<Integer, JLabel> entry : remainingFromNowLabels.entrySet()) {
            int index = entry.getKey();
            JLabel label = entry.getValue();

            if (index >= 0 && index < orderedKeys.size()) {
                String salahKey = orderedKeys.get(index);
                LocalTime salahTime = timings.get(salahKey);

                if (salahTime != null && now.isBefore(salahTime)) {
                    Duration fromNow = calculateRemainingTime(now, salahTime);
                    label.setText(LanguageHelper.getText("inTime") + " " + formatDurationHHMMSS(fromNow));
                }
            }
        }
    }

    private JPanel createUpcomingEventsSection() {
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setOpaque(false);
        containerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = createStyledLabel(
                LanguageHelper.getText("upcomingEventsTitle"),
                FONT_SIZE_MEDIUM, TEXT_SECONDARY, true
        );
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        containerPanel.add(titleLabel);
        containerPanel.add(Box.createVerticalStrut(10));

        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BACKGROUND_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        cardPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        try {
            List<IslamicCalendar.IslamicEvent> upcomingEvents = IslamicCalendar.getUpcomingEvents(LocalDate.now(), 3);

            if (upcomingEvents.isEmpty()) {
                JLabel noEvents = createStyledLabel(
                        LanguageHelper.getText("noEventsLabel"),
                        FONT_SIZE_NORMAL, TEXT_MUTED, false
                );
                noEvents.setAlignmentX(Component.CENTER_ALIGNMENT);
                cardPanel.add(noEvents);
            } else {
                int eventsToShow = Math.min(3, upcomingEvents.size());
                for (int i = 0; i < eventsToShow; i++) {
                    IslamicCalendar.IslamicEvent event = upcomingEvents.get(i);
                    long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), event.getGregorianDate());

                    JPanel eventRow = new JPanel(new BorderLayout(12, 0));
                    eventRow.setOpaque(false);
                    eventRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

                    JPanel textPanel = new JPanel();
                    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                    textPanel.setOpaque(false);

                    // Use localized event name
                    String eventName = event.getName(userLanguage);
                    JLabel eventNameLabel = createStyledLabel(eventName, FONT_SIZE_NORMAL, TEXT_PRIMARY, false);
                    eventNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                    Locale locale = getLocaleFromLanguage(userLanguage);
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM", locale);
                    JLabel eventDate = createStyledLabel(
                            event.getGregorianDate().format(dateFormatter),
                            FONT_SIZE_SMALL, TEXT_MUTED, false
                    );
                    eventDate.setAlignmentX(Component.LEFT_ALIGNMENT);

                    textPanel.add(eventNameLabel);
                    textPanel.add(Box.createVerticalStrut(2));
                    textPanel.add(eventDate);

                    String daysText;
                    if (daysUntil == 0) {
                        daysText = LanguageHelper.getText("todayLabel");
                    } else if (daysUntil == 1) {
                        daysText = LanguageHelper.getText("tomorrowLabel");
                    } else {
                        daysText = daysUntil + " " + LanguageHelper.getText("daysLabel");
                    }

                    JLabel daysLabel = createStyledLabel(
                            daysText,
                            FONT_SIZE_NORMAL,
                            daysUntil <= 7 ? ACCENT_GOLD : TEXT_SECONDARY,
                            true
                    );

                    eventRow.add(textPanel, BorderLayout.WEST);
                    eventRow.add(daysLabel, BorderLayout.EAST);

                    cardPanel.add(eventRow);

                    if (i < eventsToShow - 1) {
                        cardPanel.add(Box.createVerticalStrut(8));
                    }
                }
            }
        } catch (Exception e) {
            JLabel errorLabel = createStyledLabel(
                    LanguageHelper.getText("errorLoadingEvents"),
                    FONT_SIZE_NORMAL, TEXT_MUTED, false
            );
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardPanel.add(errorLabel);
        }

        containerPanel.add(cardPanel);
        return containerPanel;
    }

    private JPanel createButtonSection() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JButton settingsButton = createStyledButton(LanguageHelper.getText("settingsTitle"), false);
        JButton exitButton = createStyledButton(LanguageHelper.getText("exitTitle"), true);

        settingsButton.addActionListener(e -> {
            SettingsWindow settingsWindow = new SettingsWindow();
            settingsWindow.setVisible(true);
            closeWindow();
        });

        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(settingsButton);
        buttonPanel.add(exitButton);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, boolean isDanger) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);

                Color bgColor;
                Color borderColor;

                if (isDanger) {
                    bgColor = isHovered ? new Color(220, 38, 38, 100) : new Color(220, 38, 38, 50);
                    borderColor = DANGER_COLOR;
                } else {
                    bgColor = isHovered ? new Color(60, 60, 70) : BACKGROUND_CARD;
                    borderColor = BORDER_COLOR;
                }

                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_MEDIUM));
        button.setForeground(isDanger ? DANGER_TEXT : TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 44));

        return button;
    }

    private Duration calculateRemainingTime(LocalTime from, LocalTime to) {
        if (to.isAfter(from)) {
            return Duration.between(from, to);
        } else {
            return Duration.between(from, LocalTime.MAX)
                    .plus(Duration.between(LocalTime.MIN, to))
                    .plusSeconds(1);
        }
    }

    private String formatDurationHHMMSS(Duration duration) {
        long totalSeconds = duration.getSeconds();
        if (totalSeconds < 0) totalSeconds = 0;

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void startUpdateTimer() {
        updateTimer = new Timer("SalahTimesUpdateTimer", true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (isDisplayable() && !isClosing) {
                        int oldCurrentIndex = currentSalahIndex;
                        int oldNextIndex = nextSalahIndex;

                        determineCurrentAndNextSalah();
                        updateCurrentTimeLabel();
                        updateCountdownLabel();
                        updateRemainingTimes();

                        if (oldCurrentIndex != currentSalahIndex || oldNextIndex != nextSalahIndex) {
                            buildPrayerTimesContent();
                            updateNextSalahInfoLabel();
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    private void updateNextSalahInfoLabel() {
        if (nextSalahInfoLabel != null && nextSalahIndex >= 0 && !orderedKeys.isEmpty()) {
            String nextSalahName = orderedKeys.get(nextSalahIndex);
            nextSalahInfoLabel.setText(LanguageHelper.getText("nextPrayer") + ": " + nextSalahName + " " + LanguageHelper.getText("inTime") + " ");
        }
    }

    public void closeWindow() {
        if (isClosing) return;
        isClosing = true;

        stopUpdateTimer();
        cleanup();

        if (onCloseCallback != null) {
            try {
                onCloseCallback.run();
            } catch (Exception ignored) {
            }
        }

        dispose();
    }

    private void stopUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer.purge();
            updateTimer = null;
        }
    }

    private void cleanup() {
        if (outsideClickListener != null) {
            try {
                Toolkit.getDefaultToolkit().removeAWTEventListener(outsideClickListener);
            } catch (Exception ignored) {
            }
            outsideClickListener = null;
        }

        if (windowAdapter != null) {
            try {
                removeWindowListener(windowAdapter);
            } catch (Exception ignored) {
            }
            windowAdapter = null;
        }
    }

    @Override
    public void dispose() {
        stopUpdateTimer();
        cleanup();
        super.dispose();
    }

    private void setPosition() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle bounds = gc.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        int rightPadding = 12;
        int bottomPadding = 12;

        int frameWidth = getWidth();
        int frameHeight = getHeight();

        int usableRight = bounds.x + bounds.width - insets.right;
        int usableBottom = bounds.y + bounds.height - insets.bottom;

        int newX = usableRight - frameWidth - rightPadding;
        int newY = usableBottom - frameHeight - bottomPadding;

        if (newX < bounds.x) newX = bounds.x + 12;
        if (newY < bounds.y) newY = bounds.y + 12;

        setLocation(newX, newY);
    }

    private void installOutsideClickCloser() {
        outsideClickListener = event -> {
            if (!(event instanceof MouseEvent me)) return;
            if (me.getID() != MouseEvent.MOUSE_PRESSED) return;
            if (!isShowing()) return;

            try {
                Point p = me.getLocationOnScreen();
                if (!getBounds().contains(p)) {
                    closeWindow();
                }
            } catch (Exception ignored) {
            }
        };

        Toolkit.getDefaultToolkit().addAWTEventListener(
                outsideClickListener,
                AWTEvent.MOUSE_EVENT_MASK
        );

        windowAdapter = new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                closeWindow();
            }
        };

        addWindowListener(windowAdapter);
    }
}