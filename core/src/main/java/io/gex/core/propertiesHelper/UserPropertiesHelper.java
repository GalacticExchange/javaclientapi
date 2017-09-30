package io.gex.core.propertiesHelper;

import com.google.gson.JsonObject;
import io.gex.core.AppContext;
import io.gex.core.CoreMessages;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.properties.UserProperties;
import org.apache.commons.lang3.StringUtils;

public class UserPropertiesHelper extends BasePropertiesHelper<UserProperties> {

    private final static LogWrapper logger = LogWrapper.create(UserPropertiesHelper.class);

    public UserPropertiesHelper() throws GexException {
        super(LogType.USER_PROPERTIES_ERROR, CoreMessages.USER_PROPERTIES_ERROR, PropertiesHelper.userPropertiesFile);
    }

    @Override
    UserProperties update(UserProperties userProperties) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        checkIfClient();
        UserProperties newUserProperties = getProps();
        if (newUserProperties == null) {
            return userProperties;
        }
        if (StringUtils.isNotBlank(userProperties.getToken())) {
            newUserProperties.setToken(userProperties.getToken());
        }
        if (StringUtils.isNotBlank(userProperties.getTeamID())) {
            newUserProperties.setTeamID(userProperties.getTeamID());
        }
        if (StringUtils.isNotBlank(userProperties.getUsername())) {
            newUserProperties.setUsername(userProperties.getUsername());
        }
        return newUserProperties;
    }

    @Override
    UserProperties convertJSONToProperties(JsonObject obj) {
        logger.trace("Entered " + LogHelper.getMethodName());
        UserProperties userProperties = new UserProperties();
        if (obj.has(UserProperties.TOKEN_PROPERTY_NAME) && !obj.get(UserProperties.TOKEN_PROPERTY_NAME).isJsonNull()) {
            userProperties.setToken(obj.get(UserProperties.TOKEN_PROPERTY_NAME).getAsString());
        }
        if (obj.has(UserProperties.TEAM_ID_PROPERTY_NAME) && !obj.get(UserProperties.TEAM_ID_PROPERTY_NAME).isJsonNull()) {
            userProperties.setTeamID(obj.get(UserProperties.TEAM_ID_PROPERTY_NAME).getAsString());
        }
        if (obj.has(UserProperties.USERNAME_PROPERTY_NAME) && !obj.get(UserProperties.USERNAME_PROPERTY_NAME).isJsonNull()) {
            userProperties.setUsername(obj.get(UserProperties.USERNAME_PROPERTY_NAME).getAsString());
        }
        return userProperties;
    }

    @Override
    JsonObject convertPropertiesToJSON(UserProperties userProperties) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        checkIfClient();
        JsonObject obj = new JsonObject();
        if (StringUtils.isNotBlank(userProperties.getToken())) {
            obj.addProperty(UserProperties.TOKEN_PROPERTY_NAME, userProperties.getToken());
        }
        if (StringUtils.isNotBlank(userProperties.getTeamID())) {
            obj.addProperty(UserProperties.TEAM_ID_PROPERTY_NAME, userProperties.getTeamID());
        }
        if (StringUtils.isNotBlank(userProperties.getUsername())) {
            obj.addProperty(UserProperties.USERNAME_PROPERTY_NAME, userProperties.getUsername());
        }
        return obj;
    }

    public static String getCurrentUsername() throws GexException {
        if (AppContext.isSet()) {
            return AppContext.getUsername();
        } else if (!PropertiesHelper.isService()) {
            return PropertiesHelper.user.getProps().getUsername();
        } else {
            return null;
        }
    }

    private void checkIfClient() throws GexException {
        if (PropertiesHelper.isService()) {
            throw logger.logAndReturnException(CoreMessages.USER_UPDATE_ERROR +
                    PropertiesHelper.userPropertiesFile, LogType.USER_PROPERTIES_ERROR);
        }
    }

}
