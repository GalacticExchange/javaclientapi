package io.gex.core.model;


import com.google.gson.annotations.SerializedName;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class SshCredentials {
    private String username;
    private String password;
    private String host;
    private Integer port;
    private AuthMethod authMethod;
    private String privateKey;
    private String passphrase;

    public enum AuthMethod {
        @SerializedName("password")
        PASSWORD,
        @SerializedName("privateKey")
        PRIVATE_KEY
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

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    /*
            *
            * if return message it's invalid
            * */
    public static String validate(SshCredentials credentials) {
        if (credentials == null) {
            return "SSH credentials object must not me null.";
        } else if (isEmpty(credentials.username)) {
            return "SSH username must not be empty.";
        } else if (credentials.authMethod == null) {
            return "SSH authentication method must not be null.";
        } else if (credentials.authMethod == AuthMethod.PASSWORD && isEmpty(credentials.password)) {
            return "SSH user password must not be null.";
        } else if (credentials.authMethod == AuthMethod.PRIVATE_KEY && isEmpty(credentials.privateKey)) {
            return "SSH user private key must not be null.";
        } else if (isEmpty(credentials.host)) {
            return "SSH host or IP address must not be empty.";
        } else if (credentials.port == null || credentials.port < 1 || credentials.port > 65535) {
            return "SSH port must be between 1 and 65535.";
        } else {
            return null;
        }
    }
}
