package io.gex.agent;

import io.gex.agent.executor.Executor;
import io.gex.agent.webServer.GexdServer;
import io.gex.core.CoreMessages;
import io.gex.core.NodeLocker;
import io.gex.core.PropertiesHelper;
import io.gex.core.api.InstanceLevelApi;
import io.gex.core.api.MainLevelApi;
import io.gex.core.api.NodeLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.ApplicationMode;
import io.gex.core.model.EntityType;
import io.gex.core.model.NodeNotification;
import io.gex.core.model.NodeStatus;
import io.gex.core.model.properties.GexdProperties;
import io.gex.core.propertiesHelper.GexdPropertiesHelper;
import io.gex.core.propertiesHelper.NodePropertiesHelper;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;
import io.gex.core.virutalBoxHelper.VirtualBoxHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

public class GexdAgent {
    private final static LogWrapper logger = LogWrapper.create(GexdAgent.class);

    public static Executor executor;

    static void init() {
        logger.trace("Entered " + LogHelper.getMethodName());
        while (true) {
            try {
                PropertiesHelper.mode = ApplicationMode.SERVICE;
                PropertiesHelper.readProperties();
                readAdditionalProperties();
                // do not initialize  PropertiesHelper.user
                PropertiesHelper.node = new NodePropertiesHelper();
                PropertiesHelper.gexd = new GexdPropertiesHelper();
                checkNodeType();
                checkInstanceID();
                PropertiesHelper.setEnvironment();
                GexdStatusHelper.sendGexdStatus(GexdStatus.STARTING_WEB_SERVER);
                GexdServer.start();
                GexdStatusHelper.sendGexdStatus(GexdStatus.CHECKING_CONNECTION);
                executor = Executor.constructExecutor();
                logger.logInfo(GexdMessages.CONFIG_FINISHED, LogType.GENERAL);
                break;
            } catch (GexException e) {
                logger.logWarn(GexdMessages.INIT_RETRY, LogType.GENERAL);
                GexdHelper.sleep();
            }
        }
        nodeStatusCheck();
    }

    //should be after checkNodeType()
    private static void checkInstanceID() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        GexdProperties gexdProperties = PropertiesHelper.gexd.getProps();
        if (gexdProperties == null || StringUtils.isBlank(gexdProperties.getInstanceID())) {
            String instanceID = UUID.randomUUID().toString();
            GexdProperties newGexdProperties = new GexdProperties();
            newGexdProperties.setInstanceID(instanceID);
            PropertiesHelper.gexd.saveToJSON(newGexdProperties);
            InstanceLevelApi.instanceRegister(instanceID, getAWSInstanceID());
        }
    }

    private static String getAWSInstanceID() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            if (PropertiesHelper.gexd.getProps().getNodeType() == EntityType.AWS) {
                return FileUtils.readFileToString(new File("/etc/node/nodeinfo/aws_instance_id"),
                        Charset.defaultCharset());
            }
            return null;
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.AWS_INSTANCE_ID_ERROR, e, LogType.AWS_INSTANCE_ID_ERROR);
        }
    }

    private static void checkNodeType() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        GexdProperties gexdProperties = PropertiesHelper.gexd.getProps();
        if (gexdProperties == null || gexdProperties.getNodeType() == null) {
            EntityType nodeType = EntityType.ONPREM;
            if (!PropertiesHelper.isHostVirtual()) {
                String output = ShellExecutor.executeCommandOutput(ShellParameters.newBuilder(
                        Commands.bash("sudo dmidecode -s bios-version")).build());
                if (output.contains("amazon")) {
                    nodeType = EntityType.AWS;
                }
            }
            GexdProperties newGexdProperties = new GexdProperties();
            newGexdProperties.setNodeType(nodeType);
            PropertiesHelper.gexd.saveToJSON(newGexdProperties);
        }
    }

    @Deprecated // need to know kafka address
    @SuppressWarnings("unused")
    private static void updateKafkaLogger(String topic) {
        logger.trace("Entered " + LogHelper.getMethodName());
        //todo flush old logger
        GexdHelper.sleep(1000);
        try {
            Property kafkaProperty = Property.createProperty(GexdMessages.KAFKA_BOOTSTRAP_SERVERS, "kafka.galacticexchange.io:19092");

            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration configuration = context.getConfiguration();
            KafkaAppender oldKafkaAppender = configuration.getAppender(GexdMessages.KAFKA_APPENDER);
            oldKafkaAppender.stop();
            AsyncAppender oldAsyncKafkaAppender = configuration.getAppender(GexdMessages.ASYNC_KAFKA_APPENDER);
            oldAsyncKafkaAppender.stop();
            RollingFileAppender fileAppender = configuration.getAppender(GexdMessages.ROLLING_FILE);

            configuration.removeLogger(GexdMessages.LOGGER_PACKAGE);

            KafkaAppender kafkaAppender = KafkaAppender.newBuilder().setConfiguration(configuration).withLayout(oldKafkaAppender.getLayout()).
                    withName(GexdMessages.NEW_KAFKA_APPENDER).withIgnoreExceptions(true).setTopic("logs-gexd-" + topic).
                    setProperties(new Property[]{kafkaProperty}).build();
            kafkaAppender.start();
            configuration.addAppender(kafkaAppender);

            AppenderRef ref = AppenderRef.createAppenderRef(GexdMessages.NEW_KAFKA_APPENDER, null, null);
            AsyncAppender asyncKafkaAppender = AsyncAppender.newBuilder().setName(GexdMessages.NEW_ASYNC_KAFKA_APPENDER).
                    setBlocking(false).setBufferSize(512).setConfiguration(configuration).
                    setAppenderRefs(new AppenderRef[]{ref}).setShutdownTimeout(1000).build();
            asyncKafkaAppender.start();
            configuration.addAppender(asyncKafkaAppender);

            AppenderRef asyncKafkaRef = AppenderRef.createAppenderRef(GexdMessages.NEW_ASYNC_KAFKA_APPENDER, null, null);
            AppenderRef rollingFileRef = AppenderRef.createAppenderRef(GexdMessages.ROLLING_FILE, null, null);
            LoggerConfig newLoggerConfig = LoggerConfig.createLogger(false, Level.INFO, GexdMessages.LOGGER_PACKAGE,
                    "true", new AppenderRef[]{asyncKafkaRef, rollingFileRef}, null, configuration, null);
            newLoggerConfig.addAppender(asyncKafkaAppender, Level.INFO, null);
            newLoggerConfig.addAppender(fileAppender, Level.INFO, null);

            configuration.addLogger(GexdMessages.LOGGER_PACKAGE, newLoggerConfig);
            context.updateLoggers();
        } catch (Throwable e) {
            logger.logError(GexdMessages.KAFKA_LOGGER, e, LogType.KAFKA_ERROR);
        }
    }

    private static void readAdditionalProperties() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Properties properties = new Properties();
        try {
            File file = new File(PropertiesHelper.propertiesPath, PropertiesHelper.PROPERTIES_FILE_NAME);
            if (!file.exists() || file.length() == 0) {
                return;
            }
            try (FileInputStream fileStream = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fileStream, StandardCharsets.UTF_8)) {
                properties.load(inputStreamReader);
            }
            if (properties.containsKey(PropertiesHelper.RABBIT_HOST_PROP_NAME)) {
                Gexd.connection.setHost(properties.getProperty(PropertiesHelper.RABBIT_HOST_PROP_NAME).trim());
            }
            if (properties.containsKey(PropertiesHelper.RABBIT_PORT_PROP_NAME)) {
                Gexd.connection.setPort(Integer.valueOf(properties.getProperty(PropertiesHelper.RABBIT_PORT_PROP_NAME)));
            }

            //todo remove before release
            if (properties.containsKey("rabbitPrefix")) {
                Gexd.connection.setRabbitPrefix(properties.getProperty("rabbitPrefix"));
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.CONFIG_PROPERTIES_ERROR +
                    PropertiesHelper.PROPERTIES_FILE_NAME, e, LogType.PROPERTIES_ERROR);
        }
    }

    private static void nodeStatusCheck() {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            GexdStatusHelper.sendGexdStatus(GexdStatus.CHECKING_NODE_STATUS);
            NodeStatus nodeStatus = NodeLevelApi.getNodeStatus();
            if (nodeStatus == null) {
                return;
            }
            if (PropertiesHelper.isHostVirtual() &&
                    PropertiesHelper.gexd.getProps().getNodeType() != EntityType.AWS) {
                try {
                    VirtualBoxHelper.constructVirtualBoxHelper().checkVirtualBoxIsRunning();
                } catch (Exception e) {
                    logger.info("Sleep 15 sec to wait for VirtualBox");
                    GexdHelper.sleep(15000);
                }
            }
            if (nodeStatus == NodeStatus.STARTING) {
                logger.logInfo(GexdMessages.STARTING_NODE, LogType.NODE);
                GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_STARTING);
                NodeLocker.executeWithLock(() -> executor.up());
                GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_STARTED);
            } else if (nodeStatus == NodeStatus.STOPPING) {
                logger.logInfo(GexdMessages.STOPPING_NODE, LogType.NODE);
                GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_STOPPING);
                NodeLocker.executeWithLock(() -> executor.halt());
                GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_STOPPED);
            } else if (nodeStatus == NodeStatus.JOINED) {
                logger.logInfo(GexdMessages.JOINED_NODE, LogType.NODE);
                GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_STARTING);
                NodeLocker.executeWithLock(() -> executor.upNoNotify());
                GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_STARTED);
            } else if (PropertiesHelper.isHostVirtual()) {
                if (nodeStatus == NodeStatus.RESTARTING) {
                    logger.logInfo(GexdMessages.RESTARTING_NODE, LogType.NODE);
                    GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_RESTARTING);
                    NodeLocker.executeWithLock(() -> executor.reload());
                    GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_RESTARTED);
                }
            } else { //dedicated
                if (nodeStatus == NodeStatus.RESTARTING) {
                    logger.logInfo(GexdMessages.NODE_RESTART, LogType.NODE_RESTART);
                    MainLevelApi.notify(NodeNotification.NODE_RESTARTED, GexdMessages.RESTART);
                } else if (nodeStatus == NodeStatus.STOPPED) {
                    logger.logError(GexdMessages.INVALID_NODE_STATE, LogType.NODE_STOP_ERROR);
                }
            }
        } catch (Exception e) {
            // todo logger.warn(e.getMessage());
        }

    }

    static void nodeRemovedLoop() throws GexException {
        while (NodeLevelApi.getNodeStatus() == NodeStatus.REMOVED) {
            GexdStatusHelper.sendGexdStatus(GexdStatus.WAITING_WHILE_NODE_IS_REMOVED);
            GexdHelper.sleep(60000);
        }
    }

}
