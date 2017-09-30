package io.gex.core.virutalBoxHelper;


import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;
import org.apache.commons.lang3.StringUtils;

public class VirtualBoxHelperLinux extends VirtualBoxHelper {

    private final static LogWrapper logger = LogWrapper.create(VirtualBoxHelperLinux.class);

    @Override
    public boolean isInstalled() {
        logger.trace("Entered " + LogHelper.getMethodName());
        return true;
    }

    @Override
    public void checkVirtualBoxIsRunning() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String output = ShellExecutor.executeCommandOutput(ShellParameters.newBuilder(Commands.bash("vboxmanage -version")).build());
        if(output.contains("The character device /dev/vboxdrv does not exist.")){
            throw logger.logAndReturnException("The character device /dev/vboxdrv does not exist.", LogType.VIRTUAL_BOX_ERROR);
        }
    }

    @Override
    public String getVersion() throws GexException {
        String version = ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.bash("vboxmanage -version")).
                setMessageSuccess("Getting Virtual Box version executed successfully").
                setMessageError("Getting  Virtual Box version executed with error").setPrintOutput(false).build());
        return StringUtils.trim(version);
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
}
