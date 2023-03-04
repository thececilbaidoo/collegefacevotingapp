package com.biometricsx.pojo;

import java.io.Serializable;

public class CandidatePojo implements Serializable {
    private String cid, votedByUid, cname, cphoto, cdesc, havescored, canvote
          ;
    //cid,votedByUid,cname,cphoto,cdesc,havescored,canvote
    //havescored - NO then i have not voted
    //canvote - YES/NO

    public CandidatePojo() {
    }

    public CandidatePojo(String cid, String votedByUid, String cname, String cphoto, String cdesc, String havescored, String canvote) {
        this.cid = cid;
        this.votedByUid = votedByUid;
        this.cname = cname;
        this.cphoto = cphoto;
        this.cdesc = cdesc;
        this.havescored = havescored;
        this.canvote = canvote;
    }


    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getVotedByUid() {
        return votedByUid;
    }

    public void setVotedByUid(String votedByUid) {
        this.votedByUid = votedByUid;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getCphoto() {
        return cphoto;
    }

    public void setCphoto(String cphoto) {
        this.cphoto = cphoto;
    }

    public String getCdesc() {
        return cdesc;
    }

    public void setCdesc(String cdesc) {
        this.cdesc = cdesc;
    }

    public String getHavescored() {
        return havescored;
    }

    public void setHavescored(String havescored) {
        this.havescored = havescored;
    }

    public String getCanvote() {
        return canvote;
    }

    public void setCanvote(String canvote) {
        this.canvote = canvote;
    }
}
