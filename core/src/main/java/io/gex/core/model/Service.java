package io.gex.core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import io.gex.core.CoreMessages;
import io.gex.core.GsonHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;

import java.util.List;

public class Service {

    private final static LogWrapper logger = LogWrapper.create(Service.class);

    private String containerName;
    private String host;
    private boolean masterContainer;
    private String name;
    private String nodeID;
    private String nodeName;
    private Integer port;
    private String protocol;
    @SerializedName("public_ip")
    private String publicIp;
    private SocksProxy socksProxy;
    private ContainerStatus status;

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isMasterContainer() {
        return masterContainer;
    }

    public void setMasterContainer(boolean masterContainer) {
        this.masterContainer = masterContainer;
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

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public SocksProxy getSocksProxy() {
        return socksProxy;
    }

    public void setSocksProxy(SocksProxy socksProxy) {
        this.socksProxy = socksProxy;
    }

    public ContainerStatus getStatus() {
        return status;
    }

    public void setStatus(ContainerStatus status) {
        this.status = status;
    }

    public static Service parse(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(obj, Service.class, CoreMessages.SERVICE_PARSING_ERROR);
    }

    public static List<Service> parse(JsonArray servicesArray) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(servicesArray, Service.class, CoreMessages.SERVICE_PARSING_ERROR);
    }
}
