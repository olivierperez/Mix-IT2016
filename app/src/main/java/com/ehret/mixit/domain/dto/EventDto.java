package com.ehret.mixit.domain.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * @author Dev-Mind <guillaume@dev-mind.fr>
 * @since 07/04/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDto {
    private String id;
    private List<Integer> start;
    private List<Integer> end;
    private boolean current;
    private List<SponsorDto> sponsors;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Integer> getStart() {
        return start;
    }

    public void setStart(List<Integer> start) {
        this.start = start;
    }

    public List<Integer> getEnd() {
        return end;
    }

    public void setEnd(List<Integer> end) {
        this.end = end;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public List<SponsorDto> getSponsors() {
        return sponsors;
    }

    public void setSponsors(List<SponsorDto> sponsors) {
        this.sponsors = sponsors;
    }

}
