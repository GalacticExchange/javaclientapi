package io.gex.core.model.parameters;

import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

public class NodeInstallParameters {

    private final static LogWrapper logger = LogWrapper.create(NodeInstallParameters.class);

    private final static String NODE_NAME_PARAMETER = "--name=";
    private final static String CLUSTER_ID_PARAMETER = "--clusterID=";

    private String nodeName;
    private String clusterID;

    public String getNodeName() {
        return nodeName;
    }

    public String getClusterID() {
        return clusterID;
    }

    public NodeInstallParameters(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            for (String argument : arguments) {
                if (StringUtils.containsIgnoreCase(argument, NODE_NAME_PARAMETER)) {
                    this.nodeName = BaseHelper.trimAndRemoveSubstring(argument, NODE_NAME_PARAMETER);
                    if (StringUtils.isBlank(this.nodeName)) {
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
