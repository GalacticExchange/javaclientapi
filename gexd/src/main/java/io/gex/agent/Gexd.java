package io.gex.agent;

import io.gex.agent.webServer.GexdServer;
import io.gex.core.exception.ExceptionHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Gexd {

    public static Path getGexdLogFolder() {
        return SystemUtils.IS_OS_WINDOWS ? Paths.get(System.getenv("ProgramData"), ".gex")
                : Paths.get(System.getProperty("user.home"), ".gex");
    }

    public static String getCustomLogFileName(String name) {
        return getGexdLogFolder().resolve(name + ".log").toString();
    }

    static {
        System.setProperty("agentLogFilename", getCustomLogFileName("gex_agent_log"));
    }

    private final static LogWrapper logger = LogWrapper.create(Gexd.class);

    public static RabbitMQConnection connection;

    //todo check
    private static void registerSendGexdStatusTask() {
        ScheduledExecutorService collectTask = Executors.newSingleThreadScheduledExecutor();
        collectTask.scheduleAtFixedRate(GexdStatusHelper::sendGexdStatus, getNumberOfSecondsBeforeNextHour(),
                60 * 60, TimeUnit.SECONDS);
    }

    private static long getNumberOfSecondsBeforeNextHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, +1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = (calendar.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 1000;
        logger.logInfo(GexdMessages.DELAY + time + "s.", LogType.GENERAL);
        return time;
    }

    public static void main(String[] args) {
        connection = new RabbitMQConnection();
        GexdAgent.init();
        registerSendGexdStatusTask();
        while (true) {
            try {
                connection.waitForNodeID();
                GexdServer.sendIPAndPort();
                GexdAgent.nodeRemovedLoop();
                if (!connection.open()) {
                    continue;
                }
                connection.consume();
            } catch (Throwable e) {
                if (ExceptionHelper.getStackTraceString(e).contains("com.rabbitmq.client.ConsumerCancelledException")) {
                    //todo should be warn or error?
                    logger.logWarn(GexdMessages.QUEUE_DISCONNECTED, e, LogType.RABBIT);
                } else {
                    logger.logError(e, LogType.GENERAL);
                }
                connection.close();
                GexdHelper.sleep();
            }
        }
    }
}
