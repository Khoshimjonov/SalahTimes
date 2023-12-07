
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class PrayerTimesResponse {
    @SerializedName("data")
    @Expose
    private Data data;


    public Data getData() {
        return data;
    }

}
