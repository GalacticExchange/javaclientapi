package io.gex.core.api;

import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Invitation;
import io.gex.core.model.parameters.InviteParameters;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.InviteLevelRest;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class InviteLevelApi {

    private final static LogWrapper logger = LogWrapper.create(InviteLevelApi.class);

    public static void invitationRemove(Long id) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (id == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_INVITATION_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        InviteLevelRest.inviteRemove(id, BasePropertiesHelper.getValidToken());
    }

    public static void inviteShare(InviteParameters inviteParameters) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (inviteParameters == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_EMAIL + "\n" + CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(inviteParameters.getEmail())) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_EMAIL, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(inviteParameters.getClusterID())) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_CLUSTER_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        InviteLevelRest.inviteShare(inviteParameters, BasePropertiesHelper.getValidToken());
    }

    public static List<Invitation> shareInvitationList(String clusterID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return InviteLevelRest.shareInvitationList(clusterID, BasePropertiesHelper.getValidToken());
    }

    public static void inviteUser(String email) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(email)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_EMAIL, LogType.EMPTY_PROPERTY_ERROR);
        }
        InviteLevelRest.inviteUser(email, BasePropertiesHelper.getValidToken());
    }

    public static List<Invitation> userInvitationList() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return InviteLevelRest.userInvitationList(BasePropertiesHelper.getValidToken());
    }

}
