package io.gex.core.api;

import io.gex.core.AppContext;
import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.parameters.LogFileParameters;
import org.apache.commons.lang3.StringUtils;

public class LogLevelApi {

    private final static LogWrapper logger = LogWrapper.create(LogLevelApi.class);

    public static String getNodeLocalLogs(LogFileParameters logFileParameters) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String nodeID;
        if (logFileParameters == null || StringUtils.isBlank(logFileParameters.getNodeID())) {
            if (StringUtils.isBlank(nodeID = PropertiesHelper.node.getProps().getNodeID())) {
                throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_ID, LogType.EMPTY_PROPERTY_ERROR);
            }
        } else {
            nodeID = logFileParameters.getNodeID();
        }
        if (logFileParameters != null && StringUtils.isNotBlank(logFileParameters.getToken())) {
            AppContext.set(logFileParameters.getToken());
        }
        if (StringUtils.equals(PropertiesHelper.node.getProps().getNodeID(), nodeID)) {
            return PropertiesHelper.agentLogPath;
        } else {
            DownloadFile nodeLogsDownload = NodeAgentLevelApi.nodeAgentLog(nodeID);
            nodeLogsDownload.downloadSync();
            if (!nodeLogsDownload.isDownloaded()) {
                throw logger.logAndReturnException(CoreMessages.GET_LOG_FILE_ERROR, nodeLogsDownload.getException(), LogType.LOCAL_LOG_ERROR);
            }
            return nodeLogsDownload.getFile().getAbsolutePath();
        }
    }
}
