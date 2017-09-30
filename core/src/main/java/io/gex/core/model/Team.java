package io.gex.core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.gex.core.CoreMessages;
import io.gex.core.GsonHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;

import java.util.List;
import java.util.Objects;

public class Team {

    private final static LogWrapper logger = LogWrapper.create(Team.class);

    private String id;
    private String name;
    private String about;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public static Team parse(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(obj, Team.class, CoreMessages.TEAM_PARSING_ERROR);
    }

    public static List<Team> parse(JsonArray teamsArray) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(teamsArray, Team.class, CoreMessages.TEAM_PARSING_ERROR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
