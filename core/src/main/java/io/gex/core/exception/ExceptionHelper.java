package io.gex.core.exception;

import com.google.gson.JsonParser;
import io.gex.core.CoreMessages;
import io.gex.core.PropertiesHelper;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.ServerError;
import io.gex.core.model.properties.UserProperties;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHelper {

    private final static LogWrapper logger = LogWrapper.create(ExceptionHelper.class);

    public static void handleAuthenticatedServerResponse(String url, int responseStatus, String body) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (responseStatus == 403 && !StringUtils.isBlank(body)
                && (new JsonParser().parse(body).getAsJsonObject()).get("errorname").getAsString().toLowerCase().equals("token_invalid")) {
            if (PropertiesHelper.isCLI()) {
                PropertiesHelper.user.remove(UserProperties.TOKEN_PROPERTY_NAME);
            }
            throw logger.logAndReturnAuthException(CoreMessages.RE_AUTHENTICATE, LogType.AUTHENTICATION_ERROR);
        }
        handleServerResponse(url, responseStatus, body);
    }

    public static void handleServerResponse(String url, int responseStatus, String body) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        throw logger.logAndReturnException(ServerError.parse(url, responseStatus, body).getErrorString(), LogType.SERVER_RESPONSE_ERROR);
    }

    public static String getStackTraceString(Throwable e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

}
