package io.gex.core.propertiesHelper;

import com.google.gson.JsonObject;
import io.gex.core.CoreMessages;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.EntityType;
import io.gex.core.model.properties.GexdProperties;
import org.apache.commons.lang3.StringUtils;

public class GexdPropertiesHelper extends BasePropertiesHelper<GexdProperties> {

    private final static LogWrapper logger = LogWrapper.create(GexdPropertiesHelper.class);

    public GexdPropertiesHelper() throws GexException {
        super(LogType.GEXD_PROPERTIES_ERROR, CoreMessages.GEXD_PROPERTIES_ERROR, PropertiesHelper.gexdPropertiesFile, true);
    }

    @Override
    GexdProperties update(GexdProperties gexdProperties) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        checkIfGexd();
        GexdProperties newGexdProperties = getProps();
        if (newGexdProperties == null) {
            return gexdProperties;
        }
        if (StringUtils.isNotBlank(gexdProperties.getWebServerPort())) {
            newGexdProperties.setWebServerPort(gexdProperties.getWebServerPort());
        }
        if (StringUtils.isNotBlank(gexdProperties.getInstanceID())) {
            newGexdProperties.setInstanceID(gexdProperties.getInstanceID());
        }
        if (gexdProperties.getNodeType() != null) {
            newGexdProperties.setNodeType(gexdProperties.getNodeType());
        }
        return newGexdProperties;
    }

    @Override
    GexdProperties convertJSONToProperties(JsonObject obj) {
        logger.trace("Entered " + LogHelper.getMethodName());
        GexdProperties gexdProperties = new GexdProperties();
        if (obj.has(GexdProperties.WEB_SERVER_PORT_PROPERTY_NAME) &&
                !obj.get(GexdProperties.WEB_SERVER_PORT_PROPERTY_NAME).isJsonNull()) {
            gexdProperties.setWebServerPort(obj.get(GexdProperties.WEB_SERVER_PORT_PROPERTY_NAME).getAsString());
        }
        if (obj.has(GexdProperties.INSTANCE_ID_PROPERTY_NAME) &&
                !obj.get(GexdProperties.INSTANCE_ID_PROPERTY_NAME).isJsonNull()) {
            gexdProperties.setInstanceID(obj.get(GexdProperties.INSTANCE_ID_PROPERTY_NAME).getAsString());
        }
        if (obj.has(GexdProperties.NODE_TYPE_PROPERTY_NAME) &&
                !obj.get(GexdProperties.NODE_TYPE_PROPERTY_NAME).isJsonNull()) {
            gexdProperties.setNodeType(EntityType.valueOf(obj.get(GexdProperties.NODE_TYPE_PROPERTY_NAME).getAsString().toUpperCase()));
        }
        return gexdProperties;
    }

    @Override
    JsonObject convertPropertiesToJSON(GexdProperties gexdProperties) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        checkIfGexd();
        JsonObject obj = new JsonObject();
        if (StringUtils.isNotBlank(gexdProperties.getWebServerPort())) {
            obj.addProperty(GexdProperties.WEB_SERVER_PORT_PROPERTY_NAME, gexdProperties.getWebServerPort());
        }
        if (StringUtils.isNotBlank(gexdProperties.getInstanceID())) {
            obj.addProperty(GexdProperties.INSTANCE_ID_PROPERTY_NAME, gexdProperties.getInstanceID());
        }
        if (gexdProperties.getNodeType() != null) {
            obj.addProperty(GexdProperties.NODE_TYPE_PROPERTY_NAME, gexdProperties.getNodeType().getName());
        }
        return obj;
    }

    private void checkIfGexd() throws GexException {
        if (!PropertiesHelper.isService()) {
            throw logger.logAndReturnException(CoreMessages.SERVICE_UPDATE_ERROR +
                    PropertiesHelper.gexdPropertiesFile, LogType.GEXD_PROPERTIES_ERROR);
        }
    }
}

