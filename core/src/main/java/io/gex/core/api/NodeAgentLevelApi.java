package io.gex.core.api;

import io.gex.core.ConnectionChecker;
import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.UrlHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Agent;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.NodeAgentLevelRest;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class NodeAgentLevelApi {

    private final static LogWrapper logger = LogWrapper.create(NodeAgentLevelApi.class);

    public static List<Agent> nodeAgentsInfo(String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(clusterID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        return NodeAgentLevelRest.nodeAgentsInfo(clusterID, BasePropertiesHelper.getValidToken());
    }

    public static void sendIPAndPort(String ip, Integer port, String nodeAgentToken) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(nodeAgentToken)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_AGENT_TOKEN, LogType.EMPTY_PROPERTY_ERROR);
        }
        if (StringUtils.isBlank(ip)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_IP, LogType.EMPTY_PROPERTY_ERROR);
        }
        if (port == null || port == 0) {
            throw logger.logAndReturnException(CoreMessages.INVALID_PORT, LogType.EMPTY_PROPERTY_ERROR);
        }
        NodeAgentLevelRest.sendIPAndPort(ip, port, nodeAgentToken);
    }

    public static DownloadFile nodeAgentLog(String nodeID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(nodeID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        Agent agent = NodeAgentLevelRest.nodeAgentInfo(nodeID, BasePropertiesHelper.getValidToken());
        if (agent != null && StringUtils.isNotBlank(agent.getIp()) && agent.getPort() != null) {
            String url = UrlHelper.httpResolver(UrlHelper.concatenate(agent.getPort(), agent.getIp()) + "/logs");
            url += "?token=" + BasePropertiesHelper.getValidToken();
            if (ConnectionChecker.isReachable(
                    UrlHelper.httpResolver(UrlHelper.concatenate(agent.getPort(), agent.getIp()) + "/itsalive"))) {
                return new DownloadFile(url, "log");
            }
        }
        throw logger.logAndReturnException(CoreMessages.GET_LOG_FILE_ERROR, LogType.LOG_ERROR);
    }


    public static boolean nodeViewLogs(String token, String nodeAgentToken) {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            if (StringUtils.isBlank(token)) {
                throw logger.logAndReturnException(CoreMessages.EMPTY_TOKEN, LogType.EMPTY_PROPERTY_ERROR);
            }
            if (StringUtils.isBlank(nodeAgentToken)) {
                throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_AGENT_TOKEN, LogType.EMPTY_PROPERTY_ERROR);
            }
            NodeAgentLevelRest.nodeViewLogs(token, nodeAgentToken);
        } catch (Exception e) {
            logger.logError(e, LogType.PERMISSIONS_CHECK_NODE_VIEW_LOGS_ERROR);
            return false;
        }
        return true;
    }
}
