package io.gex.core;

import io.gex.core.api.UserLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.model.User;
import org.apache.logging.log4j.ThreadContext;

public class AppContext {

    public static final String USERNAME_PARAM = "username";
    private static final String TOKEN_PARAM = "token";

    public static void set(String token) throws GexException {
        ThreadContext.put(TOKEN_PARAM, token);
        User user = UserLevelApi.userInfo();
        ThreadContext.put(USERNAME_PARAM, user.getUsername());
    }

    public static void clear() {
        ThreadContext.clearMap();
    }

    public static boolean isSet() {
        return !ThreadContext.isEmpty();
    }

    private AppContext() {

    }

    public static String getUsername() {
        return ThreadContext.get(USERNAME_PARAM);
    }

    public static String getToken() {
        return ThreadContext.get(TOKEN_PARAM);
    }
}
