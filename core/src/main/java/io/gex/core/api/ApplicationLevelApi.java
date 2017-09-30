package io.gex.core.api;

import io.gex.core.CoreMessages;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Application;
import io.gex.core.model.ApplicationNotification;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.ApplicationLevelRest;
import io.gex.core.rest.MainLevelRest;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.List;


public class ApplicationLevelApi {

    private final static LogWrapper logger = LogWrapper.create(ApplicationLevelApi.class);

    public static void applicationInstall(String name) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ApplicationLevelApi.applicationExistenceCheckByName(name, false);
        String nodeID = PropertiesHelper.node.getProps().getNodeID();
        if (StringUtils.isBlank(nodeID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        if (StringUtils.isBlank(name)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_APPLICATION_NAME, LogType.EMPTY_PROPERTY_ERROR);
        }
        ApplicationLevelRest.applicationInstall(nodeID, name, BasePropertiesHelper.getValidToken());
    }

    public static void applicationUninstall(String applicationID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(applicationID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_APPLICATION_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        String nodeID = PropertiesHelper.node.getProps().getNodeID();
        if (StringUtils.isBlank(nodeID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        /*Application application = PropertiesHelper.node.findApplicationByName(applicationID);
        if(application == null){
            throw ExceptionHelper.logAndReturnException(logger,CoreMessages.replaceTemplate(CoreMessages.APPLICATION_NOT_EXIST, applicationID),
                    LogType.APPLICATION_ERROR);
        }
        if(StringUtils.isBlank(application.getId())){
            throw ExceptionHelper.logAndReturnException(logger,CoreMessages.EMPTY_APPLICATION_ID, LogType.APPLICATION_ERROR);
        }*/
        ApplicationLevelApi.applicationExistenceCheckByID(applicationID, true);
        ApplicationLevelRest.applicationUninstall(nodeID, applicationID, BasePropertiesHelper.getValidToken());
    }

    public static List<Application> applicationSupported(String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return ApplicationLevelRest.applicationSupported(clusterID, BasePropertiesHelper.getValidToken());
    }

    public static List<Application> applicationList(String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return ApplicationLevelRest.applicationList(clusterID, BasePropertiesHelper.getValidToken());
    }

    public static void applicationExistenceCheckFolder(String name, boolean shouldExist) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (shouldExist) {
            if (!Paths.get(PropertiesHelper.nodeConfig, "applications", name)
                    .toFile().exists()) {
                throw logger.logAndReturnException(CoreMessages.replaceTemplate(CoreMessages.APPLICATION_NOT_EXIST, name), LogType.APPLICATION_ERROR);
            }
        } else {
            if (Paths.get(PropertiesHelper.nodeConfig, "applications", name)
                    .toFile().exists()) {
                throw logger.logAndReturnException(CoreMessages.replaceTemplate(CoreMessages.APPLICATION_EXIST, name), LogType.APPLICATION_ERROR);
            }
        }
    }

    public static void applicationExistenceCheckByName(String applicationName, boolean shouldExist) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Application application = PropertiesHelper.node.findApplicationByName(applicationName);
        if (shouldExist) {
            if (application == null) {
                throw logger.logAndReturnException(CoreMessages.replaceTemplate(CoreMessages.APPLICATION_NOT_EXIST, applicationName), LogType.APPLICATION_ERROR);
            }
        } else {
            if (application != null) {
                throw logger.logAndReturnException(CoreMessages.replaceTemplate(CoreMessages.APPLICATION_EXIST, applicationName), LogType.APPLICATION_ERROR);
            }
        }
    }

    public static void applicationExistenceCheckByID(String applicationID, boolean shouldExist) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Application application = PropertiesHelper.node.findApplicationByID(applicationID);
        if (shouldExist) {
            if (application == null) {
                throw logger.logAndReturnException(CoreMessages.replaceTemplate(CoreMessages.APPLICATION_NOT_EXIST, StringUtils.EMPTY), LogType.APPLICATION_ERROR);
            }
        } else {
            if (application != null) {
                throw logger.logAndReturnException(CoreMessages.replaceTemplate(CoreMessages.APPLICATION_EXIST, StringUtils.EMPTY), LogType.APPLICATION_ERROR);
            }
        }
    }

    public static void notify(ApplicationNotification event, String description, String applicationID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MainLevelRest.notify(event, description, applicationID,
                LogType.NOTIFY_ERROR, BasePropertiesHelper.getValidToken());
    }

}
