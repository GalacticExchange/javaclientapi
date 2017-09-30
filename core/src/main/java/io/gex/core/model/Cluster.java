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
import java.util.Objects;

public class Cluster {

    private final static LogWrapper logger = LogWrapper.create(Cluster.class);

    private String id;
    private String name;
    @SerializedName("domainname")
    private String domainName;
    private Team team;
    private ClusterStatus status;
    @SerializedName("settings")
    private ClusterSettings clusterSettings;
    private String hadoopApplicationID;
    private EntityType clusterType;

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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public ClusterStatus getStatus() {
        return status;
    }

    public void setStatus(ClusterStatus status) {
        this.status = status;
    }

    public ClusterSettings getClusterSettings() {
        return clusterSettings;
    }

    public void setClusterSettings(ClusterSettings clusterSettings) {
        this.clusterSettings = clusterSettings;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getHadoopApplicationID() {
        return hadoopApplicationID;
    }

    public void setHadoopApplicationID(String hadoopApplicationID) {
        this.hadoopApplicationID = hadoopApplicationID;
    }

    public EntityType getClusterType() {
        return clusterType;
    }

    public void setClusterType(EntityType clusterType) {
        this.clusterType = clusterType;
    }

    public static Cluster parse(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(obj, Cluster.class, CoreMessages.CLUSTER_PARSING_ERROR);
    }

    public static List<Cluster> parse(JsonArray clustersArray) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        return GsonHelper.parse(clustersArray, Cluster.class, CoreMessages.CLUSTER_PARSING_ERROR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cluster cluster = (Cluster) o;
        return Objects.equals(id, cluster.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
