package io.gex.core.rest;

import com.google.gson.JsonObject;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Cluster;
import io.gex.core.model.Team;
import io.gex.core.model.User;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;


public class TeamLevelRest {

    private final static LogWrapper logger = LogWrapper.create(TeamLevelRest.class);

    private final static String TEAM_USERS = "/teamUsers";
    private final static String TEAM_CLUSTERS = "/teamClusters";
    private final static String TEAM_INFO = "/teamInfo";


    public static Team teamInfo(String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, TEAM_INFO,
                LogType.TEAM_INFO_ERROR, null, null, null, token);
        return obj.has("team") ? Team.parse(obj.getAsJsonObject("team")) : null;
    }

    public static void teamUpdate(Team team, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        if (StringUtils.isNotBlank(team.getAbout())) {
            body.add("about", team.getAbout());
        }
        Rest.sendAuthenticatedRequest(HttpMethod.PUT, TEAM_INFO,  LogType.TEAM_UPDATE_ERROR,
                null, body, null, token);
    }

    public static List<Cluster> teamClusters(String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, TEAM_CLUSTERS,
                LogType.TEAM_CLUSTERS_ERROR, null, null, null, token);
        return obj.has("clusters") ? Cluster.parse(obj.getAsJsonArray("clusters")) : new ArrayList<>(0);
    }

    public static List<User> teamUsers(String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, TEAM_USERS,
                LogType.TEAM_USERS_ERROR, null, null, null, token);
        return obj.has("users") ? User.parse(obj.getAsJsonArray("users")) : new ArrayList<>(0);
    }
}
