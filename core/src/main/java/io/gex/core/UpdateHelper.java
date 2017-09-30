package io.gex.core;


import io.gex.core.api.FileLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.ApplicationMode;
import io.gex.core.shell.Commands;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class UpdateHelper {

    private final static LogWrapper logger = LogWrapper.create(UpdateHelper.class);

    public static void startUpdater(String updaterPath, String distributionPath, ApplicationMode mode) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        OSCheck();
        if (StringUtils.isBlank(updaterPath)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_UPDATER, LogType.EMPTY_PROPERTY_ERROR);
        }
        if (StringUtils.isBlank(distributionPath)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_DISTRIBUTION, LogType.EMPTY_PROPERTY_ERROR);
        }
        if (mode != ApplicationMode.CLI && mode != ApplicationMode.UI) {
            throw logger.logAndReturnException(CoreMessages.INVALID_APPLICATION_MODE, LogType.UPDATER_ERROR);
        }
        UpdateHelper.copyTmpJava();
        List<String> cmd;
        if (SystemUtils.IS_OS_MAC) {
            FileLevelApi.setDirectoryPermissions(getTmpJavaPath());
            FileLevelApi.setPermissions(updaterPath, 700);
            FileLevelApi.setPermissions(distributionPath, 700);
            if (mode == ApplicationMode.CLI) {
                cmd = Commands.osascript("tell application \"Terminal\" to do script \"" + Paths.get(getTmpJavaPath(),
                        "Contents", "Home", "bin", "java").toString() + " -jar -Dprism.order=sw "
                        + updaterPath + " " + mode + " " + distributionPath + "\"");
            } else {
                cmd = Commands.bash(Paths.get(getTmpJavaPath(), "Contents", "Home", "bin", "java").toString()
                        + " -jar -Dprism.order=sw " + updaterPath + " " + mode + " " +
                        distributionPath);
            }
        } else {
            String javaPath = Paths.get(getTmpJavaPath(), "bin", (mode == ApplicationMode.CLI ? "java.exe" : "javaw.exe")).toString();
            cmd = Commands.cmd(mode == ApplicationMode.CLI ? "start cmd.exe /k " : StringUtils.EMPTY + javaPath + " -jar " +
                    updaterPath + " " + mode + " " + distributionPath);
        }
        logger.logInfo(CoreMessages.EXECUTE_COMMAND + BaseHelper.listToString(cmd), LogType.UPDATER);
        try {
            new ProcessBuilder(cmd).start();
        } catch (Throwable e) {
            throw logger.logAndReturnException(CoreMessages.UPDATER_START_ERROR, e, LogType.UPDATER_ERROR);
        }
    }

    private static String getTmpJavaPath() {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (SystemUtils.IS_OS_MAC) {
            return Paths.get(FileUtils.getTempDirectoryPath(), "gex", "java").toString();
        } else {
            return Paths.get(FileUtils.getTempDirectoryPath(), "gex", "jre").toString();
        }
    }

    private static void copyTmpJava() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        File tmpDir = Paths.get(FileUtils.getTempDirectoryPath(), "gex").toFile();
        try {
            FileUtils.copyDirectoryToDirectory(new File(PropertiesHelper.javaHome), tmpDir);
        } catch (IOException e) {
            throw logger.logAndReturnException(CoreMessages.JAVA_COPY_ERROR, e, LogType.FILE_ERROR);
        }
    }

    private static void OSCheck() throws GexException {
        if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC) {
            throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.HARDWARE_INFO_ERROR);
        }
    }
}
