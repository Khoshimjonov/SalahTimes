
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Meta {

    @SerializedName("latitude")
    @Expose
    private Float latitude;
    @SerializedName("longitude")
    @Expose
    private Float longitude;
    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("method")
    @Expose
    private Method method;
    @SerializedName("latitudeAdjustmentMethod")
    @Expose
    private String latitudeAdjustmentMethod;
    @SerializedName("midnightMode")
    @Expose
    private String midnightMode;
    @SerializedName("school")
    @Expose
    private String school;
    @SerializedName("offset")
    @Expose
    private Offset offset;

}
