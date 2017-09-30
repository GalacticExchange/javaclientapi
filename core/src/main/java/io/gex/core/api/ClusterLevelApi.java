package io.gex.core.api;

import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Cluster;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.ClusterLevelRest;
import org.apache.commons.lang3.StringUtils;

public class ClusterLevelApi {

    private final static LogWrapper logger = LogWrapper.create(ClusterLevelApi.class);

    public static Cluster clusterInfo(String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(clusterID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        return ClusterLevelRest.clusterInfo(clusterID, BasePropertiesHelper.getValidToken());
    }

    public static Cluster clusterCreate(Cluster cluster) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (cluster == null || cluster.getClusterType() == null || cluster.getClusterSettings() == null ||
                StringUtils.isBlank(cluster.getClusterSettings().getHadoopType())) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_CREATE, LogType.EMPTY_PROPERTY_ERROR);
        }
        return ClusterLevelRest.clusterCreate(cluster, BasePropertiesHelper.getValidToken());
    }

}
