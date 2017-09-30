package io.gex.core.model;

import com.google.gson.annotations.SerializedName;

public enum ContainerStatus {
    @SerializedName("installing")
    INSTALLING("installing"),
    @SerializedName("install_error")
    INSTALL_ERROR("install error"),
    @SerializedName("installed")
    INSTALLED("installed"),

    @SerializedName("starting")
    STARTING("starting"),
    @SerializedName("start_error")
    START_ERROR("start error"),

    @SerializedName("removing")
    REMOVING("removing"),
    @SerializedName("remove_error")
    REMOVE_ERROR("remove error"),
    @SerializedName("removed")
    REMOVED("removed"),

    @SerializedName("uninstalling")
    UNINSTALLING("uninstalling"),
    @SerializedName("uninstall_error")
    UNINSTALL_ERROR("uninstall error"),
    @SerializedName("uninstalled")
    UNINSTALLED("uninstalled"),

    @SerializedName("stopping")
    STOPPING("stopping"),
    @SerializedName("stop_error")
    STOP_ERROR("stop error"),
    @SerializedName("stopped")
    STOPPED("stopped"),

    @SerializedName("restarting")
    RESTARTING("restarting"),
    @SerializedName("restart_error")
    RESTART_ERROR("restart error");


    private String name;

    ContainerStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
