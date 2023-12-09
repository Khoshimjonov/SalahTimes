
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Hijri {

    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("format")
    @Expose
    private String format;
    @SerializedName("day")
    @Expose
    private String day;
    @SerializedName("weekday")
    @Expose
    private Weekday weekday;
    @SerializedName("month")
    @Expose
    private Month month;
    @SerializedName("year")
    @Expose
    private String year;
    @SerializedName("designation")
    @Expose
    private Designation designation;
    @SerializedName("holidays")
    @Expose
    private List<Object> holidays;

    public String getDate() {
        return date;
    }

    public String getFormat() {
        return format;
    }

    public String getDay() {
        return day;
    }

    public Weekday getWeekday() {
        return weekday;
    }

    public Month getMonth() {
        return month;
    }

    public String getYear() {
        return year;
    }

    public Designation getDesignation() {
        return designation;
    }

    public List<Object> getHolidays() {
        return holidays;
    }
}
