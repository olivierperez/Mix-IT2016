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

import android.content.Context;
import android.util.Log;
import android.util.LongSparseArray;

import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.people.Member;
import com.ehret.mixit.utils.FileUtils;
import com.ehret.mixit.utils.Utils;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

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

    private static LongSparseArray<Member> membres = new LongSparseArray<>();

    private static LongSparseArray<Member> speaker = new LongSparseArray<>();

    private static LongSparseArray<Member> speakerlt = new LongSparseArray<>();

    private static LongSparseArray<Member> staff = new LongSparseArray<>();

    private static LongSparseArray<Member> sponsors = new LongSparseArray<>();

    /**
     * Permet de vider le cache de données
     */
    public void viderCache() {
        membres.clear();
        speaker.clear();
        speakerlt.clear();
        staff.clear();
        sponsors.clear();
    }

    /**
     * Permet de vider le cache de données
     */
    public void viderCacheSpeakerStaffSponsor() {
        speaker.clear();
        speakerlt.clear();
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
        if (TypeFile.members.name().equals(typeAppel)) {
            getMapMembres(context, typeAppel, membres);
            return Ordering.from(getComparatorByName()).sortedCopy(filtrerMembre(membres, filtre));
        } else if (TypeFile.staff.name().equals(typeAppel)) {
            getMapMembres(context, typeAppel, staff);
            return Ordering.from(getComparatorByName()).sortedCopy(filtrerMembre(staff, filtre));
        } else if (TypeFile.sponsor.name().equals(typeAppel)) {
            getMapMembres(context, typeAppel, sponsors);
            return Ordering.from(getComparatorByDate()).compound(getComparatorByLevel()).sortedCopy(filtrerMembre(sponsors, filtre));
        } else if (TypeFile.speaker.name().equals(typeAppel)) {
            getMapMembres(context, typeAppel, speaker);
            return Ordering.from(getComparatorByName()).sortedCopy(filtrerMembre(speaker, filtre));
        } else if (TypeFile.speakerlt.name().equals(typeAppel)) {
            getMapMembres(context, typeAppel, speakerlt);
            return Ordering.from(getComparatorByName()).sortedCopy(filtrerMembre(speakerlt, filtre));
        }
        return null;
    }

    /**
     * Filtre la liste des membres
     */
    private List<Member> filtrerMembre(LongSparseArray<Member> talks, final String filtre) {
        return FluentIterable.from(Utils.asList(talks)).filter(new Predicate<Member>() {
            @Override
            public boolean apply(Member input) {
                if("MIX-IT".equals(input.getLastname()) || "Free slot".equals(input.getCompany())){
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
                if (m1.getLevel() == null || m1.getLevel().isEmpty() || m1.getLevel().get(0).getKey()== null) {
                    return 1;
                }
                if (m2.getLevel() == null || m2.getLevel().isEmpty() || m2.getLevel().get(0).getKey()== null) {
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
                if (m1.getLevel() == null || m1.getLevel().isEmpty() || m1.getLevel().get(0).getValue()== null) {
                    return 1;
                }
                if (m2.getLevel() == null || m2.getLevel().isEmpty() || m2.getLevel().get(0).getValue()== null) {
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
    private void getMapMembres(Context context, String type, LongSparseArray<Member> membres) {
        if (membres.size()==0) {
            InputStream is = null;
            List<Member> membreListe = null;
            JsonParser jp;
            try {
                //On regarde si fichier telecharge
                File myFile = FileUtils.getFileJson(context, TypeFile.getTypeFile(type));
                if (myFile == null) {
                    //On prend celui inclut dans l'archive
                    is = FileUtils.getRawFileJson(context, TypeFile.getTypeFile(type));
                } else {
                    is = new FileInputStream(myFile);
                }
                jp = this.jsonFactory.createJsonParser(is);
                membreListe = this.objectMapper.readValue(jp, new TypeReference<List<Member>>() {
                });
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de la recuperation des " + type, e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Impossible de fermer le fichier " + type, e);
                    }
                }
            }
            //On transforme la liste en Map
            if (membreListe != null) {
                for (Member m : membreListe) {
                    membres.put(m.getIdMember(), m);
                }
            }
        }
    }

    public Member getMembre(Context context, String typeAppel, Long key) {
        if (TypeFile.members.name().equals(typeAppel)) {
            getMapMembres(context, typeAppel, membres);
            return membres.get(key);
        } else if (TypeFile.staff.name().equals(typeAppel)) {
            getMapMembres(context, typeAppel, staff);
            return staff.get(key);
        } else if (TypeFile.sponsor.name().equals(typeAppel)) {
            getMapMembres(context, typeAppel, sponsors);
            return sponsors.get(key);
        } else if (TypeFile.speaker.name().equals(typeAppel)) {
            getMapMembres(context, typeAppel, speaker);
            return speaker.get(key);
        } else if (TypeFile.speakerlt.name().equals(typeAppel)) {
            getMapMembres(context, typeAppel, speakerlt);
            return speakerlt.get(key);
        }
        return null;
    }
}
