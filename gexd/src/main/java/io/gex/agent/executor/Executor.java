package io.gex.agent.executor;

import com.google.gson.JsonObject;
import io.gex.agent.GexdMessages;
import io.gex.core.CoreMessages;
import io.gex.core.PropertiesHelper;
import io.gex.core.api.MainLevelApi;
import io.gex.core.api.NodeLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.NodeNotification;
import io.gex.core.model.properties.NodeProperties;
import io.gex.core.shell.Commands;
import io.gex.core.vagrantHelper.VagrantHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;

public abstract class Executor {

    private final static LogWrapper logger = LogWrapper.create(Executor.class);

    public static Executor constructExecutor() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (PropertiesHelper.isHostVirtual()) {
            return new VagrantExecutor();
        } else {
            return new ServerExecutor();
        }
    }

    public abstract void upNoNotify() throws GexException;

    public abstract void up() throws GexException;

    public abstract void halt() throws GexException;

    public abstract void reload() throws GexException;

    public abstract void installContainer(String tar, String applicationName) throws GexException;

    public abstract void runContainer(String json, String applicationName) throws GexException;

    public abstract void uninstallContainer(String applicationName) throws GexException;

    public abstract void startContainer(String containerName) throws GexException;

    public abstract void stopContainer(String containerName) throws GexException;

    public abstract void restartContainer(String containerName) throws GexException;

    //todo it's continue uninstalling unless nodeUninstallStartNotification failed
    public abstract String nodeUninstall() throws GexException;

    public abstract String nodeUninstallForce();

    void nodeUninstallStartNotification() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String nodeID = PropertiesHelper.node.getProps().getNodeID();
        if (StringUtils.isBlank(nodeID)) {
            throw logger.logAndReturnException(CoreMessages.NODE_NOT_EXIST, LogType.NODE_UNINSTALL_ERROR);
        }
        MainLevelApi.notify(NodeNotification.NODE_UNINSTALLING, CoreMessages.NODE_UNINSTALLING);
    }

    void nodeUninstallFinishNotification(boolean forceUninstall) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            NodeLevelApi.nodeNotify(false);
        } catch (GexException e) {
            if (!forceUninstall) {
                MainLevelApi.notify(NodeNotification.NODE_UNINSTALL_ERROR, CoreMessages.NODE_UNINSTALL_ERROR);
                throw e;
            }
        }
        PropertiesHelper.node.remove(NodeProperties.NODE_ID_PROPERTY_NAME, NodeProperties.NODE_AGENT_TOKEN_PROPERTY_NAME,
                NodeProperties.CLUSTER_ID_PROPERTY_NAME, NodeProperties.CLUSTER_NAME_PROPERTY_NAME,
                NodeProperties.HADOOP_TYPE_PROPERTY_NAME);
    }

    void nodeUninstallRemoveConfigFiles() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (PropertiesHelper.isHostVirtual()) {
            // remove box
            File dir = Paths.get(VagrantHelper.vagrantHome, "boxes").toFile();
            if (dir.exists()) {
                File[] files = dir.listFiles((currentDir, name) -> (Paths.get(currentDir.getAbsolutePath(), name)
                        .toFile().isDirectory()) && name.contains(Commands.BOX_NAME_SLASH));
                try {
                    if (files != null) {
                        for (File file : files) {
                            logger.logInfo(GexdMessages.DELETING_FILE + file.getAbsolutePath(), LogType.NODE_UNINSTALL_REMOVE_CONFIG_FILES);
                            FileUtils.deleteDirectory(file);
                        }
                    }
                    logger.logInfo(CoreMessages.CONFIG_FILES_REMOVED, LogType.NODE_UNINSTALL_REMOVE_CONFIG_FILES);
                } catch (Exception e) {
                    logger.logError(CoreMessages.CONFIG_FILES_REMOVE_ERROR, e, LogType.NODE_UNINSTALL_REMOVE_CONFIG_FILES_ERROR);
                }
            }
            // delete installation files
            FileUtils.deleteQuietly(new File(PropertiesHelper.nodeConfig));
        }
        // remove credentials
        PropertiesHelper.node.remove(NodeProperties.NODE_NUMBER_PROPERTY_NAME, NodeProperties.NODE_NAME_PROPERTY_NAME,
                NodeProperties.APPLICATIONS_PROPERTY_NAME);
    }

    static void checkRequestAttributes(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        // TODO :: stupid check
        if (!obj.has(GexdMessages.CONTAINER_NAME) || obj.get(GexdMessages.CONTAINER_NAME).isJsonNull()) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CONTAINER_NAME, LogType.CONTAINER_ERROR);
        }
        if (!obj.has(GexdMessages.CONTAINER_ID) || obj.get(GexdMessages.CONTAINER_ID).isJsonNull()) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CONTAINER_ID, LogType.CONTAINER_ERROR);
        }
    }

}
