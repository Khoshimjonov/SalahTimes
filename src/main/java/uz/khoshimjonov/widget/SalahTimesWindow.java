package uz.khoshimjonov.widget;

import lombok.Getter;
import lombok.Setter;
import uz.khoshimjonov.dto.Hijri;
import uz.khoshimjonov.service.LanguageHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.util.Map;

@Getter
@Setter
public class SalahTimesWindow extends JDialog {

    private AWTEventListener outsideClickListener;
    private WindowAdapter windowAdapter;
    private final Runnable onCloseCallback;
    private boolean isClosing = false;

    public SalahTimesWindow(Map<String, LocalTime> timings, Hijri hijriDate, Runnable onCloseCallback) {
        this.onCloseCallback = onCloseCallback;

        setUndecorated(true);
        setMinimumSize(new Dimension(260, 360));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        toFront();
        setFocusableWindowState(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(new Color(32, 33, 36));

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        JLabel timeLabel = new JLabel(hijriDate != null ? hijriDate.getYear() : "");
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel dateLabel = new JLabel(hijriDate != null ? (hijriDate.getMonth().getEn() + " " + hijriDate.getDay()) : LanguageHelper.getText("loadingTitle"));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(Color.WHITE);
        dateLabel.setForeground(new Color(200, 200, 200));
        headerPanel.setLayout(new GridLayout(2, 1));
        headerPanel.add(timeLabel);
        headerPanel.add(dateLabel);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 10, 0));

        JPanel prayerTimesPanel = new JPanel(new GridBagLayout());
        prayerTimesPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 6, 6, 6);

        if (timings == null || timings.isEmpty()) {
            JLabel loading = new JLabel(LanguageHelper.getText("loadingTitle"));
            loading.setForeground(Color.WHITE);
            loading.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            prayerTimesPanel.add(loading);
        } else {
            int i = 0;
            for (Map.Entry<String, LocalTime> entry : timings.entrySet()) {
                gbc.gridx = 0;
                gbc.gridy = i;
                JLabel nameLabel = new JLabel(entry.getKey(), SwingConstants.RIGHT);
                nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                nameLabel.setForeground(new Color(210, 210, 210));
                prayerTimesPanel.add(nameLabel, gbc);

                gbc.gridx = 1;
                JLabel timeValueLabel = new JLabel(String.valueOf(entry.getValue()), SwingConstants.LEFT);
                timeValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                timeValueLabel.setForeground(Color.WHITE);
                prayerTimesPanel.add(timeValueLabel, gbc);
                i++;
            }
        }

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton settingsButton = new JButton(LanguageHelper.getText("settingsTitle"));
        JButton exitButton = new JButton(LanguageHelper.getText("exitTitle"));

        settingsButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        exitButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        settingsButton.addActionListener(e1 -> {
            SettingsWindow settingsWindow = new SettingsWindow();
            settingsWindow.setVisible(true);
            closeWindow();
        });

        exitButton.addActionListener(e12 -> System.exit(0));

        buttonPanel.setLayout(new GridLayout(1, 2, 8, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.add(settingsButton);
        buttonPanel.add(exitButton);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(45, 47, 51));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(prayerTimesPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(card, BorderLayout.CENTER);

        getContentPane().setBackground(new Color(32, 33, 36));
        getContentPane().add(mainPanel);
        setLocationRelativeTo(null);
        pack();
        setPosition();
        installOutsideClickCloser();
        setVisible(true);
    }

    public void closeWindow() {
        if (isClosing) return;
        isClosing = true;

        cleanup();

        if (onCloseCallback != null) {
            try {
                onCloseCallback.run();
            } catch (Exception ignored) {}
        }

        dispose();
    }

    private void cleanup() {
        if (outsideClickListener != null) {
            try {
                Toolkit.getDefaultToolkit().removeAWTEventListener(outsideClickListener);
            } catch (Exception ignored) {}
            outsideClickListener = null;
        }

        if (windowAdapter != null) {
            try {
                removeWindowListener(windowAdapter);
            } catch (Exception ignored) {}
            windowAdapter = null;
        }
    }

    @Override
    public void dispose() {
        cleanup();
        super.dispose();
    }

    private void setPosition() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle bounds = gc.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        int rightPadding = 5;
        int bottomPadding = 5;

        int frameWidth = getWidth();
        int frameHeight = getHeight();

        int usableRight = bounds.x + bounds.width - insets.right;
        int usableBottom = bounds.y + bounds.height - insets.bottom;

        int newX = usableRight - frameWidth - rightPadding;
        int newY = usableBottom - frameHeight - bottomPadding;

        if (newX < bounds.x) newX = bounds.x + 10;
        if (newY < bounds.y) newY = bounds.y + 10;

        setLocation(newX, newY);
    }

    private void installOutsideClickCloser() {
        outsideClickListener = event -> {
            if (!(event instanceof MouseEvent)) return;
            MouseEvent me = (MouseEvent) event;
            if (me.getID() != MouseEvent.MOUSE_PRESSED) return;
            if (!isShowing()) return;

            try {
                Point p = me.getLocationOnScreen();
                if (!getBounds().contains(p)) {
                    closeWindow();
                }
            } catch (Exception ignored) {}
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