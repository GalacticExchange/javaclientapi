package io.gex.cli;

import io.gex.core.DownloadFile;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.ApplicationMode;
import io.gex.core.vagrantHelper.VagrantHelper;

import java.util.Arrays;

class VagrantLevel {
    private final static LogWrapper logger = LogWrapper.create(VagrantLevel.class);

    private static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                addLine(CliMessages.VAGRANT_START_HELP_SYMBOL + CliMessages.VAGRANT_INSTALL_COMMAND,
                        CliMessages.VAGRANT_INSTALL_DESCRIPTION).
                addLine(CliMessages.VAGRANT_START_HELP_SYMBOL + CliMessages.VAGRANT_CHECK_COMMAND,
                        CliMessages.VAGRANT_CHECK_DESCRIPTION);
        columns.print();
    }

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.mainMenuHelpCheck(arguments)) {
            help();
        } else if (arguments[0].toLowerCase().equals(CliMessages.VAGRANT_INSTALL_COMMAND)) {
            vagrantInstall(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.VAGRANT_CHECK_COMMAND)) {
            vagrantCheck(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
            System.exit(1);
        }
    }

    private static void vagrantInstall(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.VAGRANT_INSTALL_PARAMS);
            return;
        }
        else if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.VAGRANT_INSTALL_PARAMS);
            System.exit(1);
        }
        checkVagrant();
    }

    //todo print version
    private static void vagrantCheck(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.VAGRANT_CHECK_PARAMS);
            return;
        }
        else if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.VAGRANT_CHECK_PARAMS);
            System.exit(1);
        }
        VagrantHelper vagrantHelper = VagrantHelper.constructVagrantHelper();
        if (vagrantHelper.isInstalled()) {
            if (!vagrantHelper.hasValidVersion()) {
                if (PropertiesHelper.mode == ApplicationMode.UI) {
                    System.err.println(CliMessages.VAGRANT_VERSION);
                    System.exit(1);
                }
                throw logger.logAndReturnException(CliMessages.VAGRANT_VERSION, LogType.VAGRANT_ERROR);
            }
            System.out.println(PropertiesHelper.mode == ApplicationMode.UI ? "false" : "\t" + CliMessages.VAGRANT_IS_INSTALLED);
        } else {
            System.out.println(PropertiesHelper.mode == ApplicationMode.UI ? "true" : "\t" + CliMessages.VAGRANT_IS_NOT_INSTALLED);
        }
    }

    static void checkVagrant() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        VagrantHelper vagrantHelper = VagrantHelper.constructVagrantHelper();
        LogHelper.print(CliMessages.VAGRANT_CHECK);
        if (vagrantHelper.isInstalled()) {
            if (!vagrantHelper.hasValidVersion()) {
                throw logger.logAndReturnException(CliMessages.VAGRANT_VERSION, LogType.VAGRANT_ERROR);
            }
            LogHelper.print(CliMessages.DONE);
        } else {
            System.out.println();
            LogHelper.print(CliMessages.VAGRANT_INSTALLATION_START);
            DownloadFile vagrantInstaller = vagrantHelper.download();
            vagrantInstaller.downloadParallel();
            LogHelper.print(CliMessages.INSTALLING_VAGRANT);
            vagrantHelper.install(vagrantInstaller);
            LogHelper.print(CliMessages.VAGRANT_INSTALLED);
        }
    }
}

