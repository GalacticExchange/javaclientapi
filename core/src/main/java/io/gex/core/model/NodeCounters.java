package io.gex.core.model;


import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import io.gex.core.CoreMessages;
import io.gex.core.GsonHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;

//todo remove ?
public class NodeCounters {

    private final static LogWrapper logger = LogWrapper.create(NodeCounters.class);

    @SerializedName("cpu")
    private Double CPU;
    private Double memory;

    public Double getCPU() {
        return CPU;
    }

    public void setCPU(Double cPU) {
        CPU = cPU;
    }

    public Double getMemory() {
        return memory;
    }

    public void setMemory(Double memory) {
        this.memory = memory;
    }

    public static NodeCounters parse(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(obj, NodeCounters.class, CoreMessages.NODE_COUNTERS_PARSING_ERROR);
    }
}
