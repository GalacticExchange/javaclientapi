package io.gex.core.rest;

import com.google.gson.JsonObject;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Invitation;
import io.gex.core.model.parameters.InviteParameters;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class InviteLevelRest {

    private final static LogWrapper logger = LogWrapper.create(InviteLevelRest.class);

    private final static String INVITATIONS = "/invitations";
    private final static String SHARE_INVITATIONS = "/shareInvitations";
    private final static String USER_INVITATIONS = "/userInvitations";

    public static void inviteRemove(Long id, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> header = new MultivaluedHashMap<>();
        if (id != null) {
            header.add("id", String.valueOf(id));
        }
        Rest.sendAuthenticatedRequest(HttpMethod.DELETE, INVITATIONS,
                LogType.INVITATION_REMOVE_ERROR, header, null, null, token);
    }

    public static void inviteShare(InviteParameters inviteParameters, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("email", inviteParameters.getEmail());
        body.add("clusterID", inviteParameters.getClusterID());
        Rest.sendAuthenticatedRequest(HttpMethod.POST, SHARE_INVITATIONS,  LogType.SHARE_INVITE_ERROR,
                null, body, null, token);
    }

    public static List<Invitation> shareInvitationList(String clusterID, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.add("clusterID", clusterID);
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, SHARE_INVITATIONS,
                LogType.SHARE_INVITATIONS_LIST_ERROR, null, null, query, token);
        return obj.has("invitations") ? Invitation.parse(obj.getAsJsonArray("invitations")) : new ArrayList<>(0);
    }

    public static void inviteUser(String email, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("email", email);
        Rest.sendAuthenticatedRequest(HttpMethod.POST, USER_INVITATIONS,  LogType.USER_INVITE_ERROR, null,
                body, null, token);
    }

    public static List<Invitation> userInvitationList(String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, USER_INVITATIONS,
                LogType.USER_INVITATIONS_LIST_ERROR, null, null, null, token);
        return obj.has("invitations") ? Invitation.parse(obj.getAsJsonArray("invitations")) : new ArrayList<>(0);
    }

}
