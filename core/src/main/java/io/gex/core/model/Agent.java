package io.gex.core.model;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.gex.core.CoreMessages;
import io.gex.core.GsonHelper;
import io.gex.core.UrlHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Agent {

    private final static LogWrapper logger = LogWrapper.create(Agent.class);

    private String ip;
    private Integer port;
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public static Agent parse(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(obj, Agent.class, CoreMessages.AGENT_PARSING_ERROR);
    }

    public static List<Agent> parse(JsonArray agentArray) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        List<Agent> agents = new ArrayList<>();
        if (agentArray != null && agentArray.size() != 0) {
            for (int i = 0; i < agentArray.size(); i++) {
                Agent agent = parse(agentArray.get(i).getAsJsonObject());
                if (agent != null)
                    agents.add(agent);
            }
        }
        return agents;
    }

    public URL getURL() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return UrlHelper.concatenate(this.port, this.ip);
    }
}
