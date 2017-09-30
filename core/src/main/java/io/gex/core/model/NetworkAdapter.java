package io.gex.core.model;


import com.google.gson.annotations.SerializedName;

public class NetworkAdapter {
    private String name;
    @SerializedName("isWifi")
    private Boolean wifi;

    public NetworkAdapter(String name, boolean wifi) {
        this.name = name;
        this.wifi = wifi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getWifi() {
        return wifi;
    }

    public void setWifi(Boolean wifi) {
        this.wifi = wifi;
    }
}

