package io.gex.core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.gex.core.CoreMessages;
import io.gex.core.GsonHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;

import java.util.List;

//todo remove after ssh
public class Container {

    private final static LogWrapper logger = LogWrapper.create(Container.class);

    private String containerID;
    private String name;
    private String nodeID;

    public String getContainerID() {
        return containerID;
    }

    public void setContainerID(String containerID) {
        this.containerID = containerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public static Container parse(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(obj, Container.class, CoreMessages.CONTAINER_PARSING_ERROR);
    }

    public static List<Container> parse(JsonArray applicationsArray) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(applicationsArray, Container.class, CoreMessages.CONTAINER_PARSING_ERROR);
    }

    public Container copy() {
        Container res = new Container();
        res.containerID = this.containerID;
        res.name = this.name;
        res.nodeID = this.nodeID;
        return res;
    }

}
