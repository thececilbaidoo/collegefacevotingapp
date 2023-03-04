package com.biometricsx.pojo;

import java.io.Serializable;

public class UsrsPojo implements Serializable {
    ////uid,name,email,contact, idno
    private String uid;
    private String name;
    private String email;

    private String contact;
    private String identificationno;

    public UsrsPojo(String uid, String name, String email, String contact, String identificationno) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.contact = contact;
        this.identificationno = identificationno;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getIdentificationno() {
        return identificationno;
    }

    public void setIdentificationno(String identificationno) {
        this.identificationno = identificationno;
    }
}