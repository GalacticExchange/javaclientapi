package io.gex.core.model;

import com.google.gson.annotations.SerializedName;

public enum NodeState {
    @SerializedName("running")
    RUNNING,
    @SerializedName("disconnected")
    DISCONNECTED
}
