package io.gex.core.api;

import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Cluster;
import io.gex.core.model.Team;
import io.gex.core.model.User;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.TeamLevelRest;

import java.util.List;

public class TeamLevelApi {

    private final static LogWrapper logger = LogWrapper.create(TeamLevelApi.class);

    public static Team teamInfo() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return TeamLevelRest.teamInfo(BasePropertiesHelper.getValidToken());
    }

    public static void teamUpdate(Team team) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (team == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_TEAM, LogType.EMPTY_PROPERTY_ERROR);
        }
        TeamLevelRest.teamUpdate(team, BasePropertiesHelper.getValidToken());
    }

    public static List<Cluster> teamClusters() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return TeamLevelRest.teamClusters(BasePropertiesHelper.getValidToken());
    }

    public static List<User> teamUsers() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return TeamLevelRest.teamUsers(BasePropertiesHelper.getValidToken());
    }

}
