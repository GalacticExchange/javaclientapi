package io.gex.core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import io.gex.core.CoreMessages;
import io.gex.core.GsonHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;

import java.util.List;

//todo divide two entities
public class Application {

    private final static LogWrapper logger = LogWrapper.create(Application.class);

    private String id;
    private String name;
    private String title;
    private String categoryTitle;
    private String companyName;
    // todo change type
    private String releaseDate;
    // todo change type
    private String status;
    private String notes;
    private String clusterID;
    @SerializedName("clusterApplicationId")
    private String clusterApplicationID;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getClusterID() {
        return clusterID;
    }

    public void setClusterID(String clusterID) {
        this.clusterID = clusterID;
    }

    public String getClusterApplicationID() {
        return clusterApplicationID;
    }

    public void setClusterApplicationID(String clusterApplicationID) {
        this.clusterApplicationID = clusterApplicationID;
    }

    public static Application parse(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(obj, Application.class, CoreMessages.APPLICATION_PARSING_ERROR);
    }

    public static List<Application> parse(JsonArray applicationsArray) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(applicationsArray, Application.class, CoreMessages.APPLICATION_PARSING_ERROR);
    }

    public Application copy() {
        Application res = new Application();
        res.id = this.id;
        res.name = this.name;
        return res;
    }

}
