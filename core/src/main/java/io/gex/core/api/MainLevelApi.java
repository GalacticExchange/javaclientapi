package io.gex.core.api;

import io.gex.core.CoreMessages;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.NodeNotification;
import io.gex.core.model.properties.NodeProperties;
import io.gex.core.model.properties.UserProperties;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.MainLevelRest;
import org.apache.commons.lang3.StringUtils;


public class MainLevelApi {

    private final static LogWrapper logger = LogWrapper.create(MainLevelApi.class);

    public static void login(String username, String password) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(username)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_USERNAME, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(password)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_PASSWORD, LogType.EMPTY_PROPERTY_ERROR);
        }
        MainLevelRest.login(username, password);
    }

    public static void notify(NodeNotification event, String description) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        NodeProperties nodeProperties = PropertiesHelper.node.getProps();
        notify(event, description, nodeProperties);
    }

    public static void notify(NodeNotification event, String description, NodeProperties nodeProperties) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MainLevelRest.notify(event, description, (event == NodeNotification.NODE_UNINSTALLING
                || event == NodeNotification.NODE_UNINSTALLED || event == NodeNotification.NODE_UNINSTALL_ERROR)
                ? LogType.NOTIFY_UNINSTALL_ERROR : LogType.NOTIFY_ERROR, BasePropertiesHelper.getValidToken(), nodeProperties);
    }

    public static void notifyContainer(NodeNotification event, String description, String containerID, LogType type) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        NodeProperties nodeProperties = PropertiesHelper.node.getProps();
        MainLevelRest.notify(event, description, containerID, type, BasePropertiesHelper.getValidToken(), nodeProperties);
    }

    public static void logout() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            String token = BasePropertiesHelper.getValidToken();
            MainLevelRest.logout(token);
        } finally {
            PropertiesHelper.user.remove(UserProperties.TOKEN_PROPERTY_NAME);
            PropertiesHelper.user.remove(UserProperties.TEAM_ID_PROPERTY_NAME);
            PropertiesHelper.user.remove(UserProperties.USERNAME_PROPERTY_NAME);
        }

    }

}
