package io.gex.agent;

import io.gex.core.log.LogWrapper;

public class GexdStatusHelper {

    private final static LogWrapper logger = LogWrapper.create(GexdStatusHelper.class);
    private static GexdStatus status = GexdStatus.NOT_DEFINED;

    public static void sendGexdStatus() {
        logger.logDebug(GexdMessages.GEXD_STATUS + status.toString());
    }

    public static void sendGexdStatus(GexdStatus newStatus) {
        status = newStatus;
        sendGexdStatus();
    }

    public static void sendGexdStatus(GexdStatus newStatus, String message) {
        status = newStatus;
        logger.logDebug(GexdMessages.GEXD_STATUS + status.toString() + " " + message);
    }
}
