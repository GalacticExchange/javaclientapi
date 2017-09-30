package io.gex.cli;

import com.google.gson.JsonObject;
import io.gex.core.PropertiesHelper;
import io.gex.core.api.LogLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.parameters.LogFileParameters;

import java.util.Arrays;


public class LogLevel {

    private final static LogWrapper logger = LogWrapper.create(LogLevel.class);

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (arguments.length > 0 && arguments[0].toLowerCase().equals(CliMessages.LOG_FILE_COMMAND)) {
            logFile(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
            System.exit(1);
        }
    }

    private static void logFile(String[] arguments) throws GexException{
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.LOG_FILE_PARAMS);
            return;
        } else if (arguments.length > 2) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.LOG_FILE_PARAMS);
            System.exit(1);
        }
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.addProperty("filePath", LogLevelApi.getNodeLocalLogs(new LogFileParameters(arguments)));
        } catch (GexException e) {
            if (PropertiesHelper.isUI()) {
                CliHelper.printErrMessageForUi(e);
                return;
            } else {
                throw e;
            }
        }
        System.out.println(jsonObject);
    }


}
