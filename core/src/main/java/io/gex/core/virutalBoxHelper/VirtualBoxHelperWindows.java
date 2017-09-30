package io.gex.core.virutalBoxHelper;

import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.WindowsInstallInfoHelper;
import io.gex.core.api.ServerPropertiesLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class VirtualBoxHelperWindows extends VirtualBoxHelper {

    private final static LogWrapper logger = LogWrapper.create(VirtualBoxHelperWindows.class);
    public final static String VBOX_INSTALL_PATH = "VBOX_INSTALL_PATH";
    public final static String VBOX_MSI_INSTALL_PATH = "VBOX_MSI_INSTALL_PATH";
    public final static String VBOX_DEFAULT_INSTALL_PATH = "C:\\Program Files\\Oracle\\VirtualBox\\";
    public final static String DEFAULT_VBOX_USER_HOME = Paths.get(System.getProperty("user.home"), ".gex",
            "VirtualBox Vms").toString();

    public static void setMachineFolderProperty(String machineFolder) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        WindowsInstallInfoHelper.write("vbox_user_home", machineFolder, LogType.VBOX_MACHINE_FOLDER,
                CoreMessages.MACHINE_FOLDER_ERROR);
    }

    public static void setMachineFolderProperty() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        setMachineFolderProperty(DEFAULT_VBOX_USER_HOME);
    }

    @Override
    public boolean isInstalled() {
        logger.trace("Entered " + LogHelper.getMethodName());
        return StringUtils.isNotEmpty(getVBoxInstallPath());
    }

    private String getVBoxInstallPath() {
        String vBoxPath = StringUtils.isNotEmpty(System.getenv(VBOX_MSI_INSTALL_PATH)) ?
                System.getenv(VBOX_MSI_INSTALL_PATH) : System.getenv(VBOX_INSTALL_PATH);
        if (StringUtils.isEmpty(vBoxPath)) {
            vBoxPath = BaseHelper.getEnvVariableFromRegistry(VBOX_MSI_INSTALL_PATH);
            if (StringUtils.isEmpty(vBoxPath)) {
                vBoxPath = BaseHelper.getEnvVariableFromRegistry(VBOX_INSTALL_PATH);
            }
        }
        return vBoxPath;
    }

    private Path getVBoxManagePath() {
        return Paths.get(getVBoxInstallPath(), "VBoxManage.exe");
    }

    public void setMachineFolder(String path) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ShellExecutor.executeCommand(ShellParameters.newBuilder(Commands.exec("\"\"" + getVBoxManagePath().toString() +
                "\" setproperty machinefolder \"" + path + "\"\"")).setPrintOutput(false).build());
    }

    public boolean isVMPresent(String nodeName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Process p = ShellExecutor.getExecutionProcess(ShellParameters.newBuilder(Commands.exec("\"\"" + getVBoxManagePath().toString() +
                "\" list vms | find \"" + nodeName + "\"\"")).build());
        try {
            return p.waitFor() == 0;
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.SHELL_ERROR, e, LogType.VIRTUAL_BOX_ERROR);
        }
    }

    public String getMachineFolder() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String property = "Default machine folder:", def = "default";
        List<String> cmd = Commands.cmd("\"\"" + getVBoxManagePath().toString() +
                "\" list systemproperties | find \"" + property + "\"\"");
        Process p = ShellExecutor.getExecutionProcess(ShellParameters.newBuilder(cmd).build());
        try {
            InputStream in = p.getInputStream();
            if (in != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                String line = bufferedReader.readLine();
                if (StringUtils.isNotBlank(line)) {
                    logger.logInfo(line, LogType.VIRTUAL_BOX);
                    if (!line.contains(property)) {
                        return def;
                    }
                    line = line.replace(property, StringUtils.EMPTY).trim();
                    return line;
                }
                return def;
            }
            if (p.waitFor() != 0) {
                throw new InterruptedException();
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.SHELL_ERROR, e, LogType.VIRTUAL_BOX_ERROR);
        }
        throw logger.logAndReturnException(CoreMessages.GET_DEFAULT_MACHINE_FOLDER_ERROR, LogType.VIRTUAL_BOX_ERROR);
    }

    @Override
    public void checkVirtualBoxIsRunning() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String vboxPath = getVBoxInstallPath();
        if (StringUtils.isEmpty(vboxPath)) {
            throw logger.logAndReturnException(CoreMessages.VIRTUAL_BOX_NOT_INSTALLED, LogType.VIRTUAL_BOX_ERROR);
        }
        String output = ShellExecutor.executeCommandOutput((ShellParameters.newBuilder(Commands.exec("\"\"" +
                Paths.get(vboxPath, "VBoxManage.exe").toString() + "\" -version\""))).build());
        if(output.contains("The character device /dev/vboxdrv does not exist.")){
            throw logger.logAndReturnException("The character device /dev/vboxdrv does not exist.", LogType.VIRTUAL_BOX_ERROR);
        }
    }

    @Override
    public String getVersion() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String vboxPath = getVBoxInstallPath();
        if (StringUtils.isEmpty(vboxPath)) {
            throw logger.logAndReturnException(CoreMessages.VIRTUAL_BOX_NOT_INSTALLED, LogType.VIRTUAL_BOX_ERROR);
        }
        String version = ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.exec("\"\"" +
                Paths.get(vboxPath, "VBoxManage.exe").toString() + "\" -version\"")).
                setMessageSuccess("Getting Virtual Box version executed successfully").
                setMessageError("Getting  Virtual Box version executed with error").setPrintOutput(false).build());

        return StringUtils.trim(version);
    }

    //call isInstalled method first
    @Override
    public boolean hasValidVersion() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return hasValidVersion(getVersion());
    }

    @Override
    public DownloadFile download() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            URL url = new URL(ServerPropertiesLevelApi.getProperty("virtualbox_url_windows"));
            return new DownloadFile(url.toString(), "exe");
        } catch (MalformedURLException e) {
            throw logger.logAndReturnException(CoreMessages.DOWNLOAD_FILE_ERROR, e, LogType.GET_PROPERTY_ERROR);
        }
    }

    @Override
    public void install(DownloadFile downloadFile) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String line = StringUtils.EMPTY;
        File elevatePath = new File(WindowsInstallInfoHelper.getInstallationPath() + "/usr/lib/gex/elevate.exe");
        List<String> cmd = Commands.cmd("\"" + elevatePath.getAbsolutePath() + "\" -w \"" +
                downloadFile.getFile().getAbsolutePath() + "\" -l --silent -msiparams ALLUSERS=1 VBOX_INSTALLDESKTOPSHORTCUT=0" +
                " VBOX_INSTALLQUICKLAUNCHSHORTCUT=0 VBOX_REGISTERFILEEXTENSIONS=0 VBOX_START=0 && echo %errorlevel%");
        Process p = ShellExecutor.getExecutionProcess(ShellParameters.newBuilder(cmd).build());
        try {
            InputStream in = p.getInputStream();
            byte[] buffer = new byte[512];
            if (in != null) {
                int i;
                while ((i = in.read(buffer, 0, 512)) >= 0) {
                    if (i > 0) {
                        line += new String(buffer, 0, i);
                    }
                }
                if (StringUtils.isNotBlank(line)) {
                    logger.logInfo(line, LogType.VIRTUAL_BOX);
                } else {
                    throw new InterruptedException();
                }
                if (!line.trim().equals("0")) {
                    throw logger.logAndReturnException(CoreMessages.INSTALL_VIRTUAL_BOX_ERROR + " Error: " + line, LogType.VIRTUAL_BOX_ERROR);
                }
            }
            if (p.waitFor() != 0) {
                throw new InterruptedException();
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.getExecutionCommandMessage(BaseHelper.listToString(cmd)) +
                    ":\n" + line, e, LogType.SHELL_ERROR);
        } finally {
            downloadFile.deleteFile();
        }
    }
}
