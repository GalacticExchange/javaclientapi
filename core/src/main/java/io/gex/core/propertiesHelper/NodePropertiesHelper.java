package io.gex.core.propertiesHelper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.gex.core.CoreMessages;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Application;
import io.gex.core.model.properties.NodeProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static io.gex.core.model.properties.NodeProperties.*;

public class NodePropertiesHelper extends BasePropertiesHelper<NodeProperties> {

    private final static LogWrapper logger = LogWrapper.create(NodePropertiesHelper.class);

    public NodePropertiesHelper() throws GexException {
        super(LogType.NODE_PROPERTIES_ERROR, CoreMessages.NODE_PROPERTIES_ERROR, PropertiesHelper.nodePropertiesFile);
    }

    public Application findApplicationByName(String name) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(name)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_APPLICATION_NAME, LogType.EMPTY_PROPERTY_ERROR);
        }
        return getProps().getApplications().stream().filter(it -> it.getName().equals(name)).findFirst().orElse(null);
    }

    public Application findApplicationByID(String id) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(id)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_APPLICATION_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        return getProps().getApplications().stream().filter(it -> it.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Application> readApplications() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return getProps().getApplications();
    }

    public void removeApplicationByID(String applicationID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        checkIfGexd();
        if (StringUtils.isBlank(applicationID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_APPLICATION_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        NodeProperties updNodeProperties = getProps();

        List<Application> applications = updNodeProperties.getApplications();
        if (applications != null) {
            applications = (applications.stream().filter(it ->
                    applicationID.equals(it.getId())).collect(Collectors.toList()));
            if (applications.size() > 0) {
                updNodeProperties.setApplications(applications);
                saveToJSON(updNodeProperties);
            } else {
                remove(NodeProperties.APPLICATIONS_PROPERTY_NAME);
            }
        }
    }

    @Override
    NodeProperties update(NodeProperties nodeProperties) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        checkIfGexd();
        NodeProperties newNodeProperties = getProps();
        if (newNodeProperties == null) {
            return nodeProperties;
        }
        if (StringUtils.isNotBlank(nodeProperties.getClusterID())) {
            newNodeProperties.setClusterID(nodeProperties.getClusterID());
        }
        if (StringUtils.isNotBlank(nodeProperties.getNodeID())) {
            newNodeProperties.setNodeID(nodeProperties.getNodeID());
        }
        if (StringUtils.isNotBlank(nodeProperties.getNodeName())) {
            newNodeProperties.setNodeName(nodeProperties.getNodeName());
        }
        if (nodeProperties.getNodeNumber() != null) {
            newNodeProperties.setNodeNumber(nodeProperties.getNodeNumber());
        }
        if (StringUtils.isNotBlank(nodeProperties.getClusterName())) {
            newNodeProperties.setClusterName(nodeProperties.getClusterName());
        }
        if (StringUtils.isNotBlank(nodeProperties.getClusterDomainName())) {
            newNodeProperties.setClusterDomainName(nodeProperties.getClusterDomainName());
        }
        if (StringUtils.isNotBlank(nodeProperties.getNodeAgentToken())) {
            newNodeProperties.setNodeAgentToken(nodeProperties.getNodeAgentToken());
        }
        if (StringUtils.isNotBlank(nodeProperties.getHadoopType())) {
            newNodeProperties.setHadoopType(nodeProperties.getHadoopType());
        }
        if (CollectionUtils.isNotEmpty(nodeProperties.getApplications())) {
            newNodeProperties.setApplications(nodeProperties.getApplications());
        }
        return newNodeProperties;
    }

    @Override
    NodeProperties convertJSONToProperties(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        NodeProperties nodeProperties = new NodeProperties();
        if (obj.has(CLUSTER_ID_PROPERTY_NAME)
                && !obj.get(CLUSTER_ID_PROPERTY_NAME).isJsonNull()) {
            nodeProperties.setClusterID(obj.get(CLUSTER_ID_PROPERTY_NAME).getAsString());
        }
        if (obj.has(NODE_ID_PROPERTY_NAME) && !obj.get(NODE_ID_PROPERTY_NAME).isJsonNull()) {
            nodeProperties.setNodeID(obj.get(NODE_ID_PROPERTY_NAME).getAsString());
        }
        if (obj.has(CLUSTER_NAME_PROPERTY_NAME)
                && !obj.get(CLUSTER_NAME_PROPERTY_NAME).isJsonNull()) {
            nodeProperties.setClusterName(obj.get(CLUSTER_NAME_PROPERTY_NAME).getAsString());
        }
        if (obj.has(NodeProperties.CLUSTER_DOMAIN_NAME_PROPERTY_NAME)
                && !obj.get(NodeProperties.CLUSTER_DOMAIN_NAME_PROPERTY_NAME).isJsonNull()) {
            nodeProperties.setClusterDomainName(obj.get(NodeProperties.CLUSTER_DOMAIN_NAME_PROPERTY_NAME).getAsString());
        }
        if (obj.has(NODE_NAME_PROPERTY_NAME)
                && !obj.get(NODE_NAME_PROPERTY_NAME).isJsonNull()) {
            nodeProperties.setNodeName(obj.get(NODE_NAME_PROPERTY_NAME).getAsString());
        }
        if (obj.has(NodeProperties.NODE_NUMBER_PROPERTY_NAME)
                && !obj.get(NodeProperties.NODE_NUMBER_PROPERTY_NAME).isJsonNull()) {
            nodeProperties.setNodeNumber(obj.get(NodeProperties.NODE_NUMBER_PROPERTY_NAME).getAsInt());
        }
        if (obj.has(NodeProperties.NODE_AGENT_TOKEN_PROPERTY_NAME)
                && !obj.get(NodeProperties.NODE_AGENT_TOKEN_PROPERTY_NAME).isJsonNull()) {
            nodeProperties.setNodeAgentToken(obj.get(NodeProperties.NODE_AGENT_TOKEN_PROPERTY_NAME).getAsString());
        }
        if (obj.has(NodeProperties.HADOOP_TYPE_PROPERTY_NAME)
                && !obj.get(NodeProperties.HADOOP_TYPE_PROPERTY_NAME).isJsonNull()) {
            nodeProperties.setHadoopType(obj.get(NodeProperties.HADOOP_TYPE_PROPERTY_NAME).getAsString());
        }
        if (obj.has(NodeProperties.APPLICATIONS_PROPERTY_NAME)
                && !obj.get(NodeProperties.APPLICATIONS_PROPERTY_NAME).isJsonNull()) {
            nodeProperties.setApplications(Application.parse(new JsonParser().parse(obj.get(
                    NodeProperties.APPLICATIONS_PROPERTY_NAME).getAsString()).getAsJsonArray()));
        }
        return nodeProperties;
    }

    @Override
    JsonObject convertPropertiesToJSON(NodeProperties nodeProperties) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        checkIfGexd();
        JsonObject obj = new JsonObject();
        if (StringUtils.isNotBlank(nodeProperties.getClusterID())) {
            obj.addProperty(CLUSTER_ID_PROPERTY_NAME, nodeProperties.getClusterID());
        }
        if (StringUtils.isNotBlank(nodeProperties.getNodeID())) {
            obj.addProperty(NODE_ID_PROPERTY_NAME, nodeProperties.getNodeID());
        }
        if (StringUtils.isNotBlank(nodeProperties.getNodeName())) {
            obj.addProperty(NODE_NAME_PROPERTY_NAME, nodeProperties.getNodeName());
        }
        if (StringUtils.isNotBlank(nodeProperties.getClusterName())) {
            obj.addProperty(CLUSTER_NAME_PROPERTY_NAME, nodeProperties.getClusterName());
        }
        if (StringUtils.isNotBlank(nodeProperties.getClusterDomainName())) {
            obj.addProperty(NodeProperties.CLUSTER_DOMAIN_NAME_PROPERTY_NAME, nodeProperties.getClusterDomainName());
        }
        if (nodeProperties.getNodeNumber() != null) {
            obj.addProperty(NodeProperties.NODE_NUMBER_PROPERTY_NAME, nodeProperties.getNodeNumber());
        }
        if (StringUtils.isNotBlank(nodeProperties.getNodeAgentToken())) {
            obj.addProperty(NodeProperties.NODE_AGENT_TOKEN_PROPERTY_NAME, nodeProperties.getNodeAgentToken());
        }
        if (StringUtils.isNotBlank(nodeProperties.getHadoopType())) {
            obj.addProperty(NodeProperties.HADOOP_TYPE_PROPERTY_NAME, nodeProperties.getHadoopType());
        }
        if (CollectionUtils.isNotEmpty(nodeProperties.getApplications())) {
            obj.addProperty(NodeProperties.APPLICATIONS_PROPERTY_NAME,
                    new Gson().toJson(nodeProperties.getApplications()));
        }
        return obj;
    }

    public String get(String propertyName) {
        switch (propertyName) {
            case NODE_ID_PROPERTY_NAME:
                return getProps().getNodeID();
            case CLUSTER_ID_PROPERTY_NAME:
                return getProps().getClusterID();
            case CLUSTER_NAME_PROPERTY_NAME:
                return getProps().getClusterName();
            case NODE_NAME_PROPERTY_NAME:
                return getProps().getNodeName();
            case NODE_NUMBER_PROPERTY_NAME:
                return String.valueOf(getProps().getNodeNumber());
            case NODE_AGENT_TOKEN_PROPERTY_NAME:
                return getProps().getNodeAgentToken();
            case CLUSTER_DOMAIN_NAME_PROPERTY_NAME:
                return getProps().getClusterDomainName();
            case HADOOP_TYPE_PROPERTY_NAME:
                return getProps().getHadoopType();
            case APPLICATIONS_PROPERTY_NAME:
                return null; //not implemented because it's array
            default:
                return null;
        }
    }

    private void checkIfGexd() throws GexException {
        if (!PropertiesHelper.isService()) {
            throw logger.logAndReturnException(CoreMessages.SERVICE_UPDATE_ERROR +
                    PropertiesHelper.nodePropertiesFile, LogType.NODE_PROPERTIES_ERROR);
        }
    }

}
