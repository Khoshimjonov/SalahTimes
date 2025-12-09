package uz.khoshimjonov.widget;

import uz.khoshimjonov.dto.WidgetTextDto;
import uz.khoshimjonov.service.ConfigurationManager;
import uz.khoshimjonov.service.LanguageHelper;
import uz.khoshimjonov.service.SalahTimeService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SalahWidget {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    private final SalahTimeService salahTimeService = new SalahTimeService();
    private FrameDragListener frameDragListener = null;
    private SalahTimesWindow salahTimesWindow;
    private final TrayIcon trayIcon;
    private final SystemTray tray;
    private final boolean SET_LOOK_AND_FEEL;
    private final int UPDATE_DELAY;
    private final int POINT_X;
    private final int POINT_Y;

    private volatile long lastWindowCloseTime = 0;
    private static final long DEBOUNCE_MS = 300;

    public SalahWidget() {
        try {
            if (!SystemTray.isSupported()) {
                System.out.println("SystemTray is not supported");
                throw new UnsupportedOperationException();
            }

            this.SET_LOOK_AND_FEEL = configurationManager.getLookAndFeelEnabled();
            this.UPDATE_DELAY = configurationManager.getUpdateDelay();
            this.POINT_X = configurationManager.getPointX();
            this.POINT_Y = configurationManager.getPointY();
            this.tray = SystemTray.getSystemTray();

            BufferedImage trayIconImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/app.png")));
            int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
            this.trayIcon = new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH), LanguageHelper.getText("tooltipTitle"));
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        showSalahTimesWindow();
                    }
                }
            });
            tray.add(trayIcon);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void displayWidget() {
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
            dialog.setMinimumSize(new Dimension(350, 30));
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
                WidgetTextDto widgetText = salahTimeService.getWidgetText(trayIcon);
                if (widgetText != null) {
                    timeLabel.setText(widgetText.getNextSalah());
                    remainingLabel.setText(widgetText.getRemainingTime());
                    remainingLabel.setForeground(widgetText.getTextColor());
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
            dialog.setLocation(POINT_X, POINT_Y);
            dialog.setVisible(true);

            if (configurationManager.isDraggable()) {
                frameDragListener = new FrameDragListener(dialog, dialog.getLocation());
                dialog.addMouseListener(frameDragListener);
                dialog.addMouseMotionListener(frameDragListener);
            }

            PopupMenu popupMenu = new PopupMenu();

            MenuItem showTimesItem = new MenuItem(LanguageHelper.getText("showTimesTitle"));
            showTimesItem.addActionListener(e -> showSalahTimesWindow());
            popupMenu.add(showTimesItem);

            popupMenu.addSeparator();

            MenuItem settingsItem = new MenuItem(LanguageHelper.getText("settingsTitle"));
            settingsItem.addActionListener(e -> showSettingsWindow());
            popupMenu.add(settingsItem);

            MenuItem exitItem = new MenuItem(LanguageHelper.getText("exitTitle"));
            exitItem.addActionListener(e -> {
                cleanup();
                tray.remove(trayIcon);
                System.exit(0);
            });
            popupMenu.add(exitItem);

            trayIcon.setPopupMenu(popupMenu);
            showSettingsWindow();
        };
        SwingUtilities.invokeLater(runnable);
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }

    private void showSettingsWindow() {
        SettingsWindow settingsWindow = new SettingsWindow();
        settingsWindow.setVisible(true);
    }

    private void showSalahTimesWindow() {
        try {
            salahTimeService.getWidgetText(trayIcon);
        } catch (Exception ignored) {}

        // Debounce: if window was closed very recently, this click caused it
        if (System.currentTimeMillis() - lastWindowCloseTime < DEBOUNCE_MS) {
            return;
        }

        if (salahTimesWindow != null && salahTimesWindow.isDisplayable()) {
            salahTimesWindow.dispose();
            salahTimesWindow = null;
        } else {
            salahTimesWindow = new SalahTimesWindow(
                    salahTimeService.getTimings(),
                    salahTimeService.getHijriDate(),
                    () -> lastWindowCloseTime = System.currentTimeMillis()
            );
        }
    }

    private void cleanup() {
        System.out.println("App is closing...");
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
        // Also shutdown prayer notification scheduler
        try {
            uz.khoshimjonov.service.PrayerTimeScheduler.getInstance().shutdown();
        } catch (Exception ignored) {}
    }

}