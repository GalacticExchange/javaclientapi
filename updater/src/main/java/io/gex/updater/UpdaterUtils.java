package io.gex.updater;

import io.gex.core.PropertiesHelper;
import io.gex.core.exception.ExceptionHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogSource;
import io.gex.core.log.LogType;
import io.gex.core.model.ApplicationMode;
import io.gex.core.propertiesHelper.UserPropertiesHelper;
import io.gex.core.shell.Commands;
import io.gex.core.shell.Mounter;
import io.gex.core.shell.ShellExecutor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

public class UpdaterUtils {

    private final static Logger logger = LogManager.getLogger(UpdaterUtils.class);

    public static InputStream getResourceAsStream(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

    public static void update(String updatePath) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ShellExecutor.executeCommand(getCommand(updatePath), LogSource.APP);
    }

    private static List<String> getCommand(String updatePath) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if ((new File(updatePath).exists())) {
            if (SystemUtils.IS_OS_MAC) {
                String mountPointPath = Mounter.mount(updatePath, FilenameUtils.removeExtension(FilenameUtils.getName(updatePath)));
                return Commands.osascript("do shell script \"/usr/sbin/installer -pkg \\\"" +
                        Paths.get(mountPointPath, "ClusterGX.pkg") + "\\\" -target /\" with administrator privileges");
            } else {
                return Commands.cmd(updatePath +
                        " -q -console -overwrite -Dinstall4j.logToStderr=true -Dinstall4j.detailStdout=true");
            }
        } else {
            throw ExceptionHelper.logAndReturnException(logger,new GexException(UpdaterMessages.UPDATE_FILE_NOT_FOUND),
                    LogSource.APP, LogType.SHELL_ERROR);

        }
    }

    public static String getLogFilePath() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        RollingFileAppender appender = config.getAppender("RollingFile");

        return appender.getFileName();
    }

    public static void init() {
        logger.trace("Entered " + LogHelper.getMethodName());
        PropertiesHelper.mode = ApplicationMode.UPDATE;
        try {
            PropertiesHelper.readProperties();
            UserPropertiesHelper.getPropertiesDirectory();
        } catch (GexException e) {
            logger.error(e.getMessage());
        }
    }

}
