package io.gex.core.uploader;

import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.UrlHelper;
import io.gex.core.api.ServerPropertiesLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class Uploader {
    private final static LogWrapper logger = LogWrapper.create(Uploader.class);

    private URL storageUrl;
    String version;

    private final static String VERSION_FILE_NAME = "version.txt";
    final static String PRODUCT_NAME = "gex";

    void initialize(String dir) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        storageUrl = requestStorageUrl();
        version = requestVersion(dir);
    }

    private URL requestStorageUrl() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            return new URL(ServerPropertiesLevelApi.getProperty(ServerPropertiesLevelApi.STORAGE_URL));
        } catch (MalformedURLException e) {
            throw logger.logAndReturnException(CoreMessages.UPLOADS_ERROR, e, LogType.GET_PROPERTY_ERROR);
        }
    }

    private String requestVersion(String dir) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        URL url = UrlHelper.concatenate(storageUrl, dir, VERSION_FILE_NAME);
        logger.logInfo("Version url: " + url, LogType.UPDATER);
        String version = BaseHelper.readFileToString(url);
        logger.logInfo("Version: " + version, LogType.UPDATER);
        return version;
    }

    DownloadFile getDownloadFile(String path, String extension) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        URL url = UrlHelper.concatenate(storageUrl, path);
        logger.logInfo(url.toString(), LogType.UPDATER);
        return new DownloadFile(url.toString(), extension);
    }

    DownloadFile getDownloadFile(String path, File file) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        URL url = UrlHelper.concatenate(storageUrl, path);
        logger.logInfo(url.toString(), LogType.UPDATER);
        return new DownloadFile(url.toString(), file);
    }
}
