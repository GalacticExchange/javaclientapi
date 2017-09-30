package io.gex.core.model;

import com.google.gson.annotations.SerializedName;

public enum HostType {
    @SerializedName("dedicated")
    DEDICATED("dedicated"),
    @SerializedName("virtualbox")
    VIRTUALBOX("virtualbox");

    private String name;

    HostType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
