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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ehret.mixit.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.dto.EventDto;
import com.ehret.mixit.domain.dto.SponsorDto;
import com.ehret.mixit.domain.dto.UserDto;
import com.ehret.mixit.domain.people.Level;
import com.ehret.mixit.domain.people.Member;
import com.ehret.mixit.utils.FileUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

/**
 * Le but de ce fichier est de s'interfacer avec le fichier Json gerant les
 * différents membres.
 */
public class MembreFacade {
    /**
     * Factory Json
     */
    private JsonFactory jsonFactory;
    /**
     * Objetc mapper permettant de faire le binding entre le JSON et les objets
     */
    private ObjectMapper objectMapper;
    /**
     * Instance du singleton
     */
    private static MembreFacade membreFacade;

    private final static String TAG = "MembreFacade";

    private static Map<String, Member> membres = new HashMap<>();

    private static Map<String, Member> staff = new HashMap<>();

    private static Map<String, Member> sponsors = new HashMap<>();

    /**
     * Permet de vider le cache de données
     */
    public void viderCache() {
        membres.clear();
        staff.clear();
        sponsors.clear();
    }

    /**
     * Permet de vider le cache de données
     */
    public void viderCacheSpeakerStaffSponsor() {
        staff.clear();
        sponsors.clear();
    }

    /**
     * Permet de vider le cache de données
     */
    public void viderCacheMembres() {
        membres.clear();
    }

    /**
     * Constructeur prive car singleton
     */
    private MembreFacade() {
        //Creation de nos objets
        this.jsonFactory = new JsonFactory();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    /**
     * Retourne le singleton
     */
    public static MembreFacade getInstance() {
        if (membreFacade == null) {
            membreFacade = new MembreFacade();
        }
        return membreFacade;
    }

    public List<Member> getMembres(Context context, String typeAppel, String filtre) {
        switch (TypeFile.getTypeFile(typeAppel)) {
            case speaker:
                getMapMembres(context, TypeFile.members, membres);
                //Hack to load talk
                ConferenceFacade.getInstance().getTalks(context, null);
                List<Member> speakers = FluentIterable
                        .from(filtrerMembre(membres, filtre))
                        .filter(new Predicate<Member>() {
                            @Override
                            public boolean apply(Member input) {
                                return input.isSpeaker();
                            }
                        }).toList();
                return Ordering.from(getComparatorByName()).sortedCopy(speakers);
            case members:
                getMapMembres(context, TypeFile.members, membres);
                return Ordering.from(getComparatorByName()).sortedCopy(filtrerMembre(membres, filtre));
            case staff:
                getMapMembres(context, TypeFile.staff, staff);
                return Ordering.from(getComparatorByName()).sortedCopy(filtrerMembre(staff, filtre));
            default:
                getMapMembres(context, TypeFile.sponsor, sponsors);
                return Ordering.from(getComparatorByDate()).compound(getComparatorByLevel()).sortedCopy(filtrerMembre(sponsors, filtre));
        }
    }

    /**
     * Filtre la liste des membres
     */
    private List<Member> filtrerMembre(Map<String, Member> talks, final String filtre) {
        return FluentIterable.from(talks.values()).filter(new Predicate<Member>() {
            @Override
            public boolean apply(Member input) {
                if ("MIX-IT".equals(input.getLastname()) || "Free slot".equals(input.getCompany())) {
                    return false;
                }
                return (filtre == null ||
                        (input.getFirstname() != null && input.getFirstname().toLowerCase().contains(filtre.toLowerCase())) ||
                        (input.getLastname() != null && input.getLastname().toLowerCase().contains(filtre.toLowerCase())) ||
                        (input.getShortDescription() != null && input.getShortDescription().toLowerCase().contains(filtre.toLowerCase())));
            }
        }).toList();
    }

    /**
     * Comparaison par nom
     */
    private Comparator<Member> getComparatorByLevel() {
        return new Comparator<Member>() {
            @Override
            public int compare(Member m1, Member m2) {
                if (m1.getLevel() == null || m1.getLevel().isEmpty() || m1.getLevel().get(0).getKey() == null) {
                    return 1;
                }
                if (m2.getLevel() == null || m2.getLevel().isEmpty() || m2.getLevel().get(0).getKey() == null) {
                    return -1;
                }
                return m1.getLevel().get(0).getKey().compareTo(m2.getLevel().get(0).getKey());
            }
        };
    }

    /**
     * Comparaison par nom
     */
    private Comparator<Member> getComparatorByDate() {
        return new Comparator<Member>() {
            @Override
            public int compare(Member m1, Member m2) {
                if (m1.getLevel() == null || m1.getLevel().isEmpty() || m1.getLevel().get(0).getValue() == null) {
                    return 1;
                }
                if (m2.getLevel() == null || m2.getLevel().isEmpty() || m2.getLevel().get(0).getValue() == null) {
                    return -1;
                }
                return m1.getLevel().get(0).getValue().compareTo(m2.getLevel().get(0).getValue());
            }
        };
    }

    /**
     * Comparaison par nom
     */
    private Comparator<Member> getComparatorByName() {
        return new Comparator<Member>() {
            @Override
            public int compare(Member m1, Member m2) {
                if (m1.getLastname() == null) {
                    return 1;
                }
                if (m2.getLastname() == null) {
                    return -1;
                }
                return m1.getLastname().compareTo(m2.getLastname());
            }
        };
    }

    /**
     * Permet de recuperer la liste des membres
     */
    private void getMapMembres(Context context, TypeFile type, Map<String, Member> membres) {
        if (membres.size() == 0) {
            InputStream is = null;
            JsonParser jp;
            try {
                //On regarde si fichier telecharge
                File myFile = FileUtils.getFileJson(context, type);
                if (myFile == null) {
                    //On prend celui inclut dans l'archive
                    is = FileUtils.getRawFileJson(context, type);
                }
                else {
                    is = new FileInputStream(myFile);
                }
                jp = this.jsonFactory.createJsonParser(is);

                if (type == TypeFile.sponsor) {
                    EventDto eventDto = this.objectMapper.readValue(jp, EventDto.class);
                    if (eventDto.getSponsors() != null) {
                        List<Member> members = MembreFacade.getInstance().getMembres(context, "members", null);
                        for (final SponsorDto sponsor : eventDto.getSponsors()) {
                            Member sp = FluentIterable.from(members).firstMatch(new Predicate<Member>() {
                                @Override
                                public boolean apply(Member input) {
                                    return input.getLogin().equals(sponsor.getSponsorId());
                                }
                            }).get();
                            sp.setSponsor(true);
                            sp.setLogo(sp.getHash());
                            sp.setLevel(Arrays.asList(new Level().setKey(sponsor.getLevel()).setValue(sponsor.getLevel())));
                            sponsors.put(sponsor.getSponsorId(), sp);
                        }
                    }

                }
                else {
                    List<UserDto> users = this.objectMapper.readValue(jp, new TypeReference<List<UserDto>>() {
                    });
                    //On transforme la liste en Map
                    if (users != null) {
                        for (UserDto m : users) {
                            membres.put(m.getLogin(), m.toMember());
                        }
                    }
                }
            }
            catch (IOException e) {
                Log.e(TAG, "Erreur lors de la recuperation des " + type, e);
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                        Log.e(TAG, "Impossible de fermer le fichier " + type, e);
                    }
                }
            }
        }
    }

    public Member getMembre(Context context, String typeAppel, String key) {
        switch (TypeFile.getTypeFile(typeAppel)) {
            case speaker:
                getMapMembres(context, TypeFile.members, membres);
                return membres.get(key);
            case members:
                getMapMembres(context, TypeFile.members, membres);
                return membres.get(key);
            case staff:
                getMapMembres(context, TypeFile.staff, staff);
                return staff.get(key);
            default:
                getMapMembres(context, TypeFile.sponsor, sponsors);
                return sponsors.get(key);
        }
    }
}
