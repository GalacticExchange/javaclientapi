package io.gex.core.model;

import com.google.gson.annotations.SerializedName;

public enum NodeStatus {
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

    @SerializedName("joined")
    JOINED("joined"),

    @SerializedName("restarting")
    RESTARTING("restarting"),
    @SerializedName("restart_error")
    RESTART_ERROR("restart error"),

    @SerializedName("pausing")
    PAUSING("pausing"),
    @SerializedName("pause_error")
    PAUSE_ERROR("pause error"),
    @SerializedName("paused")
    PAUSED("paused"),

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
    STOPPED("stopped");

    private String name;

    NodeStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
