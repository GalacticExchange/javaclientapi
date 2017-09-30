package io.gex.core.box;


import io.gex.core.CoreMessages;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.util.FileUtils;

import java.nio.file.Paths;

public class ApplicationHelper extends DownloadHelper {

    private final static LogWrapper logger = LogWrapper.create(ApplicationHelper.class);

    public ApplicationHelper(String distribution) throws GexException {
        super(distribution);
    }

    @Override
    void init(String distribution) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(distribution)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_HADOOP_TYPE, LogType.BOX_ERROR);
        }
        this.distribution = distribution;
        folder = Paths.get(PropertiesHelper.nodeConfig, "applications", distribution).toFile();
        try {
            FileUtils.mkdir(folder, true);
        } catch (Exception e) {
            throw logger.logAndReturnException(e, LogType.BOX_ERROR);
        }
        queryParameter = "applications?name=";
        extension = ".tar.gz";
        serverProperty = "client_application_download_urls";
        type = LogType.APPLICATION;
    }

    @Override
    String getName() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(version)) {
            getInfo();
        }
        return "gex-" + distribution + "-" + version + extension;
    }

}
