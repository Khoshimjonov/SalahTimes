package uz.khoshimjonov.widget;

import uz.khoshimjonov.dto.Hijri;
import uz.khoshimjonov.service.LanguageHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.util.Map;

public class SalahTimesWindow extends JDialog {

    public SalahTimesWindow(Map<String, LocalTime> timings, Hijri hijriDate, MouseEvent e) {

        if (timings.isEmpty()) {
            return;
        }

        setUndecorated(true);
        setMinimumSize(new Dimension(200, 340));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        pack();
        toFront();
        setFocusableWindowState(false);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel headerPanel = new JPanel();
        JLabel timeLabel = new JLabel(hijriDate.getYear(), SwingConstants.CENTER);
        JLabel dateLabel = new JLabel(hijriDate.getMonth().getEn() + " " + hijriDate.getDay(), SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        headerPanel.setLayout(new GridLayout(2, 1));
        headerPanel.add(timeLabel);
        headerPanel.add(dateLabel);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel prayerTimesPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);

        int i = 0;
        for (Map.Entry<String, LocalTime> entry : timings.entrySet()) {
            gbc.gridx = 0;
            gbc.gridy = i++;
            JLabel nameLabel = new JLabel(entry.getKey(), SwingConstants.RIGHT);
            nameLabel.setFont(nameLabel.getFont().deriveFont(18.0f));
            prayerTimesPanel.add(nameLabel, gbc);

            gbc.gridx = 1;
            JLabel timeValueLabel = new JLabel(String.valueOf(entry.getValue()), SwingConstants.LEFT);
            timeValueLabel.setFont(timeValueLabel.getFont().deriveFont(18.0f));
            prayerTimesPanel.add(timeValueLabel, gbc);
        }

        JPanel buttonPanel = new JPanel();
        JButton settingsButton = new JButton(LanguageHelper.getText("settingsTitle"));
        JButton exitButton = new JButton(LanguageHelper.getText("exitTitle"));

        settingsButton.setFont(settingsButton.getFont().deriveFont(16.0f));
        exitButton.setFont(exitButton.getFont().deriveFont(16.0f));

        settingsButton.addActionListener(e1 -> {
            SettingsWindow settingsWindow = new SettingsWindow();
            settingsWindow.setVisible(true);
            setVisible(false);
        });

        exitButton.addActionListener(e12 -> System.exit(0));

        buttonPanel.add(settingsButton);
        buttonPanel.add(exitButton);
        buttonPanel.setLayout(new GridLayout(2, 1));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(prayerTimesPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
        setLocationRelativeTo(null);
        setPosition(e);
        setVisible(true);

    }

    private void setPosition(MouseEvent e) {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Rectangle bounds = gd.getDefaultConfiguration().getBounds();

        Point mouseClickPoint = e.getPoint();

        int frameWidth = getWidth();
        int frameHeight = getHeight();

        int halfWidth = frameWidth / 2;
        int padding = 20;

        int newX = mouseClickPoint.x - halfWidth;
        int newY = mouseClickPoint.y + padding;

        if (newX + frameWidth > bounds.width) {
            newX = bounds.width - frameWidth - padding;
        }
        if (newY + frameHeight > bounds.height) {
            newY = mouseClickPoint.y - frameHeight - padding;
        }

        if (newX < 0) {
            newX = mouseClickPoint.x + padding;
        }

        if (newY < 0) {
            newX = mouseClickPoint.y + padding;
        }

        setLocation(newX, newY);
    }
}
