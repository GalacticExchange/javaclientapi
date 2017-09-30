package io.gex.core.virutalBoxHelper;

import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.PropertiesHelper;
import io.gex.core.api.ServerPropertiesLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.shell.Commands;
import io.gex.core.shell.Mounter;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

public class VirtualBoxHelperMac extends VirtualBoxHelper {

    private final static LogWrapper logger = LogWrapper.create(VirtualBoxHelperMac.class);
    private final static String V_BOX_MANAGE = "/usr/local/bin/VBoxManage";

    @Override
    public boolean isInstalled() {
        logger.trace("Entered " + LogHelper.getMethodName());
        File file = new File(V_BOX_MANAGE);
        return file.exists();
    }

    @Override
    public void checkVirtualBoxIsRunning() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String output = ShellExecutor.executeCommandOutput(ShellParameters.newBuilder(Commands.bash(V_BOX_MANAGE + " -version")).build());
        if(output.contains("The character device /dev/vboxdrv does not exist.")){
            throw logger.logAndReturnException("The character device /dev/vboxdrv does not exist.", LogType.VIRTUAL_BOX_ERROR);
        }
    }

    @Override
    public String getVersion() throws GexException {
        String version = ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.bash(V_BOX_MANAGE +
                " -version")).setMessageSuccess("Getting Virtual Box version executed successfully").setMessageError(
                "Getting  Virtual Box version executed with error").setPrintOutput(false).build());
        return StringUtils.trim(version);
    }

    @Override
    public boolean hasValidVersion() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return hasValidVersion(getVersion());
    }

    @Override
    public DownloadFile download() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            URL url = new URL(ServerPropertiesLevelApi.getProperty("virtualbox_url_mac"));
            return new DownloadFile(url.toString(), "dmg");
        } catch (MalformedURLException e) {
            throw logger.logAndReturnException(CoreMessages.DOWNLOAD_FILE_ERROR, e, LogType.GET_PROPERTY_ERROR);
        }
    }

    @Override
    public void install(DownloadFile downloadFile) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String mountPointPath = null;
        try {
            mountPointPath = Paths.get(Mounter.mount(downloadFile.getFile().getAbsolutePath(), "gex_virtual_box"),
                    "VirtualBox.pkg").toString();
            String command = PropertiesHelper.isUI() ? "/Library/Application\\ Support/gex/exec_with_exit_code.sh " +
                    "\"/usr/sbin/installer -allowUntrusted -verboseR -pkg " + mountPointPath + " -target /\"" :
                    "sudo /usr/sbin/installer -allowUntrusted -verboseR -pkg " + mountPointPath + " -target /";
            ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.bash(command)).build());
        } finally {
            try {
                if (StringUtils.isNotBlank(mountPointPath) && new File(mountPointPath).exists()) {
                    Mounter.unmount(mountPointPath);
                }
            } finally {
                downloadFile.deleteFile();
            }
        }
    }
}
