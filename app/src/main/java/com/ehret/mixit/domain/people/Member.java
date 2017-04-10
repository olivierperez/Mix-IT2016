package com.ehret.mixit.domain.people;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Member DTO for API HATEOAS
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Member {

    private String login;
    private String firstname;
    private String lastname;
    private String company;
    private String logo;
    private String extension;
    private String hash;
    private String shortDescription;
    private String longDescription;
    private boolean sponsor;
    private boolean speaker;

    private List<Link> shareLinks = new ArrayList<>();
    private List<Level> level = new ArrayList<>();

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

    public String getExtension() {
        if(this.logo != null){
            try {
                return this.logo.substring(this.logo.lastIndexOf(".") + 1);
            }
            catch (RuntimeException e){
                return "jpg";
            }
        }
        return "jpg";
    }

    public String getUrlImage() {
        if(this.logo != null){
            if(this.logo.startsWith("/images")){
                return "https://mixitconf.org/" + this.logo;
            }
            if(this.logo.startsWith("sponsor")){
                return "https://mixitconf.org/images/" + this.logo;
            }
            return this.logo;
        }
        if (this.hash != null) {
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
        if (getLongDescription().length() > 100) {
            return getLongDescription().substring(0, 100) + " ...";
        }
        return getLongDescription();
    }

    public String getLongDescription() {
        return longDescription == null ? "" : longDescription;
    }

    public Member setLongDescription(String longDescription) {
        this.longDescription = longDescription;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public String getCompleteName() {
        if (isSponsor()) {
            return company + "  [" + level.get(0).getKey() + "]";
        }
        if (firstname == null || "".equals(firstname)) {
            return lastname != null ? lastname.toUpperCase() : "unknown";
        }
        return firstname + " " + lastname.toUpperCase();
    }

    public Member setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public List<Link> getSharedLinks() {
        return shareLinks;
    }

    public Member setShareLinks(List<Link> shareLinks) {
        this.shareLinks = shareLinks;
        return this;
    }

    public Member setSponsor(boolean sponsor) {
        this.sponsor = sponsor;
        return this;
    }

    public boolean isSpeaker() {
        return speaker;
    }

    public Member setSpeaker(boolean speaker) {
        this.speaker = speaker;
        return this;
    }

    public List<Level> getLevel() {
        return level;
    }

    public void setLevel(List<Level> level) {
        this.level = level;
    }

    public boolean isSponsor() {
        return getLevel() != null && !getLevel().isEmpty() && getLevel().get(0).getKey() != null && !"".equals(getLevel().get(0).getKey());
    }

}
