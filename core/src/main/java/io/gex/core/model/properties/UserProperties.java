package io.gex.core.model.properties;

public class UserProperties extends BaseProperties {

    private String username;
    private String token;
    private String teamID;

    public final static String TEAM_ID_PROPERTY_NAME = "teamID";
    public final static String USERNAME_PROPERTY_NAME = "username";
    public final static String TOKEN_PROPERTY_NAME = "token";

    public String getTeamID() {
        return teamID;
    }

    public void setTeamID(String teamID) {
        this.teamID = teamID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public UserProperties copy() {
        UserProperties res = new UserProperties();
        res.username = this.username;
        res.token = this.token;
        res.teamID = this.teamID;
        return res;
    }

}
