package io.gex.core.rest;

import com.google.gson.JsonObject;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Cluster;
import io.gex.core.model.ClusterSettings;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class ClusterLevelRest {

    private final static LogWrapper logger = LogWrapper.create(ClusterLevelRest.class);

    final static String CLUSTERS = "/clusters";

    // todo remove "settings" duplicate in response
    public static Cluster clusterInfo(String clusterID, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, CLUSTERS + "/" + clusterID + "/info",
                LogType.CLUSTER_INFO_ERROR, null, null, null, token);
        return obj.has("cluster") ? Cluster.parse(obj.getAsJsonObject("cluster")) : null;
    }

    public static Cluster clusterCreate(Cluster cluster, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        if (cluster.getClusterType() != null) {
            body.add("clusterType", cluster.getClusterType().toString().toLowerCase());
        }
        ClusterSettings clusterSettings = cluster.getClusterSettings();
        if (clusterSettings != null) {
            if (StringUtils.isNotBlank(clusterSettings.getHadoopType())) {
                body.add("hadoopType", clusterSettings.getHadoopType());
            }
            if (StringUtils.isNotBlank(clusterSettings.getProxyIP())) {
                body.add("proxyIP", clusterSettings.getProxyIP());
            }
            if (StringUtils.isNotBlank(clusterSettings.getProxyUser())) {
                body.add("proxyUser", clusterSettings.getProxyUser());
            }
            if (StringUtils.isNotBlank(clusterSettings.getProxyPassword())) {
                body.add("proxyPassword", clusterSettings.getProxyPassword());
            }
            body.add("staticIPs", clusterSettings.isStaticIPs() ? String.valueOf(1) : String.valueOf(0));
            if (StringUtils.isNotBlank(clusterSettings.getNetworkMask())) {
                body.add("networkMask", clusterSettings.getNetworkMask());
            }
            if (StringUtils.isNotBlank(clusterSettings.getGatewayIP())) {
                body.add("gatewayIP", clusterSettings.getGatewayIP());
            }
            if (StringUtils.isNotBlank(clusterSettings.getNetworkIPRangeStart())) {
                body.add("networkIPRangeStart", clusterSettings.getNetworkIPRangeStart());
            }
            if (StringUtils.isNotBlank(clusterSettings.getNetworkIPRangeEnd())) {
                body.add("networkIPRangeEnd", clusterSettings.getNetworkIPRangeEnd());
            }
            if (StringUtils.isNotBlank(clusterSettings.getAwsKeyID())) {
                body.add("awsKeyID", clusterSettings.getAwsKeyID());
            }
            if (StringUtils.isNotBlank(clusterSettings.getAwsSecretKey())) {
                // reverse name
                body.add("awsSecretKey", clusterSettings.getAwsSecretKey());
            }
            if (StringUtils.isNotBlank(clusterSettings.getAwsRegion())) {
                body.add("awsRegion", clusterSettings.getAwsRegion());
            }
        }
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.POST, CLUSTERS,  LogType.CLUSTER_CREATE_ERROR, null, body, null, token);
        //todo save info to file?
        return obj.has("cluster") ? Cluster.parse(obj.getAsJsonObject("cluster")) : null;
    }
}
