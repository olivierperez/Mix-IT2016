/*
 * Copyright 2015 Guillaume EHRET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUConference WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ehret.mixit.domain.talk;

import com.ehret.mixit.domain.Salle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Classe mère des conferences contenant les données communes
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Talk {
    private String idSession;
    private String title;
    private String lang;
    private String summary;
    private String format;
    private String description;
    private List<Speaker> speakers = new ArrayList<>();
    private Date start;
    private Date end;
    private String room;
    private String track;

    public static Talk buildEventSpecial(String titleFormat, String id){
        return new Talk()
                .setIdSession(id)
                .setFormat(titleFormat)
                .setTitle(titleFormat)
                .setDescription("")
                .setLang("")
                .setFormat("Special")
                .setRoom(Salle.INCONNU.getNom());
    }

    public Date getEnd() {
        return end;
    }

    public Talk setEnd(Date end) {
        this.end = end;
        return  this;
    }

    public Date getStart() {
        return start;
    }

    public Talk setStart(Date start) {
        this.start = start;
        return  this;
    }

    public String getRoom() {
        return room;
    }

    public Talk setRoom(String room) {
        this.room = room;
        return  this;
    }

    public String getTrack() {
        return track;
    }

    public Talk setTrack(String track) {
        this.track = track;
        return  this;
    }

    public String getTitle() {
        return title;
    }

    public Talk setTitle(String title) {
        this.title = title;
        return  this;
    }

    public String getSummary() {
        return summary;
    }

    public Talk setSummary(String summary) {
        this.summary = summary;
        return  this;
    }

    public String getDescription() {
        return description;
    }

    public Talk setDescription(String description) {
        this.description = description;
        return  this;
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public Talk setSpeakers(List<Speaker> speakers) {
        this.speakers = speakers;
        return  this;
    }

    public String getIdSession() {
        return idSession;
    }

    public Talk setIdSession(String idSession) {
        this.idSession = idSession;
        return  this;
    }

    public String getLang() {
        return lang;
    }

    public Talk setLang(String lang) {
        this.lang = lang;
        return  this;
    }

    public String getFormat() {
        return format;
    }

    public Talk setFormat(String format) {
        this.format = format;
        return  this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Talk talk = (Talk) o;

        return idSession.equals(talk.idSession);
    }

    @Override
    public int hashCode() {
        return idSession.hashCode();
    }
}
