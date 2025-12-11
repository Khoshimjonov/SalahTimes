package uz.khoshimjonov.service;

import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Balanced prayer time notification scheduler.
 * <p>
 * Strategy:
 * - Uses coarse polling (every 30 seconds) most of the time
 * - Switches to fine polling (every 2 seconds) when within 2 minutes of a notification
 * - Falls back to immediate fire if notification was missed within grace period
 */
public class PrayerTimeScheduler {

    private static volatile PrayerTimeScheduler instance;

    private static final long COARSE_POLL_MS = 30_000;      // 30 seconds when idle
    private static final long FINE_POLL_MS = 2_000;         // 2 seconds when notification is near
    private static final long FINE_POLL_THRESHOLD_MS = 120_000; // Switch to fine polling 2 min before
    private static final long GRACE_PERIOD_MS = 90_000;     // Fire if missed within 90 seconds
    private static final long FIRE_WINDOW_MS = 2_500;       // Fire when within 2.5 seconds of target

    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, NotificationTarget> targets;
    private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    private volatile TrayIcon trayIcon;
    private volatile ScheduledFuture<?> currentPollTask;
    private volatile boolean isFinePollMode = false;

    private static class NotificationTarget {
        final String key;
        final Instant targetTime;
        final String title;
        final String body;
        volatile boolean fired;

        NotificationTarget(String key, Instant targetTime, String title, String body) {
            this.key = key;
            this.targetTime = targetTime;
            this.title = title;
            this.body = body;
            this.fired = false;
        }
    }

    private PrayerTimeScheduler() {
        ThreadFactory tf = r -> {
            Thread t = new Thread(r, "prayer-scheduler");
            t.setDaemon(true);
            return t;
        };
        this.scheduler = Executors.newSingleThreadScheduledExecutor(tf);
        this.targets = new ConcurrentHashMap<>();
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
        this.trayIcon = trayIcon;

        try {
            LocalDate today = LocalDate.now();
            ZoneId zone = ZoneId.systemDefault();

            // Clean obsolete targets
            Set<String> validNames = timings.keySet();
            targets.keySet().removeIf(key -> {
                String base = key.contains("#") ? key.substring(0, key.indexOf('#')) : key;
                return !validNames.contains(base);
            });

            // Register targets
            for (Map.Entry<String, LocalTime> entry : timings.entrySet()) {
                registerTargets(entry.getKey(), entry.getValue(), today, zone);
            }

            // Start or adjust polling
            adjustPollingMode();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerTargets(String prayerName, LocalTime prayerTime, LocalDate date, ZoneId zone) {
        int beforeMinutes = Math.max(0, configurationManager.getNotificationBeforeMinutes());
        boolean notifyBefore = configurationManager.isNotifyBefore();
        boolean notifyOnTime = configurationManager.isNotifyOnTime();

        Instant prayerInstant = ZonedDateTime.of(date, prayerTime, zone).toInstant();
        Instant now = Instant.now();
        Instant cutoff = now.minus(Duration.ofMillis(GRACE_PERIOD_MS));

        // Before notification
        String beforeKey = prayerName + "#before";
        if (notifyBefore && beforeMinutes > 0) {
            Instant beforeInstant = prayerInstant.minus(Duration.ofMinutes(beforeMinutes));
            if (beforeInstant.isAfter(cutoff)) {
                NotificationTarget existing = targets.get(beforeKey);
                if (existing == null || !existing.targetTime.equals(beforeInstant)) {
                    String title = String.format(
                            LanguageHelper.getText("notificationBeforeMessage"),
                            beforeMinutes, prayerName
                    );
                    targets.put(beforeKey, new NotificationTarget(
                            beforeKey, beforeInstant, title, prayerTime.format(timeFmt)
                    ));
                }
            }
        } else {
            targets.remove(beforeKey);
        }

        // On-time notification
        String onTimeKey = prayerName + "#ontime";
        if (notifyOnTime && prayerInstant.isAfter(cutoff)) {
            NotificationTarget existing = targets.get(onTimeKey);
            if (existing == null || !existing.targetTime.equals(prayerInstant)) {
                String title = String.format(
                        LanguageHelper.getText("notificationTitle"), prayerName
                );
                targets.put(onTimeKey, new NotificationTarget(
                        onTimeKey, prayerInstant, title, prayerTime.format(timeFmt)
                ));
            }
        } else if (!notifyOnTime) {
            targets.remove(onTimeKey);
        }
    }

    private synchronized void adjustPollingMode() {
        long nextTargetMs = getMillisToNextTarget();
        boolean needFinePoll = nextTargetMs >= 0 && nextTargetMs <= FINE_POLL_THRESHOLD_MS;

        if (needFinePoll && !isFinePollMode) {
            switchToFinePoll();
        } else if (!needFinePoll && isFinePollMode) {
            switchToCoarsePoll();
        } else if (currentPollTask == null || currentPollTask.isDone()) {
            // Start polling if not running
            if (needFinePoll) {
                switchToFinePoll();
            } else {
                switchToCoarsePoll();
            }
        }
    }

    private void switchToFinePoll() {
        cancelCurrentTask();
        isFinePollMode = true;
        currentPollTask = scheduler.scheduleAtFixedRate(
                this::poll, 0, FINE_POLL_MS, TimeUnit.MILLISECONDS
        );
    }

    private void switchToCoarsePoll() {
        cancelCurrentTask();
        isFinePollMode = false;
        currentPollTask = scheduler.scheduleAtFixedRate(
                this::poll, 0, COARSE_POLL_MS, TimeUnit.MILLISECONDS
        );
    }

    private void cancelCurrentTask() {
        if (currentPollTask != null && !currentPollTask.isDone()) {
            currentPollTask.cancel(false);
        }
    }

    private long getMillisToNextTarget() {
        Instant now = Instant.now();
        long minMs = Long.MAX_VALUE;

        for (NotificationTarget target : targets.values()) {
            if (!target.fired) {
                long ms = Duration.between(now, target.targetTime).toMillis();
                if (ms > -GRACE_PERIOD_MS && ms < minMs) {
                    minMs = ms;
                }
            }
        }

        return minMs == Long.MAX_VALUE ? -1 : minMs;
    }

    private void poll() {
        try {
            Instant now = Instant.now();
            TrayIcon icon = this.trayIcon;
            if (icon == null) return;

            for (NotificationTarget target : targets.values()) {
                if (target.fired) continue;

                long diffMs = Duration.between(now, target.targetTime).toMillis();

                // Fire if within window or just missed (within grace period)
                if (diffMs <= FIRE_WINDOW_MS && diffMs >= -GRACE_PERIOD_MS) {
                    fire(target, icon);
                }
            }

            // Adjust polling mode after checking
            adjustPollingMode();

            // Cleanup old targets
            Instant cutoff = now.minus(Duration.ofMinutes(5));
            targets.entrySet().removeIf(e ->
                    e.getValue().fired && e.getValue().targetTime.isBefore(cutoff)
            );

            System.out.println(getDebugInfo());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fire(NotificationTarget target, TrayIcon icon) {
        if (target.fired) return;

        synchronized (target) {
            if (target.fired) return;
            target.fired = true;
        }

        try {
            icon.displayMessage(target.title, target.body, TrayIcon.MessageType.INFO);
        } catch (Exception e) {
            target.fired = false; // Allow retry
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            cancelCurrentTask();
            targets.clear();
            scheduler.shutdown();
            if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (Exception e) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Returns current scheduler state for debugging.
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        Instant now = Instant.now();
        ZoneId zone = ZoneId.systemDefault();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

        sb.append("=== PrayerTimeScheduler Debug ===\n");
        sb.append("Time: ").append(ZonedDateTime.now(zone).format(fmt)).append("\n");
        sb.append("Zone: ").append(zone).append("\n");
        sb.append("Poll Mode: ").append(isFinePollMode ? "FINE (2s)" : "COARSE (30s)").append("\n");
        sb.append("Poll Task Active: ").append(currentPollTask != null && !currentPollTask.isDone()).append("\n");
        sb.append("Targets: ").append(targets.size()).append("\n\n");

        if (targets.isEmpty()) {
            sb.append("No scheduled notifications.\n");
        } else {
            for (NotificationTarget target : targets.values()) {
                long diffMs = Duration.between(now, target.targetTime).toMillis();
                long diffSec = diffMs / 1000;

                String status;
                if (target.fired) {
                    status = "FIRED";
                } else if (diffMs < 0) {
                    status = "OVERDUE by " + Math.abs(diffSec) + "s";
                } else if (diffMs < FINE_POLL_THRESHOLD_MS) {
                    status = "SOON in " + diffSec + "s";
                } else {
                    long minutes = diffSec / 60;
                    status = "PENDING in " + minutes + "m";
                }

                ZonedDateTime localTarget = target.targetTime.atZone(zone);

                sb.append(String.format("  [%s] %s%n", status, target.key));
                sb.append(String.format("       Target: %s%n", localTarget.format(fmt)));
                sb.append(String.format("       Title: %s%n", target.title));
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}