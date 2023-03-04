package com.biometricsx.pojo;

import java.io.Serializable;

public class FacePojo implements Serializable {
    //ng uid, string photo, string pid, string pgid, string faceid, string faceuri,string faceatt
    private String  pid, pgid, faceid, faceuri, faceatt;

    public FacePojo(String pid, String pgid, String faceid, String faceuri, String faceatt) {
        this.pid = pid;
        this.pgid = pgid;
        this.faceid = faceid;
        this.faceuri = faceuri;
        this.faceatt = faceatt;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPgid() {
        return pgid;
    }

    public void setPgid(String pgid) {
        this.pgid = pgid;
    }

    public String getFaceid() {
        return faceid;
    }

    public void setFaceid(String faceid) {
        this.faceid = faceid;
    }

    public String getFaceuri() {
        return faceuri;
    }

    public void setFaceuri(String faceuri) {
        this.faceuri = faceuri;
    }

    public String getFaceatt() {
        return faceatt;
    }

    public void setFaceatt(String faceatt) {
        this.faceatt = faceatt;
    }
}
