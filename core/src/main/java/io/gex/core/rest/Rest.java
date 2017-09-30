package io.gex.core.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.gex.core.AppContext;
import io.gex.core.CoreMessages;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.ExceptionHelper;
import io.gex.core.exception.GexAuthException;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.properties.NodeProperties;
import io.gex.core.model.properties.UserProperties;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.concurrent.Future;

import static io.gex.core.CoreMessages.UNEXPECTED_AUTH_EXCEPTION;

public class Rest {

    private static final LogWrapper logger = LogWrapper.create(Rest.class);

    private static final Client client = initClient();

    public static JsonObject sendRequest(String requestType, String url, LogType type,
                                         MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                                         MultivaluedMap<String, String> query) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ReconnectWrapper<JsonObject> reconnectWrapper = new ReconnectWrapper<JsonObject>() {
            @Override
            JsonObject sendRequest(String requestType, String url, LogType type,
                                   MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                                   MultivaluedMap<String, String> query, String token, boolean authenticated,
                                   boolean localWebServer) throws GexException {
                return sendRequest0(requestType, url, type, header, body, query, token, authenticated,
                        localWebServer).getAsJsonObject();
            }
        };
        try {
            return reconnectWrapper.execute(requestType, url, type, header, body, query, null, false, false);
        } catch (GexAuthException e) {
            throw logger.logAndReturnAuthException(UNEXPECTED_AUTH_EXCEPTION, e, LogType.UNEXPECTED);
        }
    }

    public static JsonObject sendAuthenticatedRequest(String requestType, String url, LogType type,
                                                      MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                                                      MultivaluedMap<String, String> query, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ReconnectWrapper<JsonObject> reconnectWrapper = new ReconnectWrapper<JsonObject>() {
            @Override
            JsonObject sendRequest(String requestType, String url, LogType type,
                                   MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                                   MultivaluedMap<String, String> query, String token, boolean authenticated,
                                   boolean localWebServer) throws GexException {
                return sendRequest0(requestType, url, type, header, body, query, token, authenticated,
                        localWebServer).getAsJsonObject();
            }
        };
        return reconnectWrapper.execute(requestType, url, type, header, body, query, token, true, false);
    }

    public static JsonArray getArrayFromRequest(String requestType, String url, LogType type,
                                                MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                                                MultivaluedMap<String, String> query) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ReconnectWrapper<JsonArray> reconnectWrapper = new ReconnectWrapper<JsonArray>() {
            @Override
            public JsonArray sendRequest(String requestType, String url, LogType type,
                                         MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                                         MultivaluedMap<String, String> query, String token, boolean authenticated,
                                         boolean localWebServer) throws GexException {
                return sendRequest0(requestType, url, type, header, body, query, token, authenticated,
                        localWebServer).getAsJsonArray();
            }
        };
        try {
            return reconnectWrapper.execute(requestType, url, type, header, body, query, null, false, false);
        } catch (GexAuthException e) {
            throw logger.logAndReturnAuthException(UNEXPECTED_AUTH_EXCEPTION, e, LogType.UNEXPECTED);
        }
    }

    public static InputStream getInputStreamFromAuthenticatedRequest(String requestType, String url, LogType type,
                                                                     MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                                                                     MultivaluedMap<String, String> query, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ReconnectWrapper<InputStream> reconnectWrapper = new ReconnectWrapper<InputStream>() {
            @Override
            public InputStream sendRequest(String requestType, String url, LogType type,
                                           MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                                           MultivaluedMap<String, String> query, String token, boolean authenticated,
                                           boolean localWebServer) throws GexException {
                return getInputStreamFromAuthenticatedRequest0(requestType, url, type, header, body, query, token);
            }
        };
        return reconnectWrapper.execute(requestType, url, type, header, body, query, token, true, false);
    }

    public static GxFuture<JsonObject> sendAsyncAuthenticatedRequest(String requestType, String url, LogType type,
                                                                     MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                                                                     MultivaluedMap<String, String> query, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        LogHelper.requestDebug(requestType, header, body, query, PropertiesHelper.apiUrl + url, token,
                PropertiesHelper.VERSION);
        try {
            Invocation.Builder builder = constructRequestBuilder(PropertiesHelper.apiUrl, url, type, header,
                    query, token);
            AsyncInvoker asyncBuilderInvoker = builder.async();
            Future<Response> response = asyncBuilderInvoker.method(requestType, body != null ? Entity.form(body) : null);
            return new FutureResponseToJson(response, PropertiesHelper.apiUrl + url, type, requestType, true);
        } catch (RuntimeException e) {
            throw logger.logAndReturnException(e, type);
        }
    }

    public static JsonObject sendLocalRequest(String requestType, String url, LogType type,
                                              MultivaluedMap<String, String> header, JsonObject body,
                                              MultivaluedMap<String, String> query) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            return sendRequestJson0(requestType, url, type, header, body, query, null, false, true).getAsJsonObject();
        } catch (GexAuthException e) {
            throw logger.logAndReturnAuthException(UNEXPECTED_AUTH_EXCEPTION, e, LogType.UNEXPECTED);
        } catch (RuntimeException e) {
            throw logger.logAndReturnException(e, type);
        }
    }

    private static JsonElement sendRequest0(String requestType, String url, LogType type,
                                            MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                                            MultivaluedMap<String, String> query, String token, boolean authenticated,
                                            boolean localWebServer) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String serverUrl = getServerUrl(localWebServer);
        LogHelper.requestDebug(requestType, header, body, query, serverUrl + url, token, PropertiesHelper.VERSION);
        try {
            Invocation.Builder builder = constructRequestBuilder(serverUrl, url, type, header, query, token);
            Response response = builder.build(requestType, body != null ? Entity.form(body) : null).invoke();
            return processResponse(response, serverUrl + url, type, requestType, authenticated);
        } catch (RuntimeException e) {
            throw logger.logAndReturnException(e, LogType.CONNECTION_ERROR);
        }
    }

    private static JsonElement sendRequestJson0(String requestType, String url, LogType type,
                                                MultivaluedMap<String, String> header, JsonObject body,
                                                MultivaluedMap<String, String> query, String token, boolean authenticated,
                                                boolean localWebServer) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String serverUrl = getServerUrl(localWebServer);
        LogHelper.requestDebug(requestType, header, body, query, serverUrl + url, token, PropertiesHelper.VERSION);
        try {
            Invocation.Builder builder = constructRequestBuilder(serverUrl, url, type, header, query, token);
            Response response = builder.build(requestType, Entity.json(body != null ? body.toString() : null)).invoke();
            return processResponse(response, serverUrl + url, type, requestType, authenticated);
        } catch (RuntimeException e) {
            throw logger.logAndReturnException(e, LogType.CONNECTION_ERROR);
        }
    }

    private static Invocation.Builder constructRequestBuilder(String serverUrl, String url, LogType type,
                                                              MultivaluedMap<String, String> header,
                                                              MultivaluedMap<String, String> query, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        WebTarget target = client.target(serverUrl + url);
        if (MapUtils.isNotEmpty(query)) {
            Iterator<String> queryIterator = query.keySet().iterator();
            try {
                while (queryIterator.hasNext()) {
                    String key = queryIterator.next();
                    target = target.queryParam(key, URLEncoder.encode(query.getFirst(key), "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                throw logger.logAndReturnException(CoreMessages.INVALID_QUERY, e, type);
            }
        }
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE).header("Accept", "application/json")
                .header("clientVersion", PropertiesHelper.VERSION);
        if (StringUtils.isNotEmpty(token)) {
            builder = builder.header((PropertiesHelper.isService() && !AppContext.isSet()) ?
                    NodeProperties.NODE_AGENT_TOKEN_PROPERTY_NAME : UserProperties.TOKEN_PROPERTY_NAME, token);
        }
        if (MapUtils.isNotEmpty(header)) {
            for (String key : header.keySet()) {
                builder = builder.header(key, header.getFirst(key));
            }
        }
        return builder;
    }

    static JsonElement processResponse(Response response, String url, LogType type, String requestType,
                                       boolean authenticated) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (response == null) {
            throw logger.logAndReturnException(CoreMessages.SERVER_RESPONSE_ERROR + " Response is null.", type);
        }
        JsonElement obj = null;
        String output = null;
        if (response.hasEntity()) {
            output = response.readEntity(String.class);
            try {
                obj = new JsonParser().parse(output);
            } catch (Exception e) {
                throw logger.logAndReturnException(CoreMessages.SERVER_RESPONSE_ERROR + " " + e.getMessage() + " JSON: " + output, e, type);
            }
        }
        LogHelper.responseDebug(response.getStatus(), obj == null ? null : obj.getAsJsonObject());
        if (response.getStatus() != 200) {
            if (authenticated) {
                ExceptionHelper.handleAuthenticatedServerResponse(url, response.getStatus(), output);
            } else {
                ExceptionHelper.handleServerResponse(url, response.getStatus(), output);
            }
        }
        return obj;
    }

    private static InputStream getInputStreamFromAuthenticatedRequest0(String requestType, String url, LogType type,
                                                                       MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                                                                       MultivaluedMap<String, String> query, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        LogHelper.requestDebug(requestType, header, body, query, PropertiesHelper.apiUrl + url, token, PropertiesHelper.VERSION);
        try {
            Invocation.Builder builder = constructRequestBuilder(PropertiesHelper.apiUrl, url, type, header, query, token);
            Response response = builder.build(requestType, body != null ? Entity.form(body) : null).invoke();
            JsonObject obj = new JsonObject();
            obj.addProperty("content", "[file content]");
            LogHelper.responseDebug(response.getStatus(), obj);
            if (response.getStatus() != 200) {
                ExceptionHelper.handleAuthenticatedServerResponse(PropertiesHelper.apiUrl + url, response.getStatus(),
                        response.hasEntity() ? response.getEntity().toString() : null);
            }
            return response.readEntity(InputStream.class);
        } catch (RuntimeException e) {
            throw logger.logAndReturnException(e, LogType.CONNECTION_ERROR);
        }
    }

    private static abstract class ReconnectWrapper<T> {
        abstract T sendRequest(String requestType, String url, LogType type,
                               MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                               MultivaluedMap<String, String> query, String token, boolean authenticated,
                               boolean localWebServer) throws GexException;

        T execute(String requestType, String url, LogType type,
                         MultivaluedMap<String, String> header, MultivaluedMap<String, String> body,
                         MultivaluedMap<String, String> query, String token, boolean authenticated,
                         boolean localWebServer) throws GexException {
            RuntimeException exception = null;
            for (int i = 0; i < 3; i++) {
                try {
                    return sendRequest(requestType, url, type, header, body, query, token, authenticated,
                            localWebServer);
                } catch (RuntimeException e) {
                    exception = e;
                    logger.logWarn(CoreMessages.RECONNECTING + i, LogType.SERVER_CONNECTION_ERROR);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        // do nothing
                    }
                }
            }
            throw logger.logAndReturnException(CoreMessages.SERVER_CONNECTION_ERROR, exception, type);
        }
    }

    private static Client initClient() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Client client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 20000);
        return client;
    }

    private static String getServerUrl(boolean localWebServer) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (localWebServer) {
            String webServerPort = PropertiesHelper.gexd.getProps().getWebServerPort();
            if (StringUtils.isBlank(webServerPort)) {
                logger.logWarn(CoreMessages.READ_WEB_SERVER_PORT_ERROR, LogType.WEB_SERVER_ERROR);
                webServerPort = String.valueOf(PropertiesHelper.DEFAULT_WEB_SERVER_PORT);
            }
            return "http://localhost:" + webServerPort;
        }
        return PropertiesHelper.apiUrl;
    }

}
