package uz.khoshimjonov.service;

import java.awt.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class PrayerTimeScheduler {

    private static volatile PrayerTimeScheduler instance;

    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks;
    private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    private PrayerTimeScheduler() {
        ThreadFactory tf = r -> {
            Thread t = new Thread(r, "prayer-time-scheduler");
            t.setDaemon(true);
            return t;
        };
        this.scheduler = Executors.newSingleThreadScheduledExecutor(tf);
        this.scheduledTasks = new ConcurrentHashMap<>();
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
            // Cancel tasks that are no longer relevant (e.g., due to language change or day switch)
            Set<String> validKeys = new HashSet<>(timings.keySet());
            for (String key : scheduledTasks.keySet()) {
                if (!validKeys.contains(key)) {
                    ScheduledFuture<?> future = scheduledTasks.remove(key);
                    if (future != null) future.cancel(false);
                }
            }

            // (Re)schedule for all provided timings
            for (Map.Entry<String, LocalTime> entry : timings.entrySet()) {
                scheduleNotification(entry.getKey(), entry.getValue(), trayIcon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleNotification(String prayerName, LocalTime prayerTime, TrayIcon trayIcon) {
        int beforeMinutes = Math.max(0, configurationManager.getNotificationBeforeMinutes());
        LocalTime targetTime = prayerTime.minusMinutes(beforeMinutes);
        LocalTime now = LocalTime.now();

        if (!targetTime.isAfter(now)) {
            // Target already passed for today – do not schedule.
            cancelIfExists(prayerName);
            return;
        }

        long delaySeconds = Duration.between(now, targetTime).toSeconds();

        // Cancel any previous task for this prayer
        cancelIfExists(prayerName);

        String title = String.format(LanguageHelper.getText("notificationTitle"), prayerName);
        String body;
        if (beforeMinutes > 0) {
            body = String.format("%s (%d min)", prayerTime.format(timeFmt), beforeMinutes);
        } else {
            body = prayerTime.format(timeFmt);
        }

        ScheduledFuture<?> future = scheduler.schedule(
                () -> trayIcon.displayMessage(title, body, TrayIcon.MessageType.INFO),
                delaySeconds,
                TimeUnit.SECONDS
        );
        scheduledTasks.put(prayerName, future);
    }

    private void cancelIfExists(String prayerName) {
        ScheduledFuture<?> prev = scheduledTasks.remove(prayerName);
        if (prev != null) {
            prev.cancel(false);
        }
    }

    public void shutdown() {
        try {
            for (ScheduledFuture<?> f : scheduledTasks.values()) {
                f.cancel(false);
            }
            scheduledTasks.clear();
            scheduler.shutdown();
        } catch (Exception ignored) {
        }
    }
}
