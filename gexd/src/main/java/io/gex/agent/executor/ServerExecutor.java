package io.gex.agent.executor;

import com.google.gson.JsonObject;
import io.gex.agent.GexdMessages;
import io.gex.agent.RabbitMQConnection;
import io.gex.core.PropertiesHelper;
import io.gex.core.api.MainLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Action;
import io.gex.core.model.NodeNotification;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;
import org.apache.commons.lang3.StringUtils;

class ServerExecutor extends Executor {

    private final static LogWrapper logger = LogWrapper.create(ServerExecutor.class);

    @Override
    public void upNoNotify() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.bash(
                    "sudo ruby " + PropertiesHelper.nodeConfig + "/provision.rb")).setMessageSuccess(GexdMessages.NODE_START).
                    setMessageError(GexdMessages.NODE_START_ERROR).setTypeSuccess(LogType.NODE_START).build());
        } catch (GexException e) {
            logger.logError(GexdMessages.NODE_START_ERROR, LogType.NODE_START_ERROR);
            try {
                MainLevelApi.notify(NodeNotification.NODE_START_ERROR, e.getMessage());
            } catch (GexException e1) {
                // do nothing
            }
        }
    }

    @Override
    public void up() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.bash(
                    "sudo ruby " + PropertiesHelper.nodeConfig + "/provision.rb")).setMessageSuccess(GexdMessages.NODE_START).
                    setMessageError(GexdMessages.NODE_START_ERROR).setTypeSuccess(LogType.NODE_START).build());
            MainLevelApi.notify(NodeNotification.NODE_STARTED, GexdMessages.SERVER_MODE);
        } catch (GexException e) {
            logger.logError(GexdMessages.NODE_START_ERROR, LogType.NODE_START_ERROR);
            MainLevelApi.notify(NodeNotification.NODE_START_ERROR, e.getMessage());
        }
    }

    //todo we send notification about success and after that about error. And we send log only about error
    @Override
    public void halt() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        // will be working only if password check is turned off
        try {
            MainLevelApi.notify(NodeNotification.NODE_STOPPED, GexdMessages.STOPPED_NODE);
            ShellExecutor.executeCommand(ShellParameters.newBuilder(Commands.bash("sudo shutdown")).build());
        } catch (GexException e) {
            logger.logError(GexdMessages.SHUT_DOWN_ERROR, LogType.NODE_STOP_ERROR);
            MainLevelApi.notify(NodeNotification.NODE_STOP_ERROR, GexdMessages.SHUT_DOWN_ERROR);
            throw e;
        }
    }

    @Override
    public void reload() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            ShellExecutor.executeCommand(ShellParameters.newBuilder(Commands.bash("sudo reboot")).build());
        } catch (GexException e) {
            logger.logError(GexdMessages.RESTART_ERROR, LogType.NODE_RESTART);
            MainLevelApi.notify(NodeNotification.NODE_RESTART_ERROR, GexdMessages.RESTART_ERROR);
            throw e;
        }
    }

    @Override
    public void installContainer(String tar, String applicationName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.bash(
                "sudo /home/vagrant/ruby_scripts/install_container.rb " + tar + " " + applicationName)).
                setMessageSuccess(GexdMessages.INSTALL_CONTAINER).setMessageError(GexdMessages.INSTALL_CONTAINER_ERROR).
                setType(LogType.APPLICATION_INSTALL, LogType.APPLICATION_INSTALL_ERROR).build());
    }

    @Override
    public void runContainer(String json, String applicationName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.bash(
                "sudo /home/vagrant/ruby_scripts/run_container.rb " + applicationName + " " + json)).
                setMessageSuccess(GexdMessages.RUN_CONTAINER).setMessageError(GexdMessages.RUN_CONTAINER_ERROR).
                setType(LogType.APPLICATION_RUN, LogType.APPLICATION_RUN_ERROR).build());
    }

    @Override
    public void uninstallContainer(String applicationName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.bash(
                "sudo /home/vagrant/ruby_scripts/remove_container.rb " + applicationName)).
                setMessageSuccess(GexdMessages.REMOVE_CONTAINER).setMessageError(GexdMessages.REMOVE_CONTAINER_ERROR).
                setType(LogType.APPLICATION_UNINSTALL, LogType.APPLICATION_UNINSTALL_ERROR).build());
    }

    @Override
    public void startContainer(String message) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = RabbitMQConnection.getAttributes(message);
        checkRequestAttributes(obj);
        String containerName = obj.get(GexdMessages.CONTAINER_NAME).getAsString();
        String containerID = obj.get(GexdMessages.CONTAINER_ID).getAsString();
        try {
            ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec("sudo" +
                    Commands.CONTAINER_NAME_ENV + containerName + Commands.ACTION_ENV + Action.start.toString() + Commands.CHANGE_CONTROLLER_STATE_PATH)).
                    setMessageSuccess(GexdMessages.CONTAINER_START).setMessageError(GexdMessages.CONTAINER_START_ERROR).
                    setType(LogType.CONTAINER_START, LogType.CONTAINER_START_ERROR).build());
        } catch (GexException e) {
            MainLevelApi.notifyContainer(NodeNotification.CONTAINER_START_ERROR, e.getMessage(), containerID, LogType.CONTAINER_START_ERROR);
            return;
        }
        MainLevelApi.notifyContainer(NodeNotification.CONTAINER_STARTED, GexdMessages.CONTAINER_START, containerID, LogType.CONTAINER_START_ERROR);
    }

    @Override
    public void stopContainer(String message) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = RabbitMQConnection.getAttributes(message);
        checkRequestAttributes(obj);
        String containerName = obj.get(GexdMessages.CONTAINER_NAME).getAsString();
        String containerID = obj.get(GexdMessages.CONTAINER_ID).getAsString();
        try {
            ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec("sudo" +
                    Commands.CONTAINER_NAME_ENV + containerName + Commands.ACTION_ENV + Action.stop.toString() + Commands.CHANGE_CONTROLLER_STATE_PATH)).
                    setMessageSuccess(GexdMessages.CONTAINER_STOP).setMessageError(GexdMessages.CONTAINER_STOP_ERROR).
                    setType(LogType.CONTAINER_STOP, LogType.CONTAINER_STOP_ERROR).build());
        } catch (GexException e) {
            MainLevelApi.notifyContainer(NodeNotification.CONTAINER_STOP_ERROR, e.getMessage(), containerID, LogType.CONTAINER_STOP_ERROR);
            return;
        }
        MainLevelApi.notifyContainer(NodeNotification.CONTAINER_STOPPED, GexdMessages.CONTAINER_STOP, containerID, LogType.CONTAINER_STOP_ERROR);
    }

    @Override
    public void restartContainer(String message) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = RabbitMQConnection.getAttributes(message);
        checkRequestAttributes(obj);
        String containerName = obj.get(GexdMessages.CONTAINER_NAME).getAsString();
        String containerID = obj.get(GexdMessages.CONTAINER_ID).getAsString();
        try {
            ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec("sudo" +
                    Commands.CONTAINER_NAME_ENV + containerName + Commands.ACTION_ENV + Action.restart.toString() + Commands.CHANGE_CONTROLLER_STATE_PATH)).
                    setMessageSuccess(GexdMessages.CONTAINER_RESTART).setMessageError(GexdMessages.CONTAINER_RESTART_ERROR).
                    setType(LogType.CONTAINER_RESTART, LogType.CONTAINER_RESTART_ERROR).build());
        } catch (GexException e) {
            MainLevelApi.notifyContainer(NodeNotification.CONTAINER_RESTART_ERROR, e.getMessage(), containerID, LogType.CONTAINER_RESTART_ERROR);
            return;
        }
        MainLevelApi.notifyContainer(NodeNotification.CONTAINER_RESTARTED, GexdMessages.CONTAINER_RESTART, containerID, LogType.CONTAINER_RESTART_ERROR);
    }

    @Override
    public String nodeUninstallForce() {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            return nodeUninstall0();
        } catch (GexException e) {
            logger.logError(GexdMessages.NODE_UNINSTALL_ERROR, LogType.NODE_UNINSTALL_ERROR);
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String nodeUninstall() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return nodeUninstall0();
    }

    private String nodeUninstall0() throws GexException {
        try {
            return ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.bash("cd /home/vagrant/gexstarter/node_manage && sudo rake delete_node_local")).build());
        } catch (Exception e) {
            logger.logError(GexdMessages.NODE_UNINSTALL_ERROR, LogType.NODE_UNINSTALL_ERROR);
            throw e;
        }
    }
}
