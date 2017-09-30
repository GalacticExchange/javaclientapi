package io.gex.cli;

import io.gex.core.api.InviteLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Invitation;
import io.gex.core.model.parameters.InviteParameters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import static io.gex.core.DateConverter.DATE_TIME_FORMAT;
import static io.gex.core.DateConverter.localDateTimeToString;

class InviteLevel {

    private final static LogWrapper logger = LogWrapper.create(InviteLevel.class);

    private static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                addLine(CliMessages.INVITE_START_HELP_SYMBOL + CliMessages.INVITE_USER_COMMAND, CliMessages.INVITE_USER_DESCRIPTION).
                addLine(CliMessages.INVITE_START_HELP_SYMBOL + CliMessages.INVITE_SHARE_COMMAND, CliMessages.INVITE_SHARE_DESCRIPTION).
                addLine(CliMessages.INVITE_START_HELP_SYMBOL + CliMessages.INVITE_REMOVE_COMMAND, CliMessages.INVITE_REMOVE_DESCRIPTION).
                addLine(CliMessages.INVITE_START_HELP_SYMBOL + CliMessages.INVITE_USERLIST_COMMAND, CliMessages.INVITE_USERLIST_DESCRIPTION).
                addLine(CliMessages.INVITE_START_HELP_SYMBOL + CliMessages.INVITE_SHARELIST_COMMAND, CliMessages.INVITE_SHARELIST_DESCRIPTION);
        columns.print();
    }

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.mainMenuHelpCheck(arguments)) {
            help();
        } else if (arguments[0].toLowerCase().equals(CliMessages.INVITE_REMOVE_COMMAND)) {
            inviteRemove(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.INVITE_USER_COMMAND)) {
            inviteUser(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.INVITE_SHARE_COMMAND)) {
            inviteShare(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.INVITE_USERLIST_COMMAND)) {
            userInvitationList(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.INVITE_SHARELIST_COMMAND)) {
            shareInvitationList(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
            System.exit(1);
        }
    }

    private static void inviteRemove(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.INVITE_REMOVE_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.INVITE_REMOVE_PARAMS);
            System.exit(1);
        }
        try {
            InviteLevelApi.invitationRemove(Long.parseLong(arguments[0], 10));
            LogHelper.print(CliMessages.INVITE_REMOVED);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(CliMessages.INVITE_ID_ERROR);
        }

    }

    private static void inviteUser(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.INVITE_USER_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.INVITE_USER_PARAMS);
            System.exit(1);
        }
        InviteLevelApi.inviteUser(arguments[0]);
        LogHelper.print(CliMessages.INVITE);
    }

    private static void inviteShare(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.INVITE_SHARE_PARAMS);
            return;
        } else if (arguments.length != 2) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.INVITE_SHARE_PARAMS);
            System.exit(1);
        }
        InviteLevelApi.inviteShare(new InviteParameters(arguments));
        LogHelper.print(CliMessages.INVITE);
    }

    private static void userInvitationList(String[] arguments) throws GexException{
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.INVITE_USERLIST_PARAMS);
            return;
        } else if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.INVITE_USERLIST_PARAMS);
            System.exit(1);
        }
        printInvitationList(InviteLevelApi.userInvitationList());
    }

    private static void shareInvitationList(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.INVITE_SHARELIST_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.INVITE_SHARELIST_PARAMS);
            System.exit(1);
        }
        printInvitationList(InviteLevelApi.shareInvitationList(arguments[0]));
    }

    private static void printInvitationList(List<Invitation> invitations) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CollectionUtils.isEmpty(invitations)) {
            LogHelper.print(CliMessages.INVITATIONS_EMPTY);
            return;
        }
        for (Invitation invitation : invitations) {
            System.out.println(CliMessages.DELIMITER);
            printInvitation(invitation);
            System.out.println(CliMessages.DELIMITER);
        }
    }

    private static void printInvitation(Invitation invitation) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (invitation == null) {
            return;
        }
        Columns columns = new Columns();
        if (invitation.getId() != null) {
            columns.addLine(CliMessages.ID, invitation.getId().toString());
        }
        if (StringUtils.isNotBlank(invitation.getEmail())) {
            columns.addLine(CliMessages.EMAIL, invitation.getEmail());
        }
        if (invitation.getDate() != null) {
            columns.addLine(CliMessages.DATE, localDateTimeToString(invitation.getDate(), DATE_TIME_FORMAT));
        }
        columns.print();
    }
}
