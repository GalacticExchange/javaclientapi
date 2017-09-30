package io.gex.agent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import io.gex.agent.app.ApplicationManager;
import io.gex.core.CoreMessages;
import io.gex.core.NodeLocker;
import io.gex.core.PropertiesHelper;
import io.gex.core.api.NodeLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Action;
import io.gex.core.model.NodeStatus;
import io.gex.core.model.properties.NodeProperties;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnection {

    private final static LogWrapper logger = LogWrapper.create(RabbitMQConnection.class);

    private Connection connection;
    private Channel channel;
    private String host;
    private Integer port;
    private String nodeID;
    private QueueingConsumer consumer;

    //todo remove before release
    private String rabbitPrefix;

    RabbitMQConnection() {
        host = "rabbit.galacticexchange.io";
        port = 443;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setRabbitPrefix(String rabbitPrefix) {
        this.rabbitPrefix = rabbitPrefix;
    }

    public String getRabbitPrefix() {
        return rabbitPrefix;
    }

    public void waitForNodeID() {
        logger.trace("Entered " + LogHelper.getMethodName());
        GexdStatusHelper.sendGexdStatus(GexdStatus.WAITING_FOR_NODE_ID);
        nodeID = GexdHelper.waitForProperty(NodeProperties.NODE_ID_PROPERTY_NAME);
    }

    public boolean open() throws GexException, IOException, TimeoutException {
        logger.trace("Entered " + LogHelper.getMethodName());
        GexdStatusHelper.sendGexdStatus(GexdStatus.OPENING_RABBITMQ_CONNECTION);
        String prefix = StringUtils.isNoneEmpty(rabbitPrefix) ? rabbitPrefix : "gex";
        String queueName = prefix + ".nodes." + nodeID + ".commands";
        String exchangeName = prefix + ".nodes." + nodeID + ".exchange";
        logger.logInfo(GexdMessages.RABBIT_QUEUE + queueName, LogType.RABBIT);
        String token = PropertiesHelper.node.getProps().getNodeAgentToken();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setPassword(token);
        factory.setUsername("node" + nodeID);
        connection = factory.newConnection();
        channel = connection.createChannel();
        try {
            channel.exchangeDeclarePassive(exchangeName);
        } catch (Exception e) {
            logger.logWarn(GexdMessages.EXCHANGE_CONNECTION, LogType.RABBIT_ERROR);
            this.close();
            GexdHelper.sleep(20000);
            return false;
        }
        channel.queueDeclarePassive(queueName);
        channel.queueBind(queueName, exchangeName, queueName);
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);
        GexdStatusHelper.sendGexdStatus(GexdStatus.RABBITMQ_CONNECTION_OPENED);
        logger.logInfo(GexdMessages.RABBIT_CONNECTION_OPENED, LogType.RABBIT);
        return true;
    }

    public void close() {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            GexdStatusHelper.sendGexdStatus(GexdStatus.CLOSING_RABBITMQ_CONNECTION);
            if (channel != null && channel.isOpen()) {
                logger.logInfo(GexdMessages.CLOSING_CHANNEL, LogType.RABBIT);
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                logger.logInfo(GexdMessages.CLOSING_CONNECTION, LogType.RABBIT);
                connection.close();
            }
            GexdStatusHelper.sendGexdStatus(GexdStatus.RABBITMQ_CONNECTION_CLOSED);
        } catch (Exception e) {
            logger.logError(GexdMessages.CLOSING_ERROR, e, LogType.RABBIT_ERROR);
        }
    }

    public void reconnect() {
        logger.trace("Entered " + LogHelper.getMethodName());
        GexdStatusHelper.sendGexdStatus(GexdStatus.RECONNECTING);
        close();
        GexdHelper.sleep(2000);
        GexdStatusHelper.sendGexdStatus(GexdStatus.RECONNECTED);
    }

    public void consume() throws InterruptedException {
        logger.trace("Entered " + LogHelper.getMethodName());
        while (true) {
            logger.logInfo(GexdMessages.WAITING, LogType.RABBIT);
            GexdStatusHelper.sendGexdStatus(GexdStatus.WAITING_FOR_COMMAND);
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message, command;
            try {
                message = new String(delivery.getBody());
                logger.logInfo(GexdMessages.RECEIVED + message, LogType.RABBIT);
                command = getCommand(message);
                GexdStatusHelper.sendGexdStatus(GexdStatus.RECEIVED_COMMAND, ": " + command);
            } catch (GexException e) {
                continue;
            }
            if (StringUtils.isNotBlank(command)) {
                try {
                    if (command.toLowerCase().equals(Action.restart.toString())) {
                        GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_RESTARTING);
                        NodeLocker.executeWithLock(() -> GexdAgent.executor.reload());
                        GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_RESTARTED);
                    } else if (command.toLowerCase().equals(Action.stop.toString())) {
                        GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_HALTING);
                        NodeLocker.executeWithLock(() -> GexdAgent.executor.halt());
                        GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_HALTED);
                    } else if (command.toLowerCase().equals(Action.start.toString())) {
                        if (!PropertiesHelper.isHostVirtual() && NodeStatus.JOINED == NodeLevelApi.getNodeStatus()) {
                            GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_ALREADY_STARTED);
                        } else {
                            GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_STARTING);
                            NodeLocker.executeWithLock(() -> GexdAgent.executor.up());
                            GexdStatusHelper.sendGexdStatus(GexdStatus.NODE_STARTED);
                        }
                    } else if (command.toLowerCase().equals(Action.reconnect.toString())) {
                        reconnect();
                    } else if (command.toLowerCase().equals(Action.installApplication.toString().toLowerCase())) {
                        GexdStatusHelper.sendGexdStatus(GexdStatus.APPLICATION_INSTALLING);
                        NodeLocker.executeWithLock(() -> ApplicationManager.installApplication(message));
                        GexdStatusHelper.sendGexdStatus(GexdStatus.APPLICATION_INSTALLED);
                    } else if (command.toLowerCase().equals(Action.uninstallApplication.toString().toLowerCase())) {
                        GexdStatusHelper.sendGexdStatus(GexdStatus.APPLICATION_UNINSTALLING);
                        NodeLocker.executeWithLock(() -> ApplicationManager.uninstallApplication(message));
                        GexdStatusHelper.sendGexdStatus(GexdStatus.APPLICATION_UNINSTALLED);
                    } else if (command.toLowerCase().equals(Action.startContainer.toString().toLowerCase())) {
                        GexdStatusHelper.sendGexdStatus(GexdStatus.CONTAINER_STARTING);
                        NodeLocker.executeWithLock(() -> GexdAgent.executor.startContainer(message));
                        GexdStatusHelper.sendGexdStatus(GexdStatus.CONTAINER_STARTED);
                    } else if (command.toLowerCase().equals(Action.stopContainer.toString().toLowerCase())) {
                        GexdStatusHelper.sendGexdStatus(GexdStatus.CONTAINER_STOPPING);
                        NodeLocker.executeWithLock(() -> GexdAgent.executor.stopContainer(message));
                        GexdStatusHelper.sendGexdStatus(GexdStatus.CONTAINER_STOPPED);
                    } else if (command.toLowerCase().equals(Action.restartContainer.toString().toLowerCase())) {
                        GexdStatusHelper.sendGexdStatus(GexdStatus.CONTAINER_RESTARTING);
                        NodeLocker.executeWithLock(() -> GexdAgent.executor.restartContainer(message));
                        GexdStatusHelper.sendGexdStatus(GexdStatus.CONTAINER_RESTARTED);
                    }
                } catch (Exception e) {
                    //todo
                    //do nothing
                }
            }
        }
    }

    private static String getCommand(String message) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(message)) {
            return null;
        }
        String attribute = "command", value = null;
        try {
            JsonObject obj = new JsonParser().parse(message).getAsJsonObject();
            if (obj.has(attribute)) {
                value = obj.get(attribute).getAsString();
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.SERVER_RESPONSE_ERROR, e, LogType.RABBIT_ERROR);
        }
        return value;
    }

    public static JsonObject getAttributes(String message) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            if (StringUtils.isBlank(message)) {
                throw logger.logAndReturnException(CoreMessages.EMPTY_MESSAGE, LogType.RABBIT_ERROR);
            }
            return new JsonParser().parse(message).getAsJsonObject();
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.SERVER_RESPONSE_ERROR, e, LogType.RABBIT_ERROR);
        }
    }
}
