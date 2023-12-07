package uz.khoshimjonov.widget;

import uz.khoshimjonov.service.ConfigurationManager;
import uz.khoshimjonov.service.SalahTimeService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SalahWidget {

    private final boolean SET_LOOK_AND_FEEL;
    private final int UPDATE_DELAY;
    private final SalahTimeService salahTimeService = new SalahTimeService();
    private FrameDragListener frameDragListener = null;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final SystemTray tray;
    private final TrayIcon trayIcon;
    private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();

    public SalahWidget() {
        try {
            SET_LOOK_AND_FEEL = configurationManager.getLookAndFeelEnabled();
            UPDATE_DELAY = configurationManager.getUpdateDelay();

            if (!SystemTray.isSupported()) {
                System.out.println("SystemTray is not supported");
                throw new UnsupportedOperationException();
            }
            this.tray = SystemTray.getSystemTray();

            BufferedImage trayIconImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/app.png")));
            int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
            this.trayIcon = new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH), "Salah times");
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                    }
                }
            });
            tray.add(trayIcon);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void displayWidget() {

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();
        final Rectangle bounds = screens[0].getDefaultConfiguration().getBounds();

        Runnable runnable = () -> {
            if (SET_LOOK_AND_FEEL) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
            }

            final JDialog dialog = new JDialog();
            final JLabel timeLabel = new AntiAliasedLabel();
            final JLabel remainingLabel = new AntiAliasedLabel();

            dialog.setLayout(new FlowLayout(FlowLayout.LEFT));
            dialog.requestFocus();
            dialog.setMinimumSize(new Dimension(280, 30));
            dialog.setFocusableWindowState(false);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setUndecorated(true);
            dialog.setBackground(new Color(0, 0, 0, 0));
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setAlwaysOnTop(true);
            dialog.add(timeLabel);
            dialog.add(remainingLabel);
            dialog.toFront();

            scheduler.scheduleAtFixedRate(() -> {
                String[] widgetText = salahTimeService.getWidgetText();
                if (widgetText.length == 3) {
                    timeLabel.setText(widgetText[0]);
                    remainingLabel.setText(widgetText[1]);
                    if (widgetText[2].equals("RED")){
                        remainingLabel.setForeground(new Color(185, 73, 58));
                    } else {
                        remainingLabel.setForeground(Color.WHITE);
                    }
                }
                if (configurationManager.isAlwaysOnTop()) {
                    dialog.toFront();
                }
                if (frameDragListener != null && frameDragListener.getSavedPosition() != null) {
                    dialog.setLocation(frameDragListener.getSavedPosition());
                }
            }, 0, UPDATE_DELAY, TimeUnit.SECONDS);

            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent windowEvent) {
                    cleanup();
                }
            });
            Point bottomRight = new Point(50, bounds.y + bounds.height - dialog.getHeight() - 9);
            dialog.setLocation(bottomRight);
            dialog.setVisible(true);

            if (configurationManager.isDraggable()) {
                frameDragListener = new FrameDragListener(dialog, dialog.getLocation());
                dialog.addMouseListener(frameDragListener);
                dialog.addMouseMotionListener(frameDragListener);
            }

            PopupMenu popupMenu = new PopupMenu();

            MenuItem settingsItem = new MenuItem("Settings");
            settingsItem.addActionListener(e -> showSettingsWindow());
            popupMenu.add(settingsItem);

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                cleanup();
                tray.remove(trayIcon);
                System.exit(0);
            });
            popupMenu.add(exitItem);

            trayIcon.setPopupMenu(popupMenu);
        };
        SwingUtilities.invokeLater(runnable);
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }

    private void showNotification() {
        try {
            trayIcon.displayMessage("Time is almost up!", "Hurry up!", TrayIcon.MessageType.INFO);
            //tray.remove(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSettingsWindow() {
        SettingsWindow settingsWindow = new SettingsWindow();
        settingsWindow.setVisible(true);
    }

    private void cleanup() {
        System.err.println("App is closing...");
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Scheduler did not terminate in time.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}