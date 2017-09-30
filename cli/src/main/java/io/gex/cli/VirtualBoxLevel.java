package io.gex.cli;

import io.gex.core.DownloadFile;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.ApplicationMode;
import io.gex.core.virutalBoxHelper.VirtualBoxHelper;

import java.util.Arrays;

class VirtualBoxLevel {
    private final static LogWrapper logger = LogWrapper.create(VirtualBoxLevel.class);

    private static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                addLine(CliMessages.VIRTUAL_BOX_START_HELP_SYMBOL + CliMessages.VIRTUAL_BOX_INSTALL_COMMAND,
                        CliMessages.VIRTUAL_BOX_INSTALL_DESCRIPTION).
                addLine(CliMessages.VIRTUAL_BOX_START_HELP_SYMBOL + CliMessages.VIRTUAL_BOX_CHECK_COMMAND,
                        CliMessages.VIRTUAL_BOX_CHECK_DESCRIPTION);
        columns.print();
    }

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.mainMenuHelpCheck(arguments)) {
            help();
        } else if (arguments[0].toLowerCase().equals(CliMessages.VIRTUAL_BOX_INSTALL_COMMAND)) {
            virtualBoxInstall(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.VIRTUAL_BOX_CHECK_COMMAND)) {
            virtualBoxCheck(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
        }
    }

    private static void virtualBoxInstall(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.VIRTUAL_BOX_INSTALL_PARAMS);
            return;
        } else if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.VIRTUAL_BOX_INSTALL_PARAMS);
            return;
        }
        checkVirtualBox();
    }

    //todo print version
    private static void virtualBoxCheck(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.VIRTUAL_BOX_CHECK_PARAMS);
            return;
        }
        else if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.VIRTUAL_BOX_CHECK_PARAMS);
            return;
        }
        VirtualBoxHelper virtualBoxHelper = VirtualBoxHelper.constructVirtualBoxHelper();
        if (virtualBoxHelper.isInstalled()) {
            if (!virtualBoxHelper.hasValidVersion()) {
                if (PropertiesHelper.mode == ApplicationMode.UI) {
                    System.err.println(CliMessages.VIRTUAL_BOX_VERSION);
                    System.exit(1);
                }
                throw logger.logAndReturnException(CliMessages.VIRTUAL_BOX_VERSION, LogType.VIRTUAL_BOX_ERROR);
            }
            System.out.println(PropertiesHelper.mode == ApplicationMode.UI ? "false" : "\t" + CliMessages.VIRTUAL_BOX_IS_INSTALLED);
        } else {
            System.out.println(PropertiesHelper.mode == ApplicationMode.UI ? "true" : "\t" + CliMessages.VIRTUAL_BOX_IS_NOT_INSTALLED);
        }
    }

    static void checkVirtualBox() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        VirtualBoxHelper virtualBoxHelper = VirtualBoxHelper.constructVirtualBoxHelper();
        LogHelper.print(CliMessages.VIRTUAL_BOX_CHECK);
        if (virtualBoxHelper.isInstalled()) {
            if (!virtualBoxHelper.hasValidVersion()) {
                throw logger.logAndReturnException(CliMessages.VIRTUAL_BOX_VERSION, LogType.VIRTUAL_BOX_ERROR);
            }
            LogHelper.print(CliMessages.DONE);
        } else {
            VirtualBoxHelper.checkRunningVMs();
            System.out.println();
            LogHelper.print(CliMessages.VIRTUAL_BOX_INSTALLATION_START);
            DownloadFile virtualBoxInstaller = virtualBoxHelper.download();
            virtualBoxInstaller.downloadParallel();
            LogHelper.print(CliMessages.INSTALLING_VIRTUAL_BOX);
            virtualBoxHelper.install(virtualBoxInstaller);
            LogHelper.print(CliMessages.VIRTUAL_BOX_INSTALLED);
        }
    }
}

