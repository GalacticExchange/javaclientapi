package io.gex.core.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class ServerPropertiesLevelRest {

    private final static LogWrapper logger = LogWrapper.create(ServerPropertiesLevelRest.class);

    private final static String PROPERTIES = "/properties";

    public static String getProperty(String name) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("name", name);
        JsonObject obj = Rest.sendRequest(HttpMethod.GET, PROPERTIES,  LogType.GET_PROPERTY_ERROR,
                null, null, query);
        return obj.has(name) && !obj.get(name).isJsonNull() ? obj.get(name).getAsString() : null;
    }

    public static JsonArray getPropertyArray(String name) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("name", name);
        JsonObject obj = Rest.sendRequest(HttpMethod.GET, PROPERTIES,  LogType.GET_PROPERTY_ERROR,
                null, null, query);
        return obj.has(name) && !obj.get(name).isJsonNull() ? obj.get(name).getAsJsonArray() : new JsonArray();
    }

}
