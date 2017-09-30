package io.gex.cli;

import com.google.gson.JsonObject;
import io.gex.core.CoreMessages;
import io.gex.core.DownloadFile;
import io.gex.core.PropertiesHelper;
import io.gex.core.UpdateHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.ApplicationMode;
import io.gex.core.uploader.UpdateUploader;

import java.util.Arrays;

@Deprecated
class UpdateLevel {
    private final static LogWrapper logger = LogWrapper.create(UpdateLevel.class);

    private static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                addLine(CliMessages.UPDATE_START_HELP_SYMBOL + CliMessages.UPDATE_INSTALL_COMMAND,CliMessages.UPDATE_INSTALL_DESCRIPTION).
                addLine(CliMessages.UPDATE_START_HELP_SYMBOL + CliMessages.UPDATE_CHECK_COMMAND,CliMessages.UPDATE_CHECK_DESCRIPTION);
        columns.print();
    }

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.mainMenuHelpCheck(arguments)) {
            help();
        } else if (arguments[0].toLowerCase().equals(CliMessages.UPDATE_INSTALL_COMMAND)) {
            updateInstall(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.UPDATE_CHECK_COMMAND)) {
            updateCheck(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
            System.exit(1);
        }
    }

    private static void updateCheck(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.UPDATE_CHECK_PARAMS);
            return;
        } else if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.UPDATE_CHECK_PARAMS);
            System.exit(1);
        }
        try {
            UpdateUploader uploader = new UpdateUploader();
            String versionForUpdate = uploader.versionForUpdate();
            if (versionForUpdate != null) {
                if (PropertiesHelper.isUI()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("latestVersion", versionForUpdate);
                    jsonObject.addProperty("updateAvailable", true);
                    System.out.println(jsonObject.toString());
                } else {
                    LogHelper.print(CoreMessages.replaceTemplate(CliMessages.NEW_VERSION_TEMPLATE,versionForUpdate));
                }
            } else {
                if (PropertiesHelper.isUI()) {
                    System.out.println("{}");
                } else {
                    LogHelper.print(CliMessages.NO_UPDATES);
                }
            }
        } catch (GexException e) {
            if (PropertiesHelper.isUI()) {
                CliHelper.printErrMessageForUi(e);
            } else {
                throw e;
            }
        }
    }

    private static void updateInstall(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.UPDATE_INSTALL_PARAMS);
            return;
        } else if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.UPDATE_INSTALL_PARAMS);
            System.exit(1);
        }
        UpdateUploader uploader = new UpdateUploader();
        if (uploader.checkForUpdates()) {
            LogHelper.print(CliMessages.NEW_VERSION);
            if (CliHelper.getYesOrNoFromConsole(CliMessages.UPDATE_QUESTION)) {
                LogHelper.print(CliMessages.DOWNLOADING_UPDATER);
                DownloadFile updater = uploader.getUpdater();
                updater.downloadParallel();
                LogHelper.print(CliMessages.DOWNLOADING_DISTRIBUTION);
                DownloadFile distribution = uploader.getDistribution();
                distribution.downloadParallel();
                LogHelper.print(CliMessages.UPDATE_DOWNLOADED);
                UpdateHelper.startUpdater(updater.getPath(), distribution.getPath(), ApplicationMode.CLI);
                System.out.println(CliMessages.UPDATER_STARTED);
            }
        } else {
            System.out.println();
            LogHelper.print(CliMessages.NO_UPDATES);
        }
    }
}
