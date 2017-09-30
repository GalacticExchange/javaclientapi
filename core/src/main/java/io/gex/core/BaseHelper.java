package io.gex.core;


import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BaseHelper {

    private final static LogWrapper logger = LogWrapper.create(BaseHelper.class);

    private static Charset consoleCharset;

    public static boolean isKDE() {
        return "KDE".equalsIgnoreCase(System.getenv("XDG_CURRENT_DESKTOP"));
    }

    public static boolean isXfce() {
        return "XFCE".equalsIgnoreCase(System.getenv("XDG_CURRENT_DESKTOP"));
    }

    public static boolean isLXDE() {
        return "LXDE".equalsIgnoreCase(System.getenv("XDG_CURRENT_DESKTOP"));
    }

    @SuppressWarnings("unused")
    public static boolean isRoot() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
            try {
                Process p = Runtime.getRuntime().exec("id -u");
                InputStream in = p.getInputStream();
                if (in != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = bufferedReader.readLine();
                    logger.logInfo(line, LogType.GENERAL);
                    if (line.trim().equals("0"))
                        return true;
                } else {
                    throw new InterruptedException();
                }
                if (p.waitFor() != 0)
                    throw new InterruptedException();
            } catch (Exception e) {
                throw logger.logAndReturnException(CoreMessages.ACCOUNT_INFO_ERROR, e, LogType.HARDWARE_INFO_ERROR);
            }
        } else {
            throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.HARDWARE_INFO_ERROR);
        }
        return false;
    }

    public static boolean isCentos() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (!SystemUtils.IS_OS_LINUX) {
            return false;
        }
        try {
            return  FileUtils.readFileToString(new File("/etc/os-release"), UTF_8).toLowerCase().contains("centos");
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.OS_INFO_ERROR, e, LogType.HARDWARE_INFO_ERROR);
        }
    }

    public static boolean checkRootPassword(String password) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            ShellExecutor.executeCommand(ShellParameters.newBuilder(Commands.bash("echo " + password +
                            " | sudo -S sleep 0.01")).setPrintCommand(false).build());
        } catch (GexException e) {
            return false;
        }
        return true;
    }

    public static String trimAndRemoveSubstring(String target, String regex) throws GexException {
        try {
            return target.trim().replaceAll("(?i)" + regex, StringUtils.EMPTY);
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.INVALID_PARAMETER + regex, LogType.PARSE_ERROR);
        }
    }

    public static String readFileToString(URL url) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ConnectionChecker.checkConnectionToFile(url);
        try {
            String result = StringUtils.trimToNull(IOUtils.toString(url.openStream(), StandardCharsets.UTF_8));
            if (StringUtils.isBlank(result)) {
                throw new IOException();
            }
            return result;
        } catch (IOException e) {
            throw logger.logAndReturnException(CoreMessages.replaceTemplate(CoreMessages.READING_FROM_RESOURCE_ERROR,
                    url.toString()), e, LogType.READ_INPUT_STREAM_ERROR);
        }
    }

    public static String listToString(List<String> list) {
        return list.stream().collect(Collectors.joining(StringUtils.SPACE));
    }

    public static Charset getConsoleEncoding() {
        if (consoleCharset == null) {
            try {
                if (SystemUtils.IS_OS_WINDOWS) {
                    String charset = ShellExecutor.executeCommandOutputNoEncoding(
                            ShellParameters.newBuilder(Commands.cmd("CHCP")).build());
                    String[] words = charset.trim().split("\\s+");
                    consoleCharset = Charset.forName(words[words.length - 1]);
                } else {
                    consoleCharset = Charset.forName(StringUtils.trim(ShellExecutor
                            .executeCommandOutputNoEncoding(ShellParameters.newBuilder(
                                    Commands.bash("locale charmap")).build())));
                }
            } catch (Exception e) {
                logger.logError(e,  LogType.SHELL_ERROR);
                consoleCharset = Charset.defaultCharset();
            }
        }
        return consoleCharset;
    }

    public static long getCRC32Checksum(File file) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Checksum checksum = new CRC32();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[4096];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                checksum.update(byteArray, 0, bytesCount);
            }
        } catch (Exception e) {
            throw logger.logAndReturnException("Error creating checksum for file: " + file.getAbsolutePath() + ". "
                    + e.getMessage(), e, LogType.CHECKSUM_ERROR);
        }
        return checksum.getValue();
    }

    public static String getEnvVariableFromRegistry(String variable) {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment", variable);
        } catch (Win32Exception e) {
            return null;
        }
    }

    public static int compareAppVersions(String version1, String version2) {
        String[] version1Arr = StringUtils.split(version1, ".");
        String[] version2Arr = StringUtils.split(version2, ".");
        int maxLength = Math.max(version1Arr.length, version2Arr.length);

        for (int i = 0; i < maxLength; i++) {
            try {
                int version1Part = i < version1Arr.length ? Integer.parseInt(version1Arr[i]) : 0;
                int version2Part = i < version2Arr.length ? Integer.parseInt(version2Arr[i]) : 0;
                if (version1Part < version2Part) {
                    return -1;
                } else if (version1Part > version2Part) {
                    return 1;
                }
            } catch (NumberFormatException ex) {
                String version1Part = i < version1Arr.length ? version1Arr[i] : "";
                String version2Part = i < version2Arr.length ? version2Arr[i] : "";
                int res = StringUtils.compare(version1Part, version2Part);
                if (res > 0) {
                    return 1;
                } else if (res < 0) {
                    return -1;
                }
            }
        }
        return 0;
    }
}
