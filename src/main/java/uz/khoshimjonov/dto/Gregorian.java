
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Gregorian {

    @SerializedName("date")
    @Expose
    public String date;
    @SerializedName("format")
    @Expose
    public String format;
    @SerializedName("day")
    @Expose
    public String day;
    @SerializedName("weekday")
    @Expose
    public Weekday weekday;
    @SerializedName("month")
    @Expose
    public Month month;
    @SerializedName("year")
    @Expose
    public String year;
    @SerializedName("designation")
    @Expose
    public Designation designation;

}
