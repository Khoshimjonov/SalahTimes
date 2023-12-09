
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Offset {

    @SerializedName("Imsak")
    @Expose
    private Integer imsak;
    @SerializedName("Fajr")
    @Expose
    private Integer fajr;
    @SerializedName("Sunrise")
    @Expose
    private Integer sunrise;
    @SerializedName("Dhuhr")
    @Expose
    private Integer dhuhr;
    @SerializedName("Asr")
    @Expose
    private Integer asr;
    @SerializedName("Maghrib")
    @Expose
    private Integer maghrib;
    @SerializedName("Sunset")
    @Expose
    private Integer sunset;
    @SerializedName("Isha")
    @Expose
    private Integer isha;
    @SerializedName("Midnight")
    @Expose
    private Integer midnight;

}
