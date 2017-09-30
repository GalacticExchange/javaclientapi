package io.gex.core.api;

import com.google.gson.JsonArray;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;
import io.gex.core.rest.ServerPropertiesLevelRest;


public class ServerPropertiesLevelApi {

    private final static LogWrapper logger = LogWrapper.create(ServerPropertiesLevelApi.class);
    public final static String API_VERSION = "api_version";
    public final static String STORAGE_URL = "gex_storage_url";

    public static String getProperty(String name) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return ServerPropertiesLevelRest.getProperty(name);
    }

    public static JsonArray getPropertyArray(String name) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return ServerPropertiesLevelRest.getPropertyArray(name);
    }

}
