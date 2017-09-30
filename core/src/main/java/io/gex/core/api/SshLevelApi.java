package io.gex.core.api;


import com.jcraft.jsch.*;
import io.gex.core.BaseHelper;
import io.gex.core.WindowsInstallInfoHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Service;
import io.gex.core.model.SocksProxy;
import io.gex.core.shell.ServiceResolver;
import io.gex.core.shell.SshConnectionManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static io.gex.core.shell.ShellExecutor.waitProcessFinished;
import static org.apache.commons.lang3.StringUtils.defaultString;

public class SshLevelApi {

    private static final LogWrapper logger = LogWrapper.create(SshLevelApi.class);

    private static final int DEFAULT_CLUSTER_PROXY_PORT = 80;

    public static void connect(Service service, String username, String password) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (!"ssh".equals(service.getProtocol())) {
            throw logger.logAndReturnException("Wrong protocol to connect. Need ssh, but protocol " + service.getProtocol(), LogType.SERVICE_RESOLVER_ERROR);
        }
        if (!SystemUtils.IS_OS_LINUX) {
            throw logger.logAndReturnException("On Windows and Mac works only native ssh.", LogType.SERVICE_RESOLVER_ERROR);
        }
        String host = ServiceResolver.getHost(service);
        Integer port = ServiceResolver.getPort(service);
        if (StringUtils.isEmpty(host) || port == null) {
            throw logger.logAndReturnException("For service " + service.getName() + " cannot resolve host or port. " +
                    " Host: " + host + " Port: " + port, LogType.SERVICE_RESOLVER_ERROR);
        }
        emptyConnect(username, password, host, port, service.getSocksProxy());
        SshConnectionManager.connect(host, username, password, port, createJschSocksProxy(service.getSocksProxy()));
        SshConnectionManager.executeCommands();
    }

    public static void connectNative(Service service, String username, String password) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (!"ssh".equals(service.getProtocol())) {
            throw logger.logAndReturnException("Wrong protocol to connect. Need ssh, but protocol " + service.getProtocol(), LogType.SERVICE_RESOLVER_ERROR);
        }
        String host = ServiceResolver.getHost(service);
        Integer port = ServiceResolver.getPort(service);
        if (StringUtils.isEmpty(host) || port == null) {
            throw logger.logAndReturnException("For service " + service.getName() + " cannot resolve host or port. " +
                    " Host: " + host + " Port: " + port, LogType.SERVICE_RESOLVER_ERROR);
        }
        connectNative(host, port, username, password, service.getSocksProxy());
    }

    public static void connectNative(String host, int port, String username, String password, SocksProxy proxy) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (SystemUtils.IS_OS_WINDOWS) {
            emptyConnect(username, password, host, port, proxy);
        }
        List<String> cmd = new ArrayList<>();
        File additionalResource = null;
        if (SystemUtils.IS_OS_MAC) {
            cmd.add("osascript");
            cmd.add("-e");
            cmd.add("tell application \"Terminal\" to activate do script \"\\\"/Library/Application Support/gex/ssh_con.command\\\" " +
                    username + "@" + host + " " + port + " " + password + " mac" +
                    (proxy != null ? " " + proxy.getHost() + " " + DEFAULT_CLUSTER_PROXY_PORT : StringUtils.EMPTY) +
                    (proxy != null && StringUtils.isNotEmpty(proxy.getUser()) ? " " + proxy.getUser() : StringUtils.EMPTY) +
                    (proxy != null && StringUtils.isNotEmpty(proxy.getUser()) && StringUtils.isNotEmpty(proxy.getPassword()) ?
                            " " + proxy.getPassword() : StringUtils.EMPTY) + "\"");
        } else if (SystemUtils.IS_OS_LINUX) {
            if (BaseHelper.isKDE()) {
                cmd.add("konsole");
                cmd.add("-e");
            } else if (BaseHelper.isXfce()) {
                cmd.add("xfce4-terminal");
                cmd.add("-x");
            } else if (BaseHelper.isLXDE()) {
                cmd.add("lxterminal");
                cmd.add("-e");
            } else {
                cmd.add("gnome-terminal");
                cmd.add("-x");
            }
            cmd.add("/usr/lib/gex/success-or-shell.command");
            cmd.add("/usr/lib/gex/ssh_con.command");
            cmd.add(username + "@" + host);
            cmd.add(String.valueOf(port));
            cmd.add(password);
            cmd.add("linux");
            if (proxy != null) {
                cmd.add(proxy.getHost());
                cmd.add(String.valueOf(DEFAULT_CLUSTER_PROXY_PORT));
                if (StringUtils.isNotEmpty(proxy.getUser())) {
                    cmd.add(proxy.getUser());
                    if (StringUtils.isNotEmpty(proxy.getPassword())) {
                        cmd.add(proxy.getPassword());
                    }
                }
            }
        } else if (SystemUtils.IS_OS_WINDOWS) {
            File puttyPath = new File(WindowsInstallInfoHelper.getInstallationPath() + "/usr/lib/gex/putty/putty.exe");
            cmd.add(puttyPath.getAbsolutePath());
            if (proxy != null) {
                cmd.add("-load");
                additionalResource = createPuttySessionFile(proxy);
                cmd.add(additionalResource.getName());
            }
            cmd.add("-ssh");
            cmd.add("-P");
            cmd.add(String.valueOf(port));
            cmd.add("-pw");
            cmd.add(password);
            cmd.add(username + "@" + host);
        }
        try {
            waitProcessFinished(new ProcessBuilder(cmd).start());
            FileUtils.deleteQuietly(additionalResource);
        } catch (Exception e) {
            throw logger.logAndReturnException(e, LogType.SERVICE_RESOLVER_ERROR);
        }
    }

    private static void emptyConnect(String username, String password, String host, int port, SocksProxy proxy) {
        logger.trace("Entered " + LogHelper.getMethodName());
        final int connectionTimeout = 10000;
        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setProxy(createJschSocksProxy(proxy));
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect(connectionTimeout);

            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("echo empty connect");

            channel.setInputStream(null);
            InputStream err = ((ChannelExec) channel).getErrStream();
            InputStream in = channel.getInputStream();

            channel.connect(connectionTimeout);

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                }
                while (err.available() > 0) {
                    int i = err.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    logger.logError(err.toString(), LogType.SSH_ERROR);
                }
                if (channel.isClosed()) {
                    if (in.available() > 0 || err.available() > 0) {
                        continue;
                    }
                    logger.logInfo("empty connect exit status " + channel.getExitStatus(), LogType.SSH);
                    break;
                }
            }

        } catch (Exception e) {
            logger.logError(e, LogType.SSH_ERROR);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private static File createPuttySessionFile(SocksProxy proxy) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String fileString;
        try {
            fileString = FileUtils.readFileToString(new File(WindowsInstallInfoHelper.getInstallationPath()
                    + "/usr/lib/gex/putty/proxy.session"), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw logger.logAndReturnException(e, LogType.FILE_ERROR);
        }
        fileString = StringUtils.replaceEach(fileString, new String[]{"{proxyHost}", "{proxyPort}", "{proxyUsername}", "{proxyPassword}"},
                new String[]{proxy.getHost(), String.valueOf(DEFAULT_CLUSTER_PROXY_PORT), defaultString(proxy.getUser()),
                        defaultString(proxy.getPassword())});

        String fileName = "proxy" + DateFormatUtils.format(new Date(), "yyMMddhhmmssSSS") + ".session";
        File resFile;
        try {
            resFile = new File(System.getenv("ProgramData") + "/.gex/putty/ssh/ses/" + fileName);
            resFile.deleteOnExit();
            FileUtils.writeStringToFile(resFile, fileString, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw logger.logAndReturnException(e, LogType.FILE_ERROR);
        }

        return resFile;
    }

    private static ProxySOCKS5 createJschSocksProxy(SocksProxy proxy) {
        if (proxy == null) {
            return null;
        }

        ProxySOCKS5 jschProxy = new ProxySOCKS5(proxy.getHost(), DEFAULT_CLUSTER_PROXY_PORT);
        if (StringUtils.isNotEmpty(proxy.getUser())) {
            jschProxy.setUserPasswd(proxy.getUser(), proxy.getPassword());
        }
        return jschProxy;
    }

}
