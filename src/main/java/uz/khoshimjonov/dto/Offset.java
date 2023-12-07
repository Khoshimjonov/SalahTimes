
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Offset {

    @SerializedName("Imsak")
    @Expose
    public Integer imsak;
    @SerializedName("Fajr")
    @Expose
    public Integer fajr;
    @SerializedName("Sunrise")
    @Expose
    public Integer sunrise;
    @SerializedName("Dhuhr")
    @Expose
    public Integer dhuhr;
    @SerializedName("Asr")
    @Expose
    public Integer asr;
    @SerializedName("Maghrib")
    @Expose
    public Integer maghrib;
    @SerializedName("Sunset")
    @Expose
    public Integer sunset;
    @SerializedName("Isha")
    @Expose
    public Integer isha;
    @SerializedName("Midnight")
    @Expose
    public Integer midnight;

}
