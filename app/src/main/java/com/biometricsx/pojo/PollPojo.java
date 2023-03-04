package com.biometricsx.pojo;

import java.io.Serializable;

public class PollPojo implements Serializable {
    //pid,pname,pdesc,pstart,pend,isprivate,scode
    //scored,tot - only for current

    private String pid, pname, pdesc, pstart, pend, isprivate, scode, scoredCurrent, totCurrent;

    public PollPojo(String pid, String pname, String pdesc, String pstart, String pend, String isprivate, String scode) {
        this.pid = pid;
        this.pname = pname;
        this.pdesc = pdesc;
        this.pstart = pstart;
        this.pend = pend;
        this.isprivate = isprivate;
        this.scode = scode;
    }

    public PollPojo(String pid, String pname, String pdesc, String pstart, String pend, String isprivate, String scode,
                    String scoredCurrent, String totCurrent) {
        this.pid = pid;
        this.pname = pname;
        this.pdesc = pdesc;
        this.pstart = pstart;
        this.pend = pend;
        this.isprivate = isprivate;
        this.scode = scode;
        this.scoredCurrent = scoredCurrent;
        this.totCurrent = totCurrent;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getPdesc() {
        return pdesc;
    }

    public void setPdesc(String pdesc) {
        this.pdesc = pdesc;
    }

    public String getPstart() {
        return pstart;
    }

    public void setPstart(String pstart) {
        this.pstart = pstart;
    }

    public String getPend() {
        return pend;
    }

    public void setPend(String pend) {
        this.pend = pend;
    }

    public String getScoredCurrent() {
        return scoredCurrent;
    }

    public void setScoredCurrent(String scoredCurrent) {
        this.scoredCurrent = scoredCurrent;
    }

    public String getTotCurrent() {
        return totCurrent;
    }

    public void setTotCurrent(String totCurrent) {
        this.totCurrent = totCurrent;
    }

    public String getIsprivate() {
        return isprivate;
    }

    public void setIsprivate(String isprivate) {
        this.isprivate = isprivate;
    }

    public String getScode() {
        return scode;
    }

    public void setScode(String scode) {
        this.scode = scode;
    }
}
