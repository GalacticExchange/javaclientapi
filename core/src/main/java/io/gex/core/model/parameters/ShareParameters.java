package io.gex.core.model.parameters;

import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

public class ShareParameters {

    private final static LogWrapper logger = LogWrapper.create(ShareParameters.class);

    private final static String USERNAME_PARAMETER = "--username=";
    private final static String CLUSTER_ID_PARAMETER = "--clusterID=";

    private String username;
    private String clusterID;

    public String getUsername() {
        return username;
    }

    public String getClusterID() {
        return clusterID;
    }

    public ShareParameters(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            for (String argument : arguments) {
                if (StringUtils.containsIgnoreCase(argument, USERNAME_PARAMETER)) {
                    this.username = BaseHelper.trimAndRemoveSubstring(argument, USERNAME_PARAMETER);
                    if (StringUtils.isBlank(this.username)) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, CLUSTER_ID_PARAMETER)) {
                    this.clusterID = BaseHelper.trimAndRemoveSubstring(argument, CLUSTER_ID_PARAMETER);
                    if (StringUtils.isBlank(this.clusterID)) {
                        throw new IllegalArgumentException(argument);
                    }
                } else {
                    throw new IllegalArgumentException(argument);
                }
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.INVALID_PARAMETER + e.getMessage(), LogType.PARSE_ERROR);
        }
    }
}
