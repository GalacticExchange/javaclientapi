package io.gex.agent.webServer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.gex.agent.*;
import io.gex.agent.app.ApplicationManager;
import io.gex.core.*;
import io.gex.core.api.MainLevelApi;
import io.gex.core.api.NodeAgentLevelApi;
import io.gex.core.api.NodeLevelApi;
import io.gex.core.box.BoxHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.*;
import io.gex.core.model.properties.GexdProperties;
import io.gex.core.model.properties.NodeProperties;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.virutalBoxHelper.VirtualBoxHelperWindows;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.util.FileUtils;
import org.glassfish.jersey.client.ClientProperties;
import spark.Request;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spark.Spark.*;

public class GexdServer {
    private final static LogWrapper logger = LogWrapper.create(GexdServer.class);
    private static Integer webServerPort;
    private static String ip;
    private static String nodeAgentToken;
    // todo move node installation to another class
    private final static AtomicReference<InstallStatus> nodeInstallStatus = new AtomicReference<>();

    private final static String ITSALIVE = "/itsalive";
    private final static String LOGS = "/logs";
    private final static String BOXES = "/boxes";
    private final static String VIRTUALIZATION = "/virtualization";
    private final static String CHECK_HYPERV = "/hyperv/check";
    private final static String RECONNECT = "/reconnect";
    private final static String NODES = "/nodes";
    private final static String NODES_ID = "/nodes/id";
    private final static String NODES_LOCAL = "/nodes/local";
    private final static String NODES_FORCE = "/nodes/force";
    private final static String APPS_LOCAL = "/apps/local";
    private final static String NODES_SETUP = "/nodes/setup";
    private final static String NODES_REMOTE = "/nodes/remote";
    private final static String NODES_REMOTE_LOGS = "/nodes/remote/logs";
    private final static String NODES_REMOTE_UNINSTALL = "/nodes/remote/uninstall";

    private static void localhostCheck(String ip) {
        if (!ip.equals("127.0.0.1") && !ip.equals("::1") && !ip.equals("0:0:0:0:0:0:0:1")) {
            logger.logError("Localhost check: Received ip:" + ip, LogType.WEB_SERVER);
            logAndHalt(403, GexdMessages.FORBIDDEN);
        }
    }

    public static void sendIPAndPort() {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            GexdStatusHelper.sendGexdStatus(GexdStatus.WAITING_FOR_AGENT_TOKEN);
            String currNodeAgentToken = GexdHelper.waitForProperty(NodeProperties.NODE_AGENT_TOKEN_PROPERTY_NAME);
            // if nodeAgentToken changed - resent IP/port
            if (!currNodeAgentToken.equals(nodeAgentToken)) {
                if (StringUtils.isBlank(ip) || webServerPort == null) {
                    setIPAndPort();
                }
                GexdStatusHelper.sendGexdStatus(GexdStatus.SENDING_IP_AND_PORT);
                NodeAgentLevelApi.sendIPAndPort(ip, webServerPort, currNodeAgentToken);
                nodeAgentToken = currNodeAgentToken;
            }
        } catch (Exception e) {
            logger.logError(e, LogType.NODE_AGENT_INFO_ERROR);
        }

    }

    private static void setIPAndPort() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(ip)) {
            GexdStatusHelper.sendGexdStatus(GexdStatus.SETTING_IP);
            ip = HardwareHelper.getBestIPV4();
            if (StringUtils.isBlank(ip)) {
                throw logger.logAndReturnException(GexdMessages.WEB_SERVER_IP_SEARCH_ERROR, LogType.WEB_SERVER_ERROR);
            }
            logger.logInfo("ip: " + ip, LogType.WEB_SERVER);
        }
        if (webServerPort == null) {
            GexdStatusHelper.sendGexdStatus(GexdStatus.SETTING_PORT);
            webServerPort = selectPort();
            logger.logInfo("port: " + webServerPort, LogType.WEB_SERVER);
            GexdStatusHelper.sendGexdStatus(GexdStatus.PORT_SELECTED, ": " + webServerPort);
            if (SystemUtils.IS_OS_WINDOWS) {
                GexdServerWindows.deleteWindowsFirewallRules(webServerPort);
                GexdServerWindows.checkWindowsFirewall(webServerPort);
            }
        }
    }

    private static boolean isLogBeforeOrAfter(Request request) {
        return !request.pathInfo().equals(NODES_ID) && !request.pathInfo().equals(NODES_LOCAL)
                && !request.pathInfo().equals(APPS_LOCAL) && !request.pathInfo().equals(NODES_REMOTE_LOGS);
    }

    private static JsonArray remoteLogFilenamesToJson(Pattern pattern, File[] files) {
        JsonArray resArray = new JsonArray();
        if (ArrayUtils.isNotEmpty(files)) {
            for (File file : files) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.find()) { //todo if not find
                    JsonObject jsonLog = new JsonObject();
                    jsonLog.addProperty("nodeName", matcher.group());
                    jsonLog.addProperty("path", file.getAbsolutePath());
                    resArray.add(jsonLog);
                }
            }
        }
        return resArray;
    }

    public static void start() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        setIPAndPort();
        port(webServerPort);

        before((request, response) -> {
            response.type("application/json; charset=utf-8");
            if (isLogBeforeOrAfter(request)) {
                logger.logDebug(GexdMessages.RECEIVED + request.pathInfo() + " " + request.requestMethod() + " from " + request.ip(), LogType.WEB_SERVER);
            }
        });

        after((request, response) -> {
            if (isLogBeforeOrAfter(request)) {
                logger.logDebug(GexdMessages.SENT + " Status: " + response.status() + " Body: " + response.body(), LogType.WEB_SERVER);
            }
        });

        notFound((req, res) -> {
            res.type("application/json");
            return formErrorResponse("404 Resource not found.").toString();
        });

        internalServerError((req, res) -> {
            res.type("application/json");
            return formErrorResponse("500 Internal server error.").toString();
        });

        get(NODES_ID, (req, res) -> {
            localhostCheck(req.ip());
            JsonObject jsonObject = new JsonObject();
            try {
                jsonObject.addProperty(NodeProperties.NODE_ID_PROPERTY_NAME,
                        PropertiesHelper.node.getProps().getNodeID());
            } catch (Throwable e) {
                logAndHalt(GexdMessages.NODE_ID_ERROR, e, LogType.NODE_ID_ERROR);
            }
            return jsonObject.toString();
        });

        post(NODES_SETUP, (req, res) -> {
            localhostCheck(req.ip());
            JsonObject jsonResponse = new JsonObject();
            JsonObject body;
            try {
                try {
                    body = new JsonParser().parse(req.body()).getAsJsonObject();
                } catch (Exception e) {
                    throw logger.logAndReturnException(CoreMessages.INVALID_REQUEST, e, LogType.PARSE_ERROR);
                }
                String customName;
                if (body.has("customName") && !body.get("customName").isJsonNull()) {
                    customName = body.get("customName").getAsString();
                } else {
                    customName = null;
                }
                String nodeID, nodeIDParameter = "nodeID";
                if (body.has(nodeIDParameter) && body.get(nodeIDParameter).isJsonPrimitive()) {
                    nodeID = body.getAsJsonPrimitive(nodeIDParameter).getAsString();
                } else {
                    throw logger.logAndReturnException(nodeIDParameter + " parameter is wrong.", LogType.PARSE_ERROR);
                }

                String nodeAgentToken, nodeAgentTokenParameter = "nodeAgentToken";
                if (body.has(nodeAgentTokenParameter) && body.get(nodeAgentTokenParameter).isJsonPrimitive()) {
                    nodeAgentToken = body.getAsJsonPrimitive(nodeAgentTokenParameter).getAsString();
                } else {
                    throw logger.logAndReturnException(nodeAgentTokenParameter + " parameter is wrong.", LogType.PARSE_ERROR);
                }

                new Thread(() -> {
                    try {
                        NodeLocker.executeWithLock(() -> {
                            GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_SETTING_UP);
                            NodeLevelApi.nodeSetup(nodeID, nodeAgentToken, PropertiesHelper.gexd.getProps().getInstanceID(),
                                    PropertiesHelper.gexd.getProps().getNodeType(), customName);
                            String clusterID = PropertiesHelper.node.getProps().getClusterID();
                            if (StringUtils.isBlank(clusterID)) {
                                throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
                            }
                            NodeLevelApi.downloadInstallationFiles(nodeID, clusterID);
                            NodeLevelApi.nodeNotify(true);
                        });
                    } catch (Exception e) {
                        try {
                            MainLevelApi.notify(NodeNotification.NODE_CLIENT_INSTALL_ERROR, CoreMessages.NODE_INSTALL_ERROR);
                        } catch (GexException ex) {
                            // do nothing
                        }
                    }
                }).start();
            } catch (Throwable e) {
                logAndHalt(GexdMessages.NODE_SETUP_ERROR, e, LogType.NODE_SETUP_ERROR);
            }
            return jsonResponse.toString();
        });

        post(NODES_REMOTE, (req, res) -> {
            localhostCheck(req.ip());
            try {
                JsonObject body;
                try {
                    body = new JsonParser().parse(req.body()).getAsJsonObject();
                } catch (Exception e) {
                    throw logger.logAndReturnException(CoreMessages.INVALID_REQUEST, e, LogType.PARSE_ERROR);
                }

                final String token = req.headers("token");
                if (StringUtils.isBlank(token)) {
                    throw logger.logAndReturnException(CoreMessages.EMPTY_TOKEN, LogType.PARSE_ERROR);
                }

                if (body.get("clusterId") == null || !body.get("clusterId").isJsonPrimitive()
                        || !body.getAsJsonPrimitive("clusterId").isString()) {
                    throw logger.logAndReturnException("Parameter clusterId is invalid.", LogType.PARSE_ERROR);
                }
                final String clusterId = body.get("clusterId").getAsString();

                if (body.get("nodesConfigs") == null || !body.get("nodesConfigs").isJsonArray()
                        || body.getAsJsonArray("nodesConfigs").size() == 0) {
                    throw logger.logAndReturnException("JSON array nodesConfigs is invalid.", LogType.PARSE_ERROR);
                }
                List<NodeInstConfig> nodeInstConfigs;
                try {
                    nodeInstConfigs = GsonHelper.parse(body.getAsJsonArray("nodesConfigs"), NodeInstConfig.class, CoreMessages.NODE_INST_CONF_PARSING_ERROR);
                } catch (Exception e) {
                    throw logger.logAndReturnException("Cannot parse JSON array nodesConfigs.", e, LogType.PARSE_ERROR);
                }
                for (NodeInstConfig instConfig : nodeInstConfigs) {
                    String validateError = NodeInstConfig.validate(instConfig);
                    if (StringUtils.isNotEmpty(validateError)) {
                        throw logger.logAndReturnException("Install config invalid for node "
                                + StringUtils.defaultString(instConfig.getNodeName()) + ": " + validateError + ".", LogType.VALIDATE_ERROR);
                    }
                }

                for (NodeInstConfig instConfig : nodeInstConfigs) {
                    SshHelper.testSshConn(instConfig.getSshCredentials());
                }

                for (NodeInstConfig instConfig : nodeInstConfigs) {
                    new Thread(() -> {
                        ThreadContext.put("sepLogFileName", Gexd.getCustomLogFileName("remote-inst-" + instConfig.getNodeName()));
                        try {
                            AppContext.set(token);
                            NodeLevelApi.installNodeRemotely(instConfig, clusterId, token);
                        } catch (Exception e) {
                            logger.logError(e, LogType.NODE_INSTALL_ERROR);
                        }
                    }).start();
                }
            } catch (Throwable e) {
                logAndHalt(GexdMessages.NODE_INST_REMOTE_ERROR + "\n" + e.getMessage(), e, LogType.NODE_INSTALL_ERROR);
            }

            return "{}";
        });


        post(NODES_REMOTE_UNINSTALL, (req, res) -> {
            localhostCheck(req.ip());
            try {
                JsonObject body;
                try {
                    body = new JsonParser().parse(req.body()).getAsJsonObject();
                } catch (Exception e) {
                    throw logger.logAndReturnException(CoreMessages.INVALID_REQUEST, e, LogType.PARSE_ERROR);
                }

                final String token = req.headers("token");
                if (StringUtils.isBlank(token)) {
                    throw logger.logAndReturnException(CoreMessages.EMPTY_TOKEN, LogType.PARSE_ERROR);
                }

                if (body.get("clusterId") == null || !body.get("clusterId").isJsonPrimitive()
                        || !body.getAsJsonPrimitive("clusterId").isString()) {
                    throw logger.logAndReturnException("Parameter clusterId is invalid.", LogType.PARSE_ERROR);
                }
                final String clusterId = body.get("clusterId").getAsString();

                if (body.get("nodesConfigs") == null || !body.get("nodesConfigs").isJsonArray()
                        || body.getAsJsonArray("nodesConfigs").size() == 0) {
                    throw logger.logAndReturnException("JSON array nodesConfigs is invalid.", LogType.PARSE_ERROR);
                }
                List<NodeUninstConfig> nodeUninstConfigs;
                try {
                    nodeUninstConfigs = GsonHelper.parse(body.getAsJsonArray("nodesConfigs"), NodeUninstConfig.class, CoreMessages.NODE_UNINST_CONF_PARSING_ERROR);
                } catch (Exception e) {
                    throw logger.logAndReturnException("Cannot parse JSON array nodesConfigs.", e, LogType.PARSE_ERROR);
                }
                for (NodeUninstConfig uninstConfig : nodeUninstConfigs) {
                    String validateError = NodeUninstConfig.validate(uninstConfig);
                    if (StringUtils.isNotEmpty(validateError)) {
                        throw logger.logAndReturnException("Uninstall config invalid for node "
                                + StringUtils.defaultString(uninstConfig.getNodeName()) + ": " + validateError + ".", LogType.VALIDATE_ERROR);
                    }
                }

                for (NodeUninstConfig uninstConfig : nodeUninstConfigs) {
                    NodeLevelApi.testNodeBeforeRemoteUninst(uninstConfig);
                }

                for (NodeUninstConfig uninstConfig : nodeUninstConfigs) {
                    new Thread(() -> {
                        ThreadContext.put("sepLogFileName", Gexd.getCustomLogFileName("remote-uninst-" + uninstConfig.getNodeName()));
                        try {
                            AppContext.set(token);
                            NodeLevelApi.uninstallNodeRemotely(uninstConfig, clusterId, token);
                        } catch (Exception e) {
                            logger.logError(e, LogType.NODE_UNINSTALL_ERROR);
                        }
                    }).start();
                }
            } catch (Throwable e) {
                logAndHalt(GexdMessages.NODE_UNINST_REMOTE_ERROR + "\n" + e.getMessage(), e, LogType.NODE_UNINSTALL_ERROR);
            }

            return "{}";
        });

        post(NODES, (req, res) -> {
            localhostCheck(req.ip());
            JsonObject jsonObject = new JsonObject();
            JsonObject body;
            try {
                nodeInstallStatus.set(new InstallStatus("Preparing", 0));
                try {
                    body = new JsonParser().parse(req.body()).getAsJsonObject();
                } catch (Exception e) {
                    throw logger.logAndReturnException(CoreMessages.INVALID_REQUEST, e, LogType.PARSE_ERROR);
                }
                String token = req.headers("token");
                if (StringUtils.isBlank(token)) {
                    throw logger.logAndReturnException(CoreMessages.EMPTY_TOKEN, LogType.PARSE_ERROR);
                }
                AppContext.set(token);

                NodeLocker.executeWithLock(() -> {
                    if (SystemUtils.IS_OS_WINDOWS) {
                        GexdStatusHelper.sendGexdStatus(GexdStatus.VIRTUAL_BOX_WINDOWS_CHECKING);
                        String machineFolderName = "vBoxMachineFolder";
                        if (body.has(machineFolderName) && !body.get(machineFolderName).isJsonNull()) {
                            String machineFolder = body.get(machineFolderName).getAsString();
                            try {
                                FileUtils.mkdir(new File(machineFolder), true);
                            } catch (Exception e) {
                                throw logger.logAndReturnException(CoreMessages.SET_VIRTUAL_BOX_HOME_ERROR, e,
                                        LogType.VBOX_MACHINE_FOLDER);
                            }
                            VirtualBoxHelperWindows.setMachineFolderProperty(machineFolder);
                        } else {
                            VirtualBoxHelperWindows.setMachineFolderProperty();
                        }
                    }
                    nodeInstallStatus.set(new InstallStatus("Creating node", 30));
                    GexdStatusHelper.sendGexdStatus(GexdStatus.CREATING_NODE);
                    JsonElement interfaceJson = body.get("selectedNetInterface");
                    NetworkAdapter networkAdapter = null;
                    if (interfaceJson != null && !interfaceJson.isJsonNull()) {
                        networkAdapter = GsonHelper.parse(interfaceJson.getAsJsonObject(), NetworkAdapter.class,
                                "Cannot parse network interface: " + interfaceJson.toString());
                    }
                    String clusterID = null, customName = null, hadoopApp = null;
                    if (body.has("customName") && !body.get("customName").isJsonNull()) {
                        customName = body.get("customName").getAsString();
                    }
                    if (body.has("clusterID") && !body.get("clusterID").isJsonNull()) {
                        clusterID = body.get("clusterID").getAsString();
                    }
                    if (StringUtils.isBlank(clusterID)) {
                        throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_ID, LogType.PARSE_ERROR);
                    }
                    if (body.has("hadoopApp") && !body.get("hadoopApp").isJsonNull()) {
                        hadoopApp = body.get("hadoopApp").getAsString();
                    }
                    if (StringUtils.isBlank(hadoopApp)) {
                        throw logger.logAndReturnException(CoreMessages.EMPTY_HADOOP_APP, LogType.PARSE_ERROR);
                    }
                    String nodeName = NodeLevelApi.nodeInstall(networkAdapter, clusterID,
                            PropertiesHelper.gexd.getProps().getInstanceID(), PropertiesHelper.gexd.getProps().getNodeType(), customName, hadoopApp);
                    nodeInstallStatus.set(new InstallStatus("Downloading", 60, 30));
                    String nodeID = PropertiesHelper.node.getProps().getNodeID();
                    if (StringUtils.isBlank(nodeID)) {
                        throw logger.logAndReturnException(CoreMessages.EMPTY_NODE_ID, LogType.EMPTY_PROPERTY_ERROR);
                    }
                    try {
                        NodeLevelApi.downloadInstallationFiles(nodeID, clusterID);
                        if (PropertiesHelper.isHostVirtual()) {
                            GexdStatusHelper.sendGexdStatus(GexdStatus.DOWNLOADING_BOX);
                            BoxHelper.downloadAndAddBox(nodeInstallStatus, clusterID);
                        }
                    } catch (GexException e) {
                        MainLevelApi.notify(NodeNotification.NODE_CLIENT_INSTALL_ERROR, CoreMessages.NODE_INSTALL_ERROR);
                        throw e;
                    }
                    NodeLevelApi.nodeNotify(true);
                    jsonObject.addProperty(NodeProperties.NODE_NAME_PROPERTY_NAME, nodeName);
                });
            } catch (Throwable e) {
                logAndHalt(GexdMessages.NODE_INSTALL_ERROR, e, LogType.NODE_INSTALL_ERROR);
            } finally {
                nodeInstallStatus.set(null);
                AppContext.clear();
            }
            return jsonObject.toString();
        });

        get(NODES_LOCAL, (req, res) -> {
            localhostCheck(req.ip());
            JsonObject obj = new JsonObject();
            try {
                obj.addProperty("id", PropertiesHelper.node.getProps().getNodeID());
                InstallStatus installStatus = nodeInstallStatus.get();
                obj.add("installStatus", installStatus != null ? installStatus.toJson() : null);
            } catch (Throwable e) {
                logAndHalt(GexdMessages.NODE_LOCAL_ERROR, e, LogType.NODE_LOCAL_ERROR);
            }
            return obj.toString();
        });

        get(APPS_LOCAL, (req, res) -> {
            localhostCheck(req.ip());
            JsonObject obj = new JsonObject();
            try {
                InstallStatus installStatus = ApplicationManager.appInstallStatus.get();
                obj.addProperty("id", ApplicationManager.currentAppID.get());
                obj.add("installStatus", installStatus != null ? installStatus.toJson() : null);
            } catch (Throwable e) {
                logAndHalt(GexdMessages.APP_LOCAL_ERROR, e, LogType.APP_LOCAL_ERROR);
            }
            return obj.toString();
        });

        delete(NODES, (req, res) -> {
            localhostCheck(req.ip());
            String response = "{}";
            try {
                String token = req.headers("token");
                if (StringUtils.isBlank(token)) {
                    throw logger.logAndReturnException(CoreMessages.EMPTY_TOKEN, LogType.PARSE_ERROR);
                }
                AppContext.set(token);
                response = responseWrapper(req, res, GexdAgent.executor::nodeUninstall);
            } catch (Throwable e) {
                logAndHalt(GexdMessages.NODE_UNINSTALL_ERROR, e, LogType.NODE_UNINSTALL_ERROR);
            } finally {
                AppContext.clear();
            }
            return response;
        });

        delete(NODES_FORCE, (req, res) -> {
            localhostCheck(req.ip());
            String response = "{}";
            try {
                String token = req.headers("token");
                try {
                    if (StringUtils.isNotBlank(token)) {
                        AppContext.set(token);
                    }
                } catch (Throwable e) {
                    logger.logWarn(e.getMessage(), LogType.AUTHENTICATION_ERROR);
                }
                response = responseWrapper(req, res, GexdAgent.executor::nodeUninstallForce);
            } catch (Throwable e) {
                logAndHalt(GexdMessages.NODE_UNINSTALL_FORCE_ERROR, e, LogType.NODE_UNINSTALL_ERROR);
            } finally {
                AppContext.clear();
            }
            return response;
        });

        get(ITSALIVE, (req, res) -> "{}");

        get(RECONNECT, (req, res) -> {
            localhostCheck(req.ip());
            Gexd.connection.reconnect();
            return "{}";
        });

        //todo for application
        get(BOXES, (req, res) -> {
            try {
                if (StringUtils.isBlank(req.queryParams("name"))) {
                    throw new Exception(GexdMessages.INVALID_REQUEST);
                }
                String boxName = req.queryParams("name");
                File file = Paths.get(PropertiesHelper.userHome, ".gex", boxName).toFile();
                if (!file.exists()) {
                    throw new Exception(GexdMessages.BOX_NOT_FOUND);
                }
                logger.logInfo(GexdMessages.START_SENDING_BOX + boxName, LogType.DOWNLOAD);
                HttpServletResponse raw = res.raw();
                res.type("application/octet-stream");
                raw.addHeader("Content-Length", String.valueOf(file.length()));

                org.apache.commons.io.IOUtils.copyLarge(new FileInputStream(file), raw.getOutputStream());
                raw.getOutputStream().flush();
                raw.getOutputStream().close();
                return raw;
            } catch (Throwable e) {
                logAndHalt(GexdMessages.SEND_BOX_ERROR, e, LogType.BOX_ERROR);
            }
            // should never reach this line
            return null;
        });

        get(LOGS, (req, res) -> {
            try {
                if (StringUtils.isBlank(req.queryParams("token"))) {
                    throw new GexException(GexdMessages.FORBIDDEN_TOKEN);
                }
                String token = req.queryParams("token");
                if (!NodeAgentLevelApi.nodeViewLogs(token, BasePropertiesHelper.getValidToken())) {
                    throw new GexException(GexdMessages.FORBIDDEN_LOG);
                }
                File file = new File(System.getProperty("agentLogFilename"));
                if (!file.exists()) {
                    throw new GexException(GexdMessages.LOG_NOT_FOUND);
                }
                logger.logInfo(GexdMessages.START_SENDING_LOG, LogType.DOWNLOAD);
                HttpServletResponse raw = res.raw();
                res.type("application/octet-stream");
                // Don't send the file size because it is changing dynamically
                org.apache.commons.io.IOUtils.copy(new FileInputStream(file), raw.getOutputStream());
                raw.getOutputStream().flush();
                raw.getOutputStream().close();
                return raw;
            } catch (Exception e) {
                logAndHalt(GexdMessages.SEND_LOG_ERROR + " " + e.getMessage(), e, LogType.LOG_ERROR);
            }
            // should never reach this line
            return null;
        });

        get(NODES_REMOTE_LOGS, (req, res) -> {
            localhostCheck(req.ip());
            JsonObject resultJson = new JsonObject();
            try {
                File logsFolder = Gexd.getGexdLogFolder().toFile();

                Pattern instPattern = Pattern.compile("^remote-inst-.+\\.log$");
                File[] instLogs = logsFolder.listFiles((dir, name) -> instPattern.matcher(name).matches());
                Pattern instNodeNamePat = Pattern.compile("(?<=remote-inst-).+(?=\\.log)");
                resultJson.add("installs", remoteLogFilenamesToJson(instNodeNamePat, instLogs));

                Pattern uninstPattern = Pattern.compile("^remote-uninst-.+\\.log$");
                Pattern uninstNodeNamePat = Pattern.compile("(?<=remote-uninst-).+(?=\\.log)");
                File[] uninstLogs = logsFolder.listFiles((dir, name) -> uninstPattern.matcher(name).matches());
                resultJson.add("uninstalls", remoteLogFilenamesToJson(uninstNodeNamePat, uninstLogs));
            } catch (Throwable e) {
                logAndHalt(GexdMessages.NODES_REMOTE_LOGS, e, LogType.LOG_ERROR);
            }

            return resultJson.toString();
        });

        if (SystemUtils.IS_OS_WINDOWS) {
            get(VIRTUALIZATION, (req, res) -> {
                localhostCheck(req.ip());
                try {
                    // ApplicationMode doesn't impact on Windows virtualization check
                    HardwareHelper.checkVirtualizationEnabledInternal();
                    return new JsonObject().toString();
                } catch (GexException e) {
                    res.status(500);
                    return formErrorResponse(e.getMessage()).toString();
                }
            });
            get(CHECK_HYPERV, (req, res) -> {
                localhostCheck(req.ip());
                try {
                    HardwareHelper.checkHypervEnabledInternal();
                    return new JsonObject().toString();
                } catch (GexException e) {
                    res.status(500);
                    return formErrorResponse(e.getMessage()).toString();
                }
            });
        }
        for (long stop = System.nanoTime() + TimeUnit.MINUTES.toNanos(1); stop > System.nanoTime(); ) {
            GexdHelper.sleep(3000);
            if (isUp()) {
                GexdStatusHelper.sendGexdStatus(GexdStatus.WEB_SERVER_STARTED);
                break;
            }
        }
    }

    private static Integer selectPort() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String defaultPortString = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            defaultPortString = PropertiesHelper.gexd.getProps().getWebServerPort();
        }
        Integer port = PropertiesHelper.DEFAULT_WEB_SERVER_PORT, defaultPort = null;
        if (StringUtils.isNotBlank(defaultPortString)) {
            try {
                port = Integer.valueOf(defaultPortString);
                defaultPort = port;
            } catch (Exception e) {
                // do nothing
            }
        }
        while (port != PropertiesHelper.DEFAULT_WEB_SERVER_PORT + 404) {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                serverSocket.close();
                if (StringUtils.isBlank(defaultPortString) || (defaultPort != null && !defaultPort.equals(port))) {
                    GexdProperties gexdProperties = new GexdProperties();
                    gexdProperties.setWebServerPort(port.toString());
                    PropertiesHelper.gexd.saveToJSON(gexdProperties);
                }
                return port;
            } catch (IOException ex) {
                port++;
            }
        }
        throw logger.logAndReturnException(GexdMessages.PORT_SEARCH_ERROR, LogType.WEB_SERVER_ERROR);
    }

    private static boolean isUp() {
        logger.trace("Entered " + LogHelper.getMethodName());
        String url = "http://localhost:" + webServerPort + "/itsalive";
        try {
            Client client = ClientBuilder.newClient();
            client.property(ClientProperties.CONNECT_TIMEOUT, 20000);
            WebTarget target = client.target(url);
            Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
            Response response = builder.get();
            if (response.getStatus() != 200) {
                throw new Exception(CoreMessages.SERVER_COMMAND_ERROR);
            }
            return true;
        } catch (Exception e) {
            logger.logError(GexdMessages.WEB_SERVER_DOWN, e, LogType.WEB_SERVER_ERROR);
        }
        return false;
    }

    private static String responseWrapper(Request req, spark.Response res, GexExecutorOutput executor) {
        localhostCheck(req.ip());
        try {
            String result = executor.execute();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(GexdMessages.RESULT, result);
            return jsonObject.toString();
        } catch (GexException e) {
            res.status(500);
            return formErrorResponse(e.getMessage()).toString();
        }
    }

    private static JsonObject formErrorResponse(String message) {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = new JsonObject();
        obj.addProperty("code", "ERROR_CODE"); //need for electron_ui for restify
        obj.addProperty("error_name", "GEXD_ERROR");
        obj.addProperty("message", message);
        obj.addProperty("description", StringUtils.EMPTY);
        return obj;
    }

    private static void logAndHalt(String message, Throwable e, LogType type) {
        logger.logErrorForce(message, e, type);
        logAndHalt(500, message);
    }

    private static void logAndHalt(int code, String message) {
        halt(code, formErrorResponse(message).toString());
    }
}
