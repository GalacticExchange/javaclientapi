package io.gex.core.rest;

import com.google.gson.JsonObject;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Agent;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class NodeAgentLevelRest {

    private final static LogWrapper logger = LogWrapper.create(NodeAgentLevelRest.class);

    private final static String NODE_AGENT_INFO = "/nodeAgentInfo";
    private final static String NODE_AGENTS = "/nodeAgents";
    private final static String NODE_VIEW_LOGS = "/permissions/check/nodeViewLogs";

    public static void nodeViewLogs(String token, String nodeAgentToken) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("userToken", token);
        Rest.sendAuthenticatedRequest(HttpMethod.GET, NODE_VIEW_LOGS,
                LogType.PERMISSIONS_CHECK_NODE_VIEW_LOGS_ERROR, null, null, query, nodeAgentToken);
    }

    public static void sendIPAndPort(String ip, Integer port, String nodeAgentToken) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("ip", ip);
        body.add("port", String.valueOf(port));
        Rest.sendAuthenticatedRequest(HttpMethod.POST, NODE_AGENT_INFO, LogType.NODE_AGENT_INFO_ERROR,
                null, body, null, nodeAgentToken);

    }

    public static List<Agent> nodeAgentsInfo(String clusterID, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("clusterID", clusterID);
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, NODE_AGENTS,
                LogType.NODE_AGENTS_ERROR, null, null, query, token);
        return obj.has("agents") ? Agent.parse(obj.getAsJsonArray("agents")) : new ArrayList<>(0);
    }

    public static Agent nodeAgentInfo(String nodeID, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("nodeID", nodeID);
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, NODE_AGENT_INFO,
                LogType.NODE_AGENT_INFO_ERROR, null, null, query, token);
        return obj.has("agent") ? Agent.parse(obj.getAsJsonObject("agent")) : null;
    }
}
