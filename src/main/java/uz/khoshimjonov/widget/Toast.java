package uz.khoshimjonov.widget;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Toast {

    public static void showToast(Window owner, String message, int durationMillis) {
        JWindow toast = new JWindow(owner);

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(50, 50, 50));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        label.setFont(new Font("Arial", Font.PLAIN, 14));

        toast.add(label);
        toast.pack();
        toast.setLocationRelativeTo(owner);
        toast.setVisible(true);

        new Timer(durationMillis, e -> {
            toast.dispose();
            ((Timer) e.getSource()).stop();
        }).start();
    }

    public static void showSystemNotification(String title, String message) {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            ProcessBuilder builder;

            if (os.contains("win")) {
                // Windows 10/11 using PowerShell
                String escapedTitle = title.replace("'", "''").replace("\"", "`\"");
                String escapedMessage = message.replace("'", "''").replace("\"", "`\"");

                String script =
                        "[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] | Out-Null; " +
                                "[Windows.Data.Xml.Dom.XmlDocument, Windows.Data.Xml.Dom.XmlDocument, ContentType = WindowsRuntime] | Out-Null; " +
                                "$template = '<toast><visual><binding template=\"ToastText02\"><text id=\"1\">" + escapedTitle + "</text><text id=\"2\">" + escapedMessage + "</text></binding></visual></toast>'; " +
                                "$xml = New-Object Windows.Data.Xml.Dom.XmlDocument; " +
                                "$xml.LoadXml($template); " +
                                "$toast = [Windows.UI.Notifications.ToastNotification]::new($xml); " +
                                "[Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('Salah Widget').Show($toast);";

                builder = new ProcessBuilder("powershell", "-ExecutionPolicy", "Bypass", "-Command", script);

            } else if (os.contains("mac")) {
                // macOS using osascript
                String escapedTitle = title.replace("\"", "\\\"");
                String escapedMessage = message.replace("\"", "\\\"");

                String script = String.format(
                        "display notification \"%s\" with title \"%s\"",
                        escapedMessage, escapedTitle
                );
                builder = new ProcessBuilder("osascript", "-e", script);

            } else {
                // Linux using notify-send (requires libnotify)
                builder = new ProcessBuilder("notify-send", "-a", "Salah Widget", title, message);
            }

            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Don't wait for the process to complete (non-blocking)
            new Thread(() -> {
                try {
                    process.waitFor(5, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {}
            }).start();

        } catch (Exception e) {
            System.err.println("Failed to show system notification: " + e.getMessage());
            // You could fallback to trayIcon.displayMessage() here
        }
    }
}