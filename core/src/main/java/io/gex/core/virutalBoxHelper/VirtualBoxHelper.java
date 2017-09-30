package io.gex.core.virutalBoxHelper;

import io.gex.core.*;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.List;

public abstract class VirtualBoxHelper {

    private final static LogWrapper logger = LogWrapper.create(VirtualBoxHelper.class);
    private static final Integer MAJOR = 4;
    private static final Integer MINOR = 3;
    private static final Integer PATCH = 0;
    public static final String VERSION = MAJOR + "." + MINOR + "." + PATCH;

    public abstract boolean isInstalled();

    public abstract void checkVirtualBoxIsRunning() throws GexException;

    public abstract String getVersion() throws GexException;

    public abstract boolean hasValidVersion() throws GexException;

    public abstract DownloadFile download() throws GexException;

    public abstract void install(DownloadFile downloadFile) throws GexException;

    public static VirtualBoxHelper constructVirtualBoxHelper() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (SystemUtils.IS_OS_WINDOWS) {
            return new VirtualBoxHelperWindows();
        } else if (SystemUtils.IS_OS_MAC) {
            return new VirtualBoxHelperMac();
        } else if (SystemUtils.IS_OS_LINUX) {
            return new VirtualBoxHelperLinux();
        }
        throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.HARDWARE_INFO_ERROR);
    }

    public static void checkRunningVMs() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        List<String> cmd;
        if (SystemUtils.IS_OS_LINUX) {
            // VirtualBox installation only for Mac and Windows
            return;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            cmd = Commands.cmd(System.getenv("windir") + "\\system32\\tasklist.exe | find \"VBoxHeadless.exe\"");
        } else {
            cmd = Commands.bash("ps aux | grep [V]Box");
        }
        Process p = ShellExecutor.getExecutionProcess(ShellParameters.newBuilder(cmd).build());
        try {
            if (p.waitFor() == 0) {
                throw logger.logAndReturnException(CoreMessages.RUNNING_VM, LogType.VIRTUAL_BOX_ERROR);
            }
        } catch (InterruptedException e) {
            throw logger.logAndReturnException(CoreMessages.SHELL_ERROR, e, LogType.VIRTUAL_BOX_ERROR);
        }
    }

    protected boolean hasValidVersion(String version) {
        logger.trace("Entered " + LogHelper.getMethodName());
        return BaseHelper.compareAppVersions(version, VERSION) >= 0;
    }

    public void checkDKMS() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (getVersion().contains("linux-headers-generic")) {
            throw logger.logAndReturnException(CoreMessages.DKMS_ERROR, LogType.VIRTUAL_BOX_ERROR);
        }
    }

    public static String getVBoxLog() {
        logger.trace("Entered " + LogHelper.getMethodName());
        int linesNumber = 256;
        StringBuilder output = new StringBuilder();
        try {
            String nodeName = PropertiesHelper.node.getProps().getNodeName();
            String clusterName = PropertiesHelper.node.getProps().getClusterName();
            if (StringUtils.isAnyBlank(clusterName, nodeName)) {
                logger.logError(CoreMessages.VIRTUAL_BOX_NAME_ERROR, LogType.VIRTUAL_BOX_LOG_ERROR);
                return null;
            }
            String virtualBoxHome;
            if (SystemUtils.IS_OS_WINDOWS) {
                String vBoxUserHome = null;
                try {
                    vBoxUserHome = WindowsInstallInfoHelper.read("vbox_user_home");
                } catch (GexException e) {
                    logger.logError(e.getMessage(), e, LogType.VIRTUAL_BOX_LOG_ERROR);
                }
                virtualBoxHome = StringUtils.isNotBlank(vBoxUserHome) ? vBoxUserHome :
                        Paths.get(PropertiesHelper.userHome, ".gex", "VirtualBox VMs").toString();
            } else {
                virtualBoxHome = Paths.get(PropertiesHelper.userHome, "VirtualBox VMs").toString();
            }
            if (StringUtils.isBlank(virtualBoxHome)) {
                logger.logError(CoreMessages.GET_VIRTUAL_BOX_HOME_ERROR, LogType.VIRTUAL_BOX_LOG_ERROR);
                return null;
            }
            File file = new File(Paths.get(virtualBoxHome, "gex_" + clusterName
                    + "_" + nodeName, "Logs", "VBox.log").toString());
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                FileChannel channel = fileInputStream.getChannel();
                ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                buffer.position((int) channel.size());
                int count = 0;
                StringBuilder builder = new StringBuilder();
                for (long i = channel.size() - 1; i >= 0; i--) {
                    char c = (char) buffer.get((int) i);
                    builder.append(c);
                    if (c == '\n') {
                        if (count == linesNumber) break;
                        count++;
                        builder.reverse();
                        output.append(builder.reverse().toString());
                        builder = new StringBuilder();
                    }
                }
                channel.close();
            } else {
                logger.logWarn(CoreMessages.NO_VIRTUAL_BOX_LOG_FILE, LogType.VIRTUAL_BOX_LOG_ERROR);
            }
        } catch (Exception e) {
            logger.logError(CoreMessages.VIRTUAL_BOX_LOG_FILE_ERROR + e.getMessage(), e, LogType.VIRTUAL_BOX_LOG_ERROR);
        }
        return output.reverse().toString();
    }

}
