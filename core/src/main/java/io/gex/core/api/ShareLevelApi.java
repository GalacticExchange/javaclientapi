package io.gex.core.api;

import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Cluster;
import io.gex.core.model.User;
import io.gex.core.model.parameters.ShareParameters;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.ShareLevelRest;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ShareLevelApi {

    private final static LogWrapper logger = LogWrapper.create(ShareLevelApi.class);

    public static void shareCreate(ShareParameters shareParameters) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (shareParameters == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_EMAIL + "\n" + CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(shareParameters.getUsername())) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_USERNAME, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(shareParameters.getClusterID())) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        ShareLevelRest.shareCreate(shareParameters, BasePropertiesHelper.getValidToken());
    }

    public static void shareRemove(ShareParameters shareParameters) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (shareParameters == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_EMAIL + "\n" + CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(shareParameters.getUsername())) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_USERNAME, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(shareParameters.getClusterID())) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        ShareLevelRest.shareRemove(shareParameters, BasePropertiesHelper.getValidToken());
    }

    public static List<User> shareUserList(String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(clusterID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        return ShareLevelRest.shareUserList(clusterID, BasePropertiesHelper.getValidToken());
    }

    public static List<Cluster> shareClusterList() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return ShareLevelRest.shareClusterList(BasePropertiesHelper.getValidToken());
    }

}
