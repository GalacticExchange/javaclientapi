package io.gex.core.model;


import com.google.gson.JsonObject;
import io.gex.core.CoreMessages;
import io.gex.core.GsonHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;

public class SocksProxy {

    private final static LogWrapper logger = LogWrapper.create(SocksProxy.class);

    private String host;
    private String user;
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static SocksProxy parse(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(obj, SocksProxy.class, CoreMessages.SOCKS_PROXY_PARSING_ERROR);
    }
}
