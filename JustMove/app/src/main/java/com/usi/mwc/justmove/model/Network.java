
package com.usi.mwc.justmove.model;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Network implements Serializable
{

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("background_img")
    @Expose
    private Object backgroundImg;
    @SerializedName("logo_img")
    @Expose
    private String logoImg;
    @SerializedName("sponsors")
    @Expose
    private List<Object> sponsors = null;
    private final static long serialVersionUID = -981744171341915070L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getBackgroundImg() {
        return backgroundImg;
    }

    public void setBackgroundImg(Object backgroundImg) {
        this.backgroundImg = backgroundImg;
    }

    public String getLogoImg() {
        return logoImg;
    }

    public void setLogoImg(String logoImg) {
        this.logoImg = logoImg;
    }

    public List<Object> getSponsors() {
        return sponsors;
    }

    public void setSponsors(List<Object> sponsors) {
        this.sponsors = sponsors;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Network.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("backgroundImg");
        sb.append('=');
        sb.append(((this.backgroundImg == null)?"<null>":this.backgroundImg));
        sb.append(',');
        sb.append("logoImg");
        sb.append('=');
        sb.append(((this.logoImg == null)?"<null>":this.logoImg));
        sb.append(',');
        sb.append("sponsors");
        sb.append('=');
        sb.append(((this.sponsors == null)?"<null>":this.sponsors));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
