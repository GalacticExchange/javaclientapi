package io.gex.core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.gex.core.CoreMessages;
import io.gex.core.GsonHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;

public class ServerError {

    private final static LogWrapper logger = LogWrapper.create(ServerError.class);

    @Expose
    private Integer returnCode;
    @Expose
    private String url;
    private String code;
    @SerializedName("errorname")
    private String errorName;
    private String message;
    private String description;
    private JsonArray errors;

    public static ServerError parse(String url, Integer returnCode, String body) throws GexException {
        ServerError serverError = StringUtils.isNotBlank(body) ? GsonHelper.parse(body, ServerError.class,
                CoreMessages.SERVER_RESPONSE_ERROR) : new ServerError();
        serverError.returnCode = returnCode;
        serverError.url = url;
        return serverError;
    }

    private String getErrors() {
        try {
            if (errors != null && errors.size() > 0) {
                StringBuilder errorString = new StringBuilder();
                for (int i = 0; i < errors.size(); i++) {
                    JsonObject error = errors.get(i).getAsJsonObject();
                    if (error.has("message")) {
                        errorString.append(error.get("message").getAsString()).append(".\n\t");
                    }
                }
                return errorString.delete(errorString.length() - 2, errorString.length() - 1).toString();
            }
        } catch (Exception e) {
            logger.logWarn(CoreMessages.SERVER_RESPONSE_ERROR, e, LogType.SERVER_RESPONSE_ERROR);
        }
        return null;
    }

    public String getErrorString() {
        String errors = getErrors();
        return CoreMessages.SERVER_ERROR + " " + url + "\n" +
                returnCode + " - " + Response.Status.fromStatusCode(returnCode).getReasonPhrase() +
                (StringUtils.isBlank(errorName) ? "" : "\n" + errorName) +
                (StringUtils.isBlank(message) ? "" : "\n" + message) +
                (StringUtils.isBlank(description) ? "" : "\n" + description) +
                (StringUtils.isBlank(errors) ? "" : "\n" + "Additional info: " + errors);
    }
}
