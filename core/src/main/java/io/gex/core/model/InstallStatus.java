package io.gex.core.model;


import com.google.gson.JsonObject;

import java.util.concurrent.atomic.AtomicInteger;

public class InstallStatus {
    private String status;
    private int progress;
    private int subprogressPortion;
    private AtomicInteger subProgress;

    public InstallStatus(String status, int progress) {
        this(status, progress, 0);
    }

    public InstallStatus(String status, int progress, int subprogressPortion) {
        this.status = status;
        this.progress = progress;
        this.subprogressPortion = subprogressPortion;
        this.subProgress = new AtomicInteger(0);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getSubprogressPortion() {
        return subprogressPortion;
    }

    public void setSubprogressPortion(int subprogressPortion) {
        this.subprogressPortion = subprogressPortion;
    }

    public AtomicInteger getSubProgress() {
        return subProgress;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        int subProgress = this.subProgress.get();
        json.addProperty("status", subProgress == 0 ? status : status + ": " + subProgress + "% transferred"); //todo hotfix only for downloading progress
        json.addProperty("progress", progress + Math.round(subProgress * (subprogressPortion / 100.0)));
        return json;
    }
}
