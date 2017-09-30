package io.gex.core;

import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.log.SlackLogger;
import io.gex.core.model.ApplicationMode;
import io.gex.core.model.HostType;
import io.gex.core.propertiesHelper.GexdPropertiesHelper;
import io.gex.core.propertiesHelper.NodePropertiesHelper;
import io.gex.core.propertiesHelper.UserPropertiesHelper;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;
import io.gex.core.vagrantHelper.VagrantHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesHelper {

    private final static LogWrapper logger = LogWrapper.create(PropertiesHelper.class);
    public final static String VERSION = "0.10.22";

    public final static String PROPERTIES_FILE_NAME = "config.properties";
    private final static String TMP_PROPERTIES_PATH = "/etc/gex/";
    public final static String PROXY_PROP_NAME = "proxy";
    public final static String RABBIT_HOST_PROP_NAME = "rabbitHost";
    public final static String RABBIT_PORT_PROP_NAME = "rabbitPort";
    public final static String HOST_TYPE_PROP_NAME = "hostType";
    private final static String API_URL_PROP_NAME = "apiUrl";
    private final static String NODE_PROP_NAME = "nodePropertiesFile";
    private final static String USER_PROP_NAME = "userPropertiesFile";
    public final static String WEBPROXY_PROP_NAME = "webproxy";
    private final static String AGENT_LOG_NAME = "gex_agent_log.log";
    private final static String DEFAULT_API_URL = "http://api.galacticexchange.io";

    public static Properties properties = new Properties();
    public static String apiUrl = DEFAULT_API_URL;
    public static String javaHome;
    public static String userHome;
    public static String agentLogPath;
    public static String propertiesPath;
    public static ApplicationMode mode;
    private static HostType hostType = HostType.VIRTUALBOX;
    public static Integer DEFAULT_WEB_SERVER_PORT = 48746;
    private static String windowsPath;
    public static String nodeConfig;
    public static String tmpDir;

    public static String nodePropertiesFile = "node.json";
    public static String userPropertiesFile = "user.json";
    public static String gexdPropertiesFile = "gexd.json";
    public static NodePropertiesHelper node;
    public static UserPropertiesHelper user;
    public static GexdPropertiesHelper gexd;

    public static boolean isHostVirtual() {
        return hostType == HostType.VIRTUALBOX;
    }

    public static boolean isCLI() {
        return mode == ApplicationMode.CLI;
    }

    public static boolean isService() {
        return mode == ApplicationMode.SERVICE;
    }

    public static boolean isUI() {
        return mode == ApplicationMode.UI;
    }

    private static String getWindowsPath() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(windowsPath)) {
            windowsPath = WindowsInstallInfoHelper.read("installation_path");
            if (StringUtils.isBlank(windowsPath)) {
                logger.logInfo(CoreMessages.replaceTemplate(CoreMessages.FILE_NOT_FOUND,
                        WindowsInstallInfoHelper.WINDOWS_INSTALL_INFO_PATH), LogType.GENERAL);
                windowsPath = Paths.get(System.getenv("ProgramFiles"), "gex").toString();
            }
        }
        return windowsPath;
    }

    private static void setBaseEnvironment() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        userHome = System.getProperty("user.home");
        GsonLayout.slackLogger = new SlackLogger(apiUrl.equals(DEFAULT_API_URL));
        if (SystemUtils.IS_OS_WINDOWS) {
            javaHome = Paths.get(getWindowsPath(), "jre").toString();
            agentLogPath = Paths.get(System.getenv("ProgramData"), ".gex", AGENT_LOG_NAME).toString();
            if (isHostVirtual() && isService()) {
                userHome = WindowsInstallInfoHelper.read("user_home");
                if (StringUtils.isBlank(userHome)) {
                    throw logger.logAndReturnException(CoreMessages.WINDOWS_INSTALL_INFO_FILE_ERROR, LogType.PROPERTIES_ERROR);
                }
            }
        } else {
            javaHome = SystemUtils.IS_OS_MAC ?
                    Paths.get("/", "Library", "Application Support", "gex", "java").toString() :
                    Paths.get("/", "usr", "lib", "gex", "java").toString();
            agentLogPath = Paths.get(userHome, ".gex", AGENT_LOG_NAME).toString();
        }
    }

    public static void setEnvironment() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        nodeConfig = Paths.get(userHome, ".gex", "node").toString();
        tmpDir = System.getProperty("java.io.tmpdir");
        if (isHostVirtual()) {
            VagrantHelper.vagrantHome = getVagrantHome();
            logger.logInfo("VAGRANT_HOME=" + VagrantHelper.vagrantHome, LogType.VAGRANT);
        }
    }

    private static void getPropertiesDirectory() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String path = SystemUtils.IS_OS_WINDOWS ? Paths.get(getWindowsPath(), TMP_PROPERTIES_PATH).toString() :
                TMP_PROPERTIES_PATH;
        if (new File(path, PROPERTIES_FILE_NAME).exists()) {
            propertiesPath = path;
        } else {
            throw logger.logAndReturnException(CoreMessages.NO_CONFIG_PROPERTIES, LogType.PROPERTIES_ERROR);
        }
    }

    private static String getVagrantHome() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String tmp;
        try {
            if (!SystemUtils.IS_OS_WINDOWS) {
                tmp = ShellExecutor.executeCommandOutput(ShellParameters.newBuilder(Commands.bash(
                        "sudo -i -u " + System.getProperty("user.name") + " echo \\$" + Commands.VAGRANT_HOME)).
                        setRedirectErrorStream(false).build());
                if (StringUtils.isNotBlank(tmp)) {
                    return tmp.replace("\n", "").replace("\r", "");
                }
            } else {
                tmp = WindowsInstallInfoHelper.read(Commands.VAGRANT_HOME);
                if (StringUtils.isNotBlank(tmp)) {
                    return tmp;
                }
            }
        } catch (GexException e) {
            logger.logWarn(CoreMessages.NO_VAGRANT_HOME, LogType.VAGRANT_ERROR);
        }
        if (StringUtils.isNotBlank(System.getenv(Commands.VAGRANT_HOME))) {
            return System.getenv(Commands.VAGRANT_HOME);
        }
        return Paths.get(PropertiesHelper.userHome, ".vagrant.d").toString();
    }

    public static void readProperties() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        properties = new Properties();
        getPropertiesDirectory();
        try {
            File file = new File(propertiesPath, PROPERTIES_FILE_NAME);
            if (!file.exists() || file.length() == 0) {
                return;
            }
            try (FileInputStream fileStream = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fileStream, StandardCharsets.UTF_8)) {
                properties.load(inputStreamReader);
            }
            if (properties.containsKey(API_URL_PROP_NAME)) {
                apiUrl = UrlHelper.httpResolver(properties.getProperty(API_URL_PROP_NAME).trim());
            }
            if (properties.containsKey(NODE_PROP_NAME)) {
                nodePropertiesFile = properties.getProperty(NODE_PROP_NAME).trim();
            }
            if (properties.containsKey(USER_PROP_NAME)) {
                userPropertiesFile = properties.getProperty(USER_PROP_NAME).trim();
            }
            if (properties.containsKey(HOST_TYPE_PROP_NAME)) {
                try {
                    hostType = HostType.valueOf(properties.getProperty(HOST_TYPE_PROP_NAME).trim().toUpperCase());
                } catch (Exception e) {
                    throw logger.logAndReturnException(CoreMessages.HOST_TYPE_ERROR, LogType.PROPERTIES_ERROR);
                }
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.CONFIG_PROPERTIES_ERROR + PropertiesHelper.PROPERTIES_FILE_NAME, e, LogType.PROPERTIES_ERROR);
        }
        setBaseEnvironment();
    }

    public static String readProperty(String propertyName) {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (properties.containsKey(propertyName)) {
            return properties.getProperty(propertyName).trim();
        }
        return null;
    }

    public static Integer readPropertyInteger(String propertyName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (properties.containsKey(propertyName)) {
            try {
                return Integer.parseInt(properties.getProperty(propertyName).trim());
            } catch (Exception e) {
                throw logger.logAndReturnException(CoreMessages.CONFIG_PROPERTIES_ERROR + PropertiesHelper.PROPERTIES_FILE_NAME, e, LogType.PROPERTIES_ERROR);
            }
        }
        return null;
    }

}
