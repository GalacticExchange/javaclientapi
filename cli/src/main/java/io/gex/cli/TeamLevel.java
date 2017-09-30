package io.gex.cli;

import io.gex.core.api.TeamLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Team;
import io.gex.core.model.parameters.TeamParameters;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

class TeamLevel {
    private final static LogWrapper logger = LogWrapper.create(TeamLevel.class);

    private static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                addLine(CliMessages.TEAM_START_HELP_SYMBOL + CliMessages.TEAM_INFO_COMMAND, CliMessages.TEAM_INFO_DESCRIPTION).
                addLine(CliMessages.TEAM_START_HELP_SYMBOL + CliMessages.TEAM_UPDATE_COMMAND, CliMessages.TEAM_UPDATE_DESCRIPTION).
                addLine(CliMessages.TEAM_START_HELP_SYMBOL + CliMessages.TEAM_CLUSTERS_COMMAND, CliMessages.TEAM_CLUSTERS_DESCRIPTION).
                addLine(CliMessages.TEAM_START_HELP_SYMBOL + CliMessages.TEAM_USERS_COMMAND, CliMessages.TEAM_USERS_DESCRIPTION);
        columns.print();
    }

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.mainMenuHelpCheck(arguments)) {
            help();
        } else if (arguments[0].toLowerCase().equals(CliMessages.TEAM_INFO_COMMAND)) {
            teamInfo(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.TEAM_UPDATE_COMMAND)) {
            teamUpdate(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.TEAM_CLUSTERS_COMMAND)) {
            teamClusters(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.TEAM_USERS_COMMAND)) {
            teamUsers(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
            System.exit(1);
        }
    }

    private static void teamUsers(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.TEAM_USERS_PARAMS);
            return;
        } else if (arguments.length > 2) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.TEAM_USERS_PARAMS);
            System.exit(1);
        }
        UserLevel.printUserList(TeamLevelApi.teamUsers());
    }

    private static void teamClusters(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.TEAM_CLUSTERS_PARAMS);
            return;
        } else if (arguments.length > 2) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.TEAM_CLUSTERS_PARAMS);
            System.exit(1);
        }
        ClusterLevel.printClusterList(TeamLevelApi.teamClusters());
    }

    private static void teamInfo(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.TEAM_INFO_PARAMS);
            return;
        } else if (arguments.length > 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.TEAM_INFO_PARAMS);
            System.exit(1);
        }
        printTeam(TeamLevelApi.teamInfo());
    }

    private static void teamUpdate(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.TEAM_UPDATE_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.TEAM_UPDATE_PARAMS);
            System.exit(1);
        }
        TeamLevelApi.teamUpdate(TeamParameters.parseUpdate(arguments));
        LogHelper.print(CliMessages.TEAM_UPDATED);
    }

    private static void printTeam(Team team) {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (team == null) {
            return;
        }
        Columns columns = new Columns();
        if (StringUtils.isNotBlank(team.getId())) {
            columns.addLine(CliMessages.ID, team.getId());
        }
        if (StringUtils.isNotBlank(team.getName())) {
            columns.addLine(CliMessages.NAME, team.getName());
        }
        if (StringUtils.isNotBlank(team.getAbout())) {
            columns.addLine(CliMessages.ABOUT, team.getAbout());
        }
        columns.print();
    }
}
