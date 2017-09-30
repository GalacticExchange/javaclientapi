package io.gex.core.box;


import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.InstallStatus;
import io.gex.core.vagrantHelper.VagrantHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.util.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BoxHelper extends DownloadHelper {

    private final static LogWrapper logger = LogWrapper.create(BoxHelper.class);

    public BoxHelper() throws GexException {
        super(PropertiesHelper.node.getProps().getHadoopType());
    }

    @Override
    void init(String distribution) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        folder = new File(PropertiesHelper.userHome, ".gex");
        try {
            FileUtils.mkdir(folder, true);
        } catch (Exception e) {
            throw logger.logAndReturnException(e, LogType.BOX_ERROR);
        }
        queryParameter = "boxes?name=";
        extension = ".box";
        serverProperty = "client_box_download_urls";
        if (StringUtils.isBlank(distribution)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_HADOOP_TYPE, LogType.EMPTY_PROPERTY_ERROR);
        }
        this.distribution = distribution;
        type = LogType.BOX;
    }

    @Override
    String getName() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(version)) {
            getInfo();
        }
        return "gex-" + version + "-" + distribution + extension;
    }

    //todo remove printing
    public static void downloadAndAddBox(AtomicReference<InstallStatus> nodeInstallStatus, String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        BoxHelper boxHelper = new BoxHelper();
        try {
            boxHelper.deleteOld();
        } catch (GexException e) {
            logger.logWarn(CoreMessages.WARNING + e.getMessage(), LogType.BOX_ERROR);
        }
        logger.logInfo(CoreMessages.BOX_DOWNLOADING_START, LogType.BOX);

        if (boxHelper.isPresent()) {
            logger.logInfo(CoreMessages.BOX_PRESENT, LogType.BOX);
        } else {
            boolean isDownloaded = false;
            DownloadFile boxDownloadFile;
            List<URL> candidates = boxHelper.getLocalCandidates(clusterID);
            if (CollectionUtils.isNotEmpty(candidates)) {
                logger.logInfo(CoreMessages.LOCAL_NODES + candidates.size(), LogType.BOX);
                for (int i = 0; i < (candidates.size() < BoxHelper.DOWNLOAD_ATTEMPTS ? candidates.size() :
                        BoxHelper.DOWNLOAD_ATTEMPTS); i++) {
                    try {
                        boxDownloadFile = new DownloadFile(candidates.get(i).toString(), boxHelper.getPath());
                        logger.logInfo(CoreMessages.TRYING_TO_DOWNLOAD_FROM + candidates.get(i).toString(), LogType.BOX);
                        if (nodeInstallStatus != null) {
                            boxDownloadFile.downloadParallel(nodeInstallStatus.get().getSubProgress());
                        } else {
                            boxDownloadFile.downloadParallel();
                        }
                        isDownloaded = true;
                        logger.logInfo(CoreMessages.BOX_DOWNLOADED, LogType.BOX);
                        break;
                    } catch (GexException e) {
                        // continue
                    }
                }
            }
            if (!isDownloaded) {
                boxDownloadFile = boxHelper.getDownloadFile();
                if (nodeInstallStatus != null) {
                    boxDownloadFile.downloadParallel(nodeInstallStatus.get().getSubProgress());
                } else {
                    boxDownloadFile.downloadParallel();
                }
            }
        }

        if (boxHelper.getChecksum() == BaseHelper.getCRC32Checksum(boxHelper.getPath())) {
            if (nodeInstallStatus != null) {
                nodeInstallStatus.set(new InstallStatus("Adding box", 90));
            }
            VagrantHelper.constructVagrantHelper().addBox(boxHelper.getPath().getAbsolutePath());
        } else {
            throw logger.logAndReturnException(CoreMessages.BOX_IS_CORRUPTED, LogType.BOX_ERROR);
        }
    }

}
