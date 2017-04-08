package com.ehret.mixit.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Dev-Mind <guillaume@dev-mind.fr>
 * @since 07/04/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DescriptionDto {
    private String FRENCH;
    private String ENGLISH;

    public String getFRENCH() {
        return FRENCH;
    }

    public void setFRENCH(String FRENCH) {
        this.FRENCH = FRENCH;
    }

    public String getENGLISH() {
        return ENGLISH;
    }

    public void setENGLISH(String ENGLISH) {
        this.ENGLISH = ENGLISH;
    }
}
