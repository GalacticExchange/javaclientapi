package io.gex.core.rest;

import com.google.gson.JsonObject;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Service;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class ServiceLevelRest {
    private final static LogWrapper logger = LogWrapper.create(ServiceLevelRest.class);
    private final static String SERVICES = "/services";

    public static List<Service> services(String applicationID, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("applicationID", applicationID);
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, SERVICES,
                 LogType.SERVICE_INFO_ERROR, null, null, query, token);
        return obj.has("services") ? Service.parse(obj.getAsJsonArray("services")) : new ArrayList<>(0);
    }

}
