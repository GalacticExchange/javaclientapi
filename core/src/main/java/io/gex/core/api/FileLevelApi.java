package io.gex.core.api;

import io.gex.core.CoreMessages;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.FileGex;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.FileLevelRest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.core.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class FileLevelApi {

    private final static LogWrapper logger = LogWrapper.create(FileLevelApi.class);

    public static List<FileGex> filesList(String module, String nodeID, String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (module == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_MODULE, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(nodeID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_ID, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(clusterID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        return FileLevelRest.filesList(module, nodeID, clusterID, BasePropertiesHelper.getValidToken());
    }

    public static void fileDownload(String fileName, String dst, String nodeID, String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(fileName)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_FILE_NAME, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(dst)) {
            throw logger.logAndReturnException(CoreMessages.INVALID_DESTINATION, LogType.EMPTY_PROPERTY_ERROR);
        }
        try {
            FileUtils.mkdir(new File(dst), true);
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.INVALID_DESTINATION, e, LogType.FILE_DOWNLOAD_ERROR);
        }
        FileLevelRest.fileDownload(fileName, dst, BasePropertiesHelper.getValidToken(), nodeID, null, clusterID);
        setFilePermissions(Paths.get(dst, FilenameUtils.getName(fileName)).toFile());
    }

    public static void fileDownloadApplicationConfig(String fileName, String dst, String applicationID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(fileName)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_FILE_NAME, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(dst)) {
            throw logger.logAndReturnException(CoreMessages.INVALID_DESTINATION, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(applicationID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_APPLICATION_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        String nodeID = PropertiesHelper.node.getProps().getNodeID();
        if (StringUtils.isBlank(nodeID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        try {
            FileUtils.mkdir(new File(dst), true);
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.INVALID_DESTINATION, e, LogType.FILE_DOWNLOAD_ERROR);
        }
        FileLevelRest.fileDownload(fileName, dst, BasePropertiesHelper.getValidToken(), nodeID, applicationID, null);
        setFilePermissions(Paths.get(dst, FilenameUtils.getName(fileName)).toFile());
    }

    public static void moveFiles(List<FileGex> files, String src, String dst) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(src)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_SOURCE_FOLDER, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(dst)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_DESTINATION_FOLDER, LogType.EMPTY_PROPERTY_ERROR);
        }
        try {
            File dir = new File(dst);
            try {
                FileUtils.mkdir(dir, true);
            } catch (Exception e) {
                throw new IOException(CoreMessages.INVALID_DESTINATION);
            }
            if (CollectionUtils.isNotEmpty(files)) {
                for (FileGex f : files) {
                    String fileName = new File(f.getFileName()).getName();
                    File srcFile = Paths.get(src, fileName).toFile();
                    if (!srcFile.exists()) {
                        throw new FileNotFoundException(CoreMessages.replaceTemplate(
                                CoreMessages.FILE_NOT_FOUND, srcFile.getAbsolutePath()));
                    }
                    org.apache.commons.io.FileUtils.copyFileToDirectory(srcFile, dir);
                    File dstFile = Paths.get(dst, fileName).toFile();
                    if (!dstFile.exists()) {
                        throw new IOException("Failed to move " + srcFile.getAbsolutePath() + " to " +
                                dstFile.getAbsolutePath());
                    }
                    setFilePermissions(dstFile);
                }
                setDirectoryPermissions(dst);
            } else {
                throw new Exception(CoreMessages.NO_FILES_TO_DOWNLOAD);
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(e, LogType.FILE_ERROR);
        }
    }

    public static void setFilePermissions(File file) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        setPermissions(file, 600, false);
    }

    public static void setDirectoryPermissions(String dirName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        setPermissions(new File(dirName), 700, true);
    }

    public static void setPermissions(String dirName, Integer permission) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        setPermissions(new File(dirName), permission, false);
    }

    public static void setPermissions(File file, Integer permission, boolean recursively) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        // for Windows permissions are correct by default
        if (!SystemUtils.IS_OS_WINDOWS) {
            if (!file.exists()) {
                throw logger.logAndReturnException(
                        CoreMessages.replaceTemplate(CoreMessages.FILE_NOT_FOUND, file.getAbsolutePath()), LogType.FILE_ERROR);
            }
            try {
                String r = recursively ? "-R " : StringUtils.EMPTY;
                logger.logInfo("Set " + r + permission + " permissions to " + file.getAbsolutePath(), LogType.FILE);
                Runtime.getRuntime().exec("chmod " + r + permission + " " + file.getAbsolutePath());
            } catch (Exception e) {
                throw logger.logAndReturnException(e, LogType.FILE_ERROR);
            }
        }
    }

}
