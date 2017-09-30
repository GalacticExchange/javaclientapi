package io.gex.core.rest;

import com.google.gson.JsonObject;
import io.gex.core.HardwareHelper;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.ApplicationNotification;
import io.gex.core.model.NodeNotification;
import io.gex.core.model.properties.NodeProperties;
import io.gex.core.model.properties.UserProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;

public class MainLevelRest {

    private final static LogWrapper logger = LogWrapper.create(MainLevelRest.class);

    private final static String LOGIN = "/login";
    private final static String NOTIFY = "/notify";
    private final static String LOGOUT = "/logout";

    public static void notify(ApplicationNotification event, String description, String applicationID, LogType type, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        NodeProperties nodeProperties = PropertiesHelper.node.getProps();
        if (nodeProperties != null) {
            if (StringUtils.isNotBlank(nodeProperties.getClusterID())) {
                body.add(NodeProperties.CLUSTER_ID_PROPERTY_NAME, nodeProperties.getClusterID());
            }
            if (StringUtils.isNotBlank(nodeProperties.getNodeID())) {
                body.add(NodeProperties.NODE_ID_PROPERTY_NAME, nodeProperties.getNodeID());
            }
        }
        body.add("event", event.toString());
        body.add("description", description);
        if (StringUtils.isNotBlank(applicationID)) {
            body.add("applicationID", applicationID);
        }
        Rest.sendAuthenticatedRequest(HttpMethod.POST, NOTIFY, type, null, body, null, token);
    }

    public static void notify(NodeNotification event, String description, LogType type, String token,
                              NodeProperties nodeProperties) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        if (nodeProperties != null) {
            if (StringUtils.isNotBlank(nodeProperties.getClusterID())) {
                body.add(NodeProperties.CLUSTER_ID_PROPERTY_NAME, nodeProperties.getClusterID());
            }
            if (StringUtils.isNotBlank(nodeProperties.getNodeID())) {
                body.add(NodeProperties.NODE_ID_PROPERTY_NAME, nodeProperties.getNodeID());
            }
        }
        body.add("event", event.toString());
        body.add("description", description);

        Rest.sendAuthenticatedRequest(HttpMethod.POST, NOTIFY, type, null, body, null, token);
    }

    public static void notify(NodeNotification event, String description, String containerID, LogType type, String token,
                              NodeProperties nodeProperties) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        if (nodeProperties != null) {
            if (StringUtils.isNotBlank(nodeProperties.getClusterID())) {
                body.add(NodeProperties.CLUSTER_ID_PROPERTY_NAME, nodeProperties.getClusterID());
            }
            if (StringUtils.isNotBlank(nodeProperties.getNodeID())) {
                body.add(NodeProperties.NODE_ID_PROPERTY_NAME, nodeProperties.getNodeID());
            }
        }
        body.add("event", event.toString());
        body.add("description", description);
        body.add("containerID", containerID);
        Rest.sendAuthenticatedRequest(HttpMethod.POST, NOTIFY, type, null, body, null, token);
    }

    public static void login(String username, String password) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        FileUtils.deleteQuietly(new File(PropertiesHelper.userPropertiesFile));
        UserProperties userProperties = new UserProperties();
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("username", username);
        body.add("password", password);
        body.add("systemInfo", HardwareHelper.getNodeInfo().toString());
        JsonObject obj = Rest.sendRequest(HttpMethod.POST, LOGIN,
                LogType.LOGIN_ERROR, null, body, null);
        KeyParametersValidator validator = new KeyParametersValidator();
        validator.add(UserProperties.TOKEN_PROPERTY_NAME, String.class)
                .add(UserProperties.TEAM_ID_PROPERTY_NAME, String.class).check(obj);
        userProperties.setUsername(username);
        userProperties.setToken(obj.get(UserProperties.TOKEN_PROPERTY_NAME).getAsString());
        userProperties.setTeamID(obj.get(UserProperties.TEAM_ID_PROPERTY_NAME).getAsString());
        PropertiesHelper.user.saveToJSON(userProperties);

    }

    public static void logout(String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Rest.sendAuthenticatedRequest(HttpMethod.DELETE, LOGOUT,
                LogType.LOGOUT_ERROR, null, null, null, token);
        FileUtils.deleteQuietly(new File(PropertiesHelper.userPropertiesFile));

    }

}
