package uz.khoshimjonov.widget;

import uz.khoshimjonov.api.Api;
import uz.khoshimjonov.dto.MethodEnum;
import uz.khoshimjonov.dto.NominatimResponse;
import uz.khoshimjonov.service.ConfigurationManager;
import uz.khoshimjonov.service.LanguageHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SettingsWindow extends JFrame {

    private final ConfigurationManager configManager;
    private final Api api = new Api();

    private final JRadioButton shafiRadioButton;
    private final JRadioButton hanafiRadioButton;
    private final JTextField addressTextField;
    private final JLabel addressLabel;
    private final JTextField latitudeTextField;
    private final JTextField longitudeTextField;
    private final JComboBox<String> methodComboBox;
    private final JComboBox<String> languageComboBox;
    private final JFormattedTextField updateIntervalField;
    private final JFormattedTextField notificationBeforeField;
    private final JCheckBox notificationsCheckBox;
    private final JCheckBox lookAndFeelCheckBox;
    private final JCheckBox draggableCheckBox;
    private final JCheckBox alwaysOnTopCheckBox;

    public SettingsWindow() {
        this.configManager = ConfigurationManager.getInstance();

        setTitle(LanguageHelper.getText("settingsTitle"));
        setSize(400, 300);
        try {
            setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/main.png"))));
        } catch (Exception ignored) {}
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setLocationRelativeTo(null);

        List<String> methodNames = Arrays.stream(MethodEnum.values()).map(MethodEnum::getTitle).toList();
        String[] methodNamesArray = methodNames.toArray(new String[0]);
        String[] localesArray = LanguageHelper.getAvailableLocales();

        methodComboBox = new JComboBox<>(methodNamesArray);
        languageComboBox = new JComboBox<>(localesArray);

        shafiRadioButton = new JRadioButton(LanguageHelper.getText("shafiTitle"));
        hanafiRadioButton = new JRadioButton(LanguageHelper.getText("hanafiTitle"));

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(shafiRadioButton);
        buttonGroup.add(hanafiRadioButton);

        JPanel schoolRadioButtonPanel = new JPanel();
        schoolRadioButtonPanel.add(shafiRadioButton);
        schoolRadioButtonPanel.add(hanafiRadioButton);

        latitudeTextField = new JTextField(25);
        longitudeTextField = new JTextField(25);
        addressTextField = new JTextField(25);
        addressLabel = new JLabel(LanguageHelper.getText("addressLabelTitle"));
        JButton submitAddressButton = new JButton(LanguageHelper.getText("applyAddressTitle"));
        submitAddressButton.addActionListener(e -> fetchLatLongFromAddress());

        JPanel addressPanel = new JPanel();
        addressPanel.add(addressTextField);
        addressPanel.add(submitAddressButton);

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(1);
        formatter.setMaximum(1800);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        updateIntervalField = new JFormattedTextField(formatter);
        updateIntervalField.setValue(1);
        updateIntervalField.setPreferredSize(new Dimension(100, updateIntervalField.getPreferredSize().height));

        notificationBeforeField = new JFormattedTextField(formatter);
        notificationBeforeField.setValue(30);
        notificationBeforeField.setPreferredSize(new Dimension(100, updateIntervalField.getPreferredSize().height));

        notificationsCheckBox = new JCheckBox(LanguageHelper.getText("notificationsTitle"));
        lookAndFeelCheckBox = new JCheckBox(LanguageHelper.getText("lookAndFeelTitle"));
        draggableCheckBox = new JCheckBox(LanguageHelper.getText("draggableTitle"));
        alwaysOnTopCheckBox = new JCheckBox(LanguageHelper.getText("alwaysOnTopTitle"));


        JPanel checkBoxPanel1 = new JPanel();
        JPanel checkBoxPanel2 = new JPanel();
        checkBoxPanel1.add(lookAndFeelCheckBox);
        checkBoxPanel1.add(draggableCheckBox);
        checkBoxPanel2.add(notificationsCheckBox);
        checkBoxPanel2.add(alwaysOnTopCheckBox);

        JButton submitButton = new JButton(LanguageHelper.getText("saveTitle"));
        submitButton.addActionListener(e -> saveSettings());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(submitButton);

        int order = 0;
        JPanel northGridPanel = new JPanel();
        northGridPanel.setLayout(new GridBagLayout());

        northGridPanel.add(new JLabel(LanguageHelper.getText("warning1")), getConstraints(0, order++));
        northGridPanel.add(new JLabel(LanguageHelper.getText("warning2")), getConstraints(0, order++));
        northGridPanel.add(new JLabel(LanguageHelper.getText("warning3")), getConstraints(0, order++));
        northGridPanel.add(new JSeparator(), getConstraints(0, order++, 15));

        northGridPanel.add(new JLabel(LanguageHelper.getText("addressTitle")), getConstraints(0, order++));
        northGridPanel.add(addressPanel, getConstraints(0, order++, 5));
        northGridPanel.add(addressLabel, getConstraints(0, order++, 15));
        northGridPanel.add(new JLabel(LanguageHelper.getText("latitudeTitle")), getConstraints(0, order++));
        northGridPanel.add(latitudeTextField, getConstraints(0, order++, 15));
        northGridPanel.add(new JLabel(LanguageHelper.getText("longitudeTitle")), getConstraints(0, order++));
        northGridPanel.add(longitudeTextField, getConstraints(0, order++, 15));
        northGridPanel.add(new JLabel(LanguageHelper.getText("methodTitle")), getConstraints(0, order++));
        northGridPanel.add(methodComboBox, getConstraints(0, order++, 15));
        northGridPanel.add(new JLabel(LanguageHelper.getText("languageTitle")), getConstraints(0, order++));
        northGridPanel.add(languageComboBox, getConstraints(0, order++, 15));
        northGridPanel.add(new JLabel(LanguageHelper.getText("schoolTitle")), getConstraints(0, order++));
        northGridPanel.add(schoolRadioButtonPanel, getConstraints(0, order++, 15));
        northGridPanel.add(new JLabel(LanguageHelper.getText("updateIntervalTitle")), getConstraints(0, order++));
        northGridPanel.add(updateIntervalField, getConstraints(0, order++, 15));

        northGridPanel.add(checkBoxPanel1, getConstraints(0, order++));
        northGridPanel.add(checkBoxPanel2, getConstraints(0, order++));

        add(northGridPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);

        loadConfigValues();
    }

    private void fetchLatLongFromAddress() {
        try {
            String address = URLEncoder.encode(addressTextField.getText(), StandardCharsets.UTF_8);
            List<NominatimResponse> positionByAddress = api.getPositionByAddress(address);
            if (positionByAddress == null || positionByAddress.isEmpty()) {
                return;
            }
            NominatimResponse response = positionByAddress.getFirst();
            latitudeTextField.setText(response.getLat());
            longitudeTextField.setText(response.getLon());
            addressLabel.setText(LanguageHelper.getText("addressLabelTitle") + response.getDisplayName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadConfigValues() {
        latitudeTextField.setText(String.valueOf(configManager.getLatitude()));
        longitudeTextField.setText(String.valueOf(configManager.getLongitude()));
        methodComboBox.setSelectedItem(MethodEnum.getMethodByCode(configManager.getMethod()).getTitle());
        languageComboBox.setSelectedItem(configManager.getUserLanguage());
        int school = configManager.getSchool();
        shafiRadioButton.setSelected(school == 0);
        hanafiRadioButton.setSelected(school == 1);
        updateIntervalField.setText(String.valueOf(configManager.getUpdateDelay()));
        notificationBeforeField.setText(String.valueOf(configManager.getNotificationBeforeMinutes()));
        notificationsCheckBox.setSelected(configManager.isNotification());
        lookAndFeelCheckBox.setSelected(configManager.getLookAndFeelEnabled());
        draggableCheckBox.setSelected(configManager.isDraggable());
        alwaysOnTopCheckBox.setSelected(configManager.isAlwaysOnTop());
    }

    private void saveSettings() {
        configManager.setLatitude(Double.parseDouble(latitudeTextField.getText()));
        configManager.setLongitude(Double.parseDouble(longitudeTextField.getText()));
        configManager.setMethod((MethodEnum.getMethodByName((String) methodComboBox.getSelectedItem()).getCode()));
        configManager.setUserLanguage(String.valueOf(languageComboBox.getSelectedItem()));
        int school = shafiRadioButton.isSelected() ? 0 : 1;
        configManager.setSchool(school);
        configManager.setLookAndFeelEnabled(lookAndFeelCheckBox.isSelected());
        configManager.setDraggable(draggableCheckBox.isSelected());
        configManager.setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
        configManager.setUpdateDelay(Integer.parseInt(updateIntervalField.getText()));
        configManager.setNotification(notificationsCheckBox.isSelected());
        configManager.setNotificationBeforeMinutes(Integer.parseInt(notificationBeforeField.getText()));
        LanguageHelper.setLocale(String.valueOf(languageComboBox.getSelectedItem()));
        dispose();
    }

    private GridBagConstraints getConstraints(int x, int y) {
        return getConstraints(x, y, 2);
    }

    private GridBagConstraints getConstraints(int x, int y, int bottomPadding) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(2, 10, bottomPadding, 10);
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = 1;
        return c;
    }
}
