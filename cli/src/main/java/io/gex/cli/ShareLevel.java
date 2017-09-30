package io.gex.cli;

import io.gex.core.api.ShareLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.parameters.ShareParameters;

import java.util.Arrays;

class ShareLevel {
    private final static LogWrapper logger = LogWrapper.create(ShareLevel.class);

    private static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                addLine(CliMessages.SHARE_START_HELP_SYMBOL + CliMessages.SHARE_CREATE_COMMAND, CliMessages.SHARE_CREATE_DESCRIPTION).
                addLine(CliMessages.SHARE_START_HELP_SYMBOL + CliMessages.SHARE_REMOVE_COMMAND, CliMessages.SHARE_REMOVE_DESCRIPTION).
                addLine(CliMessages.SHARE_START_HELP_SYMBOL + CliMessages.SHARE_CLUSTERLIST_COMMAND, CliMessages.SHARE_CLUSTERLIST_DESCRIPTION).
                addLine(CliMessages.SHARE_START_HELP_SYMBOL + CliMessages.SHARE_USERLIST_COMMAND, CliMessages.SHARE_USERLIST_DESCRIPTION);
        columns.print();
    }

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.mainMenuHelpCheck(arguments)) {
            help();
        } else if (arguments[0].toLowerCase().equals(CliMessages.SHARE_CREATE_COMMAND)) {
            shareCreate(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.SHARE_REMOVE_COMMAND)) {
            shareRemove(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.SHARE_USERLIST_COMMAND)) {
            shareUserList(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.SHARE_CLUSTERLIST_COMMAND)) {
            shareClusterList(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
            System.exit(1);
        }
    }

    private static void shareCreate(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.SHARE_CREATE_PARAMS);
            return;
        }
        if (arguments.length != 2) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.SHARE_CREATE_PARAMS);
            System.exit(1);
        }
        ShareLevelApi.shareCreate(new ShareParameters(arguments));
        LogHelper.print(CliMessages.SHARE_CREATE);
    }

    private static void shareRemove(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.SHARE_REMOVE_PARAMS);
            return;
        }
        if (arguments.length != 2) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.SHARE_REMOVE_PARAMS);
            System.exit(1);
        }
        ShareLevelApi.shareRemove(new ShareParameters(arguments));
        LogHelper.print(CliMessages.SHARE_REMOVE);
    }

    private static void shareUserList(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.SHARE_USERLIST_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.SHARE_USERLIST_PARAMS);
            System.exit(1);
        }
        UserLevel.printUserList(ShareLevelApi.shareUserList(arguments[0]));
    }

    private static void shareClusterList(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.SHARE_CLUSTERLIST_PARAMS);
            return;
        } else if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.SHARE_CLUSTERLIST_PARAMS);
            System.exit(1);
        }
        ClusterLevel.printClusterList(ShareLevelApi.shareClusterList());
    }

}
