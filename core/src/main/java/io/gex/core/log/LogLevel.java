package io.gex.core.log;

import com.google.gson.annotations.SerializedName;

public enum LogLevel {
    //todo check
    @SerializedName(value="critical", alternate={"6"})
    CRITICAL(6),
    @SerializedName(value="error", alternate={"5"})
    ERROR(5),
    @SerializedName(value="warn", alternate={"4"})
    WARN(4),
    @SerializedName(value="info", alternate={"3"})
    INFO(3),
    @SerializedName(value="debug", alternate={"2"})
    DEBUG(2),
    @SerializedName(value="trace", alternate={"1"})
    TRACE(1);

    private Integer number;

    LogLevel(Integer number) {
        this.number = number;
    }

    public Integer getNumber() {
        return number;
    }
}
