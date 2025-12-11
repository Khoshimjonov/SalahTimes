package uz.khoshimjonov.widget;

import uz.khoshimjonov.api.Api;
import uz.khoshimjonov.dto.MethodEnum;
import uz.khoshimjonov.dto.NominatimResponse;
import uz.khoshimjonov.service.AutoStartManager;
import uz.khoshimjonov.service.ConfigurationManager;
import uz.khoshimjonov.service.LanguageHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SettingsWindow extends JFrame {

    private static final Color BACKGROUND_PRIMARY = new Color(18, 18, 24);
    private static final Color BACKGROUND_SECONDARY = new Color(28, 28, 36);
    private static final Color BACKGROUND_CARD = new Color(38, 38, 48);
    private static final Color BACKGROUND_INPUT = new Color(48, 48, 58);
    private static final Color ACCENT_PRIMARY = new Color(34, 197, 94);
    private static final Color ACCENT_SECONDARY = new Color(16, 185, 129);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(55, 55, 65);
    private static final Color DANGER_COLOR = new Color(220, 38, 38);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);

    private static final String FONT_FAMILY = "Segoe UI";
    private static final int FONT_SIZE_TITLE = 28;
    private static final int FONT_SIZE_SECTION = 18;
    private static final int FONT_SIZE_NORMAL = 15;
    private static final int FONT_SIZE_SMALL = 13;

    private final ConfigurationManager configManager;
    private final Api api = new Api();

    private JRadioButton shafiRadioButton;
    private JRadioButton hanafiRadioButton;
    private JTextField addressTextField;
    private JLabel addressResultLabel;
    private JTextField latitudeTextField;
    private JTextField longitudeTextField;
    private JTextField elevationTextField;
    private JComboBox<String> methodComboBox;
    private JComboBox<String> languageComboBox;
    private JFormattedTextField updateIntervalField;
    private JFormattedTextField notificationBeforeField;
    private JCheckBox notifyBeforeCheckBox;
    private JCheckBox notifyOnTimeCheckBox;
    private JCheckBox lookAndFeelCheckBox;
    private JCheckBox useApiCheckBox;
    private JCheckBox draggableCheckBox;
    private JCheckBox alwaysOnTopCheckBox;
    private JCheckBox autoStartCheckBox;

    public SettingsWindow() {
        this.configManager = ConfigurationManager.getInstance();

        setTitle(LanguageHelper.getText("settingsTitle"));
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        try {
            setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/main.png"))));
        } catch (Exception ignored) {}

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainContainer = createMainContainer();
        setContentPane(mainContainer);

        setSize(740, 800);
        setMinimumSize(new Dimension(740, 800));
        setLocationRelativeTo(null);
        setVisible(true);

        loadConfigValues();
    }

    private void applyRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private JPanel createMainContainer() {
        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BACKGROUND_PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
                g2.dispose();
            }
        };
        container.setOpaque(false);

        JPanel headerPanel = createHeaderPanel();
        container.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scrollPane.getVerticalScrollBar());
        container.add(scrollPane, BorderLayout.CENTER);

        JPanel footerPanel = createFooterPanel();
        container.add(footerPanel, BorderLayout.SOUTH);

        WindowDragHandler dragHandler = new WindowDragHandler();
        headerPanel.addMouseListener(dragHandler);
        headerPanel.addMouseMotionListener(dragHandler);

        return container;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BACKGROUND_SECONDARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 20, 20);
                g2.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titleLabel = createStyledLabel(LanguageHelper.getText("settingsTitle"), FONT_SIZE_TITLE, TEXT_PRIMARY, true);

        JButton closeButton = new JButton() {
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

                if (isHovered) {
                    g2.setColor(new Color(220, 38, 38, 100));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                }

                g2.setColor(isHovered ? new Color(255, 120, 120) : TEXT_MUTED);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int size = 10;
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g2.drawLine(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2);
                g2.drawLine(cx + size / 2, cy - size / 2, cx - size / 2, cy + size / 2);

                g2.dispose();
            }
        };
        closeButton.setPreferredSize(new Dimension(40, 40));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(closeButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(12, 24, 12, 24));

        contentPanel.add(createWarningSection());
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(createLocationSection());
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(createCalculationSection());
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(createNotificationsSection());
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(createAppearanceSection());
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(createSystemSection());
        contentPanel.add(Box.createVerticalStrut(16));

        return contentPanel;
    }

    private JPanel createWarningSection() {
        JPanel card = createCard();

        JPanel warningContent = new JPanel();
        warningContent.setLayout(new BoxLayout(warningContent, BoxLayout.Y_AXIS));
        warningContent.setOpaque(false);
        warningContent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel warningTitle = createStyledLabel("! " + LanguageHelper.getText("importantNotice"), FONT_SIZE_NORMAL, WARNING_COLOR, true);
        warningTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel warning1 = createWrappedLabel(LanguageHelper.getText("warning1"), FONT_SIZE_SMALL, TEXT_MUTED);
        JLabel warning2 = createWrappedLabel(LanguageHelper.getText("warning2"), FONT_SIZE_SMALL, TEXT_MUTED);
        JLabel warning3 = createWrappedLabel(LanguageHelper.getText("warning3"), FONT_SIZE_SMALL, TEXT_MUTED);

        warningContent.add(warningTitle);
        warningContent.add(Box.createVerticalStrut(10));
        warningContent.add(warning1);
        warningContent.add(Box.createVerticalStrut(6));
        warningContent.add(warning2);
        warningContent.add(Box.createVerticalStrut(6));
        warningContent.add(warning3);

        card.add(warningContent, BorderLayout.CENTER);
        return card;
    }

    private JPanel createLocationSection() {
        JPanel card = createCard();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = createSectionTitle(LanguageHelper.getText("locationSectionTitle"));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sectionTitle);
        content.add(Box.createVerticalStrut(18));

        JLabel addressLabel = createStyledLabel(LanguageHelper.getText("addressTitle"), FONT_SIZE_SMALL, TEXT_SECONDARY, false);
        addressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(addressLabel);
        content.add(Box.createVerticalStrut(8));

        addressTextField = createStyledTextField();
        addressTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        addressTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        content.add(addressTextField);
        content.add(Box.createVerticalStrut(12));

        JButton searchButton = createAccentButton(LanguageHelper.getText("applyAddressTitle"));
        searchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchButton.addActionListener(e -> fetchLatLongFromAddress());
        content.add(searchButton);
        content.add(Box.createVerticalStrut(10));

        addressResultLabel = createWrappedLabel(LanguageHelper.getText("addressLabelTitle"), FONT_SIZE_SMALL, TEXT_MUTED);
        content.add(addressResultLabel);
        content.add(Box.createVerticalStrut(20));

        JPanel coordsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        coordsRow.setOpaque(false);
        coordsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        coordsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        latitudeTextField = createStyledTextField();
        longitudeTextField = createStyledTextField();
        elevationTextField = createStyledTextField();

        coordsRow.add(createLabeledInput(LanguageHelper.getText("latitudeTitle"), latitudeTextField));
        coordsRow.add(createLabeledInput(LanguageHelper.getText("longitudeTitle"), longitudeTextField));
        coordsRow.add(createLabeledInput(LanguageHelper.getText("elevationTitle"), elevationTextField));

        content.add(coordsRow);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCalculationSection() {
        JPanel card = createCard();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = createSectionTitle(LanguageHelper.getText("calculationSectionTitle"));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sectionTitle);
        content.add(Box.createVerticalStrut(18));

        JLabel methodLabel = createStyledLabel(LanguageHelper.getText("methodTitle"), FONT_SIZE_SMALL, TEXT_SECONDARY, false);
        methodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(methodLabel);
        content.add(Box.createVerticalStrut(8));

        String[] methods = Arrays.stream(MethodEnum.values()).map(MethodEnum::getTitle).toArray(String[]::new);
        methodComboBox = createStyledComboBox(methods);
        methodComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        methodComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        content.add(methodComboBox);
        content.add(Box.createVerticalStrut(18));

        JLabel schoolLabel = createStyledLabel(LanguageHelper.getText("schoolTitle"), FONT_SIZE_SMALL, TEXT_SECONDARY, false);
        schoolLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(schoolLabel);
        content.add(Box.createVerticalStrut(10));

        JPanel schoolPanel = new JPanel();
        schoolPanel.setLayout(new BoxLayout(schoolPanel, BoxLayout.X_AXIS));
        schoolPanel.setOpaque(false);
        schoolPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        shafiRadioButton = createStyledRadioButton(LanguageHelper.getText("shafiTitle"));
        hanafiRadioButton = createStyledRadioButton(LanguageHelper.getText("hanafiTitle"));

        ButtonGroup schoolGroup = new ButtonGroup();
        schoolGroup.add(shafiRadioButton);
        schoolGroup.add(hanafiRadioButton);

        schoolPanel.add(shafiRadioButton);
        schoolPanel.add(Box.createHorizontalStrut(40));
        schoolPanel.add(hanafiRadioButton);
        schoolPanel.add(Box.createHorizontalGlue());

        content.add(schoolPanel);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createNotificationsSection() {
        JPanel card = createCard();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = createSectionTitle(LanguageHelper.getText("notificationsSectionTitle"));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sectionTitle);
        content.add(Box.createVerticalStrut(18));

        notifyBeforeCheckBox = createStyledCheckBox(LanguageHelper.getText("notifyBeforeEnabledTitle"));
        notifyBeforeCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(notifyBeforeCheckBox);
        content.add(Box.createVerticalStrut(12));

        JLabel beforeLabel = createStyledLabel(LanguageHelper.getText("notificationBeforeTitle"), FONT_SIZE_SMALL, TEXT_SECONDARY, false);
        beforeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(beforeLabel);
        content.add(Box.createVerticalStrut(8));

        JPanel beforeRow = new JPanel();
        beforeRow.setLayout(new BoxLayout(beforeRow, BoxLayout.X_AXIS));
        beforeRow.setOpaque(false);
        beforeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter beforeFormatter = new NumberFormatter(format);
        beforeFormatter.setValueClass(Integer.class);
        beforeFormatter.setMinimum(0);
        beforeFormatter.setMaximum(180);
        beforeFormatter.setAllowsInvalid(false);
        beforeFormatter.setCommitsOnValidEdit(true);

        notificationBeforeField = createStyledFormattedTextField(beforeFormatter);
        notificationBeforeField.setValue(30);
        notificationBeforeField.setPreferredSize(new Dimension(100, 44));
        notificationBeforeField.setMaximumSize(new Dimension(100, 44));

        JLabel minLabel = createStyledLabel(LanguageHelper.getText("minutesLabel"), FONT_SIZE_NORMAL, TEXT_MUTED, false);

        beforeRow.add(notificationBeforeField);
        beforeRow.add(Box.createHorizontalStrut(12));
        beforeRow.add(minLabel);
        beforeRow.add(Box.createHorizontalGlue());

        content.add(beforeRow);

        content.add(Box.createVerticalStrut(16));

        notifyOnTimeCheckBox = createStyledCheckBox(LanguageHelper.getText("notifyOnTimeTitle"));
        notifyOnTimeCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(notifyOnTimeCheckBox);

        // Bind enabling of minutes inputs to the toggle
        notifyBeforeCheckBox.addActionListener(e -> {
            boolean enabled = notifyBeforeCheckBox.isSelected();
            notificationBeforeField.setEnabled(enabled);
            beforeLabel.setEnabled(enabled);
            minLabel.setEnabled(enabled);
        });

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAppearanceSection() {
        JPanel card = createCard();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = createSectionTitle(LanguageHelper.getText("appearanceSectionTitle"));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sectionTitle);
        content.add(Box.createVerticalStrut(18));

        JLabel langLabel = createStyledLabel(LanguageHelper.getText("languageTitle"), FONT_SIZE_SMALL, TEXT_SECONDARY, false);
        langLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(langLabel);
        content.add(Box.createVerticalStrut(8));

        String[] availableLocales = LanguageHelper.getAvailableLocales();
        languageComboBox = createStyledComboBox(availableLocales);
        languageComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        languageComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        content.add(languageComboBox);
        content.add(Box.createVerticalStrut(18));

        JLabel intervalLabel = createStyledLabel(LanguageHelper.getText("updateIntervalTitle"), FONT_SIZE_SMALL, TEXT_SECONDARY, false);
        intervalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(intervalLabel);
        content.add(Box.createVerticalStrut(8));

        JPanel intervalRow = new JPanel();
        intervalRow.setLayout(new BoxLayout(intervalRow, BoxLayout.X_AXIS));
        intervalRow.setOpaque(false);
        intervalRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        NumberFormat formatNum = NumberFormat.getInstance();
        NumberFormatter intervalFormatter = new NumberFormatter(formatNum);
        intervalFormatter.setValueClass(Integer.class);
        intervalFormatter.setMinimum(1);
        intervalFormatter.setMaximum(1800);
        intervalFormatter.setAllowsInvalid(false);
        intervalFormatter.setCommitsOnValidEdit(true);

        updateIntervalField = createStyledFormattedTextField(intervalFormatter);
        updateIntervalField.setValue(1);
        updateIntervalField.setPreferredSize(new Dimension(100, 44));
        updateIntervalField.setMaximumSize(new Dimension(100, 44));

        JLabel secLabel = createStyledLabel(LanguageHelper.getText("secondsLabel"), FONT_SIZE_NORMAL, TEXT_MUTED, false);

        intervalRow.add(updateIntervalField);
        intervalRow.add(Box.createHorizontalStrut(12));
        intervalRow.add(secLabel);
        intervalRow.add(Box.createHorizontalGlue());

        content.add(intervalRow);
        content.add(Box.createVerticalStrut(18));

        lookAndFeelCheckBox = createStyledCheckBox(LanguageHelper.getText("lookAndFeelTitle"));
        draggableCheckBox = createStyledCheckBox(LanguageHelper.getText("draggableTitle"));
        alwaysOnTopCheckBox = createStyledCheckBox(LanguageHelper.getText("alwaysOnTopTitle"));

        lookAndFeelCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        draggableCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        alwaysOnTopCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(lookAndFeelCheckBox);
        content.add(Box.createVerticalStrut(12));
        content.add(draggableCheckBox);
        content.add(Box.createVerticalStrut(12));
        content.add(alwaysOnTopCheckBox);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSystemSection() {
        JPanel card = createCard();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = createSectionTitle(LanguageHelper.getText("systemSectionTitle"));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sectionTitle);
        content.add(Box.createVerticalStrut(18));

        useApiCheckBox = createStyledCheckBox(LanguageHelper.getText("useApiTitle"));
        autoStartCheckBox = createStyledCheckBox(LanguageHelper.getText("autoStartTitle"));

        useApiCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        autoStartCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(useApiCheckBox);
        content.add(Box.createVerticalStrut(12));
        content.add(autoStartCheckBox);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BACKGROUND_SECONDARY);
                g2.fillRoundRect(0, -10, getWidth(), getHeight() + 10, 20, 20);
                g2.dispose();
            }
        };
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);

        buttonPanel.add(Box.createHorizontalGlue());

        JButton cancelButton = createSecondaryButton(LanguageHelper.getText("cancelTitle"));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = createPrimaryButton(LanguageHelper.getText("saveTitle"));
        saveButton.addActionListener(e -> saveSettings());

        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(16));
        buttonPanel.add(saveButton);

        footerPanel.add(buttonPanel, BorderLayout.EAST);

        return footerPanel;
    }

    private JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BACKGROUND_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        label.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_SECTION));
        label.setForeground(TEXT_PRIMARY);
        return label;
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

    private JLabel createWrappedLabel(String text, int fontSize, Color color) {
        JLabel label = new JLabel("<html><body style='width: 500px'>" + text + "</body></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        label.setFont(new Font(FONT_FAMILY, Font.PLAIN, fontSize));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField() {
            private boolean isFocused = false;

            {
                addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        isFocused = true;
                        repaint();
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        isFocused = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BACKGROUND_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(isFocused ? ACCENT_PRIMARY : BORDER_COLOR);
                g2.setStroke(new BasicStroke(isFocused ? 2f : 1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(new EmptyBorder(10, 14, 10, 14));
        field.setPreferredSize(new Dimension(200, 44));
        return field;
    }

    private JFormattedTextField createStyledFormattedTextField(NumberFormatter formatter) {
        JFormattedTextField field = new JFormattedTextField(formatter) {
            private boolean isFocused = false;

            {
                addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        isFocused = true;
                        repaint();
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        isFocused = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BACKGROUND_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(isFocused ? ACCENT_PRIMARY : BORDER_COLOR);
                g2.setStroke(new BasicStroke(isFocused ? 2f : 1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(new EmptyBorder(10, 14, 10, 14));
        return field;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL));
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBackground(BACKGROUND_INPUT);
        comboBox.setPreferredSize(new Dimension(200, 44));

        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    public void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        applyRenderingHints(g2);
                        g2.setColor(BACKGROUND_INPUT);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(TEXT_SECONDARY);
                        int[] xPoints = {getWidth() / 2 - 6, getWidth() / 2 + 6, getWidth() / 2};
                        int[] yPoints = {getHeight() / 2 - 3, getHeight() / 2 - 3, getHeight() / 2 + 4};
                        g2.fillPolygon(xPoints, yPoints, 3);
                        g2.dispose();
                    }
                };
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setPreferredSize(new Dimension(36, 36));
                return button;
            }

            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox) {
                    @Override
                    protected JScrollPane createScroller() {
                        JScrollPane scroller = new JScrollPane(list,
                                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        scroller.getViewport().setBackground(BACKGROUND_INPUT);
                        scroller.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
                        return scroller;
                    }
                };
                popup.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
                return popup;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BACKGROUND_INPUT);
                g2.fillRoundRect(bounds.x, bounds.y, bounds.width + 36, bounds.height, 10, 10);
                g2.dispose();
            }

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setFont(comboBox.getFont());
                g2.setColor(TEXT_PRIMARY);
                String text = comboBox.getSelectedItem() != null ? comboBox.getSelectedItem().toString() : "";
                FontMetrics fm = g2.getFontMetrics();
                int y = bounds.y + (bounds.height + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, bounds.x + 14, y);
                g2.dispose();
            }
        });

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBackground(isSelected ? ACCENT_PRIMARY : BACKGROUND_INPUT);
                label.setForeground(TEXT_PRIMARY);
                label.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL));
                label.setBorder(new EmptyBorder(10, 14, 10, 14));
                label.setOpaque(true);
                return label;
            }
        });

        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, BORDER_COLOR),
                new EmptyBorder(0, 0, 0, 0)
        ));

        return comboBox;
    }

    private JCheckBox createStyledCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);

                int boxSize = 22;
                int boxY = (getHeight() - boxSize) / 2;

                g2.setColor(isSelected() ? ACCENT_PRIMARY : BACKGROUND_INPUT);
                g2.fillRoundRect(0, boxY, boxSize, boxSize, 6, 6);

                g2.setColor(isSelected() ? ACCENT_PRIMARY : BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, boxY, boxSize - 1, boxSize - 1, 6, 6);

                if (isSelected()) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(5, boxY + 11, 9, boxY + 16);
                    g2.drawLine(9, boxY + 16, 17, boxY + 7);
                }

                g2.setColor(TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = boxSize + 14;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());
                int width = 22 + 14 + fm.stringWidth(getText()) + 30;
                return new Dimension(width, 36);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 36);
            }
        };
        checkBox.setOpaque(false);
        checkBox.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL));
        checkBox.setForeground(TEXT_PRIMARY);
        checkBox.setFocusPainted(false);
        checkBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return checkBox;
    }

    private JRadioButton createStyledRadioButton(String text) {
        JRadioButton radioButton = new JRadioButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);

                int circleSize = 22;
                int circleY = (getHeight() - circleSize) / 2;

                g2.setColor(BACKGROUND_INPUT);
                g2.fillOval(0, circleY, circleSize, circleSize);

                g2.setColor(isSelected() ? ACCENT_PRIMARY : BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(0, circleY, circleSize - 1, circleSize - 1);

                if (isSelected()) {
                    g2.setColor(ACCENT_PRIMARY);
                    g2.fillOval(5, circleY + 5, 12, 12);
                }

                g2.setColor(TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = circleSize + 14;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());
                int width = 22 + 14 + fm.stringWidth(getText()) + 20;
                return new Dimension(width, 36);
            }
        };
        radioButton.setOpaque(false);
        radioButton.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL));
        radioButton.setForeground(TEXT_PRIMARY);
        radioButton.setFocusPainted(false);
        radioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return radioButton;
    }

    private JButton createPrimaryButton(String text) {
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

                g2.setColor(isHovered ? ACCENT_SECONDARY : ACCENT_PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        button.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_NORMAL));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 48));
        return button;
    }

    private JButton createSecondaryButton(String text) {
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

                g2.setColor(isHovered ? new Color(60, 60, 70) : BACKGROUND_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                g2.setColor(TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        button.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL));
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 48));
        return button;
    }

    private JButton createAccentButton(String text) {
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

                g2.setColor(isHovered ? ACCENT_SECONDARY : ACCENT_PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());
                return new Dimension(fm.stringWidth(getText()) + 48, 44);
            }
        };
        button.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_NORMAL));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createLabeledInput(String labelText, JTextField textField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = createStyledLabel(labelText, FONT_SIZE_SMALL, TEXT_SECONDARY, false);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        panel.add(textField);

        return panel;
    }

    private void styleScrollBar(JScrollBar scrollBar) {
        scrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(70, 70, 80);
                this.trackColor = BACKGROUND_PRIMARY;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createEmptyButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createEmptyButton();
            }

            private JButton createEmptyButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(
                        thumbBounds.x + 2, thumbBounds.y + 2,
                        thumbBounds.width - 4, thumbBounds.height - 4,
                        8, 8
                );
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(trackColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }
        });
        scrollBar.setPreferredSize(new Dimension(12, 0));
    }

    private void fetchLatLongFromAddress() {
        try {
            String address = URLEncoder.encode(addressTextField.getText(), StandardCharsets.UTF_8);
            List<NominatimResponse> positionByAddress = api.getPositionByAddress(address);
            if (positionByAddress == null || positionByAddress.isEmpty()) {
                addressResultLabel.setText("<html><body style='width: 500px; color: #DC2626;'>" +
                        LanguageHelper.getText("addressNotFound") + "</body></html>");
                return;
            }
            NominatimResponse response = positionByAddress.getFirst();
            latitudeTextField.setText(response.getLat());
            longitudeTextField.setText(response.getLon());
            double elevation = api.lookupElevation(
                    Double.parseDouble(response.getLat()),
                    Double.parseDouble(response.getLon())
            );
            addressResultLabel.setText("<html><body style='width: 500px; color: #22C55E;'>" +
                    response.getDisplayName() + " (" + elevation + "m)</body></html>");
            elevationTextField.setText(String.valueOf(elevation));
        } catch (Exception e) {
            addressResultLabel.setText("<html><body style='width: 500px; color: #DC2626;'>" +
                    LanguageHelper.getText("errorFetchingAddress") + "</body></html>");
            e.printStackTrace();
        }
    }

    private void loadConfigValues() {
        addressTextField.setText(configManager.getAddress());
        latitudeTextField.setText(String.valueOf(configManager.getLatitude()));
        longitudeTextField.setText(String.valueOf(configManager.getLongitude()));
        elevationTextField.setText(String.valueOf(configManager.getElevation()));
        methodComboBox.setSelectedItem(MethodEnum.getMethodByCode(configManager.getMethod()).getTitle());
        languageComboBox.setSelectedItem(configManager.getUserLanguage());
        int school = configManager.getSchool();
        shafiRadioButton.setSelected(school == 0);
        hanafiRadioButton.setSelected(school == 1);
        updateIntervalField.setText(String.valueOf(configManager.getUpdateDelay()));
        notificationBeforeField.setText(String.valueOf(configManager.getNotificationBeforeMinutes()));
        notifyBeforeCheckBox.setSelected(configManager.isNotifyBefore());
        notifyOnTimeCheckBox.setSelected(configManager.isNotifyOnTime());
        lookAndFeelCheckBox.setSelected(configManager.getLookAndFeelEnabled());
        useApiCheckBox.setSelected(configManager.getUseApi());
        draggableCheckBox.setSelected(configManager.isDraggable());
        alwaysOnTopCheckBox.setSelected(configManager.isAlwaysOnTop());
        autoStartCheckBox.setSelected(configManager.getAutoStart());

        boolean enabled = notifyBeforeCheckBox.isSelected();
        notificationBeforeField.setEnabled(enabled);
        // Find and enable/disable the labels that depend on this toggle
        // We directly control the known label instances in scope
        // beforeLabel and minLabel are local to createNotificationsSection; rely on field states only
    }

    private void saveSettings() {
        try {
            configManager.setAddress(addressTextField.getText());
            configManager.setLatitude(Double.parseDouble(latitudeTextField.getText()));
            configManager.setLongitude(Double.parseDouble(longitudeTextField.getText()));
            configManager.setElevation(Double.parseDouble(elevationTextField.getText()));
            configManager.setMethod(MethodEnum.getMethodByName((String) methodComboBox.getSelectedItem()).getCode());
            configManager.setUserLanguage(String.valueOf(languageComboBox.getSelectedItem()));
            int school = shafiRadioButton.isSelected() ? 0 : 1;
            configManager.setSchool(school);
            configManager.setLookAndFeelEnabled(lookAndFeelCheckBox.isSelected());
            configManager.setUseApi(useApiCheckBox.isSelected());
            configManager.setDraggable(draggableCheckBox.isSelected());
            configManager.setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());

            String intervalText = updateIntervalField.getText().replace(",", "").replace(" ", "");
            configManager.setUpdateDelay(Integer.parseInt(intervalText));

            String beforeText = notificationBeforeField.getText().replace(",", "").replace(" ", "");
            configManager.setNotificationBeforeMinutes(Integer.parseInt(beforeText));

            configManager.setNotifyBefore(notifyBeforeCheckBox.isSelected());
            configManager.setNotifyOnTime(notifyOnTimeCheckBox.isSelected());

            boolean wantAutoStart = autoStartCheckBox.isSelected();
            configManager.setAutoStart(wantAutoStart);

            boolean ok = wantAutoStart ? AutoStartManager.enable() : AutoStartManager.disable();
            if (!ok) {
                showErrorDialog(
                        LanguageHelper.getText("autoStartErrorTitle"),
                        LanguageHelper.getText("autoStartErrorMessage")
                );
            }

            LanguageHelper.setLocale(String.valueOf(languageComboBox.getSelectedItem()));
            configManager.apiSettingsUpdated = true;
            dispose();
        } catch (NumberFormatException e) {
            showErrorDialog(
                    LanguageHelper.getText("validationErrorTitle"),
                    LanguageHelper.getText("validationErrorMessage")
            );
        }
    }

    private void showErrorDialog(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyRenderingHints(g2);
                g2.setColor(BACKGROUND_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(DANGER_COLOR);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel titleLabel = createStyledLabel(title, FONT_SIZE_SECTION, DANGER_COLOR, true);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html><body style='width: 280px; text-align: center;'>" + message + "</body></html>");
        messageLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_NORMAL));
        messageLabel.setForeground(TEXT_SECONDARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = new JButton("OK") {
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
                g2.setColor(isHovered ? new Color(200, 30, 30) : DANGER_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        okButton.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_NORMAL));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setContentAreaFilled(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setPreferredSize(new Dimension(100, 44));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> dialog.dispose());

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(18));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(24));
        contentPanel.add(okButton);

        panel.add(contentPanel, BorderLayout.CENTER);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 380), dialog.getHeight());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static class RoundedBorder implements javax.swing.border.Border {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    private class WindowDragHandler extends MouseAdapter {
        private Point dragOffset;

        @Override
        public void mousePressed(MouseEvent e) {
            dragOffset = e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragOffset != null) {
                Point currentLocation = e.getLocationOnScreen();
                setLocation(currentLocation.x - dragOffset.x, currentLocation.y - dragOffset.y);
            }
        }
    }
}