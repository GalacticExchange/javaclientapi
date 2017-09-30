package io.gex.core.rest;

import com.google.gson.JsonObject;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.User;
import io.gex.core.model.UserRole;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class UserLevelRest {

    private final static LogWrapper logger = LogWrapper.create(UserLevelRest.class);

    private final static String USERS = "/users";
    private final static String USER_INFO = "/userInfo";
    private final static String USERS_VERIFY = "/users/verify";
    private final static String PASSWORD = "/users/password";
    private final static String ROLE = "/users/roles";
    private final static String PASSWORD_RESET_LINK = "/users/password/resetlink";

    public static void userRemove(String username, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> header = new MultivaluedHashMap<>();
        header.add("username", username);
        Rest.sendAuthenticatedRequest(HttpMethod.DELETE, USERS,  LogType.USER_REMOVE_ERROR, header, null, null,
                token);
    }

    public static void userVerify(String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("verificationToken", token);
        Rest.sendRequest(HttpMethod.POST, USERS_VERIFY,  LogType.USER_VERIFY_ERROR, null,
                body, null);
    }

    public static User userInfo(String username, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        if (StringUtils.isNotBlank(username)) {
            query.add("username", username);
        }
        JsonObject obj = Rest.sendAuthenticatedRequest(HttpMethod.GET, USER_INFO,  LogType.USER_INFO_ERROR,
                null, null, query, token);
        return obj.has("user") ? User.parse(obj.getAsJsonObject("user")) : null;
    }

    public static void userUpdate(User user, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        if (StringUtils.isNotBlank(user.getFirstName())) {
            body.add("firstName", user.getFirstName());
        }
        if (StringUtils.isNotBlank(user.getLastName())) {
            body.add("lastName", user.getLastName());
        }
        if (StringUtils.isNotBlank(user.getAbout())) {
            body.add("about", user.getAbout());
        }
        Rest.sendAuthenticatedRequest(HttpMethod.PUT, USER_INFO,  LogType.USER_UPDATE_ERROR, null, body,
                null, token);
    }

    public static void userResetPasswordLink(String username) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("username", username);
        Rest.sendRequest(HttpMethod.POST, PASSWORD_RESET_LINK,  LogType.USER_RESET_PASSWORD_LINK_ERROR, null,
                body, null);
    }

    public static void userResetPassword(String token, String password) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("passwordToken", token);
        body.add("password", password);
        Rest.sendRequest(HttpMethod.PUT, PASSWORD,  LogType.USER_RESET_PASSWORD_ERROR, null, body, null);
    }

    public static void userChangeRole(String username, UserRole userRole, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("username", username);
        body.add("role", userRole.toString());
        Rest.sendAuthenticatedRequest(HttpMethod.PUT, ROLE,  LogType.USER_CHANGE_ROLE_ERROR, null, body,
                null, token);
    }

    public static void userChangePassword(String oldPassword, String newPassword, String username, String token) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        if (StringUtils.isNotBlank(oldPassword)) {
            body.add("oldPassword", oldPassword);
        }
        body.add("newPassword", newPassword);
        if (StringUtils.isNotBlank(username)) {
            body.add("username", username);
        }
        Rest.sendAuthenticatedRequest(HttpMethod.PUT, PASSWORD,  LogType.USER_CHANGE_PASSWORD_ERROR, null,
                body, null, token);
    }

    public static void userCreate(User user) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        if (StringUtils.isNotBlank(user.getVerificationToken())) {
            body.add("token", user.getVerificationToken());
        }
        body.add("teamname", user.getTeamName());
        body.add("firstName", user.getFirstName());
        body.add("lastName", user.getLastName());
        body.add("username", user.getUsername());
        body.add("email", user.getEmail());
        body.add("password", user.getPassword());
        body.add("phoneNumber", user.getPhoneNumber());
        Rest.sendRequest(HttpMethod.POST, USERS,  LogType.USER_CREATE_ERROR, null, body, null);
    }
}
