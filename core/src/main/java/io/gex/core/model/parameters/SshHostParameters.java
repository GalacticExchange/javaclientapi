package io.gex.core.model.parameters;

import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

public class SshHostParameters {

    private final static LogWrapper logger = LogWrapper.create(UserParameters.class);

    private String host;
    private Integer port;
    private String proxy;
    private String proxyUsername;
    private String proxyPassword;
    private String username;
    private String password;

    private final static String HOST_PARAMETER = "--host=";
    private final static String PORT_PARAMETER = "--port=";
    private final static String PROXY_PARAMETER = "--proxy=";
    private final static String PROXY_USERNAME_PARAMETER = "--proxyUsername=";
    private final static String PROXY_PASSWORD_PARAMETER = "--proxyPassword=";
    private final static String USERNAME_PARAMETER = "--username=";
    private final static String PASSWORD_PARAMETER = "--password=";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public SshHostParameters(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            for (String argument : arguments) {
                if (StringUtils.containsIgnoreCase(argument, HOST_PARAMETER)) {
                    this.setHost(BaseHelper.trimAndRemoveSubstring(argument, HOST_PARAMETER));
                    if (StringUtils.isBlank(this.getHost())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, PORT_PARAMETER)) {
                    this.setPort(Integer.valueOf(BaseHelper.trimAndRemoveSubstring(argument, PORT_PARAMETER)));
                    if (this.getPort() == null) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, PROXY_PARAMETER)) {
                    this.setProxy(BaseHelper.trimAndRemoveSubstring(argument, PROXY_PARAMETER));
                    if (StringUtils.isBlank(this.getProxy())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, USERNAME_PARAMETER)) {
                    this.setUsername(BaseHelper.trimAndRemoveSubstring(argument, USERNAME_PARAMETER));
                    if (StringUtils.isBlank(this.getUsername())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, PASSWORD_PARAMETER)) {
                    this.setPassword(BaseHelper.trimAndRemoveSubstring(argument, PASSWORD_PARAMETER));
                    if (StringUtils.isBlank(this.getPassword())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, PROXY_USERNAME_PARAMETER)) {
                    this.setProxyUsername(BaseHelper.trimAndRemoveSubstring(argument, PROXY_USERNAME_PARAMETER));
                    if (StringUtils.isBlank(this.getProxyUsername())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else if (StringUtils.containsIgnoreCase(argument, PROXY_PASSWORD_PARAMETER)) {
                    this.setProxyPassword(BaseHelper.trimAndRemoveSubstring(argument, PROXY_PASSWORD_PARAMETER));
                    if (StringUtils.isBlank(this.getProxyPassword())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else {
                    throw new IllegalArgumentException(argument);
                }
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.INVALID_PARAMETER + e.getMessage(), LogType.PARSE_ERROR);
        }
    }

}
