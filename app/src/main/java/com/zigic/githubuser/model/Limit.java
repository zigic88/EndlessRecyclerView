
package com.zigic.githubuser.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Limit {

    @SerializedName("resources")
    @Expose
    private Resources resources;
    @SerializedName("rate")
    @Expose
    private Rate rate;

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public Rate getRate() {
        return rate;
    }

    public void setRate(Rate rate) {
        this.rate = rate;
    }

}
