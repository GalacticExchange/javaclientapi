package io.gex.core.vagrantHelper;


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

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VagrantHelperWindows extends VagrantHelper {

    private final static LogWrapper logger = LogWrapper.create(VagrantHelperWindows.class);

    @Override
    protected Map<String, String> getEnv() throws GexException {
        Map<String, String> env = new HashMap<>();
        env.put(Commands.PATH_WIN, getPath());
        env.put(Commands.VAGRANT_HOME, VagrantHelper.vagrantHome);
        return env;
    }

    public static String getPath() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String path = System.getenv(Commands.PATH_WIN);
        String vagrant_check = "vagrant\\bin";
        if (!path.toLowerCase().contains(vagrant_check)) {
            if (isVagrantInPath()) {
                String vagrant = "C:\\HashiCorp\\Vagrant\\bin";
                path = !StringUtils.endsWith(path, ";") ? path + ";" + vagrant : path + vagrant;
            } else {
                throw logger.logAndReturnException(CoreMessages.VAGRANT_NOT_INSTALLED, LogType.VAGRANT_ERROR);
            }
        }
        return path;
    }

    private static Boolean isVagrantInPath() {
        logger.trace("Entered " + LogHelper.getMethodName());
        String path = BaseHelper.getEnvVariableFromRegistry("Path");
        return StringUtils.isNotBlank(path) && path.toLowerCase().contains("vagrant\\bin");
    }

    @Override
    public void addBox(String boxPath) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        URI boxUri = new File(boxPath).toURI(); //https://github.com/mitchellh/vagrant/issues/5288 because we supports vagrant 1.7
        ShellExecutor.executeCommand(ShellParameters.newBuilder(Commands.cmd(Commands.vagrantAddBox(boxUri.toString()))).setNewEnv(getEnv()).build());
    }

    @Override
    public boolean isInstalled() {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            ShellExecutor.executeCommand(ShellParameters.newBuilder(Commands.cmd(Commands.VAGRANT_V)).
                    setNewEnv(getEnv()).setLogOutput(false).setPrintOutput(false).build());
            return true;
        } catch (GexException e) {
            return false;
        }
    }

    @Override
    public boolean hasValidVersion() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return hasValidVersion(getVersion());
    }

    @Override
    public DownloadFile download() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        URL url;
        try {
            url = new URL(ServerPropertiesLevelApi.getProperty("vagrant_url_windows"));
        } catch (MalformedURLException e) {
            throw logger.logAndReturnException(CoreMessages.DOWNLOAD_FILE_ERROR, e, LogType.GET_PROPERTY_ERROR);
        }
        return new DownloadFile(url.toString(), "msi");
    }

    @Override
    public void install(DownloadFile downloadFile) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String line = StringUtils.EMPTY;
        File elevatePath = new File(WindowsInstallInfoHelper.getInstallationPath() + "/usr/lib/gex/elevate.exe");
        List<String> cmd = Commands.cmd("\"" + elevatePath.getAbsolutePath() + "\" -w C:\\Windows\\System32\\msiexec.exe /i \"" +
                downloadFile.getFile().getAbsolutePath() + "\" /qn /norestart /l* " +
                Paths.get(System.getenv("ProgramData"), ".gex", "vagrant_install.log").toString() + " && echo %errorlevel%");
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
                    logger.logInfo(line, LogType.VAGRANT);
                } else {
                    throw new InterruptedException();
                }
                if (!line.trim().equals("0")) {
                    throw logger.logAndReturnException(CoreMessages.INSTALL_VAGRANT_ERROR + " Error: " + line, LogType.VAGRANT_ERROR);
                }
            }
            if (p.waitFor() != 0) {
                throw new InterruptedException();
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(
                    CoreMessages.getExecutionCommandMessage(BaseHelper.listToString(cmd)) + ":\n" + line, e, LogType.SHELL_ERROR);
        } finally {
            downloadFile.deleteFile();
        }

    }
}
