package io.gex.cli;

import io.gex.core.api.MainLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Color;

class MainLevel {

    private final static LogWrapper logger = LogWrapper.create(MainLevel.class);

    static void login(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.LOGIN_PARAMS);
            return;
        } else if (arguments.length > 1 && arguments.length != 3) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.LOGIN_PARAMS);
            System.exit(1);
        }
        String username, password;
        if (arguments.length == 0) {
            username = CliHelper.getLineFromConsole(CliMessages.USERNAME_EMAIL);
        } else {
            username = arguments[0];
        }
        if (arguments.length == 3) {
            if (!arguments[1].toLowerCase().equals(CliMessages.PASSWORD_SHORT)) {
                CliHelper.printError(CliMessages.INVALID_INPUT);
                System.exit(1);
            }
            password = arguments[2];
        } else {
            password = CliHelper.getPasswordFromConsole(CliMessages.ENTER_PASSWORD);
        }
        MainLevelApi.login(username, password);
        LogHelper.printWithColor(Color.ANSI_YELLOW, CliMessages.LOGIN_MESSAGE);
        CliHelper.printASCIIArt(CliHelper.AUTH_PATH, Color.ANSI_YELLOW);
    }

    static void logout(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.LOGOUT_PARAMS);
            return;
        } else if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.LOGOUT_PARAMS);
            System.exit(1);
        }
        MainLevelApi.logout();
    }

}
