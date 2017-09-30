package io.gex.core.vagrantHelper;

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
import java.util.HashMap;
import java.util.Map;

public class VagrantHelperMac extends VagrantHelper {

    private final static LogWrapper logger = LogWrapper.create(VagrantHelperMac.class);
    private final static String OLD_VAGRANT_PATH = "/usr/bin/vagrant";
    private final static String VAGRANT_PATH_MAC = "/usr/local/bin/vagrant";
    public static String pathWithVagrant = System.getenv().get(Commands.PATH) + ":" + "/usr/local/bin";

    @Override
    protected Map<String, String> getEnv() throws GexException {
        Map<String, String> env = new HashMap<>();
        env.put(Commands.VAGRANT_HOME, VagrantHelper.vagrantHome);
        env.put(Commands.PATH, pathWithVagrant);
        return env;
    }

    @Override
    public void addBox(String boxPath) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ShellExecutor.executeCommand(ShellParameters.newBuilder(Commands.bash(Commands.vagrantAddBox(boxPath))).setNewEnv(getEnv()).build());
    }

    @Override
    public boolean isInstalled() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        File file = new File(VAGRANT_PATH_MAC);
        if (file.exists()) {
            return true;
        }
        // old versions of Vagrant are placed into /usr/bin directory
        file = new File(OLD_VAGRANT_PATH);
        return file.exists();
    }

    @Override
    public boolean hasValidVersion() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        // old versions of Vagrant are placed into /usr/bin directory
        return !new File(OLD_VAGRANT_PATH).exists() && hasValidVersion(getVersion());
    }

    @Override
    public DownloadFile download() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        URL url;
        try {
            url = new URL(ServerPropertiesLevelApi.getProperty("vagrant_url_mac"));
        } catch (MalformedURLException e) {
            throw logger.logAndReturnException(CoreMessages.DOWNLOAD_FILE_ERROR, e, LogType.GET_PROPERTY_ERROR);
        }
        return new DownloadFile(url.toString(), "dmg");
    }

    @Override
    public void install(DownloadFile downloadFile) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String mountPointPath = null;
        try {
            mountPointPath = Paths.get(Mounter.mount(downloadFile.getFile().getAbsolutePath(), "gex_vagrant"),
                    "Vagrant.pkg").toString();
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
