package io.gex.cli;

import io.gex.core.api.ApplicationLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Application;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

class ApplicationLevel {
    private final static LogWrapper logger = LogWrapper.create(ApplicationLevel.class);

    //todo application install/uninstall
    private static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                //addLine(CliMessages.APP_START_HELP_SYMBOL + CliMessages.APP_INSTALL_COMMAND, CliMessages.APP_INSTALL_DESCRIPTION).
                //addLine(CliMessages.APP_START_HELP_SYMBOL + CliMessages.APP_UNINSTALL_COMMAND, CliMessages.APP_UNINSTALL_DESCRIPTION).
                addLine(CliMessages.APP_START_HELP_SYMBOL + CliMessages.APP_SUPPORTED_COMMAND, CliMessages.APP_SUPPORTED_DESCRIPTION).
                addLine(CliMessages.APP_START_HELP_SYMBOL + CliMessages.APP_LIST_COMMAND, CliMessages.APP_LIST_DESCRIPTION);
        columns.print();
    }

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.mainMenuHelpCheck(arguments)) {
            help();
        /*} else if (arguments[0].toLowerCase().equals(CliMessages.APP_INSTALL_COMMAND)) {
            applicationInstall(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.APP_UNINSTALL_COMMAND)) {
            applicationUninstall(Arrays.copyOfRange(arguments, 1, arguments.length));*/
        } else if (arguments[0].toLowerCase().equals(CliMessages.APP_SUPPORTED_COMMAND)) {
            applicationSupported(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.APP_LIST_COMMAND)) {
            applicationList(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
            System.exit(1);
        }
    }

    private static void applicationInstall(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.APP_INSTALL_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.APP_INSTALL_PARAMS);
            System.exit(1);
        }
        ApplicationLevelApi.applicationInstall(arguments[0]);
        LogHelper.print(CliMessages.APP_INSTALL_START);
    }

    private static void applicationUninstall(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.APP_UNINSTALL_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.APP_UNINSTALL_PARAMS);
            System.exit(1);
        }
        ApplicationLevelApi.applicationUninstall(arguments[0]);
        LogHelper.print(CliMessages.APP_UNINSTALL_START);
    }

    private static void applicationSupported(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.APP_SUPPORTED_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.APP_SUPPORTED_PARAMS);
            System.exit(1);
        }
        printApplicationList(ApplicationLevelApi.applicationSupported(arguments[0]));
    }

    private static void applicationList(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.APP_LIST_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.APP_LIST_PARAMS);
            System.exit(1);
        }
        printApplicationList(ApplicationLevelApi.applicationList(arguments[0]));
    }

    private static void printApplicationList(List<Application> applications) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CollectionUtils.isEmpty(applications)) {
            LogHelper.print(CliMessages.APPLICATION_EMPTY);
            return;
        }
        for (Application application : applications) {
            System.out.println(CliMessages.DELIMITER);
            printApplication(application);
            System.out.println(CliMessages.DELIMITER);
        }
    }

    private static void printApplication(Application application) {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (application == null) {
            return;
        }
        Columns columns = new Columns();
        if (StringUtils.isNotBlank(application.getId())) {
            columns.addLine(CliMessages.ID, application.getId());
        }
        if (StringUtils.isNotBlank(application.getName())) {
            columns.addLine(CliMessages.NAME, application.getName());
        }
        if (StringUtils.isNotBlank(application.getClusterID())) {
            columns.addLine(CliMessages.CLUSTER_ID, application.getClusterID());
        }
        if (StringUtils.isNotBlank(application.getTitle())) {
            columns.addLine(CliMessages.TITLE, application.getTitle());
        }
        if (StringUtils.isNotBlank(application.getCategoryTitle())) {
            columns.addLine(CliMessages.CATEGORY_TITLE, application.getCategoryTitle());
        }
        if (StringUtils.isNotBlank(application.getCompanyName())) {
            columns.addLine(CliMessages.COMPANY_NAME, application.getCompanyName());
        }
        if (StringUtils.isNotBlank(application.getNotes())) {
            columns.addLine(CliMessages.NOTES, application.getNotes());
        }
        if (StringUtils.isNotBlank(application.getStatus())) {
            columns.addLine(CliMessages.STATUS, application.getStatus());
        }
        if (StringUtils.isNotBlank(application.getReleaseDate())) {
            columns.addLine(CliMessages.RELEASE_DATE, application.getReleaseDate());
        }
        if (StringUtils.isNotBlank(application.getClusterApplicationID())) {
            columns.addLine(CliMessages.CLUSTER_APPLICATION_ID, application.getClusterApplicationID());
        }
        columns.print();
    }

}

