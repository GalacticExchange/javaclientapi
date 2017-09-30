package io.gex.core.vagrantHelper;

import io.gex.core.BaseHelper;
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
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public abstract class VagrantHelper {

    private final static LogWrapper logger = LogWrapper.create(VagrantHelper.class);
    private static final Integer MAJOR = 1;
    private static final Integer MINOR = 7;
    private static final Integer PATCH = 0;
    public static final String VERSION = MAJOR + "." + MINOR + "." + PATCH;
    public static String vagrantHome;

    protected abstract Map<String, String> getEnv() throws GexException;

    public abstract void addBox(String boxPath) throws GexException;

    public abstract boolean isInstalled() throws GexException;

    public abstract boolean hasValidVersion() throws GexException;

    public String getVersion() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        List<String> cmd = SystemUtils.IS_OS_WINDOWS ? Commands.cmd(Commands.VAGRANT_V) : Commands.bash(Commands.VAGRANT_V);
        String vagrantVersion = ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(cmd).
                setNewEnv(getEnv()).setMessageSuccess(Commands.VAGRANT_V + " executed successfully").
                setMessageError(Commands.VAGRANT_V + " executed with error").setPrintOutput(false).build());
        if (StringUtils.isNotBlank(vagrantVersion)) {
            String[] parts = vagrantVersion.split(" ");
            if (parts.length == 2 && parts[0].toLowerCase().equals("vagrant")) {
                return parts[1].trim();
            }
        }
        throw logger.logAndReturnException(CoreMessages.VAGRANT_VERSION_PARSE_ERROR + vagrantVersion, LogType.VAGRANT_ERROR);
    }

    public abstract DownloadFile download() throws GexException;

    public abstract void install(DownloadFile downloadFile) throws GexException;

    public static VagrantHelper constructVagrantHelper() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (SystemUtils.IS_OS_LINUX) {
            return new VagrantHelperLinux();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return new VagrantHelperWindows();
        } else if (SystemUtils.IS_OS_MAC) {
            return new VagrantHelperMac();
        }
        throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.HARDWARE_INFO_ERROR);
    }

    public void vagrantOldBoxesCheck() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String result = ShellExecutor.executeCommandOutput(ShellParameters.newBuilder(SystemUtils.IS_OS_WINDOWS ?
                Commands.cmd(Commands.VAGRANT_BOX_LIST) : Commands.bash(Commands.VAGRANT_BOX_LIST)).setNewEnv(getEnv()).build());
        if (result.contains(Commands.BOX_NAME)) {
            throw logger.logAndReturnException(CoreMessages.VAGRANT_BOX_PRESENT +
                            Paths.get(VagrantHelper.vagrantHome, "/boxes", Commands.BOX_NAME_SLASH).toString(), LogType.VAGRANT_ERROR);
        }
    }

    protected boolean hasValidVersion(String version) {
        logger.trace("Entered " + LogHelper.getMethodName());
        return BaseHelper.compareAppVersions(version, VERSION) >= 0;
    }
}
