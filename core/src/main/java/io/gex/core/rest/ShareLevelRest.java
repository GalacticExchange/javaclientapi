package io.gex.core.rest;

import com.google.gson.JsonObject;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Cluster;
import io.gex.core.model.User;
import io.gex.core.model.parameters.ShareParameters;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;


public class ShareLevelRest {

    private final static LogWrapper logger = LogWrapper.create(ShareLevelRest.class);

    private final static String SHARE = "/shares";
    //todo rename
    private final static String SHARE_LIST = "/userShares";
    private final static String CLUSTERS_SHARED = "/clusters/shared";

    public static void shareCreate(ShareParameters shareParameters, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("username", shareParameters.getUsername());
        body.add("clusterID", shareParameters.getClusterID());
        Rest.sendAuthenticatedRequest(HttpMethod.POST, SHARE,  LogType.SHARE_CREATE_ERROR,
                null, body, null, token);
    }

    public static void shareRemove(ShareParameters shareParameters, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> header = new MultivaluedHashMap<>();
        header.add("username", shareParameters.getUsername());
        header.add("clusterID", shareParameters.getClusterID());
        Rest.sendAuthenticatedRequest(HttpMethod.DELETE, SHARE,  LogType.SHARE_REMOVE_ERROR,
                header, null, null, token);
    }

    public static List<User> shareUserList(String clusterID, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("clusterID", clusterID);
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, SHARE_LIST,
                LogType.SHARE_LIST_ERROR, null, null, query, token);
        return obj.has("shares") ? User.parse(obj.getAsJsonArray("shares")) : new ArrayList<>(0);
    }

    public static List<Cluster> shareClusterList(String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, CLUSTERS_SHARED,
                 LogType.CLUSTER_SHARED_ERROR, null, null, null, token);
        return obj.has("clusters") ? Cluster.parse(obj.getAsJsonArray("clusters")) : new ArrayList<>(0);
    }

}
