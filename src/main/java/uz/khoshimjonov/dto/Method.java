
package uz.khoshimjonov.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Method {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("params")
    @Expose
    private Params params;
    @SerializedName("location")
    @Expose
    private Location location;

}
