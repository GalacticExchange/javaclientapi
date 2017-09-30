package io.gex.core.rest;

import com.google.gson.JsonObject;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Application;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class ApplicationLevelRest {

    private final static LogWrapper logger = LogWrapper.create(ApplicationLevelRest.class);

    private final static String APPLICATIONS = "/applications";
    private final static String LIBRARY_APPLICATIONS = "/appstore/applications";

    public static void applicationInstall(String nodeID, String name, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("nodeID", nodeID);
        body.add("applicationName", name);
        Rest.sendAuthenticatedRequest(HttpMethod.POST, APPLICATIONS,
                LogType.APPLICATION_INSTALL_ERROR, null, body, null, token);
    }

    public static void applicationUninstall(String nodeID, String applicationID, String token) throws GexException{
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> header = new MultivaluedHashMap<>();
        header.add("nodeID", nodeID);
        header.add("applicationID", applicationID);
        Rest.sendAuthenticatedRequest(HttpMethod.DELETE, APPLICATIONS,
                LogType.APPLICATION_UNINSTALL_ERROR, header, null, null, token);
    }

    public static List<Application> applicationList(String clusterID, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, ClusterLevelRest.CLUSTERS + "/" + clusterID + APPLICATIONS,
                LogType.APPLICATION_LIST_ERROR, null, null, null, token);
        return obj.has("apps") ? Application.parse(obj.getAsJsonArray("apps")) : new ArrayList<>(0);
    }

    public static List<Application> applicationSupported(String clusterID, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("clusterID", clusterID);
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, LIBRARY_APPLICATIONS,
                LogType.LIBRARY_APPLICATIONS_ERROR, null, null, query, token);
        return obj.has("apps") ? Application.parse(obj.getAsJsonArray("apps")) : new ArrayList<>(0);
    }
}
