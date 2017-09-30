package io.gex.core.model;

import com.google.gson.annotations.SerializedName;

public enum ClusterStatus {
    @SerializedName("installing")
    INSTALLING,
    @SerializedName("installed")
    INSTALLED,
    @SerializedName("install_error")
    INSTALL_ERROR,
    @SerializedName("deleted")
    DELETED,
    @SerializedName("deleting")
    DELETING,
    @SerializedName("active")
    ACTIVE,
}
