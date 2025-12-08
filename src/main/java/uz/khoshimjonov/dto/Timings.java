
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import uz.khoshimjonov.service.SalahTimesCalculator;

import java.time.format.DateTimeFormatter;

public class Timings {

    @SerializedName("Fajr")
    @Expose
    private String fajr;
    @SerializedName("Sunrise")
    @Expose
    private String sunrise;
    @SerializedName("Dhuhr")
    @Expose
    private String dhuhr;
    @SerializedName("Asr")
    @Expose
    private String asr;
    @SerializedName("Sunset")
    @Expose
    private String sunset;
    @SerializedName("Maghrib")
    @Expose
    private String maghrib;
    @SerializedName("Isha")
    @Expose
    private String isha;
    @SerializedName("Imsak")
    @Expose
    private String imsak;
    @SerializedName("Midnight")
    @Expose
    private String midnight;
    @SerializedName("Firstthird")
    @Expose
    private String firstthird;
    @SerializedName("Lastthird")
    @Expose
    private String lastthird;

    public String getFajr() {
        return fajr;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getDhuhr() {
        return dhuhr;
    }

    public String getAsr() {
        return asr;
    }

    public String getSunset() {
        return sunset;
    }

    public String getMaghrib() {
        return maghrib;
    }

    public String getIsha() {
        return isha;
    }

    public String getImsak() {
        return imsak;
    }

    public String getMidnight() {
        return midnight;
    }

    public String getFirstthird() {
        return firstthird;
    }

    public String getLastthird() {
        return lastthird;
    }

    public Timings(String imsak, String fajr, String sunrise, String dhuhr, String asr, String sunset, String maghrib, String isha, String midnight, String firstthird, String lastthird) {
        this.imsak = imsak;
        this.fajr = fajr;
        this.sunrise = sunrise;
        this.dhuhr = dhuhr;
        this.asr = asr;
        this.sunset = sunset;
        this.maghrib = maghrib;
        this.isha = isha;
        this.midnight = midnight;
        this.firstthird = firstthird;
        this.lastthird = lastthird;
    }

    public Timings() {
    }

    public static Timings fromCalculation(SalahTimesCalculator.PrayerTimes prayerTimes) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        Timings timings = new Timings();
        timings.imsak = prayerTimes.imsak.format(fmt);
        timings.fajr = prayerTimes.fajr.format(fmt);
        timings.sunrise = prayerTimes.sunrise.format(fmt);
        timings.dhuhr = prayerTimes.dhuhr.format(fmt);
        timings.asr = prayerTimes.asr.format(fmt);
        timings.maghrib = prayerTimes.maghrib.format(fmt);
        timings.isha = prayerTimes.isha.format(fmt);
        timings.midnight = prayerTimes.midnight.format(fmt);
        return timings;
    }
}
