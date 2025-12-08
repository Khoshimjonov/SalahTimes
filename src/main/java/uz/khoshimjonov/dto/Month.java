
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Setter;

@Setter
public class Month {

    @SerializedName("number")
    @Expose
    private Integer number;
    @SerializedName("en")
    @Expose
    private String en;
    @SerializedName("ar")
    @Expose
    private String ar;

    public Integer getNumber() {
        return number;
    }

    public String getEn() {
        return en;
    }

    public String getAr() {
        return ar;
    }
}
