
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Date {

    @SerializedName("readable")
    @Expose
    public String readable;
    @SerializedName("timestamp")
    @Expose
    public String timestamp;
    @SerializedName("hijri")
    @Expose
    public Hijri hijri;
    @SerializedName("gregorian")
    @Expose
    public Gregorian gregorian;

}
