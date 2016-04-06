package com.ehret.mixit.domain.talk;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Speaker  {

    private Long idMember;

    private String firstname;

    private String lastname;

    private String hash;

    public Long getIdMember() {
        return idMember;
    }

    public Speaker setIdMember(Long idMember) {
        this.idMember = idMember;
        return this;
    }

    public String getFirstname() {
        return firstname;
    }

    public Speaker setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public String getLastname() {
        return lastname;
    }

    public Speaker setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public String getCompleteName() {
        if(firstname ==null || "".equals(firstname)){
            return lastname !=null ? lastname.toUpperCase() : "unknown";
        }
        return lastname.toUpperCase() + " " + firstname;
    }


    public Speaker setHash(String hash) {
        this.hash = hash;
        return this;
    }
}