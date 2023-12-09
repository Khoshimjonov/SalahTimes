
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
}
