
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Month {

    @SerializedName("number")
    @Expose
    public Integer number;
    @SerializedName("en")
    @Expose
    public String en;
    @SerializedName("ar")
    @Expose
    public String ar;

}
