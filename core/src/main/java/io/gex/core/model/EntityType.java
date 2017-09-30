package io.gex.core.model;

import com.google.gson.annotations.SerializedName;

// used an NodeType and ClusterType
public enum EntityType {
    @SerializedName("onprem")
    ONPREM("onprem"),
    @SerializedName("aws")
    AWS("aws");

    private String name;

    EntityType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
