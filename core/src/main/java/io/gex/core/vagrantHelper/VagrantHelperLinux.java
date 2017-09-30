package io.gex.core.vagrantHelper;


import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;

import java.util.HashMap;
import java.util.Map;

public class VagrantHelperLinux extends VagrantHelper {

    private final static LogWrapper logger = LogWrapper.create(VagrantHelperLinux.class);

    @Override
    protected Map<String, String> getEnv() throws GexException {
        Map<String, String> env = new HashMap<>();
        env.put(Commands.VAGRANT_HOME, VagrantHelper.vagrantHome);
        return env;
    }

    @Override
    public boolean isInstalled() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.HARDWARE_INFO_ERROR);
    }

    @Override
    public boolean hasValidVersion() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.HARDWARE_INFO_ERROR);
    }

    @Override
    public DownloadFile download() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.HARDWARE_INFO_ERROR);
    }

    @Override
    public void install(DownloadFile downloadFile) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.HARDWARE_INFO_ERROR);
    }

    @Override
    public void addBox(String boxPath) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ShellExecutor.executeCommand(ShellParameters.newBuilder(Commands.bash(Commands.vagrantAddBox(boxPath))).setNewEnv(getEnv()).build());
    }

}
