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
import android.content.SharedPreferences;
import android.util.Log;

import com.ehret.mixit.R;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.dto.TalkDto;
import com.ehret.mixit.domain.people.Member;
import com.ehret.mixit.domain.talk.Favorite;
import com.ehret.mixit.domain.talk.Speaker;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.utils.FileUtils;
import com.ehret.mixit.utils.UIUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Le but de ce fichier est de s'interfacer avec le fichier Json gerant les
 * différentes conf et lightning talks.
 */
public class ConferenceFacade {
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
    private static ConferenceFacade membreFacade;

    private final static String TAG = "ConferenceFacade";
    /**
     * Liste des talks statique pour ne pas la recharger à chaque appel
     */
    private static Map<String, Talk> talks = new HashMap<>();
    /**
     * Events du calendrier qui ne sont pas envoyés par Mixit
     */
    private static Map<String, Talk> talksSpeciaux = new HashMap<>();

    /**
     * Events du calendrier qui ne sont pas envoyés par Mixit
     */
    private static List<Talk> timeMark = new ArrayList<>();

    /**
     * Permet de vider le cache de données hormis les events speciaux
     */
    public void viderCache() {
        talks.clear();
    }

    /**
     * Constructeur prive car singleton
     */
    private ConferenceFacade() {
        //Creation des objets Jakkson
        this.jsonFactory = new JsonFactory();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Retourne le singleton
     */
    public static ConferenceFacade getInstance() {
        if (membreFacade == null) {
            membreFacade = new ConferenceFacade();
        }
        return membreFacade;
    }

    /**
     * Permet de recuperer la liste des confs mises en favoris
     */
    public List<Talk> getFavorites(Context context, String filtre) {
        List<Talk> conferences = new ArrayList<>();

        //La premiere étape consiste a reconstitue la liste
        Set<String> keys = context.getSharedPreferences(UIUtils.PREFS_FAVORITES_NAME, 0).getAll().keySet();
        for (String key : keys) {
            if (key != null) {
                //On regarde d'abord dans les confs
                Talk conf = getTalk(context,key);
                if (conf != null) {
                    conferences.add(conf);
                }
            }
        }
        return Ordering.from(getComparatorDate()).sortedCopy(filtrerConferenceParDate(filtrerConference(conferences, filtre)));
    }


    public void setFavorites(Context context, boolean reinialize) {
        InputStream is = null;
        JsonParser jp;
        try {
            //On regarde si fichier telecharge
            File myFile = FileUtils.getFileJson(context, TypeFile.favorites);
            if (myFile != null) {
                is = new FileInputStream(myFile);
                jp = this.jsonFactory.createJsonParser(is);
                List<Favorite> talkListe = this.objectMapper.readValue(jp, new TypeReference<List<Favorite>>() {
                });
                //On recupere les favoris existant si on le demande
                SharedPreferences settings = context.getSharedPreferences(UIUtils.PREFS_FAVORITES_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                if (reinialize) {
                    editor.clear();
                }
                //Les confs passées sont enregistrées
                for (Favorite m : talkListe) {
                    editor.putBoolean(String.valueOf(m.getId()), Boolean.TRUE);
                }
                editor.apply();
            }
        }
        catch (IOException e) {
            Log.e(TAG, "Erreur lors de la recuperation des favorites", e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Impossible de fermer le fichier favorites", e);
                }
            }
        }
    }

    /**
     * Permet de recuperer la liste des talks
     */
    public List<Talk> getTalks(Context context, String filtre) {
        return Ordering.from(getComparatorDate())
                .compound(getComparatorConference())
                .sortedCopy(filtrerTalk(getTalkAndWorkshops(context), TypeFile.talks, filtre));
    }


    /**
     * Permet de recuperer la liste des talks
     */
    public List<Talk> getWorkshops(Context context, String filtre) {
        return Ordering.from(getComparatorDate()).compound(getComparatorConference())
                .sortedCopy(filtrerTalk(getTalkAndWorkshops(context), TypeFile.workshops, filtre));
    }

    /**
     * Permet de recuperer la liste des talks et ateliers
     */
    public List<Talk> getWorkshopsAndTalks(Context context) {
        List<Talk> talks = new ArrayList<>();
        talks.addAll(getTalkAndWorkshops(context).values());
        talks.addAll(getEventsSpeciaux(context).values());
        talks.addAll(getTimeMarkers(context));

        return Ordering.from(getComparatorDate())
                .compound(getComparatorConference())
                .sortedCopy(talks);
    }

    /**
     * Cette méthode cherche les talks sur cette période
     */
    public List<Talk> getConferenceSurPlageHoraire(Date date, Context context) {
        List<Talk> confs = new ArrayList<>();
        //On recupere les talks
        Collection<Talk> talks = getTalkAndWorkshops(context).values();

        //On decale la date de 1 minute pour ne pas avoir de souci de comparaison
        Calendar calendar = Calendar.getInstance(Locale.FRANCE);
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, 2);
        Date dateComparee = calendar.getTime();

        for (Talk talk : talks) {
            if (talk.getStart() != null && talk.getEnd() != null) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                cal.setTime(talk.getEnd());
                if (dateComparee.before(cal.getTime())) {
                    cal.setTime(talk.getStart());
                    if (dateComparee.after(cal.getTime())) {
                        confs.add(talk);
                    }
                }
            }
        }

        //On ajoute ls events particuliers
        Collection<Talk> talkSpeciaux = getEventsSpeciaux(context).values();
        for (Talk talk : talkSpeciaux) {
            if (talk.getStart() != null && talk.getEnd() != null && (dateComparee.before(talk.getEnd()) && dateComparee.after(talk.getStart()))) {
                confs.add(talk);
            }
        }
        return confs;
    }

    /**
     * Cree la liste des marqueurs de temps pour le fil de l'eau
     */
    public List<Talk> getTimeMarkers(Context context) {
        if (timeMark.isEmpty()) {
            timeMark.add(Talk.buildEventSpecial(null, "120000")
                    .setStart(UIUtils.createPlageHoraire(20, 6, 0))
                    .setEnd(UIUtils.createPlageHoraire(20, 6, 0))
                    .setFormat("day1"));
            timeMark.add(Talk.buildEventSpecial(null, "120001")
                    .setStart(UIUtils.createPlageHoraire(21, 6, 0))
                    .setEnd(UIUtils.createPlageHoraire(21, 6, 0))
                    .setFormat("day2"));

            for (int j = 20; j < 22; j++) {
                for (int i = 8; i < 20; i++) {
                    timeMark.add(Talk.buildEventSpecial(null, "110000" + i + (20 * j % 2))
                            .setStart(UIUtils.createPlageHoraire(j, i - 1, 59))
                            .setEnd(UIUtils.createPlageHoraire(j, i, 0)));
                }
            }
        }
        return timeMark;
    }

    /**
     * Création de tous les events qui ne sont pas fournis par l'interface Mixit
     */
    public Map<String, Talk> getEventsSpeciaux(Context context) {
        if (talksSpeciaux.size() == 0) {

            Talk event = null;
            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_accueillg), "90000")
                    .setStart(UIUtils.createPlageHoraire(20, 8, 10))
                    .setEnd(UIUtils.createPlageHoraire(20, 8, 50));
            talksSpeciaux.put(event.getIdSession(), event);

            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_orgalg), "90002")
                    .setStart(UIUtils.createPlageHoraire(20, 8, 50))
                    .setEnd(UIUtils.createPlageHoraire(20, 9, 10));
            talksSpeciaux.put(event.getIdSession(), event);

            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_presseslg), "90003")
                    .setStart(UIUtils.createPlageHoraire(20, 13, 40))
                    .setEnd(UIUtils.createPlageHoraire(20, 13, 50));
            talksSpeciaux.put(event.getIdSession(), event);

            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_accueillg), "90005")
                    .setStart(UIUtils.createPlageHoraire(21, 12, 40))
                    .setEnd(UIUtils.createPlageHoraire(21, 13, 10));
            talksSpeciaux.put(event.getIdSession(), event);

            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_orgalg), "90006")
                    .setStart(UIUtils.createPlageHoraire(21, 9, 0))
                    .setEnd(UIUtils.createPlageHoraire(21, 9, 15));
            talksSpeciaux.put(event.getIdSession(), event);

            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_presseslg), "90007")
                    .setStart(UIUtils.createPlageHoraire(21, 13, 10))
                    .setEnd(UIUtils.createPlageHoraire(21, 13, 20));
            talksSpeciaux.put(event.getIdSession(), event);

            Talk repas = null;
            repas = Talk.buildEventSpecial(context.getString(R.string.calendrier_repas), "80000")
                    .setStart(UIUtils.createPlageHoraire(20, 12, 10))
                    .setEnd(UIUtils.createPlageHoraire(20, 13, 10));
            talksSpeciaux.put(repas.getIdSession(), repas);
            repas = Talk.buildEventSpecial(context.getString(R.string.calendrier_repas), "80002")
                    .setStart(UIUtils.createPlageHoraire(21, 11, 40))
                    .setEnd(UIUtils.createPlageHoraire(21, 12, 40));
            talksSpeciaux.put(repas.getIdSession(), repas);

            Talk lit = null;
            lit = Talk.buildEventSpecial(context.getString(R.string.calendrier_ligthning), "100000");
            lit.setStart(UIUtils.createPlageHoraire(20, 13, 50));
            lit.setEnd(UIUtils.createPlageHoraire(20, 14, 20));
            talksSpeciaux.put(lit.getIdSession(), lit);

            lit = Talk.buildEventSpecial(context.getString(R.string.calendrier_ligthning), "100001");
            lit.setStart(UIUtils.createPlageHoraire(21, 12, 40));
            lit.setEnd(UIUtils.createPlageHoraire(21, 13, 10));
            talksSpeciaux.put(lit.getIdSession(), lit);

            lit = Talk.buildEventSpecial(context.getString(R.string.calendrier_cloture), "100002");
            lit.setStart(UIUtils.createPlageHoraire(21, 18, 10));
            lit.setEnd(UIUtils.createPlageHoraire(21, 18, 30));
            talksSpeciaux.put(lit.getIdSession(), lit);

            lit = Talk.buildEventSpecial(context.getString(R.string.calendrier_cloture), "100003");
            lit.setStart(UIUtils.createPlageHoraire(21, 18, 40));
            lit.setEnd(UIUtils.createPlageHoraire(21, 18, 45));
            talksSpeciaux.put(lit.getIdSession(), lit);

            lit = Talk.buildEventSpecial(context.getString(R.string.calendrier_partie), "100004");
            lit.setStart(UIUtils.createPlageHoraire(20, 19, 0));
            lit.setEnd(UIUtils.createPlageHoraire(20, 23, 30));
            talksSpeciaux.put(lit.getIdSession(), lit);

        }
        return talksSpeciaux;
    }


    /**
     * Permet de recuperer la liste des talks
     */
    private Map<String, Talk> getTalkAndWorkshops(Context context) {
        if (talks.size() == 0) {
            List<Member> members = MembreFacade.getInstance().getMembres(context, "members", null);
            InputStream is = null;
            JsonParser jp = null;
            try {
                //On regarde si fichier telecharge
                File myFile = FileUtils.getFileJson(context, TypeFile.talks);
                if (myFile == null) {
                    //On prend celui inclut dans l'archive
                    is = FileUtils.getRawFileJson(context, TypeFile.talks);
                }
                else {
                    is = new FileInputStream(myFile);
                }
                jp = this.jsonFactory.createJsonParser(is);
                List<TalkDto> talkListe = this.objectMapper.readValue(jp, new TypeReference<List<TalkDto>>() {
                });
                //On transforme la liste en Map
                for (TalkDto talkDto : talkListe) {
                    talks.put(talkDto.getId(), talkDto.toTalk(members));
                }
            }
            catch (IOException e) {
                Log.e(TAG, "Erreur lors de la recuperation des talks", e);
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                        Log.e(TAG, "Impossible de fermer le fichier des talks", e);
                    }
                }
            }
        }
        return talks;
    }

    public Talk getTalk(Context context, String key) {
        return getTalkAndWorkshops(context).get(key);
    }

    /**
     * Filtre la liste des talks ou des workshops
     */
    private List<Talk> filtrerTalk(Map<String, Talk> talks, final TypeFile type, final String filtre) {
        return FluentIterable
                .from(talks.values())
                .filter(input -> {
                    boolean retenu;
                    if (type == null) {
                        retenu = true;
                    } else if (type == TypeFile.workshops) {
                        retenu = "WORKSHOP".equals(input.getFormat());
                    } else {
                        retenu = !"WORKSHOP".equals(input.getFormat());
                    }
                    return retenu &&
                            (filtre == null ||
                                    (input.getTitle() != null && input.getTitle().toLowerCase().contains(filtre.toLowerCase())) ||
                                    (input.getSummary() != null && input.getSummary().toLowerCase().contains(filtre.toLowerCase())));
                })
                .toList();
    }

    /**
     * Filtre la liste des talks ou des workshops
     */
    private List<Talk> filtrerConference(List<Talk> talks, final String filtre) {
        return FluentIterable.from(talks).filter(new Predicate<Talk>() {
            @Override
            public boolean apply(Talk input) {
                return (filtre == null ||
                        (input.getTitle() != null && input.getTitle().toLowerCase(Locale.FRENCH).contains(filtre.toLowerCase(Locale.FRENCH))) ||
                        (input.getSummary() != null && input.getSummary().toLowerCase(Locale.FRENCH).contains(filtre.toLowerCase(Locale.FRENCH))));
            }
        }).toList();
    }

    /**
     * Filtre la liste des favoris pour qu'ils disparaissent le jour de la conference
     */
    private List<Talk> filtrerConferenceParDate(List<Talk> talks) {
        return FluentIterable.from(talks).filter(new Predicate<Talk>() {
            @Override
            public boolean apply(Talk input) {
                //On verifie la date, si on est avant ou après la conf on garde tout
                if (System.currentTimeMillis() > UIUtils.CONFERENCE_START_MILLIS &&
                        System.currentTimeMillis() < UIUtils.CONFERENCE_END_MILLIS) {
                    //Si on est dedans on ne garde que les favoris qui ne sont pas passés
                    return input.getEnd() == null || input.getEnd().getTime() >= System.currentTimeMillis();
                }
                else {
                    return true;
                }
            }
        }).toList();
    }

    /**
     * Renvoie le comparator permettant de trier des conf
     */
    private <T extends Talk> Comparator<T> getComparatorDate() {
        return new Comparator<T>() {
            @Override
            public int compare(T m1, T m2) {
                if (m1.getStart() == null && m2.getStart() == null)
                    return 0;
                if (m1.getStart() == null)
                    return -1;
                if (m2.getStart() == null)
                    return 1;
                return m1.getStart().compareTo(m2.getStart());
            }
        };
    }

    /**
     * Renvoie le comparator permettant de trier des conf
     */
    private <T extends Talk> Comparator<T> getComparatorConference() {
        return new Comparator<T>() {
            @Override
            public int compare(T m1, T m2) {
                if (m1.getTitle() == null) {
                    return 1;
                }
                if (m2.getTitle() == null) {
                    return -1;
                }
                return m1.getTitle().compareTo(m2.getTitle());
            }
        };
    }

    /**
     * Renvoi la liste des membres attachés à une session
     */
    public List<Talk> getSessionMembre(Member membre, Context context) {
        List<Talk> sessions = new ArrayList<>();
        for (Talk talk : getTalkAndWorkshops(context).values()) {
            for (Speaker speaker : talk.getSpeakers()) {
                if (speaker.getIdMember().equals(membre.getLogin())) {
                    sessions.add(talk);
                }
            }
        }
        return sessions;
    }

    public Talk getSpecial(Context context, String id) {
        return getEventsSpeciaux(context).get(id);
    }
}
