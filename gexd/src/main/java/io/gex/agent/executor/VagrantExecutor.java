package io.gex.agent.executor;

import com.google.gson.JsonObject;
import io.gex.agent.GexdHelper;
import io.gex.agent.GexdMessages;
import io.gex.agent.RabbitMQConnection;
import io.gex.core.*;
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
import io.gex.core.vagrantHelper.VagrantHelper;
import io.gex.core.vagrantHelper.VagrantHelperMac;
import io.gex.core.vagrantHelper.VagrantHelperWindows;
import io.gex.core.virutalBoxHelper.VirtualBoxHelper;
import io.gex.core.virutalBoxHelper.VirtualBoxHelperWindows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class VagrantExecutor extends Executor {

    private final static LogWrapper logger = LogWrapper.create(VagrantExecutor.class);

    private final static int HALT_TRIES = 18;
    private final static int HALT_WAIT_MILLIS = 10000;

    private Map<String, String> getEnv() throws GexException {
        Map<String, String> env = new HashMap<>();
        env.put(Commands.VAGRANT_HOME, VagrantHelper.vagrantHome);
        if (SystemUtils.IS_OS_MAC) {
            env.put(Commands.PATH, VagrantHelperMac.pathWithVagrant);
        } else if (SystemUtils.IS_OS_WINDOWS) {
            env.put(Commands.PATH_WIN, VagrantHelperWindows.getPath());
            if (StringUtils.isBlank(System.getenv(VirtualBoxHelperWindows.VBOX_INSTALL_PATH))
                    && StringUtils.isBlank(System.getenv(VirtualBoxHelperWindows.VBOX_MSI_INSTALL_PATH))) {
                env.put(VirtualBoxHelperWindows.VBOX_MSI_INSTALL_PATH, VirtualBoxHelperWindows.VBOX_DEFAULT_INSTALL_PATH);
            }
        }
        return env;
    }

    private static String vagrantWrapper(GexVagrantExecutorOutput executor) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            return executor.execute();
        } catch (Exception e) {
            String virtualBoxLog = VirtualBoxHelper.getVBoxLog();
            //todo check toString()
            logger.logWarn(StringUtils.isNotBlank(virtualBoxLog) ? virtualBoxLog :
                    CoreMessages.NO_VIRTUAL_BOX_LOG_FILE, LogType.VIRTUAL_BOX_LOG_ERROR);
            if (e.getMessage().contains(GexdMessages.VAGRANT_LOCKED) || e.getMessage().contains(GexdMessages.VAGRANT_INDEX_CORRUPTED)) {
                //todo check
                logger.logWarn(GexdMessages.REMOVE_VAGRANT_LOCK, LogType.VIRTUAL_BOX_LOG_ERROR);
                FileUtils.deleteQuietly(Paths.get(VagrantHelper.vagrantHome, "data", "machine-index", "index.lock").toFile());
                FileUtils.deleteQuietly(Paths.get(VagrantHelper.vagrantHome, "data", "machine-index", "index").toFile());
                executor.execute();
            }
            throw e;
        }
    }

    @Override
    public void up() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (SystemUtils.IS_OS_WINDOWS) {
            VirtualBoxHelperWindows vbHelperWin = new VirtualBoxHelperWindows();
            if (vbHelperWin.isVMPresent(PropertiesHelper.node.getProps().getNodeName())) {
                up0();
            } else {
                String vBoxUserHome = WindowsInstallInfoHelper.read("vbox_user_home");
                String virtualBoxHome = StringUtils.isNotBlank(vBoxUserHome) ? vBoxUserHome :
                        Paths.get(PropertiesHelper.userHome, ".gex", "VirtualBox VMs").toString();
                String defaultMachineFolder = vbHelperWin.getMachineFolder();
                try {
                    vbHelperWin.setMachineFolder(virtualBoxHome);
                    up0();
                } finally {
                    vbHelperWin.setMachineFolder(defaultMachineFolder);
                }
            }
        } else {
            up0();
        }
    }

    @Override
    public void upNoNotify() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(Commands.VAGRANT_UP)).
                    setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).setMessageSuccess(GexdMessages.VAGRANT_UP).
                    setMessageError(GexdMessages.VAGRANT_UP_ERROR).setTypeSuccess(LogType.VAGRANT_UP).build()));
        } catch (GexException e) {
            logger.logError(GexdMessages.VAGRANT_UP_ERROR, LogType.VAGRANT_UP_ERROR);
            try {
                MainLevelApi.notify(NodeNotification.NODE_START_ERROR, e.getMessage());
            } catch (GexException e1) {
                // do nothing
            }
        }
    }


    private void up0() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(Commands.VAGRANT_UP)).
                    setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).setMessageSuccess(GexdMessages.VAGRANT_UP).
                    setMessageError(GexdMessages.VAGRANT_UP_ERROR).setTypeSuccess(LogType.VAGRANT_UP).build()));
            MainLevelApi.notify(NodeNotification.NODE_STARTED, GexdMessages.VAGRANT_UP);
        } catch (GexException e) {
            logger.logError(GexdMessages.VAGRANT_UP_ERROR, LogType.VAGRANT_UP_ERROR);
            MainLevelApi.notify(NodeNotification.NODE_START_ERROR, e.getMessage());
        }
    }

    @Override
    public void halt() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(
                    Commands.exec(Commands.VAGRANT_HALT)).setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).
                    setMessageSuccess(GexdMessages.VAGRANT_HALT).setMessageError(GexdMessages.VAGRANT_HALT_ERROR).
                    setTypeSuccess(LogType.VAGRANT_HALT).build()));
        } catch (GexException e) {
            if (e.getMessage().contains(GexdMessages.VAGRANT_SSH_CONNECTION)) {
                for (int i = 0; i < HALT_TRIES; i++) {
                    try {
                        GexdHelper.sleep(HALT_WAIT_MILLIS);
                        checkNodeStatus(false);
                    } catch (GexException e1) {
                        continue;
                    }
                    logger.logInfo(GexdMessages.VAGRANT_HALT, LogType.VAGRANT_HALT);
                    MainLevelApi.notify(NodeNotification.NODE_STOPPED, GexdMessages.VAGRANT_HALT);
                    return;
                }
            }
            logger.logError(GexdMessages.VAGRANT_HALT_ERROR, LogType.VAGRANT_HALT_ERROR);
            MainLevelApi.notify(NodeNotification.NODE_STOP_ERROR, e.getMessage());
            return;
        }
        MainLevelApi.notify(NodeNotification.NODE_STOPPED, GexdMessages.VAGRANT_HALT);
    }

    @Override
    public void reload() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            //halt
            try {
                vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(Commands.VAGRANT_HALT))
                        .setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).setMessageSuccess(GexdMessages.VAGRANT_RELOAD_HALT).
                                setMessageError(GexdMessages.VAGRANT_RELOAD_HALT_ERROR).setTypeSuccess(LogType.VAGRANT_RELOAD_HALT).build()));
            } catch (GexException e) {
                if (e.getMessage().contains(GexdMessages.VAGRANT_SSH_CONNECTION)) {
                    for (int i = 0; i < HALT_TRIES; i++) {
                        try {
                            GexdHelper.sleep(HALT_WAIT_MILLIS);
                            checkNodeStatus(false);
                            break;
                        } catch (GexException e1) {
                            // do not check vagrant status. Try to execute vagrant up anyway.
                        }
                        //todo there is no VAGRANT_RELOAD_HALT_ERROR
                    }
                }
            }
            //up
            vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(Commands.VAGRANT_UP)).
                    setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).setMessageSuccess(GexdMessages.VAGRANT_RELOAD_UP).
                    setMessageError(GexdMessages.VAGRANT_RELOAD_UP_ERROR).
                    setType(LogType.VAGRANT_RELOAD_UP, LogType.VAGRANT_RELOAD_UP_ERROR).build()));
        } catch (GexException e) {
            MainLevelApi.notify(NodeNotification.NODE_RESTART_ERROR, e.getMessage());
            return;
        }
        MainLevelApi.notify(NodeNotification.NODE_RESTARTED, GexdMessages.VAGRANT_RELOAD);
    }

    private String destroyOutput() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(
                Commands.VAGRANT_DESTROY_F)).setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).
                setMessageSuccess(GexdMessages.VAGRANT_DESTROY).setMessageError(GexdMessages.VAGRANT_DESTROY_ERROR).
                setType(LogType.NODE_UNINSTALL_REMOVE_BOX, LogType.NODE_UNINSTALL_REMOVE_BOX_ERROR).build()));
    }

    private String haltOutput() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            return vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(
                    Commands.VAGRANT_HALT)).setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).
                    setMessageSuccess(GexdMessages.VAGRANT_HALT).setMessageError(GexdMessages.VAGRANT_HALT_ERROR).
                    setTypeSuccess(LogType.NODE_UNINSTALL_STOP_BOX).build()));
        } catch (GexException e) {
            if (e.getMessage().contains(GexdMessages.VAGRANT_SSH_CONNECTION)) {
                for (int i = 0; i < HALT_TRIES; i++) {
                    try {
                        GexdHelper.sleep(10000);
                        checkNodeStatus(false);
                        return null;
                    } catch (GexException e1) {
                        // do nothing
                    }
                }
            }
            logger.logError(GexdMessages.VAGRANT_HALT_ERROR, LogType.NODE_UNINSTALL_STOP_BOX_ERROR);
            throw e;
        }
    }

    @Override
    public void installContainer(String tar, String applicationName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        checkNodeStatus(true);
        final String tarUnix = FilenameUtils.separatorsToUnix(tar);
        vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(Commands.VAGRANT +
                Commands.IMAGE_FILE + tarUnix + Commands.APP_NAME + applicationName + Commands.VAGRANT_PROVISION_INSTALL +
                Commands.REDIRECT_OUTPUT)).setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).
                setMessageSuccess(GexdMessages.VAGRANT_PROVISION_INSTALL).setMessageError(GexdMessages.VAGRANT_PROVISION_INSTALL_ERROR).
                setType(LogType.APPLICATION_INSTALL, LogType.APPLICATION_INSTALL_ERROR).build()));
    }

    @Override
    public void runContainer(String json, String applicationName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        checkNodeStatus(true);
        final String jsonUnix = FilenameUtils.separatorsToUnix(json);
        vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(Commands.VAGRANT +
                Commands.JSON_FILE + jsonUnix + Commands.APP_NAME + applicationName + Commands.VAGRANT_PROVISION_RUN +
                Commands.REDIRECT_OUTPUT)).setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).
                setMessageSuccess(GexdMessages.VAGRANT_PROVISION_RUN).setMessageError(GexdMessages.VAGRANT_PROVISION_RUN_ERROR).
                setType(LogType.APPLICATION_RUN, LogType.APPLICATION_RUN_ERROR).build()));
    }

    @Override
    public void uninstallContainer(String applicationName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        checkNodeStatus(true);
        vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(Commands.VAGRANT +
                Commands.APP_NAME + applicationName + Commands.VAGRANT_PROVISION_UNINSTALL + Commands.REDIRECT_OUTPUT)).
                setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).setMessageSuccess(GexdMessages.VAGRANT_PROVISION_UNINSTALL).
                setMessageError(GexdMessages.VAGRANT_PROVISION_UNINSTALL_ERROR).
                setType(LogType.APPLICATION_UNINSTALL, LogType.APPLICATION_UNINSTALL_ERROR).build()));
    }

    @Override
    public void startContainer(String message) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = RabbitMQConnection.getAttributes(message);
        checkRequestAttributes(obj);
        String containerName = obj.get(GexdMessages.CONTAINER_NAME).getAsString();
        String containerID = obj.get(GexdMessages.CONTAINER_ID).getAsString();
        try {
            //todo do we need this check
            checkNodeStatus(true);
            vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(Commands.VAGRANT +
                    Commands.CONTAINER_NAME + containerName + Commands.ACTION + Action.start.toString() + Commands.VAGRANT_PROVISION_CHANGE + Commands.REDIRECT_OUTPUT)).
                    setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).setMessageSuccess(GexdMessages.VAGRANT_CONTAINER_START).
                    setMessageError(GexdMessages.VAGRANT_CONTAINER_START_ERROR).setType(LogType.CONTAINER_START, LogType.CONTAINER_START_ERROR).build()));
        } catch (GexException e) {
            MainLevelApi.notifyContainer(NodeNotification.CONTAINER_START_ERROR, e.getMessage(), containerID, LogType.CONTAINER_START_ERROR);
            return;
        }
        MainLevelApi.notifyContainer(NodeNotification.CONTAINER_STARTED, GexdMessages.VAGRANT_CONTAINER_START, containerID, LogType.CONTAINER_START_ERROR);
    }

    @Override
    public void stopContainer(String message) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = RabbitMQConnection.getAttributes(message);
        checkRequestAttributes(obj);
        String containerName = obj.get(GexdMessages.CONTAINER_NAME).getAsString();
        String containerID = obj.get(GexdMessages.CONTAINER_ID).getAsString();
        try {
            checkNodeStatus(true);
            vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(Commands.VAGRANT +
                    Commands.CONTAINER_NAME + containerName + Commands.ACTION + Action.stop.toString() + Commands.VAGRANT_PROVISION_CHANGE + Commands.REDIRECT_OUTPUT)).
                    setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).setMessageSuccess(GexdMessages.VAGRANT_CONTAINER_STOP).
                    setMessageError(GexdMessages.VAGRANT_CONTAINER_STOP_ERROR).setType(LogType.CONTAINER_STOP, LogType.CONTAINER_STOP_ERROR).build()));
        } catch (GexException e) {
            MainLevelApi.notifyContainer(NodeNotification.CONTAINER_STOP_ERROR, e.getMessage(), containerID, LogType.CONTAINER_STOP_ERROR);
            return;
        }
        MainLevelApi.notifyContainer(NodeNotification.CONTAINER_STOPPED, GexdMessages.VAGRANT_CONTAINER_STOP, containerID, LogType.CONTAINER_STOP_ERROR);
    }

    @Override
    public void restartContainer(String message) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = RabbitMQConnection.getAttributes(message);
        checkRequestAttributes(obj);
        String containerName = obj.get(GexdMessages.CONTAINER_NAME).getAsString();
        String containerID = obj.get(GexdMessages.CONTAINER_ID).getAsString();
        try {
            checkNodeStatus(true);
            vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(Commands.VAGRANT +
                    Commands.CONTAINER_NAME + containerName + Commands.ACTION + Action.restart.toString() + Commands.VAGRANT_PROVISION_CHANGE + Commands.REDIRECT_OUTPUT)).
                    setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).setMessageSuccess(GexdMessages.VAGRANT_CONTAINER_RESTART).
                    setMessageError(GexdMessages.VAGRANT_CONTAINER_RESTART_ERROR).setType(LogType.CONTAINER_RESTART, LogType.CONTAINER_RESTART_ERROR).build()));
        } catch (GexException e) {
            MainLevelApi.notifyContainer(NodeNotification.CONTAINER_RESTART_ERROR, e.getMessage(), containerID, LogType.CONTAINER_RESTART_ERROR);
            return;
        }
        MainLevelApi.notifyContainer(NodeNotification.CONTAINER_RESTARTED, GexdMessages.VAGRANT_CONTAINER_RESTART, containerID, LogType.CONTAINER_RESTART_ERROR);
    }

    private void checkNodeStatus(boolean isRunning) throws GexException {
        String result = vagrantWrapper(() -> ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec(
                Commands.VAGRANT_STATUS + Commands.REDIRECT_OUTPUT)).
                setDir(PropertiesHelper.nodeConfig).setNewEnv(getEnv()).setMessageSuccess(GexdMessages.VAGRANT_STATUS).
                setMessageError(GexdMessages.VAGRANT_STATUS_ERROR).build()));
        checkNodeStatus(result, isRunning);
    }

    private void checkNodeStatus(String vagrantStatusRes, boolean isRunning) throws GexException {
        if (isRunning) {
            if (!Pattern.compile("\\d+,\\w+,state,running").matcher(vagrantStatusRes).find()) {
                throw logger.logAndReturnException(CoreMessages.NODE_NOT_RUNNING, LogType.VAGRANT_STATUS_ERROR);
            }
        } else {
            if (!Pattern.compile("\\d+,\\w+,state,poweroff").matcher(vagrantStatusRes).find()) {
                throw logger.logAndReturnException(CoreMessages.NODE_NOT_POWER_OFF, LogType.VAGRANT_STATUS_ERROR);
            }
        }
    }

    @Override
    public String nodeUninstallForce() {
        logger.trace("Entered " + LogHelper.getMethodName());
        //todo refactor log
        StringBuilder result = new StringBuilder(StringUtils.EMPTY);
        try {
            MainLevelApi.notify(NodeNotification.NODE_UNINSTALLING, CoreMessages.NODE_UNINSTALLING);
        } catch (GexException e) {
            logger.logError(e, LogType.NODE_UNINSTALL_ERROR);
        }
        try {
            // halt virtualbox
            result.append(GexdMessages.STEP_1).append("\n");
            try {
                String killVagrantProcRes = killVagrantProcess();
                if (StringUtils.isNotEmpty(killVagrantProcRes)) {
                    result.append(killVagrantProcRes).append("\n");
                }
            } catch (GexException e) {
                logger.logError(e, LogType.NODE_UNINSTALL_ERROR);
            }
            result.append(haltOutput()).append("\n");
            result.append(GexdMessages.STEP_1_COMPLETED).append("\n");
        } catch (GexException e) {
            result.append(GexdMessages.STEP_1_WARNING).append("\n");
            logger.logError(e, LogType.NODE_UNINSTALL_ERROR);
        }
        try {
            // destroy virtualbox
            result.append(GexdMessages.STEP_2).append("\n");
            result.append(destroyOutput()).append("\n");
            result.append(GexdMessages.STEP_2_COMPLETED).append("\n");
        } catch (GexException e) {
            result.append(GexdMessages.STEP_2_WARNING).append("\n");
            logger.logError(e, LogType.NODE_UNINSTALL_ERROR);
        }

        try {
            //remove configuration files
            result.append(GexdMessages.STEP_3).append("\n");
            nodeUninstallRemoveConfigFiles();
            result.append(GexdMessages.STEP_3_COMPLETED).append("\n");
        } catch (GexException e) {
            logger.logError(e, LogType.NODE_UNINSTALL_ERROR);
        }
        try {
            nodeUninstallFinishNotification(true);
        } catch (GexException e) {
            logger.logError(e, LogType.NODE_UNINSTALL_ERROR);
        }

        return result.toString();
    }

    //todo it's continue uninstalling unless nodeUninstallStartNotification failed
    @Override
    public String nodeUninstall() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return NodeLocker.executeWithLockOutput(() -> {
            StringBuilder result = new StringBuilder(StringUtils.EMPTY);
            nodeUninstallStartNotification();
            try {
                // stop virtualbox
                result.append(GexdMessages.STEP_1).append("\n");
                try {
                    result.append(haltOutput());
                    result.append(GexdMessages.STEP_1_COMPLETED).append("\n");
                } catch (GexException e) {
                    result.append(GexdMessages.STEP_1_WARNING).append("\n");
                    result.append(e.getMessage()).append("\n");
                }
                // delete virtualbox
                result.append(GexdMessages.STEP_2).append("\n");
                try {
                    result.append(destroyOutput());
                    result.append(GexdMessages.STEP_2_COMPLETED).append("\n");
                } catch (GexException e) {
                    result.append(GexdMessages.STEP_2_WARNING).append("\n");
                    result.append(e.getMessage()).append("\n");
                }
                // remove configuration files
                result.append(GexdMessages.STEP_3).append("\n");
                nodeUninstallRemoveConfigFiles();
                result.append(GexdMessages.STEP_3_COMPLETED).append("\n");
            } catch (GexException e) {
                MainLevelApi.notify(NodeNotification.NODE_UNINSTALL_ERROR, CoreMessages.NODE_UNINSTALL_ERROR);
                throw e;
            }
            nodeUninstallFinishNotification(false);
            return result.toString();
        });
    }

    private String killVagrantProcess() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        SystemInfo systemInfo = new SystemInfo();
        OSProcess[] processes = systemInfo.getOperatingSystem().getProcesses(0, null);
        String procName = SystemUtils.IS_OS_WINDOWS ? "vagrant.exe" : "vagrant";
        List<OSProcess> vagrantProcesses = Arrays.stream(processes).filter(p -> procName.equals(p.getName()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(vagrantProcesses)) {
            Path javaPath = Paths.get(PropertiesHelper.javaHome);
            for (OSProcess proc : vagrantProcesses) {
                OSProcess parentProc = Arrays.stream(processes).filter(p -> p.getProcessID() == proc.getParentProcessID())
                        .findFirst().orElse(null);
                boolean needKill = false;
                if (isProcGexd(parentProc, javaPath)) {
                    needKill = true;
                } else if (parentProc != null) {
                    OSProcess grandParentProc = Arrays.stream(processes).filter(p -> p.getProcessID() == parentProc.getParentProcessID())
                            .findFirst().orElse(null);
                    needKill = isProcGexd(grandParentProc, javaPath);
                }

                if (needKill) {
                    List<String> cmd = SystemUtils.IS_OS_WINDOWS ? Commands.cmd("taskkill /f /t /pid " + proc.getProcessID())
                            : Commands.bash("kill -9 " + proc.getProcessID());
                    return ShellExecutor.executeCommandOutputWithLog(
                            ShellParameters.newBuilder(cmd).setPrintOutput(false)
                                    .setMessageError("Cannot kill process " + proc.getName() + ". PID: " + proc.getProcessID())
                                    .setMessageSuccess("Killed process " + proc.getName() + ". PID: " + proc.getProcessID()).
                                    setType(LogType.VAGRANT, LogType.VAGRANT_ERROR).build());
                }
            }
        }
        return null;
    }

    private static boolean isProcGexd(OSProcess process, Path javaPath) {
        return process != null && (SystemUtils.IS_OS_WINDOWS && "gexd.exe".equals(process.getName()) ||
                !SystemUtils.IS_OS_WINDOWS && StringUtils.isNotEmpty(process.getPath()) && Paths.get(process.getPath()).startsWith(javaPath));
    }
}
