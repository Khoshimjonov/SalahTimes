
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Meta {

    @SerializedName("latitude")
    @Expose
    public Float latitude;
    @SerializedName("longitude")
    @Expose
    public Float longitude;
    @SerializedName("timezone")
    @Expose
    public String timezone;
    @SerializedName("method")
    @Expose
    public Method method;
    @SerializedName("latitudeAdjustmentMethod")
    @Expose
    public String latitudeAdjustmentMethod;
    @SerializedName("midnightMode")
    @Expose
    public String midnightMode;
    @SerializedName("school")
    @Expose
    public String school;
    @SerializedName("offset")
    @Expose
    public Offset offset;

}
