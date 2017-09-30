package io.gex.core.model.parameters;

import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.User;
import org.apache.commons.lang3.StringUtils;

public class UserParameters {

    private final static LogWrapper logger = LogWrapper.create(UserParameters.class);

    private final static String FIRST_NAME_PARAMETER = "--firstName=";
    private final static String LAST_NAME_PARAMETER = "--lastName=";
    private final static String ABOUT_PARAMETER = "--about=";
    private final static String TEAM_NAME_PARAMETER = "--teamName=";
    private final static String EMAIL_PARAMETER = "--email=";
    private final static String VERIFICATION_TOKEN_PARAMETER = "--verifyToken=";
    private final static String PASSWORD_PARAMETER = "--password=";
    private final static String PHONE_NUM_PARAMETER = "--phoneNumber=";
    private final static String USERNAME_PARAMETER = "--username=";

    public static User parse(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        User user = new User();
        try {
            for (String argument : arguments) {
                if (StringUtils.containsIgnoreCase(argument, USERNAME_PARAMETER)) {
                    user.setUsername(BaseHelper.trimAndRemoveSubstring(argument, USERNAME_PARAMETER));
                    if (StringUtils.isBlank(user.getUsername())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, FIRST_NAME_PARAMETER)) {
                    user.setFirstName(BaseHelper.trimAndRemoveSubstring(argument, FIRST_NAME_PARAMETER));
                    if (StringUtils.isBlank(user.getFirstName())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, LAST_NAME_PARAMETER)) {
                    user.setLastName(BaseHelper.trimAndRemoveSubstring(argument, LAST_NAME_PARAMETER));
                    if (StringUtils.isBlank(user.getLastName())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, TEAM_NAME_PARAMETER)) {
                    user.setTeamName(BaseHelper.trimAndRemoveSubstring(argument, TEAM_NAME_PARAMETER));
                    if (StringUtils.isBlank(user.getTeamName())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, EMAIL_PARAMETER)) {
                    user.setEmail(BaseHelper.trimAndRemoveSubstring(argument, EMAIL_PARAMETER));
                    if (StringUtils.isBlank(user.getEmail())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, VERIFICATION_TOKEN_PARAMETER)) {
                    user.setVerificationToken(BaseHelper.trimAndRemoveSubstring(argument, VERIFICATION_TOKEN_PARAMETER));
                    if (StringUtils.isBlank(user.getVerificationToken())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, PASSWORD_PARAMETER)) {
                    user.setPassword(BaseHelper.trimAndRemoveSubstring(argument, PASSWORD_PARAMETER));
                    if (StringUtils.isBlank(user.getPassword())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, PHONE_NUM_PARAMETER)) {
                    user.setPhoneNumber(BaseHelper.trimAndRemoveSubstring(argument, PHONE_NUM_PARAMETER));
                    if (StringUtils.isBlank(user.getPhoneNumber())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else {
                    throw new IllegalArgumentException(argument);
                }
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.INVALID_PARAMETER + e.getMessage(), LogType.PARSE_ERROR);
        }
        return user;
    }

    public static User parseUpdate(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        User user = new User();
        try {
            for (String argument : arguments) {
                if (StringUtils.containsIgnoreCase(argument, FIRST_NAME_PARAMETER)) {
                    user.setFirstName(BaseHelper.trimAndRemoveSubstring(argument, FIRST_NAME_PARAMETER));
                    if (StringUtils.isBlank(user.getFirstName())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, LAST_NAME_PARAMETER)) {
                    user.setLastName(BaseHelper.trimAndRemoveSubstring(argument, LAST_NAME_PARAMETER));
                    if (StringUtils.isBlank(user.getLastName())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, ABOUT_PARAMETER)) {
                    user.setAbout(BaseHelper.trimAndRemoveSubstring(argument, ABOUT_PARAMETER));
                    if (StringUtils.isBlank(user.getAbout())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else {
                    throw new IllegalArgumentException(argument);
                }
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.INVALID_PARAMETER + e.getMessage(), LogType.PARSE_ERROR);
        }
        return user;
    }

}
