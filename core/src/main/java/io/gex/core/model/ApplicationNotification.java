package io.gex.core.model;

import com.google.gson.annotations.SerializedName;


//todo node event application event
public enum ApplicationNotification {

    @SerializedName("application_installed")
    APPLICATION_INSTALLED,
    @SerializedName("application_install_error")
    APPLICATION_INSTALL_ERROR,

    @SerializedName("application_uninstalled")
    APPLICATION_UNINSTALLED,
    @SerializedName("application_uninstall_error")
    APPLICATION_UNINSTALL_ERROR,
}
