package com.biometricsx.pojo;

import java.io.Serializable;

public class CandidatePojoExpired implements Serializable {
    private String cid, pid, cname, cphoto, cdesc, scoredCount, outOfTot;
    //cid,pid,cname,cphoto,cdesc,scoredCount,outOfTot
    //scoredCount - NO then i have not voted
    //outOfTot - YES/NO

    public CandidatePojoExpired() {
    }

    public CandidatePojoExpired(String cid, String pid, String cname, String cphoto, String cdesc, String scoredCount, String outOfTot) {
        this.cid = cid;
        this.pid = pid;
        this.cname = cname;
        this.cphoto = cphoto;
        this.cdesc = cdesc;
        this.scoredCount = scoredCount;
        this.outOfTot = outOfTot;
    }


    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
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

    public String getScoredCount() {
        return scoredCount;
    }

    public void setScoredCount(String scoredCount) {
        this.scoredCount = scoredCount;
    }

    public String getOutOfTot() {
        return outOfTot;
    }

    public void setOutOfTot(String outOfTot) {
        this.outOfTot = outOfTot;
    }
}
