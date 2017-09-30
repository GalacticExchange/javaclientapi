package io.gex.core;

import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;

public class WindowsInstallInfoHelper {

    private final static LogWrapper logger = LogWrapper.create(PropertiesHelper.class);
    public final static String WINDOWS_INSTALL_INFO_PATH = "install.info";


    // should be present
    public static String getInstallationPath() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String installationPath = read("installation_path");
        if (StringUtils.isBlank(installationPath)) {
            throw logger.logAndReturnException(CoreMessages.WINDOWS_INSTALL_INFO_FILE_ERROR, LogType.EMPTY_PROPERTY_ERROR);
        }
        return installationPath;
    }

    public static String read(String name) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (!SystemUtils.IS_OS_WINDOWS) {
            throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.HARDWARE_INFO_ERROR);
        }
        Properties properties = new Properties();
        File file = Paths.get(System.getenv("ProgramData"), ".gex", WINDOWS_INSTALL_INFO_PATH).toFile();
        try {
            try (FileInputStream fileStream = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fileStream, StandardCharsets.UTF_8)) {
                properties.load(inputStreamReader);
                String value = properties.getProperty(name);
                if (StringUtils.isNotBlank(value)) {
                    return value.trim();
                }
                return null;
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.WINDOWS_INSTALL_INFO_FILE_ERROR, e, LogType.EMPTY_PROPERTY_ERROR);
        }
    }

    public static void write(String propertyName, String propertyValue, LogType type, String message) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            Properties properties = new Properties();
            File file = Paths.get(System.getenv("ProgramData"), ".gex", WindowsInstallInfoHelper.WINDOWS_INSTALL_INFO_PATH).toFile();
            try (FileInputStream fileStream = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fileStream, StandardCharsets.UTF_8)) {
                properties.load(inputStreamReader);
            }
            properties.setProperty(propertyName, propertyValue);
            try (FileOutputStream fileStream = new FileOutputStream(file);
                 OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8)) {
                properties.store(outputStreamWriter, null);
            }
        } catch (IOException e) {
            throw logger.logAndReturnException(message, e, type);
        }
    }


}
