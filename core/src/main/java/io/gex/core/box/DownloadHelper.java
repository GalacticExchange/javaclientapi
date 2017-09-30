package io.gex.core.box;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import io.gex.core.ConnectionChecker;
import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.UrlHelper;
import io.gex.core.api.NodeAgentLevelApi;
import io.gex.core.api.ServerPropertiesLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Agent;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static io.gex.core.BaseHelper.getCRC32Checksum;

abstract class DownloadHelper {

    private final static LogWrapper logger = LogWrapper.create(DownloadHelper.class);

    public static final int DOWNLOAD_ATTEMPTS = 3;

    private String url;
    String version;
    long checksum;
    String distribution;
    File folder;
    String queryParameter;
    String extension;
    String serverProperty;
    LogType type;
    private final static String VERSION = "version";
    private final static String CHECKSUM = "checksum";

    DownloadHelper(String distribution) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        init(distribution);
        getInfo();
    }

    abstract void init(String distribution) throws GexException;

    private String getVersionRelativeURL() {
        return "/" + distribution + "-version.txt";
    }

    abstract String getName() throws GexException;

    private String getNameWithFolder() throws GexException {
        if (StringUtils.isBlank(version)) {
            getInfo();
        }
        return Paths.get(version, getName()).toString();
    }

    public File getPath() throws GexException {
        return new File(folder, getName());
    }

    //todo do we need getNameWithFolder check ?
    private String getURL() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(url)) {
            chooseURL();
        }
        String address = url + "/" + getNameWithFolder();
        logger.logInfo("address: " + address, type);
        // check that box is exist on URL in 'version' folder
        if (ConnectionChecker.getConnectionTime(address) == -1) {
            // if there is no box on that URL - check in root folder
            address = url + "/" + getName();
            if (ConnectionChecker.getConnectionTime(address) == -1) {
                throw logger.logAndReturnException(CoreMessages.REACH_DOWNLOAD_BOX_URL_ERROR + address, LogType.BOX_ERROR);
            }
        }
        return address;
    }

    public List<URL> getLocalCandidates(String clusterID) {
        logger.trace("Entered " + LogHelper.getMethodName());
        List<Agent> agents;
        try {
            agents = NodeAgentLevelApi.nodeAgentsInfo(clusterID);
        } catch (GexException e) {
            return null;
        }
        List<URL> candidates = new ArrayList<>();
        for (Agent agent : agents) {
            try {
                candidates.add(UrlHelper.concatenate(agent.getURL(), queryParameter + getName()));
            } catch (GexException e) {
                // continue
            }
        }
        return candidates;
    }

    private void chooseURL() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonArray array = ServerPropertiesLevelApi.getPropertyArray(serverProperty);
        List<String> downloadCandidates = new Gson().fromJson(array.toString(), new TypeToken<List<String>>() {
        }.getType());
        long smallestResponseTimeOut = Long.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < downloadCandidates.size(); i++) {
            long tmp = ConnectionChecker.getConnectionTime(UrlHelper.httpResolver(downloadCandidates.get(i) +
                    getVersionRelativeURL()));
            if (tmp >= 0 && smallestResponseTimeOut > tmp) {
                smallestResponseTimeOut = tmp;
                index = i;
            }
        }
        if (index == -1) {
            throw logger.logAndReturnException(CoreMessages.REACH_DOWNLOAD_BOX_URL_ERROR +
                    downloadCandidates.toString(), LogType.BOX_ERROR);
        }
        url = downloadCandidates.get(index);
        logger.logInfo("Download URL: " + url, type);
    }

    void getInfo() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(url)) {
            chooseURL();
        }
        try (InputStream in = new URL(url + getVersionRelativeURL()).openStream();
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            properties.load(reader);

            if (!properties.containsKey(VERSION)) {
                throw logger.logAndReturnException(CoreMessages.BOX_VERSION_ERROR, LogType.BOX_ERROR);
            }
            version = properties.getProperty(VERSION).trim();
            logger.logInfo("version: " + version, type);


            if (!properties.containsKey(CHECKSUM)) {
                throw logger.logAndReturnException(CoreMessages.BOX_CHECKSUM_ERROR, LogType.BOX_ERROR);
            }
            String strChecksum = StringUtils.trim(properties.getProperty(CHECKSUM));
            try {
                checksum = Long.valueOf(strChecksum);
            } catch (NumberFormatException e) {
                throw logger.logAndReturnException(CoreMessages.BOX_CHECKSUM_PARSE_ERROR + strChecksum, LogType.BOX_ERROR);
            }
            logger.logInfo("checksum: " + checksum, type);
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.BOX_INFO_ERROR, e, LogType.BOX_ERROR);
        }
    }

    public long getChecksum() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(version)) {
            getInfo();
        }
        return checksum;
    }

    public void deleteOld() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String newestName = getName();
        long checksum = getChecksum();
        if (folder.exists()) {
            List<File> files = new ArrayList<>();
            try {
                File[] tempFiles = folder.listFiles();
                if (tempFiles == null) {
                    return;
                }
                for (File file : tempFiles) {
                    String name = file.getName();
                    if (file.isFile() && name.endsWith(extension) && (!name.startsWith(newestName)
                            || checksum != getCRC32Checksum(file))) {
                        files.add(file);
                    }
                }
            } catch (Exception e) {
                throw logger.logAndReturnException(CoreMessages.replaceTemplate(
                        CoreMessages.OLD_BOXES_DELETE_GENERAL_ERROR, folder.getAbsolutePath()), LogType.BOX_ERROR);
            }
            String errorMessage = StringUtils.EMPTY;
            for (File file : files) {
                try {
                    if (!file.delete()) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    errorMessage += " " + file.getAbsolutePath() + ",";
                }
            }
            if (StringUtils.isNotEmpty(errorMessage)) {
                throw logger.logAndReturnException(CoreMessages.OLD_BOXES_DELETE_ERROR + errorMessage + ".", LogType.BOX_ERROR);
            }
        }
    }

    public boolean isPresent() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return getPath().exists();
    }

    public DownloadFile getDownloadFile() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return new DownloadFile(getURL(), getPath());
    }
}
