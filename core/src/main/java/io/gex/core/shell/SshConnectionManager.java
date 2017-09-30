package io.gex.core.shell;


import com.jcraft.jsch.*;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

public class SshConnectionManager {

    private final static LogWrapper logger = LogWrapper.create(SshConnectionManager.class);
    private static Session session;
    private static Channel channel;
    private static String username = StringUtils.EMPTY;
    private static String privateKeyPath = StringUtils.EMPTY;
    private static String password = StringUtils.EMPTY;
    private static String domainName = StringUtils.EMPTY;
    private static final int timeOut = 10000;
    private static int connectionPort = 22;

    private static Session getSession() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (session == null || !session.isConnected()) {
            if (StringUtils.isNotBlank(password)) {
                session = connect(domainName, username, password);
            } else {
                session = connectWithPrivateKey(domainName, username, privateKeyPath);
            }
        }
        return session;
    }

    private static Channel getChannel() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (channel == null || !channel.isConnected()) {
            try {
                channel = getSession().openChannel("shell");
                channel.connect();
            } catch (Exception e) {
                throw logger.logAndReturnException(CoreMessages.CHANNEL_ERROR, e, LogType.SSH_ERROR);
            }
        }
        return channel;
    }

    public static Session connect(String domainName, String username, String password) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return connect(domainName, username, password, 22, null);
    }

    public static Session connectWithPrivateKey(String domainName, String username, String privateKeyPath) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return connectWithPrivateKey(domainName, username, privateKeyPath, 22, null);
    }

    public static Session connect(String domainName, String username, String password, int port, Proxy proxy)
            throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JSch jSch = new JSch();
        SshConnectionManager.domainName = domainName;
        SshConnectionManager.username = username;
        SshConnectionManager.password = password;
        SshConnectionManager.connectionPort = port;
        try {
            session = jSch.getSession(username, domainName, connectionPort);
            session.setProxy(proxy);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(password);
            session.connect(timeOut);
            logger.logInfo("Connected to " + domainName, LogType.SSH);
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.getConnectionErrorMessage(domainName), e, LogType.SSH_ERROR);
        }
        return session;
    }

    public static Session connectWithPrivateKey(String domainName, String username, String privateKeyPath, int port,
                                                Proxy proxy) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JSch jSch = new JSch();
        SshConnectionManager.domainName = domainName;
        SshConnectionManager.username = username;
        SshConnectionManager.privateKeyPath = privateKeyPath;
        SshConnectionManager.connectionPort = port;
        try {
            session = jSch.getSession(username, domainName, connectionPort);
            session.setProxy(proxy);
            jSch.addIdentity(privateKeyPath);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(timeOut);
            logger.logInfo("Connected to " + domainName, LogType.SSH);
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.getConnectionErrorMessage(domainName), e, LogType.SSH_ERROR);
        }
        return session;
    }

    public static void executeCommands() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ChannelShell channel = (ChannelShell) getChannel();
        new Thread(new ReaderThread(channel)).start();
        Thread writerThread = new Thread(new WriterThread(channel));
        writerThread.setDaemon(true);
        writerThread.start();
    }

    public static void close() {
        logger.trace("Entered " + LogHelper.getMethodName());
        channel.disconnect();
        session.disconnect();
        logger.logInfo(CoreMessages.CLOSE_CONNECTION, LogType.SSH);
    }

}
