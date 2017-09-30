package io.gex.cli;

import io.gex.core.api.UserLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.User;
import io.gex.core.model.UserRole;
import io.gex.core.model.parameters.UserParameters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

class UserLevel {

    private final static LogWrapper logger = LogWrapper.create(UserLevel.class);

    private static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.USER_CREATE_COMMAND, " ", CliMessages.USER_CREATE_DESCRIPTION).
                addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.USER_VERIFY_COMMAND, " ", CliMessages.USER_VERIFY_DESCRIPTION).
                addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.USER_REMOVE_COMMAND, " ", CliMessages.USER_REMOVE_DESCRIPTION).
                addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.USER_INFO_COMMAND, " ", CliMessages.USER_INFO_DESCRIPTION).
                addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.USER_UPDATE_COMMAND, " ", CliMessages.USER_UPDATE_DESCRIPTION).
                addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.USER_CHANGE_ROLE_COMMAND, " ", CliMessages.USER_CHANGE_ROLE_DESCRIPTION).
                addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.PASSWORD, CliMessages.USER_CHANGE_PASSWORD_COMMAND,
                        CliMessages.USER_CHANGE_PASSWORD_DESCRIPTION).
                addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.PASSWORD, CliMessages.USER_RESET_PASSWORD_LINK_COMMAND,
                        CliMessages.USER_RESET_PASSWORD_LINK_DESCRIPTION).
                addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.PASSWORD, CliMessages.USER_CONFIRM_PASSWORD_COMMAND,
                        CliMessages.USER_CONFIRM_PASSWORD_DESCRIPTION);
        columns.print();
    }

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.mainMenuHelpCheck(arguments)) {
            help();
        } else if (arguments[0].toLowerCase().equals(CliMessages.USER_CREATE_COMMAND)) {
            userCreate(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.USER_VERIFY_COMMAND)) {
            userVerify(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.USER_CHANGE_ROLE_COMMAND)) {
            userChangeRole(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.USER_REMOVE_COMMAND)) {
            userRemove(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.USER_INFO_COMMAND)) {
            userInfo(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.USER_UPDATE_COMMAND)) {
            userUpdate(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.PASSWORD)) {
            UserPasswordLevel.executeCommand(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
            System.exit(1);
        }
    }

    private static void userInfo(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.USER_INFO_PARAMS);
            return;
        } else if (arguments.length > 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.USER_INFO_PARAMS);
            System.exit(1);
        }
        User user;
        if (arguments.length == 1) {
            user = UserLevelApi.userInfo(arguments[0]);
        } else {
            user = UserLevelApi.userInfo();
        }
        printUser(user);
    }

    private static void userUpdate(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.USER_UPDATE_PARAMS);
            return;
        } else if (arguments.length == 0 || arguments.length > 3) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.USER_UPDATE_PARAMS);
            System.exit(1);
        }
        UserLevelApi.userUpdate(UserParameters.parseUpdate(arguments));
        LogHelper.print(CliMessages.USER_UPDATED);
    }

    private static void userChangeRole(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.USER_CHANGE_ROLE_PARAMS);
            return;
        } else if (arguments.length != 2) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.USER_CHANGE_ROLE_PARAMS);
            System.exit(1);
        }
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(arguments[1].toLowerCase());
        } catch (Exception e) {
            CliHelper.printError(CliMessages.USER_ROLES);
            return;
        }
        UserLevelApi.userChangeRole(arguments[0], userRole);
        LogHelper.print(CliMessages.USER_CHANGE_ROLE);
    }

    private static void userRemove(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.USER_REMOVE_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.USER_REMOVE_PARAMS);
            System.exit(1);
        }
        UserLevelApi.userRemove(arguments[0]);
        LogHelper.print(CliMessages.USER_REMOVE);
    }

    private static void userCreate(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.USER_CREATE_PARAMS);
            return;
        } else if (arguments.length < 5 || arguments.length > 8) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.USER_CREATE_PARAMS);
            System.exit(1);
        }
        User user = UserParameters.parse(arguments);
        UserLevelApi.userCreate(user);
        if (StringUtils.isBlank(user.getVerificationToken())) {
            CliMessages.printConfirmationCodeMessage(user.getPhoneNumber());
        }
    }

    private static void userVerify(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.USER_VERIFY_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.USER_VERIFY_PARAMS);
            System.exit(1);
        }
        System.out.println(CliMessages.USER_VERIFY);
        UserLevelApi.userVerify(arguments[0]);
    }

    private static void userResetPassword(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.USER_CONFIRM_PASSWORD_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.USER_CONFIRM_PASSWORD_PARAMS);
            System.exit(1);
        }
        String password = CliHelper.getAndConfirmPasswordFromConsole(CliMessages.USER_PASSWORD);
        UserLevelApi.userResetPassword(arguments[0], password);
        LogHelper.print(CliMessages.USER_PASSWORD_CHANGED);
    }

    private static void userResetPasswordLink(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.USER_RESET_PASSWORD_LINK_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.USER_RESET_PASSWORD_LINK_PARAMS);
            System.exit(1);
        }
        UserLevelApi.userResetPasswordLink(arguments[0]);
        CliMessages.printConfirmationCodeMessage(arguments[0]);
    }

    private static void userChangePassword(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.USER_CHANGE_PASSWORD_PARAMS);
            return;
        } else if (arguments.length > 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.USER_CHANGE_PASSWORD_PARAMS);
            System.exit(1);
        }
        String username = null, oldPassword = null;
        if (arguments.length == 1) {
            username = arguments[0];
        } else {
            oldPassword = CliHelper.getPasswordFromConsole(CliMessages.OLD_PASSWORD);
        }
        String newPassword = CliHelper.getPasswordFromConsole(CliMessages.NEW_PASSWORD);
        if (StringUtils.isNoneBlank(oldPassword, newPassword)) {
            UserLevelApi.userChangeMyPassword(oldPassword, newPassword);
        } else if (StringUtils.isNoneBlank(username, newPassword)) {
            UserLevelApi.userChangeUserPassword(newPassword, username);
        } else {
            CliHelper.printError(CliMessages.USAGE + CliMessages.USER_CHANGE_PASSWORD_PARAMS);
            return;
        }
        LogHelper.print(CliMessages.USER_PASSWORD_CHANGED);
    }

    static void printUserList(List<User> users) {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CollectionUtils.isEmpty(users)) {
            LogHelper.print(CliMessages.USER_EMPTY);
            return;
        }
        for (User user : users) {
            System.out.println(CliMessages.DELIMITER);
            printUser(user);
            System.out.println(CliMessages.DELIMITER);
        }
    }

    private static void printUser(User user) {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (user == null) {
            return;
        }
        Columns columns = new Columns();
        if (StringUtils.isNotBlank(user.getUsername())) {
            columns.addLine(CliMessages.USERNAME, user.getUsername());
        }
        if (StringUtils.isNotBlank(user.getFirstName())) {
            columns.addLine(CliMessages.FIRST_NAME, user.getFirstName());
        }
        if (StringUtils.isNotBlank(user.getLastName())) {
            columns.addLine(CliMessages.LAST_NAME, user.getLastName());
        }
        if (StringUtils.isNotBlank(user.getTeamName())) {
            columns.addLine(CliMessages.TEAM_NAME, user.getTeamName());
        }
        if (StringUtils.isNotBlank(user.getEmail())) {
            columns.addLine(CliMessages.EMAIL, user.getEmail());
        }
        if (user.getRole() != null) {
            columns.addLine(CliMessages.ROLE, user.getRole().toString().toLowerCase());
        }
        if (StringUtils.isNotBlank(user.getAbout())) {
            columns.addLine(CliMessages.ABOUT, user.getAbout());
        }
        columns.print();
    }

    private static class UserPasswordLevel {

        private final static LogWrapper logger = LogWrapper.create(UserLevel.class);

        private static void help() {
            logger.trace("Entered " + LogHelper.getMethodName());
            Columns columns = new Columns().
                    addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.PASSWORD, CliMessages.USER_CHANGE_PASSWORD_COMMAND,
                            CliMessages.USER_CHANGE_PASSWORD_DESCRIPTION).
                    addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.PASSWORD, CliMessages.USER_RESET_PASSWORD_LINK_COMMAND,
                            CliMessages.USER_RESET_PASSWORD_LINK_DESCRIPTION).
                    addLine(CliMessages.USER_START_HELP_SYMBOL + CliMessages.PASSWORD, CliMessages.USER_CONFIRM_PASSWORD_COMMAND,
                            CliMessages.USER_CONFIRM_PASSWORD_DESCRIPTION);
            columns.print();
        }

        public static void executeCommand(String[] arguments) throws GexException {
            logger.trace("Entered " + LogHelper.getMethodName());
            if (CliHelper.mainMenuHelpCheck(arguments)) {
                help();
            } else if (arguments[0].toLowerCase().equals(CliMessages.USER_CHANGE_PASSWORD_COMMAND)) {
                userChangePassword(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.USER_RESET_PASSWORD_LINK_COMMAND)) {
                userResetPasswordLink(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.USER_CONFIRM_PASSWORD_COMMAND)) {
                userResetPassword(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else {
                CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
                System.exit(1);
            }
        }
    }
}
