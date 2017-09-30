package io.gex.core.model.properties;


import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NodeProperties extends BaseProperties {

    private final static LogWrapper logger = LogWrapper.create(NodeProperties.class);

    private String nodeAgentToken;
    //cluster info
    private String clusterID;
    private String clusterName;
    private String clusterDomainName;
    private String hadoopType;
    //node info
    private String nodeID;
    private String nodeName;
    private Integer nodeNumber;
    // applications
    private List<Application> applications;

    public final static String NODE_ID_PROPERTY_NAME = "nodeID";
    public final static String CLUSTER_ID_PROPERTY_NAME = "clusterID";
    public final static String CLUSTER_NAME_PROPERTY_NAME = "clusterName";
    public final static String NODE_NAME_PROPERTY_NAME = "nodeName";
    public final static String NODE_NUMBER_PROPERTY_NAME = "nodeNumber";
    public final static String NODE_AGENT_TOKEN_PROPERTY_NAME = "nodeAgentToken";
    public final static String CLUSTER_DOMAIN_NAME_PROPERTY_NAME = "clusterDomainName";
    public final static String HADOOP_TYPE_PROPERTY_NAME = "hadoopType";
    public final static String APPLICATIONS_PROPERTY_NAME = "applications";

    public NodeProperties() {
        applications = new ArrayList<>();
    }

    public Integer getNodeNumber() {
        return nodeNumber;
    }

    public String getNodeAgentToken() {
        return nodeAgentToken;
    }

    public void setNodeAgentToken(String nodeAgentToken) {
        this.nodeAgentToken = nodeAgentToken;
    }

    public void setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterDomainName() {
        return clusterDomainName;
    }

    public void setClusterDomainName(String clusterDomainName) {
        this.clusterDomainName = clusterDomainName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getClusterID() {
        return clusterID;
    }

    public void setClusterID(String clusterID) {
        this.clusterID = clusterID;
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public String getHadoopType() {
        return hadoopType;
    }

    public void setHadoopType(String hadoopType) {
        this.hadoopType = hadoopType;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public void addApplication(Application application) throws GexException {
        if (applications.stream().noneMatch(
                it -> it.getId().equals(application.getId()))) {
            applications.add(application);
        } else {
            throw logger.logAndReturnException(CoreMessages.replaceTemplate(CoreMessages.APPLICATION_PRESENT,
                    application.getName()), LogType.APPLICATION_ERROR);
        }
    }

    @Override
    public NodeProperties copy() {
        NodeProperties res = new NodeProperties();
        res.nodeAgentToken = this.nodeAgentToken;
        res.clusterID = this.clusterID;
        res.clusterName = this.clusterName;
        res.clusterDomainName = this.clusterDomainName;
        res.hadoopType = this.hadoopType;
        res.nodeID = this.nodeID;
        res.nodeName = this.nodeName;
        res.nodeNumber = this.nodeNumber;
        if (this.applications != null) {
            res.applications = this.applications.stream().map(Application::copy).collect(Collectors.toList());
        }
        return res;
    }
}
