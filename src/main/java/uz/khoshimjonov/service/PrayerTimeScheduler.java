package uz.khoshimjonov.service;

import java.awt.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PrayerTimeScheduler {

    private static volatile PrayerTimeScheduler instance;


    private ScheduledExecutorService scheduler;

    private PrayerTimeScheduler() {
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public static PrayerTimeScheduler getInstance() {
        if (instance == null) {
            synchronized (PrayerTimeScheduler.class) {
                if (instance == null) {
                    instance = new PrayerTimeScheduler();
                }
            }
        }
        return instance;
    }

    public void checkAndNotifyPrayerTimes(Map<String, LocalTime> timings, TrayIcon trayIcon) {
        try {
            scheduler.shutdownNow();
            scheduler = Executors.newScheduledThreadPool(1);

            for (Map.Entry<String, LocalTime> entry : timings.entrySet()) {
                scheduleNotification(entry.getKey(), entry.getValue(), trayIcon);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleNotification(String prayerName, LocalTime prayerTime, TrayIcon trayIcon) {
        LocalTime now = LocalTime.now();
        if (prayerTime.isAfter(now)) {
            long delay = Duration.between(now, prayerTime).toSeconds();
            scheduler.schedule(() -> trayIcon.displayMessage(String.format(LanguageHelper.getText("notificationTitle"), prayerName), prayerTime.toString(), TrayIcon.MessageType.INFO), delay, TimeUnit.SECONDS);
        }
    }
}
