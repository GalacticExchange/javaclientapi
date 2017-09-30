package io.gex.core;

import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

public class ConnectionChecker {

    private final static LogWrapper logger = LogWrapper.create(ConnectionChecker.class);

    public static boolean isAPIReachable() {
        logger.trace("Entered " + LogHelper.getMethodName());
        return isReachable(PropertiesHelper.apiUrl + "/ping");
    }

    public static boolean isInternetReachable() {
        logger.trace("Entered " + LogHelper.getMethodName());
        return isReachable("http://www.google.com");
    }

    public static boolean isReachable(String path) {
        logger.trace("Entered " + LogHelper.getMethodName());
        HttpURLConnection connection = null;
        try {
            logger.logInfo("Ping " + path, LogType.CONNECTION);
            URL url = new URL(path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            // trying to retrieve data from the source. If there is no connection, this line will fail.
            connection.getContent();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * @return -1 if connection failed
     * else connection time in milliseconds
     */
    public static long getConnectionTime(String address) {
        logger.trace("Entered " + LogHelper.getMethodName());
        HttpURLConnection connection = null;
        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(20000);
            long time = System.currentTimeMillis();
            connection.connect();
            time = System.currentTimeMillis() - time;
            if (connection.getResponseCode() != 200)
                throw new Exception();
            logger.logInfo(address + " pinged in " + time + " millisecond(s).", LogType.CONNECTION);
            return time;
        } catch (Exception e) {
            logger.logWarn(address + " ping failed.", LogType.CONNECTION_ERROR);
            return -1;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void pingRabbit() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (!pingHost(PropertiesHelper.readProperty(PropertiesHelper.RABBIT_HOST_PROP_NAME),
                PropertiesHelper.readPropertyInteger(PropertiesHelper.RABBIT_PORT_PROP_NAME), 3000)) {
            throw logger.logAndReturnException(CoreMessages.CAN_NOT_CONNECT_TO_MESSAGE_BROKER, LogType.RABBIT_ERROR);
        }
    }

    private static boolean pingHost(String host, Integer port, int timeout) {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(host) || port == null) {
            return false;
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (Exception e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    public static void checkConnectionToFile(URL url) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            if (connection.getContentType().toLowerCase().contains("html")) {
                throw new Exception(CoreMessages.INVALID_LINK + url);
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(e, LogType.READ_INPUT_STREAM_ERROR);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}