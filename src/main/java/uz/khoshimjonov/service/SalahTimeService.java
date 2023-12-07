package uz.khoshimjonov.service;

import uz.khoshimjonov.api.AlAdhanApi;
import uz.khoshimjonov.dto.PrayerTimesResponse;
import uz.khoshimjonov.dto.Timings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class SalahTimeService {
    private final AlAdhanApi api;
    private final DateTimeFormatter formatter;
    private LocalDate currentDate;
    private LocalTime tomorrowFajr;
    private final Map<String, LocalTime> timings;
    private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();

    public SalahTimeService() {
        api = new AlAdhanApi();
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        currentDate = LocalDate.now();
        timings = new LinkedHashMap<>();
    }

    public String[] getWidgetText() {
        try {
            LocalTime currentTime = LocalTime.now();
            getTimingsIfNeeded();
            if (timings.get("Isha").isBefore(currentTime)){
                LocalDateTime tomorrow = LocalDateTime.of(LocalDate.now().plusDays(1), tomorrowFajr);
                LocalDateTime today = LocalDateTime.now();
                return getResultText(today.until(tomorrow, ChronoUnit.SECONDS), "Fajr", tomorrowFajr);
            } else {
                for (Map.Entry<String, LocalTime> entry : timings.entrySet()) {
                    if (currentTime.isBefore(entry.getValue())) {
                        LocalTime nextPrayerTime = entry.getValue();
                        return getResultText(currentTime.until(nextPrayerTime, ChronoUnit.SECONDS), entry.getKey(), entry.getValue());
                    }
                }
            }
            return new String[] {"Could not get Salah times", "", "RED"};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[] {"Could not get Salah times", "", "RED"};
    }

    private void getTimingsIfNeeded() throws Exception {
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
            tomorrowFajr = LocalTime.parse(tomorrowTimings.fajr);

            Timings todaysTimings = prayerTimes.getData().getTimings();
            timings.put("Fajr", LocalTime.parse(todaysTimings.fajr));
            timings.put("Sunrise", LocalTime.parse(todaysTimings.sunrise));
            timings.put("Dhuhr", LocalTime.parse(todaysTimings.dhuhr));
            timings.put("Asr", LocalTime.parse(todaysTimings.asr));
            timings.put("Maghrib", LocalTime.parse(todaysTimings.maghrib));
            timings.put("Isha", LocalTime.parse(todaysTimings.isha));
            configurationManager.apiSettingsUpdated = false;
        }
    }

    private static String[] getResultText(long remaining, String Salah, LocalTime time) {
        long hours = remaining / 3600;
        long minutes = (remaining % 3600) / 60;
        long seconds = remaining % 60;
        String color = remaining > 1800 ? "WHITE" : "RED";
        return new String[]{String.format("%s at %s |", Salah, time), String.format("Remaining: %02d:%02d:%02d", hours, minutes, seconds), color};
    }
}
