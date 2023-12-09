
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Params {

    @SerializedName("Fajr")
    @Expose
    private String fajr;
    @SerializedName("Isha")
    @Expose
    private String isha;

}
