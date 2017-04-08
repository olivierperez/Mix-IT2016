package com.ehret.mixit.domain.dto;

import java.util.ArrayList;
import java.util.List;

import com.ehret.mixit.domain.people.Link;
import com.ehret.mixit.domain.people.Member;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Member DTO for API HATEOAS
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    private String login;
    private String firstname;
    private String lastname;
    private String company;
    private DescriptionDto description;
    private String email;
    private String emailHash;
    private String photoUrl;
    private String role;
    private List<UserLinkDto> links = new ArrayList<>();

    public Member toMember() {
        Member member = new Member()
                .setCompany(company)
                .setFirstname(firstname)
                .setHash(emailHash)
                .setLastname(lastname)
                .setLogo(photoUrl)
                .setLogin(login)
                //TODO manage english description
                .setLongDescription(description.getFRENCH()!=null ? description.getFRENCH() : description.getENGLISH());

        for (UserLinkDto link : links) {
            member.getSharedLinks().add(new Link().setRel(link.getName()).setHref(link.getUrl()));
        }
        return member;
    }

    public UserDto setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getUrlImage() {
        if (this.photoUrl != null) {
            return "http://mixitconf.org/images/" + this.photoUrl;
        }
        else if (this.emailHash != null) {
            return "https://www.gravatar.com/avatar/" + this.emailHash;
        }
        return null;
    }

    public UserDto setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public UserDto setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public UserDto setCompany(String company) {
        this.company = company;
        return this;
    }

    public String getCompleteName() {
        if (firstname == null || "".equals(firstname)) {
            return lastname != null ? lastname.toUpperCase() : "unknown";
        }
        return lastname.toUpperCase() + " " + firstname;
    }

    public String getLogin() {
        return login;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getCompany() {
        return company;
    }

    public DescriptionDto getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailHash() {
        return emailHash;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getRole() {
        return role;
    }

    public List<UserLinkDto> getLinks() {
        return links;
    }

    public UserDto setDescription(DescriptionDto description) {
        this.description = description;
        return this;
    }

    public UserDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserDto setEmailHash(String emailHash) {
        this.emailHash = emailHash;
        return this;
    }

    public UserDto setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        return this;
    }

    public UserDto setRole(String role) {
        this.role = role;
        return this;
    }

    public UserDto setLinks(List<UserLinkDto> links) {
        this.links = links;
        return this;
    }

}
