package io.gex.core.shell;

import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static io.gex.core.DateConverter.TIMESTAMP_FORMAT;
import static io.gex.core.DateConverter.localDateTimeToString;
import static java.time.ZoneOffset.UTC;

public class Mounter {

    private final static LogWrapper logger = LogWrapper.create(Mounter.class);

    private static String getMountPointPath(String mountPoint) {
        return Paths.get("/", "Volumes", mountPoint + "_" + localDateTimeToString(LocalDateTime.now(), TIMESTAMP_FORMAT,
                UTC)).toString();
    }

    public static String mount(String image, String mountPoint) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (!SystemUtils.IS_OS_MAC) {
            throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.HARDWARE_INFO_ERROR);
        }
        String mountPointPath = getMountPointPath(mountPoint);
        List<String> cmd = Arrays.asList("/usr/bin/hdiutil", "mount", image, "-mountpoint",
                mountPointPath);
        ShellExecutor.executeCommand(ShellParameters.newBuilder(cmd).build());
        return mountPointPath;
    }

    public static void unmount(String mountPointPath) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (!SystemUtils.IS_OS_MAC) {
            throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.HARDWARE_INFO_ERROR);
        }
        List<String> cmd = Arrays.asList("/usr/bin/hdiutil", "unmount", mountPointPath);
        ShellExecutor.executeCommand(ShellParameters.newBuilder(cmd).build());
    }
}
