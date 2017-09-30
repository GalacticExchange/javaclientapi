package io.gex.agent.app;


import com.google.gson.JsonObject;
import io.gex.agent.GexdAgent;
import io.gex.agent.GexdMessages;
import io.gex.agent.RabbitMQConnection;
import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.PropertiesHelper;
import io.gex.core.api.ApplicationLevelApi;
import io.gex.core.api.FileLevelApi;
import io.gex.core.box.ApplicationHelper;
import io.gex.core.box.BoxHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Application;
import io.gex.core.model.ApplicationNotification;
import io.gex.core.model.InstallStatus;
import io.gex.core.model.properties.NodeProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ApplicationManager {

    private final static LogWrapper logger = LogWrapper.create(ApplicationManager.class);
    public final static AtomicReference<InstallStatus> appInstallStatus = new AtomicReference<>();
    public final static AtomicReference<String> currentAppID = new AtomicReference<>();

    private static void checkRequestAttributes(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        // TODO :: stupid check
        if (!obj.has(GexdMessages.APPLICATION_NAME) || obj.get(GexdMessages.APPLICATION_NAME).isJsonNull()) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_APPLICATION_NAME, LogType.APPLICATION_ERROR);
        }
        if (!obj.has(GexdMessages.APPLICATION_ID) || obj.get(GexdMessages.APPLICATION_ID).isJsonNull()) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_APPLICATION_ID, LogType.APPLICATION_ERROR);
        }
    }

    private static void saveApplicationInfo(String applicationID, String applicationName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Application application = new Application();
        application.setId(applicationID);
        application.setName(applicationName);
        NodeProperties nodeProperties = new NodeProperties();
        nodeProperties.setApplications(PropertiesHelper.node.readApplications());
        nodeProperties.addApplication(application);
        PropertiesHelper.node.saveToJSON(nodeProperties);
    }

    public static void installApplication(String message) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            appInstallStatus.set(new InstallStatus("Preparing", 0));
            JsonObject obj = RabbitMQConnection.getAttributes(message);
            checkRequestAttributes(obj);
            String applicationID = obj.get(GexdMessages.APPLICATION_ID).getAsString();
            String applicationName = obj.get(GexdMessages.APPLICATION_NAME).getAsString();
            if (!obj.has(GexdMessages.EXTERNAL) || obj.get(GexdMessages.EXTERNAL).isJsonNull()) {
                throw logger.logAndReturnException(CoreMessages.EMPTY_EXTERNAL, LogType.APPLICATION_ERROR);
            }
            boolean external = obj.get(GexdMessages.EXTERNAL).getAsBoolean();
            currentAppID.set(applicationID);
            String clusterID = PropertiesHelper.node.getProps().getClusterID();
            if (StringUtils.isBlank(clusterID)) {
                throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_ID, LogType.PARSE_ERROR);
            }
            try {
                saveApplicationInfo(applicationID, applicationName);
                appInstallStatus.set(new InstallStatus("Downloading", 10, 30));
                String applicationDir = Paths.get(PropertiesHelper.nodeConfig, "applications", applicationName).toString();
                if (external) {
                    try {
                        org.apache.logging.log4j.core.util.FileUtils.mkdir(new File(applicationDir), true);
                    } catch (Exception e) {
                        throw logger.logAndReturnException(e, LogType.BOX_ERROR);
                    }
                    FileLevelApi.fileDownloadApplicationConfig("config.json", applicationDir, applicationID);
                    appInstallStatus.set(new InstallStatus("Starting application", 50));
                } else {
                    String tarPath = downloadApplication(applicationName, clusterID);
                    FileLevelApi.fileDownloadApplicationConfig("config.json", applicationDir, applicationID);
                    appInstallStatus.set(new InstallStatus("Installing application", 40));
                    GexdAgent.executor.installContainer(Paths.get("applications", applicationName,
                            new File(tarPath).getName()).toString(), applicationName);
                    appInstallStatus.set(new InstallStatus("Starting application", 70));
                }
                GexdAgent.executor.runContainer(Paths.get("applications", applicationName,
                        "config.json").toString(), applicationName);
            } catch (Exception e) {
                ApplicationLevelApi.notify(ApplicationNotification.APPLICATION_INSTALL_ERROR,
                        CoreMessages.replaceTemplate(CoreMessages.APPLICATION_INSTALL_ERROR, applicationName), applicationID);
                throw e;
            }
            //todo change notify to the single instance with node notify
            ApplicationLevelApi.notify(ApplicationNotification.APPLICATION_INSTALLED,
                    CoreMessages.replaceTemplate(CoreMessages.APPLICATION_INSTALLED, applicationName), applicationID);
        } finally {
            appInstallStatus.set(null);
            currentAppID.set(null);
        }
    }

    public static void uninstallApplication(String message) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = RabbitMQConnection.getAttributes(message);
        checkRequestAttributes(obj);
        String applicationID = obj.get(GexdMessages.APPLICATION_ID).getAsString();
        String applicationName = obj.get(GexdMessages.APPLICATION_NAME).getAsString();
        try {
            GexdAgent.executor.uninstallContainer(applicationName);
            FileUtils.deleteQuietly(Paths.get(PropertiesHelper.nodeConfig, "applications", applicationName).toFile());
            PropertiesHelper.node.removeApplicationByID(applicationID);
        } catch (Exception e) {
            ApplicationLevelApi.notify(ApplicationNotification.APPLICATION_UNINSTALL_ERROR,
                    CoreMessages.replaceTemplate(CoreMessages.APPLICATION_UNINSTALL_ERROR, applicationName), applicationID);
            throw e;
        }
        ApplicationLevelApi.notify(ApplicationNotification.APPLICATION_UNINSTALLED,
                CoreMessages.replaceTemplate(CoreMessages.APPLICATION_UNINSTALLED, applicationName), applicationID);
    }

    /**
     * @return application folder path
     */
    private static String downloadApplication(String distribution, String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ApplicationHelper applicationHelper = new ApplicationHelper(distribution);
        try {
            applicationHelper.deleteOld();
        } catch (GexException e) {
            logger.logWarn(CoreMessages.WARNING + e.getMessage(), LogType.APPLICATION_ERROR);
        }
        logger.logInfo(CoreMessages.APPLICATION_DOWNLOADING_START, LogType.APPLICATION);

        if (applicationHelper.isPresent()) {
            logger.logInfo(CoreMessages.APPLICATION_PRESENT, LogType.APPLICATION);
        } else {
            boolean isDownloaded = false;
            DownloadFile downloadFile;
            List<URL> candidates = applicationHelper.getLocalCandidates(clusterID);
            logger.logInfo(CoreMessages.LOCAL_NODES + candidates.size(), LogType.APPLICATION);
            if (CollectionUtils.isNotEmpty(candidates)) {
                for (int i = 0; i < (candidates.size() < BoxHelper.DOWNLOAD_ATTEMPTS ? candidates.size() :
                        BoxHelper.DOWNLOAD_ATTEMPTS); i++) {
                    try {
                        downloadFile = new DownloadFile(candidates.get(i).toString(), applicationHelper.getPath());
                        logger.logInfo(CoreMessages.TRYING_TO_DOWNLOAD_FROM + candidates.get(i).toString(), LogType.APPLICATION);
                        downloadFile.downloadParallel(appInstallStatus.get().getSubProgress());
                        isDownloaded = true;
                        logger.logInfo(GexdMessages.APPLICATION_DOWNLOADED, LogType.APPLICATION);
                        break;
                    } catch (GexException e) {
                        // continue
                    }
                }
            }
            if (!isDownloaded) {
                downloadFile = applicationHelper.getDownloadFile();
                downloadFile.downloadParallel(appInstallStatus.get().getSubProgress());
            }
        }

        if (applicationHelper.getChecksum() == BaseHelper.getCRC32Checksum(applicationHelper.getPath())) {
            return applicationHelper.getPath().getAbsolutePath();
        } else {
            throw logger.logAndReturnException(CoreMessages.APP_BOX_IS_CORRUPTED, LogType.APPLICATION_ERROR);
        }
    }
}
