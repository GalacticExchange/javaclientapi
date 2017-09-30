package io.gex.core.model.parameters;

import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

public class InviteParameters {

    private final static LogWrapper logger = LogWrapper.create(InviteParameters.class);

    private final static String EMAIL_PARAMETER = "--email=";
    private final static String CLUSTER_ID_PARAMETER = "--clusterID=";

    private String email;
    private String clusterID;

    public String getEmail() {
        return email;
    }

    public String getClusterID() {
        return clusterID;
    }

    public InviteParameters(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            for (String argument : arguments) {
                if (StringUtils.containsIgnoreCase(argument, EMAIL_PARAMETER)) {
                    this.email = BaseHelper.trimAndRemoveSubstring(argument, EMAIL_PARAMETER);
                    if (StringUtils.isBlank(this.email)) {
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
