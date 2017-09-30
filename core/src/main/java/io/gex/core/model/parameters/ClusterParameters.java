package io.gex.core.model.parameters;

import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Cluster;
import io.gex.core.model.ClusterSettings;
import io.gex.core.model.EntityType;
import org.apache.commons.lang3.StringUtils;

//todo create generic class for all Parameters
public class ClusterParameters {

    private final static LogWrapper logger = LogWrapper.create(ClusterParameters.class);

    private final static String STATIC_IPS_PARAMETER = "--staticIPs=";
    private final static String PROXY_IP_PARAMETER = "--proxyIP=";
    private final static String GATEWAY_IP_PARAMETER = "--gatewayIP=";
    private final static String NETWORK_IP_RANGE_START_PARAMETER = "--networkIPRangeStart=";
    private final static String NETWORK_IP_RANGE_END_PARAMETER = "--networkIPRangeEnd=";
    private final static String NETWORK_MASK_PARAMETER = "--networkMask=";
    private final static String HADOOP_TYPE_PARAMETER = "--hadoopType=";

    private final static String CLUSTER_TYPE_PARAMETER = "--clusterType=";
    private final static String AWS_KEY_ID_PARAMETER = "--awsKeyID=";
    private final static String AWS_SECRET_KEY_PARAMETER = "--awsSecretKey=";
    private final static String AWS_REGION_PARAMETER = "--awsRegion=";

    private final static String PROXY_USER_PARAMETER = "--proxyUser=";
    private final static String PROXY_PASSWORD_PARAMETER = "--proxyPassword=";

    public Cluster parse(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Cluster cluster = new Cluster();
        ClusterSettings clusterSettings = new ClusterSettings();
        try {
            for (String argument : arguments) {
                if (StringUtils.containsIgnoreCase(argument, CLUSTER_TYPE_PARAMETER)) {
                    cluster.setClusterType(EntityType.valueOf(BaseHelper.trimAndRemoveSubstring(argument, CLUSTER_TYPE_PARAMETER).toUpperCase()));
                    if (cluster.getClusterType() == null) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, PROXY_IP_PARAMETER)) {
                    clusterSettings.setProxyIP(BaseHelper.trimAndRemoveSubstring(argument, PROXY_IP_PARAMETER));
                    if (StringUtils.isBlank(clusterSettings.getProxyIP())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, GATEWAY_IP_PARAMETER)) {
                    clusterSettings.setGatewayIP(BaseHelper.trimAndRemoveSubstring(argument, GATEWAY_IP_PARAMETER));
                    if (StringUtils.isBlank(clusterSettings.getGatewayIP())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, NETWORK_MASK_PARAMETER)) {
                    clusterSettings.setNetworkMask(BaseHelper.trimAndRemoveSubstring(argument, NETWORK_MASK_PARAMETER));
                    if (StringUtils.isBlank(clusterSettings.getNetworkMask())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, NETWORK_IP_RANGE_START_PARAMETER)) {
                    clusterSettings.setNetworkIPRangeStart(BaseHelper.trimAndRemoveSubstring(argument, NETWORK_IP_RANGE_START_PARAMETER));
                    if (StringUtils.isBlank(clusterSettings.getNetworkIPRangeStart())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, NETWORK_IP_RANGE_END_PARAMETER)) {
                    clusterSettings.setNetworkIPRangeEnd(BaseHelper.trimAndRemoveSubstring(argument, NETWORK_IP_RANGE_END_PARAMETER));
                    if (StringUtils.isBlank(clusterSettings.getNetworkIPRangeEnd())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, STATIC_IPS_PARAMETER)) {
                    clusterSettings.setStaticIPs(Boolean.parseBoolean(BaseHelper.trimAndRemoveSubstring(argument, STATIC_IPS_PARAMETER)));
                } else if (StringUtils.containsIgnoreCase(argument, HADOOP_TYPE_PARAMETER)) {
                    clusterSettings.setHadoopType(BaseHelper.trimAndRemoveSubstring(argument, HADOOP_TYPE_PARAMETER));
                    if (StringUtils.isBlank(clusterSettings.getHadoopType())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, AWS_KEY_ID_PARAMETER)) {
                    clusterSettings.setAwsKeyID(BaseHelper.trimAndRemoveSubstring(argument, AWS_KEY_ID_PARAMETER));
                    if (StringUtils.isBlank(clusterSettings.getAwsKeyID())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, AWS_SECRET_KEY_PARAMETER)) {
                    clusterSettings.setAwsSecretKey(BaseHelper.trimAndRemoveSubstring(argument, AWS_SECRET_KEY_PARAMETER));
                    if (StringUtils.isBlank(clusterSettings.getAwsSecretKey())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, AWS_REGION_PARAMETER)) {
                    clusterSettings.setAwsRegion(BaseHelper.trimAndRemoveSubstring(argument, AWS_REGION_PARAMETER));
                    if (StringUtils.isBlank(clusterSettings.getAwsRegion())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, PROXY_USER_PARAMETER)) {
                    clusterSettings.setProxyUser(BaseHelper.trimAndRemoveSubstring(argument, PROXY_USER_PARAMETER));
                    if (StringUtils.isBlank(clusterSettings.getProxyUser())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, PROXY_PASSWORD_PARAMETER)) {
                    clusterSettings.setProxyPassword(BaseHelper.trimAndRemoveSubstring(argument, PROXY_PASSWORD_PARAMETER));
                    if (StringUtils.isBlank(clusterSettings.getProxyPassword())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else {
                    throw new IllegalArgumentException(argument);
                }
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.INVALID_PARAMETER + e.getMessage(), LogType.PARSE_ERROR);
        }
        // set default
        if (StringUtils.isBlank(clusterSettings.getHadoopType())) {
            clusterSettings.setHadoopType("cdh");
        }
        cluster.setClusterSettings(clusterSettings);
        validate(cluster);
        return cluster;
    }

    //todo refactor
    private void validate(Cluster cluster) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (cluster == null || cluster.getClusterSettings() == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_CREATE, LogType.PARSE_ERROR);
        }
        ClusterSettings clusterSettings = cluster.getClusterSettings();
        if (cluster.getClusterType() == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_TYPE, LogType.PARSE_ERROR);
        } else if (StringUtils.isBlank(clusterSettings.getHadoopType())) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_HADOOP_TYPE, LogType.PARSE_ERROR);
        } else if (!clusterSettings.isStaticIPs() && (StringUtils.isNotBlank(clusterSettings.getGatewayIP()) ||
                StringUtils.isNotBlank(clusterSettings.getNetworkIPRangeStart()) ||
                StringUtils.isNotBlank(clusterSettings.getNetworkIPRangeEnd()) ||
                StringUtils.isNotBlank(clusterSettings.getNetworkMask()))) {
            throw logger.logAndReturnException(STATIC_IPS_PARAMETER + " parameter should be = true.", LogType.PARSE_ERROR);
        } else if (clusterSettings.isStaticIPs() && StringUtils.isAnyBlank(clusterSettings.getGatewayIP(),
                clusterSettings.getNetworkIPRangeStart(), clusterSettings.getNetworkIPRangeEnd(),
                clusterSettings.getNetworkMask())) {
            throw logger.logAndReturnException(GATEWAY_IP_PARAMETER + ", " + NETWORK_IP_RANGE_START_PARAMETER + ", " +
                    NETWORK_IP_RANGE_END_PARAMETER + ", " + NETWORK_MASK_PARAMETER + " parameters should be present.", LogType.PARSE_ERROR);
        } else if (EntityType.ONPREM.equals(cluster.getClusterType()) &&
                (StringUtils.isNotBlank(clusterSettings.getAwsKeyID()) ||
                        StringUtils.isNotBlank(clusterSettings.getAwsSecretKey()) ||
                        StringUtils.isNotBlank(clusterSettings.getAwsRegion()))) {
            throw logger.logAndReturnException("AWS parameters should not be specified for on-premise cluster.", LogType.PARSE_ERROR);
        } else if (EntityType.AWS.equals(cluster.getClusterType()) &&
                StringUtils.isAnyBlank(clusterSettings.getAwsKeyID(), clusterSettings.getAwsSecretKey(), clusterSettings.getAwsRegion())) {
            throw logger.logAndReturnException(AWS_KEY_ID_PARAMETER + ", " + AWS_SECRET_KEY_PARAMETER + ", " + AWS_REGION_PARAMETER + " parameters should be present", LogType.PARSE_ERROR);
        } else if (clusterSettings.isStaticIPs() && EntityType.AWS.equals(cluster.getClusterType())) {
            throw logger.logAndReturnException(STATIC_IPS_PARAMETER + " parameter couldn't be used with aws " + CLUSTER_TYPE_PARAMETER + " parameter", LogType.PARSE_ERROR);
        } else if ((StringUtils.isBlank(clusterSettings.getProxyIP()) && (StringUtils.isNotBlank(clusterSettings.getProxyUser()) ||
                StringUtils.isNotBlank(clusterSettings.getProxyPassword()))
                || StringUtils.isNotBlank(clusterSettings.getProxyIP()) &&
                StringUtils.isBlank(clusterSettings.getProxyUser()) && StringUtils.isNotBlank(clusterSettings.getProxyPassword()))) {
            throw logger.logAndReturnException("Invalid proxy parameters.", LogType.PARSE_ERROR);
        }
    }

}
