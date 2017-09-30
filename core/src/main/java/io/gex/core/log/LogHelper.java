package io.gex.core.log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.gex.core.AppContext;
import io.gex.core.CoreMessages;
import io.gex.core.GsonHelper;
import io.gex.core.PropertiesHelper;
import io.gex.core.model.Color;
import io.gex.core.model.properties.NodeProperties;
import io.gex.core.model.properties.UserProperties;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

public class LogHelper {

    private final static LogWrapper logger = LogWrapper.create(LogHelper.class);

    private static long time;
    public static boolean debugMode;

    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    public static void responseDebug(int responseStatus, JsonObject body) {
        StringBuilder builder = new StringBuilder();
        builder.append(CoreMessages.RESPONSE).append("\n\t").append(CoreMessages.RETURN_CODE).append(responseStatus).append("\n\t");
        if (body != null) {
            builder.append(CoreMessages.BODY).append("\n\t");
            for (Map.Entry<String, JsonElement> entry : body.entrySet()) {
                if (entry.getKey().equals(UserProperties.TOKEN_PROPERTY_NAME) ||
                        entry.getKey().equals(NodeProperties.NODE_AGENT_TOKEN_PROPERTY_NAME)) {
                    builder.append(entry.getKey()).append(": ******").append("\n\t");
                } else {
                    builder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n\t");
                }
            }
        } else {
            builder.append(CoreMessages.BODY_EMPTY).append("\n\t");
        }
        Long responseTime = System.currentTimeMillis() - time;
        builder.append(CoreMessages.SERVER_RESPONSE_TIME).append(responseTime);
        logger.logDebug(builder.toString());
        if (debugMode) {
            print(CoreMessages.SERVER_RESPONSE_TIME + responseTime);
            responseDebugConsole(responseStatus, body);
        }
    }

    public static void requestDebug(String requestType, MultivaluedMap<String, String> header,
                                    MultivaluedMap<String, String> body, MultivaluedMap<String, String> query,
                                    String url, String token, String version) {
        String strBody = null;
        if (MapUtils.isNotEmpty(body)) {
            StringBuilder strBuilder = new StringBuilder();
            for (String key : body.keySet()) {
                if (key.toLowerCase().contains("password")) {
                    strBuilder.append(key).append(": ******").append("\n\t");
                } else {
                    strBuilder.append(key).append(": ").append(body.getFirst(key)).append("\n\t");
                }
            }
            strBody = strBuilder.deleteCharAt(strBuilder.length() - 2).toString();
        }

        requestDebug0(requestType, header, strBody, query, url, token, version);
    }

    public static void requestDebug(String requestType, MultivaluedMap<String, String> header,
                                    JsonObject body, MultivaluedMap<String, String> query,
                                    String url, String token, String version) {
        String strBody = null;
        if (body != null && !body.isJsonNull()) {
            strBody = GsonHelper.hidePasswordFields(body).toString();
        }

        requestDebug0(requestType, header, strBody, query, url, token, version);
    }


    private static void requestDebug0(String requestType, MultivaluedMap<String, String> header,
                                      String body, MultivaluedMap<String, String> query,
                                      String url, String token, String version) {
        StringBuilder builder = new StringBuilder();
        builder.append(CoreMessages.REQUEST).append(requestType).append(" ").append(url).append("\n\t");
        //todo send version only in logError and don't print here or send each time
        if (StringUtils.isNotBlank(version)) {
            builder.append(CoreMessages.VERSION).append(version).append("\n\t");
        }
        if (StringUtils.isBlank(token) && MapUtils.isEmpty(header)) {
            builder.append(CoreMessages.HEADER_EMPTY).append("\n\t");
        } else {
            builder.append(CoreMessages.HEADER).append("\n\t");
            if (StringUtils.isNotBlank(token)) {
                builder.append((PropertiesHelper.isService() && !AppContext.isSet()) ? CoreMessages.NODE_AGENT_TOKEN :
                        CoreMessages.TOKEN).append("******").append("\n\t");
            }
            if (MapUtils.isNotEmpty(header)) {
                for (String key : header.keySet()) {
                    builder.append(key).append(": ").append(header.getFirst(key)).append("\n\t");
                }
            }
        }
        if (MapUtils.isNotEmpty(query)) {
            builder.append(CoreMessages.QUERY);
            for (String key : query.keySet()) {
                builder.append(key).append(": ").append(query.getFirst(key)).append("\n\t");
            }
        } else {
            builder.append(CoreMessages.QUERY_EMPTY).append("\n\t");
        }
        if (StringUtils.isNotEmpty(body)) {
            builder.append(CoreMessages.BODY).append("\n\t").append(body);
        } else {
            builder.append(CoreMessages.BODY_EMPTY);
        }
        logger.logDebug(builder.toString());
        time = System.currentTimeMillis();
        if (debugMode) {
            requestDebugConsole(requestType, header, body, query, url, token, version);
        }
    }

    private static void responseDebugConsole(int responseStatus, JsonObject body) {
        printWithColor(Color.ANSI_YELLOW, CoreMessages.RESPONSE);
        printWithColor(Color.ANSI_YELLOW, CoreMessages.RETURN_CODE + responseStatus);
        if (body != null) {
            printWithColor(Color.ANSI_YELLOW, CoreMessages.BODY);
            for (Map.Entry<String, JsonElement> entry : body.entrySet()) {
                if (entry.getKey().equals(UserProperties.TOKEN_PROPERTY_NAME) ||
                        entry.getKey().equals(NodeProperties.NODE_AGENT_TOKEN_PROPERTY_NAME)) {
                    printWithColor(Color.ANSI_YELLOW, entry.getKey() + ": ******");
                } else {
                    printWithColor(Color.ANSI_YELLOW, entry.getKey() + ": " + entry.getValue());
                }
            }
        } else {
            printWithColor(Color.ANSI_YELLOW, CoreMessages.BODY_EMPTY);
        }
        System.out.println();
    }

    private static void requestDebugConsole(String requestType, MultivaluedMap<String, String> header,
                                            String body, MultivaluedMap<String, String> query,
                                            String url, String token, String version) {
        printWithColor(Color.ANSI_YELLOW, CoreMessages.REQUEST + requestType + " " + url);
        if (StringUtils.isNotBlank(token)) {
            printWithColor(Color.ANSI_YELLOW, (PropertiesHelper.isService() && !AppContext.isSet()) ?
                    CoreMessages.NODE_AGENT_TOKEN : CoreMessages.TOKEN + "******");
        }
        if (StringUtils.isNotBlank(version)) {
            printWithColor(Color.ANSI_YELLOW, CoreMessages.VERSION + version);
        }
        if (MapUtils.isNotEmpty(header)) {
            printWithColor(Color.ANSI_YELLOW, CoreMessages.HEADER);
            for (String key : header.keySet()) {
                printWithColor(Color.ANSI_YELLOW, key + ": " + header.getFirst(key));
            }
        } else {
            printWithColor(Color.ANSI_YELLOW, CoreMessages.HEADER_EMPTY);
        }
        if (MapUtils.isNotEmpty(query)) {
            printWithColor(Color.ANSI_YELLOW, CoreMessages.QUERY);
            for (String key : query.keySet()) {
                printWithColor(Color.ANSI_YELLOW, key + ": " + query.getFirst(key));
            }
        } else {
            printWithColor(Color.ANSI_YELLOW, CoreMessages.QUERY_EMPTY);
        }
        if (StringUtils.isNotEmpty(body)) {
            printWithColor(Color.ANSI_YELLOW, CoreMessages.BODY);
            printWithColor(Color.ANSI_YELLOW, body);
        } else {
            printWithColor(Color.ANSI_YELLOW, CoreMessages.BODY_EMPTY);
        }
        System.out.println();
    }

    public static void printWithColor(String color, String str) {
        System.out.println(SystemUtils.IS_OS_WINDOWS ? ("\t" + str) : (color + "\t" + str + Color.ANSI_RESET));
    }

    public static void printWithColor(String color, String str, boolean print) {
        if (print) {
            System.out.println(SystemUtils.IS_OS_WINDOWS ? ("\t" + str) : (color + "\t" + str + Color.ANSI_RESET));
        }
    }

    public static void print(String str) {
        System.out.println("\t" + str);
    }

    public static void print(String str, boolean print) {
        if (print) {
            System.out.println("\t" + str);
        }
    }
}
