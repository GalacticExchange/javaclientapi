package io.gex.cli;

import io.gex.core.*;
import io.gex.core.api.ServerPropertiesLevelApi;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.ApplicationMode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class GexMain {

    private final static LogWrapper logger = LogWrapper.create(GexMain.class);

    static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                addLine(CliMessages.LOGIN_COMMAND, CliMessages.LOGIN_DESCRIPTION).
                addLine(CliMessages.CLUSTER_COMMAND, CliMessages.CLUSTER_DESCRIPTION).
                addLine(CliMessages.NODE_COMMAND, CliMessages.NODE_DESCRIPTION).
                addLine(CliMessages.APPLICATION_COMMAND, CliMessages.APPLICATION_DESCRIPTION).
                addLine(CliMessages.USER_COMMAND, CliMessages.USER_DESCRIPTION).
                addLine(CliMessages.TEAM_COMMAND, CliMessages.TEAM_DESCRIPTION).
                addLine(CliMessages.INVITE_COMMAND, CliMessages.INVITE_DESCRIPTION).
                addLine(CliMessages.SHARE_COMMAND, CliMessages.SHARE_DESCRIPTION).
                addLine(CliMessages.VAGRANT_COMMAND, CliMessages.VAGRANT_DESCRIPTION).
                addLine(CliMessages.VIRTUAL_BOX_COMMAND, CliMessages.VIRTUAL_BOX_DESCRIPTION).
                addLine(CliMessages.LOGOUT_COMMAND, CliMessages.LOGOUT_DESCRIPTION).
                addLine(CliMessages.HELP_SHORT + "  " + CliMessages.HELP_LONG, CliMessages.HELP).
                addLine(CliMessages.VERSION_SHORT + "  " + CliMessages.VERSION_LONG, CliMessages.VERSION).
                addLine(CliMessages.API_LONG, CliMessages.API).
                addLine(CliMessages.FORCE_YES, CliMessages.FORCE_YES_DESCRIPTION).
                addLine(CliMessages.DEBUG, CliMessages.DEBUG_DESCRIPTION);
        //addLine(CliMessages.UPDATE_COMMAND, CliMessages.UPDATE_DESCRIPTION).
        columns.print();
    }

    public static void main(String[] arguments) {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            Init.init(ApplicationMode.CLI);
            String tokenParam = Arrays.stream(arguments).filter(param -> param.trim().startsWith(CliMessages.TOKEN)).findFirst().orElse(null);
            if (tokenParam != null) {
                String token = BaseHelper.trimAndRemoveSubstring(tokenParam, CliMessages.TOKEN);
                if (StringUtils.isNotBlank(token)) {
                    AppContext.set(token);
                }
                arguments = ArrayUtils.removeElement(arguments, tokenParam);
            }
            if (Arrays.asList(arguments).contains(CliMessages.DEBUG)) {
                LogHelper.debugMode = true;
                arguments = ArrayUtils.removeElement(arguments, CliMessages.DEBUG);
            }
            if (Arrays.asList(arguments).contains(CliMessages.FORCE_YES)) {
                CliHelper.isForceYes = true;
                arguments = ArrayUtils.removeElement(arguments, CliMessages.FORCE_YES);
            }
            if (Arrays.asList(arguments).contains(CliMessages.UI)) {
                PropertiesHelper.mode = ApplicationMode.UI;
                arguments = ArrayUtils.removeElement(arguments, CliMessages.UI);
            }
            if (CliHelper.mainMenuHelpCheck(arguments)) {
                help();
                return;
            } else if (CliHelper.mainMenuVersionCheck(arguments)) {
                System.out.println(PropertiesHelper.VERSION);
                return;
            } else if (CliHelper.mainMenuApiCheck(arguments)) {
                String api = ServerPropertiesLevelApi.getProperty(ServerPropertiesLevelApi.API_VERSION);
                System.out.println(StringUtils.isNotBlank(api) ? api : CoreMessages.SERVER_PROPERTY_ERROR);
                return;
            } else if (arguments[0].toLowerCase().equals(CliMessages.LOGIN_COMMAND)) {
                MainLevel.login(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.LOGOUT_COMMAND)) {
                MainLevel.logout(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.LOG_COMMAND)) {
                LogLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.USER_COMMAND)) {
                UserLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.CLUSTER_COMMAND)) {
                ClusterLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.NODE_COMMAND)) {
                NodeLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.APPLICATION_COMMAND)) {
                ApplicationLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.TEAM_COMMAND)) {
                TeamLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.INVITE_COMMAND)) {
                InviteLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.SHARE_COMMAND)) {
                ShareLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.SSH_COMMAND)) {
                SshLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
                //} else if (arguments[0].toLowerCase().equals(CliMessages.UPDATE_COMMAND)) {
                //    UpdateLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.VAGRANT_COMMAND)) {
                VagrantLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.VIRTUAL_BOX_COMMAND)) {
                VirtualBoxLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else {
                CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
                System.exit(1);
            }
        } catch (Throwable e) {
            logger.logError(CliMessages.GENERAL_EXCEPTION + e.getMessage(), e, LogType.GENERAL_ERROR);
            if (StringUtils.isNotBlank(e.getMessage())) {
                CliHelper.printError(CliMessages.GENERAL_EXCEPTION + e.getMessage());
            }
            System.exit(1);
        }
        System.exit(0);
    }

}
