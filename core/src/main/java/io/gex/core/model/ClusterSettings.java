package io.gex.core.model;


import com.google.gson.JsonObject;
import io.gex.core.CoreMessages;
import io.gex.core.GsonHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;

public class ClusterSettings {

    private final static LogWrapper logger = LogWrapper.create(ClusterSettings.class);

    private String proxyIP;
    private boolean staticIPs;
    private String networkMask;
    private String networkIPRangeStart;
    private String networkIPRangeEnd;
    private String gatewayIP;
    private String hadoopType;

    private String awsKeyID;
    private String awsSecretKey;
    private String awsRegion;

    private String proxyUser;
    private String proxyPassword;

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    public String getAwsKeyID() {
        return awsKeyID;
    }

    public void setAwsKeyID(String awsKeyID) {
        this.awsKeyID = awsKeyID;
    }

    public String getHadoopType() {
        return hadoopType;
    }

    public void setHadoopType(String hadoopType) {
        this.hadoopType = hadoopType;
    }

    public String getGatewayIP() {
        return gatewayIP;
    }

    public void setGatewayIP(String gatewayIP) {
        this.gatewayIP = gatewayIP;
    }

    public String getProxyIP() {
        return proxyIP;
    }

    public void setProxyIP(String proxyIP) {
        this.proxyIP = proxyIP;
    }

    public boolean isStaticIPs() {
        return staticIPs;
    }

    public void setStaticIPs(boolean staticIPs) {
        this.staticIPs = staticIPs;
    }

    public String getNetworkMask() {
        return networkMask;
    }

    public void setNetworkMask(String networkMask) {
        this.networkMask = networkMask;
    }

    public String getNetworkIPRangeStart() {
        return networkIPRangeStart;
    }

    public void setNetworkIPRangeStart(String networkIPRangeStart) {
        this.networkIPRangeStart = networkIPRangeStart;
    }

    public String getNetworkIPRangeEnd() {
        return networkIPRangeEnd;
    }

    public void setNetworkIPRangeEnd(String networkIPRangeEnd) {
        this.networkIPRangeEnd = networkIPRangeEnd;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

    public static ClusterSettings parse(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(obj, ClusterSettings.class, CoreMessages.CLUSTER_OPTIONS_PARSING_ERROR);
    }

}
