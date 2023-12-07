
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Designation {

    @SerializedName("abbreviated")
    @Expose
    public String abbreviated;
    @SerializedName("expanded")
    @Expose
    public String expanded;

}
