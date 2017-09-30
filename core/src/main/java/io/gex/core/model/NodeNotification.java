package io.gex.core.model;

import com.google.gson.annotations.SerializedName;

public enum NodeNotification {
    @SerializedName("node_client_installed")
    NODE_CLIENT_INSTALLED,
    @SerializedName("node_client_install_error")
    NODE_CLIENT_INSTALL_ERROR,

    @SerializedName("node_started")
    NODE_STARTED,
    @SerializedName("node_start_error")
    NODE_START_ERROR,

    @SerializedName("node_stopped")
    NODE_STOPPED,
    @SerializedName("node_stop_error")
    NODE_STOP_ERROR,

    @SerializedName("node_restarted")
    NODE_RESTARTED,
    @SerializedName("node_restart_error")
    NODE_RESTART_ERROR,

    @SerializedName("node_uninstalling")
    NODE_UNINSTALLING,
    @SerializedName("node_uninstalled")
    NODE_UNINSTALLED,
    @SerializedName("node_uninstall_error")
    NODE_UNINSTALL_ERROR,

    @SerializedName("container_started")
    CONTAINER_STARTED,
    @SerializedName("container_start_error")
    CONTAINER_START_ERROR,
    @SerializedName("container_stopped")
    CONTAINER_STOPPED,
    @SerializedName("container_stop_error")
    CONTAINER_STOP_ERROR,
    @SerializedName("container_restarted")
    CONTAINER_RESTARTED,
    @SerializedName("container_restart_error")
    CONTAINER_RESTART_ERROR,
}
