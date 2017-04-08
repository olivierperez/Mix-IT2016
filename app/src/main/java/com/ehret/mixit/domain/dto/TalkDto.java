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
package com.ehret.mixit.domain.dto;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ehret.mixit.domain.people.Member;
import com.ehret.mixit.domain.talk.Speaker;
import com.ehret.mixit.domain.talk.Talk;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

/**
 * Classe mère des conferences contenant les données communes
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TalkDto {
    private String id;
    private int event;
    private String title;
    private String summary;
    private String format;
    private String language;
    private List<Integer> addedAt;
    private String description;
    private String topic;
    private String video;
    private String room;
    private List<String> speakerIds;
    private List<Integer> start;
    private List<Integer> end;
    private String slug;

    public Talk toTalk(List<Member> members){
        Talk talk= new Talk()
                .setDescription(description)
                .setEnd(toDate(end))
                .setStart(toDate(start))
                .setFormat(format)
                .setIdSession(id)
                .setLang(language)
                .setRoom(room)
                .setSummary(summary)
                .setTrack(topic)
                .setTitle(title);

        for(final String id : speakerIds){
            Member member = FluentIterable.from(members).firstMatch(new Predicate<Member>() {
                @Override
                public boolean apply(Member input) {
                    return input.getLogin().equals(id);
                }
            }).get();
            member.setSpeaker(true);
            talk.getSpeakers().add(new Speaker()
                    .setIdMember(member.getLogin())
                    .setFirstname(member.getFirstname())
                    .setLastname(member.getLastname())
                    .setHash(member.getHash())
            );
        }
        return talk;
    }

    private Date toDate(List<Integer> localdate){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, localdate.get(0));
        calendar.set(Calendar.MONTH, localdate.get(1)-1);
        calendar.set(Calendar.DAY_OF_MONTH, localdate.get(2));
        calendar.set(Calendar.HOUR, localdate.get(3));
        calendar.set(Calendar.MINUTE, localdate.get(4));
        return calendar.getTime();
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<Integer> getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(List<Integer> addedAt) {
        this.addedAt = addedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public List<String> getSpeakerIds() {
        return speakerIds;
    }

    public void setSpeakerIds(List<String> speakerIds) {
        this.speakerIds = speakerIds;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
