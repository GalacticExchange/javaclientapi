package io.gex.core.rest;

import com.google.gson.JsonObject;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.FileGex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileLevelRest {

    private final static LogWrapper logger = LogWrapper.create(FileLevelRest.class);

    private final static String FILES = "/files";
    private final static String DOWNLOAD = "/files/download";

    public static List<FileGex> filesList(String module, String nodeID, String clusterID, String token)
            throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("for", module);
        query.add("clusterID", clusterID);
        query.add("nodeID", nodeID);
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, FILES, LogType.FILE_LIST_ERROR, null,
                null, query, token);
        return obj.has("files") ? FileGex.parse(obj.getAsJsonArray("files")) : new ArrayList<>(0);
    }

    public static void fileDownload(String fileName, String path, String token, String nodeID, String applicationID,
                                    String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("filename", fileName);
        if (StringUtils.isNotBlank(nodeID)) {
            query.add("nodeID", nodeID);
        }
        if (StringUtils.isNotBlank(applicationID)) {
            query.add("applicationID", applicationID);
        }
        if (StringUtils.isNotBlank(clusterID)) {
            query.add("clusterID", clusterID);
        }
        InputStream in = Rest.getInputStreamFromAuthenticatedRequest(HttpMethod.GET, DOWNLOAD,
                LogType.FILE_DOWNLOAD_ERROR, null, null, query, token);
        byte[] byteArray;
        try {
            byteArray = IOUtils.toByteArray(in);
            FileOutputStream fos = new FileOutputStream(new File(path, FilenameUtils.getName(fileName)));
            fos.write(byteArray);
            fos.flush();
            fos.close();
            File file = new File(path, FilenameUtils.getName(fileName));
            if (!file.exists() || file.length() == 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.DOWNLOAD_FILE_WITH_NAME_ERROR + fileName, e, LogType.FILE_DOWNLOAD_ERROR);
        }
    }
}
