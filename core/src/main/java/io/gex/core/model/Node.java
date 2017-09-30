package io.gex.core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import io.gex.core.CoreMessages;
import io.gex.core.GsonHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Node {

    private final static LogWrapper logger = LogWrapper.create(Node.class);
    public final static String nodeInstallModule = "nodeInstall";

    private String id;
    private String name;
    private Integer nodeNumber;
    private String ip;
    private Integer port;
    private String host;
    private Cluster cluster;
    private String hadoopType;
    private List<Service> services;
    private NodeState state;
    private NodeStatus status;
    private NodeCounters counters;
    private Boolean localNode;
    @SerializedName("status_changed")
    private LocalDateTime statusChanged;
    private HostType hostType;

    public Node() {
        services = new ArrayList<>();
    }

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

    public Integer getNodeNumber() {
        return nodeNumber;
    }

    public void setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    public NodeState getState() {
        return state;
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getClusterId() {
        return cluster != null ? cluster.getId() : null;
    }

    public void setClusterId(String clusterId) {
        Cluster cluster = new Cluster();
        cluster.setId(clusterId);
        this.cluster = cluster;
    }

    public void addService(Service service) {
        services.add(service);
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public Service getService(int i) {
        if (services.size() >= i)
            return services.get(i);
        return null;
    }

    public NodeCounters getCounters() {
        return counters;
    }

    public void setCounters(NodeCounters counters) {
        this.counters = counters;
    }

    public Boolean getLocalNode() {
        return localNode;
    }

    public void setLocalNode(Boolean localNode) {
        this.localNode = localNode;
    }

    public LocalDateTime getStatusChanged() {
        return statusChanged;
    }

    public void setStatusChanged(LocalDateTime statusChanged) {
        this.statusChanged = statusChanged;
    }

    public HostType getHostType() {
        return hostType;
    }

    public void setHostType(HostType hostType) {
        this.hostType = hostType;
    }

    public String getHadoopType() {
        return hadoopType;
    }

    public void setHadoopType(String hadoopType) {
        this.hadoopType = hadoopType;
    }

    public static Node parse(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(obj, Node.class, CoreMessages.NODE_PARSING_ERROR);
    }

    public static List<Node> parse(JsonArray nodesArray) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(nodesArray, Node.class, CoreMessages.NODE_PARSING_ERROR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
