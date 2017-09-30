package io.gex.core.api;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.PropertiesHelper;
import io.gex.core.SshHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.*;
import io.gex.core.model.properties.NodeProperties;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.NodeLevelRest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class NodeLevelApi {

    private final static LogWrapper logger = LogWrapper.create(NodeLevelApi.class);

    public static void nodeNotify(Boolean isInstall) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (isInstall) {
            MainLevelApi.notify(NodeNotification.NODE_CLIENT_INSTALLED, CoreMessages.NODE_INSTALLED);
        } else {
            MainLevelApi.notify(NodeNotification.NODE_UNINSTALLED, CoreMessages.NODE_UNINSTALLED);
        }
    }

    public static void installationNodeStatusCheck() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String nodeID = PropertiesHelper.node.getProps().getNodeID();
        if (StringUtils.isNotBlank(nodeID)) {
            throw logger.logAndReturnException(CoreMessages.NODE_EXIST, LogType.NODE_INSTALL_ERROR);
        }
    }

    public static String nodeInstall(NetworkAdapter networkAdapter, String clusterID, String instanceID, EntityType nodeType, String customName, String hadoopApp) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        installationNodeStatusCheck();
        return NodeLevelRest.nodeInstall(networkAdapter, clusterID, instanceID, nodeType, BasePropertiesHelper.getValidToken(), customName, hadoopApp);
    }

    public static void nodeSetup(String nodeID, String nodeAgentToken, String instanceID, EntityType nodeType, String customName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        installationNodeStatusCheck();
        NodeProperties nodeProperties = new NodeProperties();
        nodeProperties.setNodeAgentToken(nodeAgentToken);
        nodeProperties.setNodeID(nodeID);
        PropertiesHelper.node.saveToJSON(nodeProperties);
        NodeLevelRest.nodeUpdateGexd(nodeID, nodeAgentToken, instanceID, nodeType, customName);
    }

    public static void downloadInstallationFiles(String nodeID, String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String tmpPath = null;
        try {
            List<FileGex> files = FileLevelApi.filesList(Node.nodeInstallModule, nodeID, clusterID);
            if (CollectionUtils.isEmpty(files)) {
                throw logger.logAndReturnException(CoreMessages.NO_FILES_TO_DOWNLOAD, LogType.NODE_INSTALL_ERROR);
            }
            tmpPath = Paths.get(PropertiesHelper.tmpDir, "gex-" + UUID.randomUUID()).toString();
            for (FileGex file : files) {
                LogHelper.printWithColor(Color.ANSI_PURPLE, CoreMessages.DOWNLOADING + file.getFileName(), PropertiesHelper.isCLI());
                FileLevelApi.fileDownload(file.getFileName(), tmpPath, nodeID, clusterID);
            }
            FileLevelApi.moveFiles(files, tmpPath, PropertiesHelper.nodeConfig);
        } catch (GexException e) {
            MainLevelApi.notify(NodeNotification.NODE_CLIENT_INSTALL_ERROR, CoreMessages.NODE_INSTALL_ERROR);
            throw e;
        } finally {
            if (StringUtils.isNotBlank(tmpPath)) {
                FileUtils.deleteQuietly(new File(tmpPath));
            }
        }
    }

    public static void nodeCommands(Action action) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String nodeID = PropertiesHelper.node.getProps().getNodeID();
        if (StringUtils.isBlank(nodeID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_NAME, LogType.EMPTY_PROPERTY_ERROR);
        } else if (action == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_ACTION, LogType.EMPTY_PROPERTY_ERROR);
        }
        NodeLevelRest.nodeCommands(nodeID, action, BasePropertiesHelper.getValidToken());
    }

    public static void nodeCommands(String nodeID, Action action) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(nodeID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_ID, LogType.EMPTY_PROPERTY_ERROR);
        } else if (action == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_ACTION, LogType.EMPTY_PROPERTY_ERROR);
        }
        NodeLevelRest.nodeCommands(nodeID, action, BasePropertiesHelper.getValidToken());
    }

    public static List<Node> nodeList(String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(clusterID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        return NodeLevelRest.nodeList(clusterID, BasePropertiesHelper.getValidToken());
    }

    public static Node nodeInfo() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String nodeID = PropertiesHelper.node.getProps().getNodeID();
        if (StringUtils.isBlank(nodeID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        return NodeLevelRest.nodeInfo(nodeID, BasePropertiesHelper.getValidToken());
    }

    public static Node nodeInfo(String nodeId) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(nodeId)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        return NodeLevelRest.nodeInfo(nodeId, BasePropertiesHelper.getValidToken());
    }

    public static NodeStatus getNodeStatus() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String nodeID = PropertiesHelper.node.getProps().getNodeID();
        if (StringUtils.isBlank(nodeID)) {
            return null;
        }
        Node node = NodeLevelRest.nodeInfo(nodeID, BasePropertiesHelper.getValidToken());
        if (node == null) {
            logger.logWarn(CoreMessages.NODE_NOT_EXIST, LogType.NODE_INFO_ERROR);
            return null;
        }
        return node.getStatus();
    }

    public static String nodeUninstall(boolean isForce) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return NodeLevelRest.sendWebServerRequest("/nodes" + (isForce ? "/force" : ""), HttpMethod.DELETE,
                BasePropertiesHelper.getValidTokenOrNull());
    }

    public static void nodeRemove(String nodeID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(nodeID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        NodeLevelRest.nodeRemove(nodeID, BasePropertiesHelper.getValidToken());
    }

    public static void installNodeRemotely(NodeInstConfig instConfig, String clusterId, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());

        Session session = null;
        NodeProperties nodeProps = null;
        SshCredentials credentials = instConfig.getSshCredentials();
        try {
            nodeProps = NodeLevelRest.nodePost(null, clusterId, null,
                    EntityType.ONPREM, instConfig.getNodeName(), HostType.DEDICATED, credentials.getHost(), instConfig.getHadoopApp(), null, token);

            String bareMetalFolderUrl = ServerPropertiesLevelApi.getProperty("bare_metal_docker_url");
            DownloadFile downloadInstallScript = new DownloadFile(bareMetalFolderUrl + "/remote_installer.sh");
            downloadInstallScript.downloadSync();
            if (!downloadInstallScript.isDownloaded()) {
                throw logger.logAndReturnException(CoreMessages.DOWNLOAD_INSTALLER_ERROR, downloadInstallScript.getException(),
                        LogType.NODE_INSTALL_ERROR);
            }

            session = SshHelper.createSshSession(credentials);
            session.connect();
            logger.logInfo("Connected to machine " + credentials.getHost(), LogType.NODE_INSTALL);

            File file = downloadInstallScript.getFile();
            String resultScriptPath = "gex_remote_installer.sh";
            SshHelper.sendFile(session, file, resultScriptPath);
            logger.logInfo("Sent file " + file.getAbsolutePath(), LogType.NODE_INSTALL);

            String command = "/bin/bash -c \"export _gx_node_id='" + nodeProps.getNodeID()
                    + "' && export _gx_node_token='" + nodeProps.getNodeAgentToken()
                    + "' && export _gx_node_name='" + nodeProps.getNodeName()
                    + "' && export _gx_bare_metal_url='" + bareMetalFolderUrl
                    + "' && /bin/bash '" + resultScriptPath + "'\"";
            SshHelper.executeCommandWithSudo(session, command, credentials.getPassword(), false, false);


            logger.logInfo("Executed file " + resultScriptPath, LogType.NODE_INSTALL);

            command = "/bin/bash -c \"rm -f " + resultScriptPath + "\"";
            SshHelper.executeCommand(session, command, false, false);
            logger.logInfo("Deleted file " + resultScriptPath, LogType.NODE_INSTALL);
        } catch (JSchException | IOException | RuntimeException e) {
            GexException ex = logger.logAndReturnException("Failed to install node remotely: " + e.getMessage(), LogType.NODE_INSTALL_ERROR);
            MainLevelApi.notify(NodeNotification.NODE_CLIENT_INSTALL_ERROR, CoreMessages.NODE_REMOTE_INSTALL_ERROR, nodeProps);
            throw ex;
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    public static void testNodeBeforeRemoteUninst(NodeUninstConfig uninstConfig) throws GexException {
        Session session = null;
        SshCredentials credentials = uninstConfig.getSshCredentials();
        try {
            session = SshHelper.createSshSession(credentials);
            session.connect();

            SshHelper.SshResult sshResult = SshHelper.executeCommandWithSudo(session, "docker -v",
                    credentials.getPassword(), false, true);
            if (sshResult.getExitCode() == 127) {
                throw new GexException("Node not installed on machine " + credentials.getHost() + ".");
            }

            sshResult = SshHelper.executeCommandWithSudo(session, "docker ps -a | grep gex_bare_metal",
                    credentials.getPassword(), false, true);
            if (sshResult.getExitCode() == 1) {
                throw new GexException("Node not installed on machine " + credentials.getHost() + ".");
            }

            sshResult = SshHelper.executeCommandWithSudo(session, "docker exec -t gex_bare_metal bash -c \"hostname\"",
                    credentials.getPassword(), false, true);
            if (StringUtils.isNotEmpty(sshResult.getErr())) {
                throw new GexException(sshResult.getErr());
            } else if (sshResult.getExitCode() != 0) {
                throw new GexException(sshResult.getOut());
            }
            String nodeName = StringUtils.trim(sshResult.getOut());
            if (!StringUtils.equals(uninstConfig.getNodeName(), nodeName)) {
                throw new GexException("On machine " + credentials.getHost() + " installed another node with name " + nodeName + ".");
            }
        } catch (Exception e) {
            throw new GexException("Failed to check node " + uninstConfig.getNodeName() + " before uninstall: " + e.getMessage());
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    public static void uninstallNodeRemotely(NodeUninstConfig uninstConfig, String clusterId, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());

        NodeProperties nodeProps = new NodeProperties();
        nodeProps.setClusterID(clusterId);
        nodeProps.setNodeID(uninstConfig.getNodeId());
        nodeProps.setNodeName(uninstConfig.getNodeName());
        MainLevelApi.notify(NodeNotification.NODE_UNINSTALLING, CoreMessages.NODE_UNINSTALLING, nodeProps);

        String bareMetalFolderUrl = ServerPropertiesLevelApi.getProperty("bare_metal_docker_url");
        DownloadFile downloadInstallScript = new DownloadFile(bareMetalFolderUrl + "/remote_uninstaller.sh");
        downloadInstallScript.downloadSync();
        if (!downloadInstallScript.isDownloaded()) {
            throw logger.logAndReturnException(CoreMessages.DOWNLOAD_UNINSTALLER_ERROR, downloadInstallScript.getException(),
                    LogType.NODE_UNINSTALL_ERROR);
        }

        Session session = null;
        SshCredentials credentials = uninstConfig.getSshCredentials();
        try {
            session = SshHelper.createSshSession(credentials);
            session.connect();
            logger.logInfo("Connected to machine " + credentials.getHost(), LogType.NODE_UNINSTALL);

            File file = downloadInstallScript.getFile();
            String resultScriptPath = "gex_remote_uninstaller.sh";
            SshHelper.sendFile(session, file, resultScriptPath);
            logger.logInfo("Sent file " + file.getAbsolutePath(), LogType.NODE_UNINSTALL);

            String command = "/bin/bash -c \"export _gx_node_id='" + nodeProps.getNodeID()
                    + "' && export _gx_user_token='" + token
                    + "' && export _gx_node_name='" + nodeProps.getNodeName()
                    + "' && export _gx_bare_metal_url='" + bareMetalFolderUrl
                    + "' && /bin/bash '" + resultScriptPath + "'\"";
            SshHelper.executeCommandWithSudo(session, command, credentials.getPassword(), false, false);

            logger.logInfo("Executed file " + resultScriptPath, LogType.NODE_UNINSTALL);

            command = "/bin/bash -c \"rm -f " + resultScriptPath + "\"";
            SshHelper.executeCommand(session, command, false, false);
            logger.logInfo("Deleted file " + resultScriptPath, LogType.NODE_UNINSTALL);
            MainLevelApi.notify(NodeNotification.NODE_UNINSTALLED, CoreMessages.NODE_UNINSTALLED, nodeProps);
        } catch (JSchException | IOException | RuntimeException e) {
            GexException ex = logger.logAndReturnException("Failed to uninstall node remotely: " + e.getMessage(), LogType.NODE_UNINSTALL_ERROR);
            MainLevelApi.notify(NodeNotification.NODE_UNINSTALL_ERROR, CoreMessages.NODE_UNINSTALL_ERROR, nodeProps);
            throw ex;
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

}
