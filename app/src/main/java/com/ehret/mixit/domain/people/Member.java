package com.ehret.mixit.domain.people;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * Member DTO for API HATEOAS
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Member {

    private Long idMember;
    private String login;
    private String firstname;
    private String lastname;
    private String company;
    private String logo;
    private String hash;
    private String shortDescription;
    private String longDescription;


    private List<String> interests;
    private List<Long> sessions;
    private List<Link> shareLinks;
    private List<Level> level;

    public Long getIdMember() {
        return idMember;
    }

    public Member setIdMember(Long idMember) {
        this.idMember = idMember;
        return this;
    }

    public String getLogin() {
        return login;
    }

    public Member setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getLogo() {
        return logo;
    }

    public Member setLogo(String logo) {
        this.logo = logo;
        return this;
    }

    public String getUrlImage(){
        if(this.logo!=null){
            return "http://www.mix-it.fr/img/" + this.logo;
        }
        else if(this.hash !=null){
            return "https://www.gravatar.com/avatar/" + this.hash;
        }
        return null;
    }

    public String getFirstname() {
        return firstname;
    }

    public Member setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public String getLastname() {
        return lastname;
    }

    public Member setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public String getCompany() {
        return company;
    }

    public Member setCompany(String company) {
        this.company = company;
        return this;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public Member setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
        return this;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public Member setLongDescription(String longDescription) {
        this.longDescription = longDescription;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public String getCompleteName() {
        if(isSponsor()){
            return company + "  [" + level.get(0).getKey() + "]";
        }
        if(firstname ==null || "".equals(firstname)){
            return lastname !=null ? lastname.toUpperCase() : "unknown";
        }
        return lastname.toUpperCase() + " " + firstname;
    }

    public Member setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public List<String> getInterests() {
        return interests;
    }

    public Member setInterests(List<String> interests) {
        this.interests = interests;
        return this;
    }

    public List<Link> getSharedLinks() {
        return shareLinks;
    }

    public Member setShareLinks(List<Link> shareLinks) {
        this.shareLinks = shareLinks;
        return this;
    }

    public List<Level> getLevel() {
        return level;
    }

    public void setLevel(List<Level> level) {
        this.level = level;
    }

    public List<Long> getSessions() {
        return sessions;
    }

    public void setSessions(List<Long> sessions) {
        this.sessions = sessions;
    }

    public boolean isSponsor(){
        return getLevel() != null && !getLevel().isEmpty() && getLevel().get(0).getKey()!=null && !"".equals(getLevel().get(0).getKey());
    }

}
