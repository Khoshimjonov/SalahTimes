package uz.khoshimjonov.widget;

import uz.khoshimjonov.dto.MethodEnum;
import uz.khoshimjonov.service.ConfigurationManager;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

public class SettingsWindow extends JFrame {

    private final ConfigurationManager configManager;

    private final JRadioButton shafiRadioButton;
    private final JRadioButton hanafiRadioButton;
    private final JTextField latitudeTextField;
    private final JTextField longitudeTextField;
    private final JComboBox<String> methodComboBox;
    private final JFormattedTextField updateIntervalField;
    private final JFormattedTextField notificationBeforeField;
    private final JCheckBox notificationsCheckBox;
    private final JCheckBox lookAndFeelCheckBox;
    private final JCheckBox draggableCheckBox;
    private final JCheckBox alwaysOnTopCheckBox;
    private final JCheckBox autostartCheckBox;

    public SettingsWindow() {
        this.configManager = ConfigurationManager.getInstance();

        setTitle("Widget App Settings");
        setSize(400, 300);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setLocationRelativeTo(null);

        // Combo Box
        List<String> methodNames = Arrays.stream(MethodEnum.values()).map(MethodEnum::getTitle).toList();
        String[] methodNamesArray = methodNames.toArray(new String[0]);

        methodComboBox = new JComboBox<>(methodNamesArray);

        shafiRadioButton = new JRadioButton("Shafi (standard)");
        hanafiRadioButton = new JRadioButton("Hanafi");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(shafiRadioButton);
        buttonGroup.add(hanafiRadioButton);

        JPanel schoolRadioButtonPanel = new JPanel();
        schoolRadioButtonPanel.add(shafiRadioButton);
        schoolRadioButtonPanel.add(hanafiRadioButton);

        latitudeTextField = new JTextField(25);
        longitudeTextField = new JTextField(25);

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

        // Check Boxes
        notificationsCheckBox = new JCheckBox("Enable notifications");
        notificationsCheckBox.setSelected(true);
        lookAndFeelCheckBox = new JCheckBox("Enable Look and Feel (Restart required)");
        lookAndFeelCheckBox.setSelected(true);
        draggableCheckBox = new JCheckBox("Draggable (Restart required)");
        draggableCheckBox.setSelected(true);
        alwaysOnTopCheckBox = new JCheckBox("Always on top");
        alwaysOnTopCheckBox.setSelected(true);
        autostartCheckBox = new JCheckBox("Autostart");


        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.add(notificationsCheckBox);
        checkBoxPanel.add(lookAndFeelCheckBox);
        checkBoxPanel.add(draggableCheckBox);
        checkBoxPanel.add(alwaysOnTopCheckBox);
        checkBoxPanel.add(autostartCheckBox);

        // Buttons
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> saveSettings());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(submitButton);

        // Grid Panel
        JPanel northGridPanel = new JPanel();
        northGridPanel.setLayout(new GridBagLayout());
        northGridPanel.add(new JLabel("Latitude:"), getConstraints(0, 0));
        northGridPanel.add(latitudeTextField, getConstraints(1, 0));
        northGridPanel.add(new JLabel("Longitude:"), getConstraints(0, 1));
        northGridPanel.add(longitudeTextField, getConstraints(1, 1));
        northGridPanel.add(new JLabel("Method:"), getConstraints(0, 2));
        northGridPanel.add(methodComboBox, getConstraints(1, 2));
        northGridPanel.add(new JLabel("School:"), getConstraints(0, 3));
        northGridPanel.add(schoolRadioButtonPanel, getConstraints(1, 3));
        northGridPanel.add(new JLabel("Update interval (seconds, restart required):"), getConstraints(0, 5));
        northGridPanel.add(updateIntervalField, getConstraints(1, 5));
        northGridPanel.add(new JLabel("Notification before (minutes):"), getConstraints(0, 6));
        northGridPanel.add(notificationBeforeField, getConstraints(1, 6));

        northGridPanel.add(checkBoxPanel, getConstraints(0, 8, 2));

        // Construct the frame
        add(northGridPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);

        loadConfigValues();
    }

    private void loadConfigValues() {
        latitudeTextField.setText(String.valueOf(configManager.getLatitude()));
        longitudeTextField.setText(String.valueOf(configManager.getLongitude()));
        methodComboBox.setSelectedItem(MethodEnum.getMethodByCode(configManager.getMethod()).getTitle());
        int school = configManager.getSchool();
        shafiRadioButton.setSelected(school == 0);
        hanafiRadioButton.setSelected(school == 1);
        updateIntervalField.setText(String.valueOf(configManager.getUpdateDelay()));
        notificationBeforeField.setText(String.valueOf(configManager.getNotificationBeforeMinutes()));
        notificationsCheckBox.setSelected(configManager.isNotification());
        lookAndFeelCheckBox.setSelected(configManager.getLookAndFeelEnabled());
        draggableCheckBox.setSelected(configManager.isDraggable());
        alwaysOnTopCheckBox.setSelected(configManager.isAlwaysOnTop());
        autostartCheckBox.setSelected(configManager.isAutostart());
    }

    private void saveSettings() {
        configManager.setLatitude(Double.parseDouble(latitudeTextField.getText()));
        configManager.setLongitude(Double.parseDouble(longitudeTextField.getText()));
        configManager.setMethod((MethodEnum.getMethodByName((String) methodComboBox.getSelectedItem()).getCode()));
        int school = shafiRadioButton.isSelected() ? 0 : 1;
        configManager.setSchool(school);
        configManager.setLookAndFeelEnabled(lookAndFeelCheckBox.isSelected());
        configManager.setDraggable(draggableCheckBox.isSelected());
        configManager.setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
        configManager.setUpdateDelay(Integer.parseInt(updateIntervalField.getText()));
        configManager.setAutostart(autostartCheckBox.isSelected());
        configManager.setNotification(notificationsCheckBox.isSelected());
        configManager.setNotificationBeforeMinutes(Integer.parseInt(notificationBeforeField.getText()));
        dispose();
    }

    private GridBagConstraints getConstraints(int x, int y) {
        return getConstraints(x, y, 1);
    }

    private GridBagConstraints getConstraints(int x, int y, int width) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(5, 10, 10, 10);
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        return c;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SettingsWindow settingsWindow = new SettingsWindow();
            settingsWindow.setVisible(true);
        });
    }
}
