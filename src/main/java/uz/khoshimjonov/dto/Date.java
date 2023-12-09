
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Date {

    @SerializedName("readable")
    @Expose
    private String readable;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("hijri")
    @Expose
    private Hijri hijri;
    @SerializedName("gregorian")
    @Expose
    private Gregorian gregorian;

    public String getReadable() {
        return readable;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Hijri getHijri() {
        return hijri;
    }

    public Gregorian getGregorian() {
        return gregorian;
    }
}
