package io.gex.core.model;

import com.google.gson.annotations.SerializedName;

public enum UserRole {
    @SerializedName("superadmin")
    SUPERADMIN("superadmin"),
    @SerializedName("admin")
    ADMIN("admin"),
    @SerializedName("user")
    USER("user"),
    @SerializedName("external")
    EXTERNAL("external");

    private String name;

    UserRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
