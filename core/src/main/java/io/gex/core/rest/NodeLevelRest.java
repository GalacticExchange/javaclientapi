package io.gex.core.rest;

import com.google.gson.JsonObject;
import io.gex.core.GsonHelper;
import io.gex.core.HardwareHelper;
import io.gex.core.PropertiesHelper;
import io.gex.core.api.ClusterLevelApi;
import io.gex.core.api.NodeLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.*;
import io.gex.core.model.properties.NodeProperties;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class NodeLevelRest {

    private final static LogWrapper logger = LogWrapper.create(NodeLevelRest.class);

    private final static String NODES = "/nodes";
    private final static String GEXD_NODES = "/gexd/nodes";

    public static Node nodeInfo(String nodeId, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, NODES + "/" + nodeId + "/info",
                LogType.NODE_INFO_ERROR, null, null, null, token);
        return obj.has("node") ? Node.parse(obj.getAsJsonObject("node")) : null;
    }

    public static String nodeInstall(NetworkAdapter networkAdapter, String clusterID, String instanceID, EntityType nodeType, String token, String customName, String hadoopApp) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String hostType = PropertiesHelper.isHostVirtual() ? HostType.VIRTUALBOX.getName() : HostType.DEDICATED.getName();
        JsonObject node = Rest.sendAuthenticatedRequest(HttpMethod.POST, NODES,
                LogType.NODE_INSTALL_ERROR, null, createBodyForNode(null, networkAdapter, clusterID,
                        instanceID, nodeType, getNodeMachineInfo(), getNodeMachineHostType(), customName, null, hadoopApp), null, token);

        validateNodeAfterNodeInstall(node);
        NodeProperties nodeProperties = convertToNodeProperties(node);
        PropertiesHelper.node.saveToJSON(nodeProperties);
        sendWebServerRequest("/reconnect");
        return nodeProperties.getNodeName();
    }

    public static NodeProperties nodePost(NetworkAdapter networkAdapter, String clusterID, String instanceID, EntityType nodeType,
                                          String nodeName, HostType hostType, String serverHost, String hadoopApp, String systemInfo, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject node = Rest.sendAuthenticatedRequest(HttpMethod.POST, NODES,
                LogType.NODE_INSTALL_ERROR, null, createBodyForNode(null, networkAdapter, clusterID,
                        instanceID, nodeType, systemInfo, hostType, nodeName, serverHost, hadoopApp), null, token);
        validateNodeAfterNodeInstall(node);
        return convertToNodeProperties(node);
    }

    public static void nodeCommands(String nodeID, Action action, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("nodeID", nodeID);
        body.add("command", action.toString());
        Rest.sendAuthenticatedRequest(HttpMethod.PUT, NODES, LogType.NODE_COMMANDS_ERROR, null, body, null,
                token);
    }

    public static List<Node> nodeList(String clusterID, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("clusterID", clusterID);
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, NODES, LogType.NODE_LIST_ERROR,
                null, null, query, token);
        return obj.has("nodes") ? Node.parse(obj.getAsJsonArray("nodes")) : new ArrayList<>(0);
    }

    public static void nodeUpdateGexd(String nodeID, String nodeAgentToken, String instanceID, EntityType nodeType, String customName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Rest.sendAuthenticatedRequest(HttpMethod.PUT, GEXD_NODES, LogType.NODE_INSTALL_ERROR, null,
                createBodyForNode(nodeID, null, null, instanceID, nodeType, getNodeMachineInfo(),
                        getNodeMachineHostType(), customName, null, null), null, nodeAgentToken);
        Node node = NodeLevelApi.nodeInfo();
        Cluster cluster = ClusterLevelApi.clusterInfo(node.getClusterId());
        NodeProperties nodeProperties = convertToNodeProperties(node, cluster, nodeAgentToken);
        PropertiesHelper.node.saveToJSON(nodeProperties);
        logger.debug("node number=" + nodeProperties.getNodeNumber());
        logger.debug("cluster ID=" + nodeProperties.getClusterID());
        logger.debug("node number in file=" + PropertiesHelper.node.getProps().getNodeNumber());
        logger.debug("cluster ID in file=" + PropertiesHelper.node.getProps().getClusterID());
        sendWebServerRequest("/reconnect");
    }

    private static NodeProperties convertToNodeProperties(Node node, Cluster cluster, String nodeAgentToken) {
        NodeProperties nodeProperties = new NodeProperties();
        //node
        nodeProperties.setNodeName(node.getName());
        nodeProperties.setNodeID(node.getId());
        nodeProperties.setNodeNumber(node.getNodeNumber());
        //cluster
        nodeProperties.setClusterID(cluster.getId());
        nodeProperties.setClusterName(cluster.getName());
        if (cluster.getClusterSettings() != null) {
            nodeProperties.setHadoopType(cluster.getClusterSettings().getHadoopType());
        }
        //token
        nodeProperties.setNodeAgentToken(nodeAgentToken);
        return nodeProperties;
    }

    private static MultivaluedMap<String, String> createBodyForNode(String nodeID, NetworkAdapter networkAdapter,
                                                                    String clusterID, String instanceID, EntityType nodeType,
                                                                    String systemInfo, HostType hostType, String customName,
                                                                    String serverHost, String hadoopApp)
            throws GexException {
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        if (StringUtils.isNotBlank(nodeID)) {
            body.add("nodeID", nodeID);
        }
        if (StringUtils.isNotBlank(clusterID)) {
            body.add("clusterID", clusterID);
        }
        if (StringUtils.isNotBlank(hadoopApp)) {
            body.add("hadoopApp", hadoopApp);
        }
        body.add("systemInfo", systemInfo);
        body.add("hostType", hostType.getName());
        JsonObject options = new JsonObject();
        options.add("selected_interface", networkAdapter != null ? GsonHelper.toJsonTree(networkAdapter) : null);
        options.addProperty("server_host", serverHost);
        body.add("options", options.toString());
        body.add("instanceID", instanceID);
        body.add("nodeType", nodeType.getName());
        if (StringUtils.isNotBlank(customName)) {
            body.add("customName", customName);
        }
        return body;
    }

    private static void validateNodeAfterNodeInstall(JsonObject node) throws GexException {
        KeyParametersValidator validator = new KeyParametersValidator();
        validator.add(NodeProperties.NODE_NAME_PROPERTY_NAME, String.class)
                .add(NodeProperties.CLUSTER_ID_PROPERTY_NAME, String.class)
                .add(NodeProperties.NODE_ID_PROPERTY_NAME, String.class)
                .add(NodeProperties.NODE_AGENT_TOKEN_PROPERTY_NAME, String.class)
                .add(NodeProperties.HADOOP_TYPE_PROPERTY_NAME, String.class)
                .add(NodeProperties.NODE_NUMBER_PROPERTY_NAME, Integer.class).check(node);
    }

    private static NodeProperties convertToNodeProperties(JsonObject node) {
        NodeProperties nodeProperties = new NodeProperties();
        nodeProperties.setNodeName(node.get(NodeProperties.NODE_NAME_PROPERTY_NAME).getAsString());
        nodeProperties.setClusterID(node.get(NodeProperties.CLUSTER_ID_PROPERTY_NAME).getAsString());
        nodeProperties.setClusterName(node.get(NodeProperties.CLUSTER_NAME_PROPERTY_NAME).getAsString());
        nodeProperties.setNodeID(node.get(NodeProperties.NODE_ID_PROPERTY_NAME).getAsString());
        nodeProperties.setNodeNumber(node.get(NodeProperties.NODE_NUMBER_PROPERTY_NAME).getAsInt());
        nodeProperties.setNodeAgentToken(node.get(NodeProperties.NODE_AGENT_TOKEN_PROPERTY_NAME).getAsString());
        nodeProperties.setHadoopType(node.get(NodeProperties.HADOOP_TYPE_PROPERTY_NAME).getAsString());
        return nodeProperties;
    }

    public static void nodeRemove(String nodeID, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> header = new MultivaluedHashMap<>();
        header.add("nodeID", nodeID);
        Rest.sendAuthenticatedRequest(HttpMethod.DELETE, NODES, LogType.NODE_REMOVE_ERROR, header, null,
                null, token);
    }

    public static String sendWebServerRequest(String relativeAddress) throws GexException {
        return sendWebServerRequest(relativeAddress, HttpMethod.GET, null);
    }

    public static String sendWebServerRequest(String relativeAddress, String requestType, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> header = new MultivaluedHashMap<>();
        if (StringUtils.isNotBlank(token)) {
            header.add("token", token);
        }
        JsonObject obj = Rest.sendLocalRequest(requestType, relativeAddress, LogType.WEB_SERVER_ERROR,
                header, null, null);
        return obj.has("result") && !obj.get("result").isJsonNull() ? obj.get("result").getAsString() : null;
    }

    private static String getNodeMachineInfo() {
        return HardwareHelper.getNodeInfo().toString();
    }

    private static HostType getNodeMachineHostType() {
        return PropertiesHelper.isHostVirtual() ? HostType.VIRTUALBOX : HostType.DEDICATED;
    }
}
