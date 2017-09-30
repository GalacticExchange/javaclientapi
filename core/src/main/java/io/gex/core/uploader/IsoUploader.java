package io.gex.core.uploader;

import io.gex.core.DownloadFile;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;

import java.io.File;

public class IsoUploader extends Uploader {

    private final static LogWrapper logger = LogWrapper.create(IsoUploader.class);

    private final static String ISO = "iso";

    public IsoUploader() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        initialize(ISO);
    }

    public DownloadFile getDownloadFile(File file) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return getDownloadFile(ISO + "/" + PRODUCT_NAME + "_" + version + "." + ISO, file);
    }

    public DownloadFile getDownloadFile() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return getDownloadFile(ISO + "/" + PRODUCT_NAME + "_" + version + "." + ISO, ISO);
    }
}
