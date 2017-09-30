package io.gex.core;

import io.gex.core.exception.GexException;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;

public class UrlHelper {

    private final static LogWrapper logger = LogWrapper.create(UrlHelper.class);

    private final static String HTTP = "http://";
    private final static String HTTPS = "https://";
    private final static String DELIMITER = "/";

    public static URL concatenate(String first, String... more) throws GexException {
        try {
            String result = StringUtils.trimToEmpty(first);
            if (!result.startsWith(HTTP) && !result.startsWith(HTTPS)) {
                result = HTTP + result;
            }
            for (String item : more) {
                if (!result.substring(result.length() - 1).equals(DELIMITER) && !result.substring(0, 1).equals(DELIMITER)) {
                    result += DELIMITER;
                }
                result += item;
            }
            return new URL(result);
        } catch (Exception e) {
            throw logger.logAndReturnException(e, LogType.PARSE_ERROR);
        }
    }

    public static URL concatenate(Integer port, String first, String... more) throws GexException {
        try {
            String result = StringUtils.trimToEmpty(first);
            if (!result.startsWith(HTTP) && !result.startsWith(HTTPS)) {
                result = HTTP + result;
            }
            for (String item : more) {
                if (!result.substring(result.length() - 1).equals(DELIMITER) && !result.substring(0, 1).equals(DELIMITER)) {
                    result += DELIMITER;
                }
                result += item;
            }
            result += ":" + port;
            return new URL(result);
        } catch (Exception e) {
            throw logger.logAndReturnException(e, LogType.PARSE_ERROR);
        }
    }

    public static URL concatenate(URL first, String... more) throws GexException {
        try {
            String result = first.toString();
            for (String item : more) {
                if (!result.substring(result.length() - 1).equals(DELIMITER) && !result.substring(0, 1).equals(DELIMITER)) {
                    result += DELIMITER;
                }
                result += item;
            }
            return new URL(result);
        } catch (Exception e) {
            throw logger.logAndReturnException(e, LogType.PARSE_ERROR);
        }
    }

    public static String httpResolver(String http) {
        if (!http.startsWith(HTTP) && !http.startsWith(HTTPS)) {
            return HTTP + http;
        }
        return http;
    }


}
