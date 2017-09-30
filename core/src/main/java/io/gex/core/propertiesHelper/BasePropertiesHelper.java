package io.gex.core.propertiesHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.gex.core.AppContext;
import io.gex.core.CoreMessages;
import io.gex.core.PropertiesHelper;
import io.gex.core.api.FileLevelApi;
import io.gex.core.exception.GexAuthException;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.properties.BaseProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public abstract class BasePropertiesHelper<T extends BaseProperties> {

    private final static LogWrapper logger = LogWrapper.create(BasePropertiesHelper.class);

    private LogType type;
    private String errorMessage;
    private File propertiesFile;

    private volatile T propsInMem;

    BasePropertiesHelper(LogType type, String errorMessage, String propertiesFileName) throws GexException {
        this(type, errorMessage, propertiesFileName, false);
    }

    BasePropertiesHelper(LogType type, String errorMessage, String propertiesFileName, boolean isServiceFile) throws GexException {
        this.type = type;
        this.errorMessage = errorMessage;
        if (isServiceFile && SystemUtils.IS_OS_WINDOWS) {
            this.propertiesFile = setWindowsServiceDirectory(propertiesFileName);
        } else {
            this.propertiesFile = setDirectory(propertiesFileName);
        }
        logger.trace(propertiesFileName + " path: " + propertiesFile.getAbsolutePath());
        fetchPropsInMem();
    }

    private File setDirectory(String fileName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String path = Paths.get(PropertiesHelper.userHome, ".gex").toString();
        File file = new File(path);
        if (!file.exists() && !file.mkdir()) {
            throw logger.logAndReturnException(CoreMessages.NO_PROPERTIES_DIRECTORY, type);
        }
        return Paths.get(path, fileName).toFile();
    }

    private File setWindowsServiceDirectory(String fileName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String path = Paths.get(System.getenv("ProgramData"), ".gex").toString();
        File file = new File(path);
        if (!file.exists() && !file.mkdir()) {
            throw logger.logAndReturnException(CoreMessages.NO_SERVICE_PROPERTIES_DIRECTORY, type);
        }
        return Paths.get(path, fileName).toFile();
    }

    public void remove(String... properties) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (propsInMem == null) {
            return;
        }
        JsonObject obj = convertPropertiesToJSON(getProps());
        for (String propertyName : properties) {
            if (obj.has(propertyName)) {
                obj.remove(propertyName);
            }
        }
        try (FileOutputStream fileStream = new FileOutputStream(propertiesFile);
             OutputStreamWriter out = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(obj, out);
        } catch (Exception e) {
            throw logger.logAndReturnException(errorMessage, e, type);
        }
        propsInMem = this.convertJSONToProperties(obj);
        if (propertiesFile.length() == 0) {
            FileUtils.deleteQuietly(propertiesFile);
        }
    }

    public void saveToJSON(T properties) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            if (propertiesFile.createNewFile()) {
                FileLevelApi.setFilePermissions(propertiesFile);
            }
            T updProperties = update(properties);
            JsonObject obj = convertPropertiesToJSON(updProperties);
            try (FileOutputStream fileStream = new FileOutputStream(propertiesFile);
                 OutputStreamWriter out = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8)) {
                Gson gson = new GsonBuilder().create();
                gson.toJson(obj, out);
                propsInMem = updProperties;
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(errorMessage, e, type);
        }
    }

    private T readFromJSON() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (!propertiesFile.exists() || propertiesFile.length() == 0) {
            return convertJSONToProperties(new JsonObject());
        }
        try (FileInputStream inF = new FileInputStream(propertiesFile);
             InputStreamReader in = new InputStreamReader(inF, StandardCharsets.UTF_8)) {
            JsonObject obj = new JsonParser().parse(in).getAsJsonObject();
            return convertJSONToProperties(obj);
        } catch (Exception e) {
            throw logger.logAndReturnException(errorMessage, e, type);
        }
    }

    abstract T update(T properties) throws GexException;

    abstract T convertJSONToProperties(JsonObject obj) throws GexException;

    abstract JsonObject convertPropertiesToJSON(T properties) throws GexException;

    public static String getValidToken() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String token;
        if (AppContext.isSet()) {
            token = AppContext.getToken();
        } else if (PropertiesHelper.isService()) {
            token = PropertiesHelper.node.getProps().getNodeAgentToken();
        } else {
            token = PropertiesHelper.user.getProps().getToken();
        }
        if (StringUtils.isBlank(token)) {
            throw logger.logAndReturnAuthException(CoreMessages.AUTHENTICATION_ERROR, LogType.AUTHENTICATION_ERROR);
        }
        return token;
    }

    public static String getValidTokenOrNull() throws GexException {
        try {
            return getValidToken();
        } catch (GexAuthException e) {
            return null;
        }
    }

    public void fetchPropsInMem() throws GexException {
        propsInMem = readFromJSON();
    }

    @SuppressWarnings("unchecked")
    public T getProps() {
        return (T) propsInMem.copy();
    }
}
