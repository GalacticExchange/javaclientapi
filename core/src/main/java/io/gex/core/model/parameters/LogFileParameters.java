package io.gex.core.model.parameters;


import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

public class LogFileParameters {

    private final static LogWrapper logger = LogWrapper.create(InviteParameters.class);

    private final static String NODE_ID_PROPERTY_CLI_NAME = "--nodeID=";
    private final static String TOKEN_PROPERTY_CLI_NAME = "--userToken=";

    private String nodeID;
    private String token;

    public String getNodeID() {
        return nodeID;
    }

    public String getToken() {
        return token;
    }

    public LogFileParameters(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            for (String argument : arguments) {
                if (StringUtils.containsIgnoreCase(argument, TOKEN_PROPERTY_CLI_NAME)) {
                    this.token = BaseHelper.trimAndRemoveSubstring(argument, TOKEN_PROPERTY_CLI_NAME);
                    if (StringUtils.isBlank(this.token)) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, NODE_ID_PROPERTY_CLI_NAME)) {
                    this.nodeID = BaseHelper.trimAndRemoveSubstring(argument, NODE_ID_PROPERTY_CLI_NAME);
                    if (StringUtils.isBlank(this.nodeID)) {
                        throw new IllegalArgumentException(argument);
                    }
                } else {
                    throw new IllegalArgumentException(argument);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(CoreMessages.INVALID_PARAMETER + e.getMessage());
        }
    }
}
