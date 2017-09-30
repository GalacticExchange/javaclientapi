package io.gex.core.api;

import io.gex.core.CoreMessages;
import io.gex.core.ValidationHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.User;
import io.gex.core.model.UserRole;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.UserLevelRest;
import org.apache.commons.lang3.StringUtils;

public class UserLevelApi {

    private final static LogWrapper logger = LogWrapper.create(UserLevelApi.class);

    public static User userInfo(String username) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(username)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_USERNAME, LogType.EMPTY_PROPERTY_ERROR);
        }
        return UserLevelRest.userInfo(username, BasePropertiesHelper.getValidToken());
    }

    public static User userInfo() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return UserLevelRest.userInfo(null, BasePropertiesHelper.getValidToken());
    }

    public static void userVerify(String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(token)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_VERIFICATION_TOKEN, LogType.EMPTY_PROPERTY_ERROR);
        }
        UserLevelRest.userVerify(token);
    }

    public static void userCreate(User user) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ValidationHelper.teamNameValidator(user.getTeamName());
        ValidationHelper.firstNameValidator(user.getFirstName());
        ValidationHelper.lastNameValidator(user.getLastName());
        ValidationHelper.usernameValidator(user.getUsername());
        ValidationHelper.emailValidator(user.getEmail());
        ValidationHelper.passwordIfNotEmptyValidator(user.getPassword());
        if (StringUtils.isBlank(user.getPhoneNumber())) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_PHONE_NUMBER, LogType.EMPTY_PROPERTY_ERROR);
        }
        UserLevelRest.userCreate(user);
    }

    public static void userResetPasswordLink(String username) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(username)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_USERNAME, LogType.EMPTY_PROPERTY_ERROR);
        }
        UserLevelRest.userResetPasswordLink(username);
    }

    public static void userResetPassword(String token, String password) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(token)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_VERIFICATION_TOKEN, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(password)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_PASSWORD, LogType.EMPTY_PROPERTY_ERROR);
        }
        UserLevelRest.userResetPassword(token, password);
    }

    public static void userRemove(String username) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(username)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_USERNAME, LogType.EMPTY_PROPERTY_ERROR);
        }
        UserLevelRest.userRemove(username, BasePropertiesHelper.getValidToken());
    }

    public static void userChangeMyPassword(String oldPassword, String newPassword) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(oldPassword)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_OLD_PASSWORD, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(newPassword)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NEW_PASSWORD, LogType.EMPTY_PROPERTY_ERROR);
        } else if (oldPassword.equals(newPassword)) {
            throw logger.logAndReturnException(CoreMessages.SAME_PASSWORDS, LogType.EMPTY_PROPERTY_ERROR);
        }
        UserLevelRest.userChangePassword(oldPassword, newPassword, null, BasePropertiesHelper.getValidToken());
    }

    public static void userChangeUserPassword(String newPassword, String username) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(newPassword)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_NEW_PASSWORD, LogType.EMPTY_PROPERTY_ERROR);
        } else if (StringUtils.isBlank(username)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_USERNAME, LogType.EMPTY_PROPERTY_ERROR);
        }
        UserLevelRest.userChangePassword(null, newPassword, username, BasePropertiesHelper.getValidToken());
    }

    public static void userChangeRole(String username, UserRole role) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(username)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_USERNAME, LogType.EMPTY_PROPERTY_ERROR);
        }
        if (role == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_ROLE, LogType.EMPTY_PROPERTY_ERROR);
        }
        UserLevelRest.userChangeRole(username, role, BasePropertiesHelper.getValidToken());
    }

    public static void userUpdate(User user) throws GexException{
        logger.trace("Entered " + LogHelper.getMethodName());
        if (user == null) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_USER, LogType.EMPTY_PROPERTY_ERROR);
        }
        UserLevelRest.userUpdate(user, BasePropertiesHelper.getValidToken());
    }

}
