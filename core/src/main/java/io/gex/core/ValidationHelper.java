package io.gex.core;

import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class ValidationHelper {

    private final static LogWrapper logger = LogWrapper.create(ValidationHelper.class);
    public final static Integer MIN_PASSWORD_LENGTH = 8;
    public final static Integer MAX_PASSWORD_LENGTH = 60;
    public final static Integer MIN_NAME_LENGTH = 2;
    public final static Integer MAX_NAME_LENGTH = 32;
    public final static Integer MIN_USERNAME_LENGTH = 2;
    public final static Integer MAX_USERNAME_LENGTH = 20;
    private static String regex = "^[a-z](-[a-z\\d]|[a-z\\d])+$";

    public static void emailValidator(String email) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(email)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_EMAIL, LogType.EMPTY_PROPERTY_ERROR);
        }
    }

    public static void passwordValidator(String password) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(password)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_PASSWORD, LogType.EMPTY_PROPERTY_ERROR);
        }
        if (password.length() > MAX_PASSWORD_LENGTH || password.length() < MIN_PASSWORD_LENGTH) {
            throw logger.logAndReturnException(getPasswordLengthMessage(), LogType.INVALID_PROPERTY_ERROR);
        }
    }

    public static void passwordIfNotEmptyValidator(String password) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isNotBlank(password) && (password.length() > MAX_PASSWORD_LENGTH || password.length() < MIN_PASSWORD_LENGTH)) {
            throw logger.logAndReturnException(getPasswordLengthMessage(), LogType.INVALID_PROPERTY_ERROR);
        }
    }

    public static void firstNameValidator(String firstName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(firstName)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_FIRST_NAME, LogType.EMPTY_PROPERTY_ERROR);
        }
        if (firstName.length() > MAX_NAME_LENGTH || firstName.length() < MIN_NAME_LENGTH) {
            throw logger.logAndReturnException(getFirstNameLengthMessage(), LogType.INVALID_PROPERTY_ERROR);
        }
    }

    public static void lastNameValidator(String lastName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(lastName)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_LAST_NAME, LogType.EMPTY_PROPERTY_ERROR);
        }
        if (lastName.length() > MAX_NAME_LENGTH || lastName.length() < MIN_NAME_LENGTH) {
            throw logger.logAndReturnException(getLastNameLengthMessage(), LogType.INVALID_PROPERTY_ERROR);
        }
    }

    public static void usernameValidator(String username) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(username)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_USERNAME, LogType.EMPTY_PROPERTY_ERROR);
        }
        if (username.length() > MAX_USERNAME_LENGTH || username.length() < MIN_USERNAME_LENGTH) {
            throw logger.logAndReturnException(getUsernameLengthMessage(), LogType.INVALID_PROPERTY_ERROR);
        }
        Pattern pattern = Pattern.compile(regex);
        if (!pattern.matcher(username).matches()) {
            throw logger.logAndReturnException(CoreMessages.USERNAME_REGEX_ERROR, LogType.INVALID_PROPERTY_ERROR);
        }
    }

    public static void teamNameValidator(String teamName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(teamName)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_TEAM_NAME, LogType.EMPTY_PROPERTY_ERROR);
        }
        if (teamName.length() > MAX_USERNAME_LENGTH || teamName.length() < MIN_USERNAME_LENGTH) {
            throw logger.logAndReturnException(getTeamNameLengthMessage(), LogType.INVALID_PROPERTY_ERROR);
        }
        Pattern pattern = Pattern.compile(regex);
        if (!pattern.matcher(teamName).matches()) {
            throw logger.logAndReturnException(CoreMessages.TEAM_NAME_REGEX_ERROR, LogType.INVALID_PROPERTY_ERROR);
        }
    }

    private static String getPasswordLengthMessage() {
        return "Password length must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters long.";
    }

    private static String getFirstNameLengthMessage() {
        return "First name length must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters long.";
    }

    private static String getLastNameLengthMessage() {
        return "Last name length must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters long.";
    }

    private static String getUsernameLengthMessage() {
        return "Username length must be between " + MIN_USERNAME_LENGTH + " and " + MAX_USERNAME_LENGTH + " characters long.";
    }

    private static String getTeamNameLengthMessage() {
        return "Teamname length must be between " + MIN_USERNAME_LENGTH + " and " + MAX_USERNAME_LENGTH + " characters long.";
    }
}
