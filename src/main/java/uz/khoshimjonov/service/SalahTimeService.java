package uz.khoshimjonov.service;

import uz.khoshimjonov.api.Api;
import uz.khoshimjonov.dto.Hijri;
import uz.khoshimjonov.dto.PrayerTimesResponse;
import uz.khoshimjonov.dto.Timings;
import uz.khoshimjonov.dto.WidgetTextDto;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class SalahTimeService {
    private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    private final PrayerTimeScheduler prayerTimeScheduler = PrayerTimeScheduler.getInstance();
    private final Map<String, LocalTime> timings;
    private final DateTimeFormatter formatter;
    private final Api api;
    private LocalTime tomorrowFajr;
    private LocalDate currentDate;
    private Hijri hijriDate;

    public Map<String, LocalTime> getTimings() {
        return timings;
    }

    public Hijri getHijriDate() {
        return hijriDate;
    }

    public SalahTimeService() {
        api = new Api();
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        currentDate = LocalDate.now();
        timings = new LinkedHashMap<>();
    }

    public WidgetTextDto getWidgetText(TrayIcon trayIcon) {
        try {
            LocalTime currentTime = LocalTime.now();
            getTimingsIfNeeded(trayIcon);
            if (timings.get(title("ishaTitle")).isBefore(currentTime)){
                LocalDateTime tomorrow = LocalDateTime.of(LocalDate.now().plusDays(1), tomorrowFajr);
                LocalDateTime today = LocalDateTime.now();
                return getResultText(today.until(tomorrow, ChronoUnit.SECONDS), title("fajrTitle"), tomorrowFajr);
            } else {
                for (Map.Entry<String, LocalTime> entry : timings.entrySet()) {
                    if (currentTime.isBefore(entry.getValue())) {
                        LocalTime nextPrayerTime = entry.getValue();
                        return getResultText(currentTime.until(nextPrayerTime, ChronoUnit.SECONDS), entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new WidgetTextDto(LanguageHelper.getText("cantGetTitle"), "", new Color(185, 73, 58));
    }

    private void getTimingsIfNeeded(TrayIcon trayIcon) throws Exception {
        LocalDate realDate = LocalDate.now();
        if (configurationManager.apiSettingsUpdated || !currentDate.equals(realDate) || timings.isEmpty()){
            currentDate = realDate;
            timings.clear();

            PrayerTimesResponse prayerTimes = api.getSalahTimes(currentDate.format(formatter), configurationManager.getSchool(), configurationManager.getMethod(), String.valueOf(configurationManager.getLatitude()), String.valueOf(configurationManager.getLongitude()));
            PrayerTimesResponse tomorrowPrayerTimes = api.getSalahTimes(currentDate.plusDays(1).format(formatter), configurationManager.getSchool(), configurationManager.getMethod(), String.valueOf(configurationManager.getLatitude()), String.valueOf(configurationManager.getLongitude()));
            if (prayerTimes == null || tomorrowPrayerTimes == null){
                throw new RuntimeException();
            }

            Timings tomorrowTimings = tomorrowPrayerTimes.getData().getTimings();
            tomorrowFajr = LocalTime.parse(tomorrowTimings.getFajr());

            Timings todaysTimings = prayerTimes.getData().getTimings();
            timings.put(title("fajrTitle"), LocalTime.parse(todaysTimings.getFajr()));
            timings.put(title("sunriseTitle"), LocalTime.parse(todaysTimings.getSunrise()));
            timings.put(title("dhuhrTitle"), LocalTime.parse(todaysTimings.getDhuhr()));
            timings.put(title("asrTitle"), LocalTime.parse(todaysTimings.getAsr()));
            timings.put(title("maghribTitle"), LocalTime.parse(todaysTimings.getMaghrib()));
            timings.put(title("ishaTitle"), LocalTime.parse(todaysTimings.getIsha()));
            hijriDate = prayerTimes.getData().getDate().getHijri();
            configurationManager.apiSettingsUpdated = false;
            if (configurationManager.isNotification()) {
                prayerTimeScheduler.checkAndNotifyPrayerTimes(timings, trayIcon);
            }
        }
    }

    private String title(String key) {
        return LanguageHelper.getText(key);
    }

    private WidgetTextDto getResultText(long remaining, String Salah, LocalTime time) {
        long hours = remaining / 3600;
        long minutes = (remaining % 3600) / 60;
        long seconds = remaining % 60;
        Color color = remaining > 1800 ? Color.WHITE : new Color(185, 73, 58);
        return new WidgetTextDto(String.format(title("widgetTextTitle"), Salah, time), String.format(title("remainingTitle"), hours, minutes, seconds), color);
    }
}
