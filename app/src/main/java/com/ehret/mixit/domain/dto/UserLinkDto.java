package com.ehret.mixit.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Dev-Mind <guillaume@dev-mind.fr>
 * @since 07/04/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLinkDto {
    private String url;
    private String name;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
