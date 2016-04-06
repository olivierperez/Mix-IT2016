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
import android.util.LongSparseArray;

import com.ehret.mixit.R;
import com.ehret.mixit.domain.Salle;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.people.Member;
import com.ehret.mixit.domain.talk.Favorite;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.utils.FileUtils;
import com.ehret.mixit.utils.UIUtils;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    private static LongSparseArray<Talk> talks = new LongSparseArray<>();
    /**
     * Liste des lightning talk statique pour ne pas la recharger à chaque appel
     */
    private static LongSparseArray<Talk> lightningtalks = new LongSparseArray<>();

    /**
     * Events du calendrier qui ne sont pas envoyés par Mixit
     */
    private static LongSparseArray<Talk> talksSpeciaux = new LongSparseArray<>();

    /**
     * Events du calendrier qui ne sont pas envoyés par Mixit
     */
    private static List<Talk> timeMark = new ArrayList<>();

    /**
     * Permet de vider le cache de données hormis les events speciaux
     */
    public void viderCache() {
        talks.clear();
        lightningtalks.clear();
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
                Talk conf = getTalk(context, Long.valueOf(key));
                if (conf != null) {
                    conferences.add(conf);
                } else {
                    //On regarde dans les ligthning talks
                    conf = getLightningtalk(context, Long.valueOf(key));
                    if (conf != null) {
                        conferences.add(conf);
                    }
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
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la recuperation des favorites", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "Impossible de fermer le fichier favorites", e);
                }
            }
        }


    }

    /**
     * Permet de recuperer la liste des talks
     */
    public List<Talk> getTalks(Context context, String filtre) {
        return Ordering.from(getComparatorDate()).compound(getComparatorConference())
                .sortedCopy(filtrerTalk(getTalkAndWorkshops(context), TypeFile.talks, filtre));
    }

    /**
     * Permet de recuperer la liste des talks
     */
    public List<Talk> getLightningTalks(Context context, String filtre) {
        return Ordering.from(getComparatorDate()).compound(getComparatorLightningtalk())
                .sortedCopy(filtrerLightningTalk(getLightningtalks(context), filtre));
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
        talks.addAll(filtrerTalk(getTalkAndWorkshops(context), null, null));
        talks.addAll(FluentIterable.from(Utils.asList(getEventsSpeciaux(context))).toList());
        talks.addAll(getTimeMarkers(context));

        return Ordering.from(getComparatorDate()).compound(getComparatorConference()).sortedCopy(talks);
    }

    /**
     * Cette méthode cherche les talks sur cette période
     */
    public List<Talk> getConferenceSurPlageHoraire(Date date, Context context) {
        List<Talk> confs = new ArrayList<>();
        //On recupere les talks
        Collection<Talk> talks = Utils.asList(getTalkAndWorkshops(context));

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
        Collection<Talk> talkSpeciaux = Utils.asList(getEventsSpeciaux(context));
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
            timeMark.add(Talk.buildEventSpecial(null, 120000)
                    .setStart(UIUtils.createPlageHoraire(21, 6, 0))
                    .setEnd(UIUtils.createPlageHoraire(21, 6, 0))
                    .setFormat("day1"));
            timeMark.add(Talk.buildEventSpecial(null, 120001)
                    .setStart(UIUtils.createPlageHoraire(22, 6, 0))
                    .setEnd(UIUtils.createPlageHoraire(22, 6, 0))
                    .setFormat("day2"));

            for (int j = 21; j < 23; j++) {
                for (int i = 8; i < 20; i++) {
                    timeMark.add(Talk.buildEventSpecial(null, 110000 + i + (20 * j % 2))
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
    public LongSparseArray<Talk> getEventsSpeciaux(Context context) {
        if (talksSpeciaux.size() == 0) {

            Talk event = null;
            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_accueillg), 90000)
                    .setStart(UIUtils.createPlageHoraire(21, 8, 10))
                    .setEnd(UIUtils.createPlageHoraire(21, 8, 50));
            talksSpeciaux.put(event.getIdSession(), event);
            
            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_orgalg), 90002)
                    .setStart(UIUtils.createPlageHoraire(21, 8, 50))
                    .setEnd(UIUtils.createPlageHoraire(21, 9, 10));
            talksSpeciaux.put(event.getIdSession(), event);

            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_presseslg), 90003)
                    .setStart(UIUtils.createPlageHoraire(21, 13, 40))
                    .setEnd(UIUtils.createPlageHoraire(21, 13, 50));
            talksSpeciaux.put(event.getIdSession(), event);

            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_accueillg), 90005)
                    .setStart(UIUtils.createPlageHoraire(22, 12, 40))
                    .setEnd(UIUtils.createPlageHoraire(22, 13, 10));
            talksSpeciaux.put(event.getIdSession(), event);

            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_orgalg), 90006)
                    .setStart(UIUtils.createPlageHoraire(22, 9, 0))
                    .setEnd(UIUtils.createPlageHoraire(22, 9, 15));
            talksSpeciaux.put(event.getIdSession(), event);

            event = Talk.buildEventSpecial(context.getString(R.string.calendrier_presseslg), 90007)
                    .setStart(UIUtils.createPlageHoraire(22, 13, 10))
                    .setEnd(UIUtils.createPlageHoraire(22, 13, 20));
            talksSpeciaux.put(event.getIdSession(), event);

            Talk repas = null;
            repas = Talk.buildEventSpecial(context.getString(R.string.calendrier_repas), 80000)
                    .setStart(UIUtils.createPlageHoraire(21, 12, 10))
                    .setEnd(UIUtils.createPlageHoraire(21, 13, 10));
            talksSpeciaux.put(repas.getIdSession(), repas);
            repas = Talk.buildEventSpecial(context.getString(R.string.calendrier_repas), 80002)
                    .setStart(UIUtils.createPlageHoraire(22, 11, 40))
                    .setEnd(UIUtils.createPlageHoraire(22, 12, 40));
            talksSpeciaux.put(repas.getIdSession(), repas);

//            Talk pause = null;
//            pause = Talk.buildEventSpecial(context.getString(R.string.calendrier_pause), 70000)
//                    .setStart(UIUtils.createPlageHoraire(21, 10, 50))
//                    .setEnd(UIUtils.createPlageHoraire(21, 11, 10));
//            talksSpeciaux.put(pause.getIdSession(), pause);
//            pause = Talk.buildEventSpecial(context.getString(R.string.calendrier_pause), 70001)
//                    .setStart(UIUtils.createPlageHoraire(21, 14, 30))
//                    .setEnd(UIUtils.createPlageHoraire(21, 14, 50));
//            talksSpeciaux.put(pause.getIdSession(), pause);
//            pause = Talk.buildEventSpecial(context.getString(R.string.calendrier_pause), 70002)
//                    .setStart(UIUtils.createPlageHoraire(21, 9, 40))
//                    .setEnd(UIUtils.createPlageHoraire(21, 10, 0));
//            talksSpeciaux.put(pause.getIdSession(), pause);
//            pause = Talk.buildEventSpecial(context.getString(R.string.calendrier_pause), 70003)
//                    .setStart(UIUtils.createPlageHoraire(21, 15, 40))
//                    .setEnd(UIUtils.createPlageHoraire(21, 16, 0));
//            talksSpeciaux.put(pause.getIdSession(), pause);
//            pause = Talk.buildEventSpecial(context.getString(R.string.calendrier_pause), 70004)
//                    .setStart(UIUtils.createPlageHoraire(21, 16, 50))
//                    .setEnd(UIUtils.createPlageHoraire(21, 17, 10));
//            talksSpeciaux.put(pause.getIdSession(), pause);
//
//            pause = Talk.buildEventSpecial(context.getString(R.string.calendrier_pause), 70006)
//                    .setStart(UIUtils.createPlageHoraire(22, 9, 40))
//                    .setEnd(UIUtils.createPlageHoraire(22, 10, 0));
//            talksSpeciaux.put(pause.getIdSession(), pause);
//            pause = Talk.buildEventSpecial(context.getString(R.string.calendrier_pause), 70005)
//                    .setStart(UIUtils.createPlageHoraire(22, 10, 50))
//                    .setEnd(UIUtils.createPlageHoraire(22, 11, 10));
//            talksSpeciaux.put(pause.getIdSession(), pause);
//            pause = Talk.buildEventSpecial(context.getString(R.string.calendrier_pause), 70007)
//                    .setStart(UIUtils.createPlageHoraire(22, 14, 30))
//                    .setEnd(UIUtils.createPlageHoraire(22, 14, 50));
//            talksSpeciaux.put(pause.getIdSession(), pause);
//            pause = Talk.buildEventSpecial(context.getString(R.string.calendrier_pause), 70008)
//                    .setStart(UIUtils.createPlageHoraire(22, 15, 40))
//                    .setEnd(UIUtils.createPlageHoraire(22, 16, 0));
//            talksSpeciaux.put(pause.getIdSession(), pause);
//            pause = Talk.buildEventSpecial(context.getString(R.string.calendrier_pause), 70009)
//                    .setStart(UIUtils.createPlageHoraire(22, 16, 50))
//                    .setEnd(UIUtils.createPlageHoraire(22, 16, 10));
//            talksSpeciaux.put(pause.getIdSession(), pause);


            Talk lit = null;
            lit = Talk.buildEventSpecial(context.getString(R.string.calendrier_ligthning), 100000);
            lit.setStart(UIUtils.createPlageHoraire(21, 13, 50));
            lit.setEnd(UIUtils.createPlageHoraire(21, 14, 20));
            talksSpeciaux.put(lit.getIdSession(), lit);

            lit = Talk.buildEventSpecial(context.getString(R.string.calendrier_ligthning), 100001);
            lit.setStart(UIUtils.createPlageHoraire(22, 12, 40));
            lit.setEnd(UIUtils.createPlageHoraire(22, 13, 10));
            talksSpeciaux.put(lit.getIdSession(), lit);

            lit = Talk.buildEventSpecial(context.getString(R.string.calendrier_cloture), 100002);
            lit.setStart(UIUtils.createPlageHoraire(22, 18, 10));
            lit.setEnd(UIUtils.createPlageHoraire(22, 18, 30));
            talksSpeciaux.put(lit.getIdSession(), lit);

            lit = Talk.buildEventSpecial(context.getString(R.string.calendrier_cloture), 100003);
            lit.setStart(UIUtils.createPlageHoraire(22, 18, 40));
            lit.setEnd(UIUtils.createPlageHoraire(22, 18, 45));
            talksSpeciaux.put(lit.getIdSession(), lit);

            lit = Talk.buildEventSpecial(context.getString(R.string.calendrier_partie), 100004);
            lit.setStart(UIUtils.createPlageHoraire(21, 19, 0));
            lit.setEnd(UIUtils.createPlageHoraire(21, 23, 30));
            talksSpeciaux.put(lit.getIdSession(), lit);

        }
        return talksSpeciaux;
    }


    /**
     * Permet de recuperer la liste des talks
     */
    private LongSparseArray<Talk> getTalkAndWorkshops(Context context) {
        if (talks.size() == 0) {
            InputStream is = null;
            List<Talk> talkListe = null;
            JsonParser jp = null;
            try {
                //On regarde si fichier telecharge
                File myFile = FileUtils.getFileJson(context, TypeFile.talks);
                if (myFile == null) {
                    //On prend celui inclut dans l'archive
                    is = FileUtils.getRawFileJson(context, TypeFile.talks);
                } else {
                    is = new FileInputStream(myFile);
                }
                jp = this.jsonFactory.createJsonParser(is);
                talkListe = this.objectMapper.readValue(jp, new TypeReference<List<Talk>>() {
                });
                //On transforme la liste en Map
                for (Talk m : talkListe) {
                    if("Random".equals(m.getFormat())){
                        m.setStart(UIUtils.createPlageHoraire(22, 12, 40));
                        m.setEnd(UIUtils.createPlageHoraire(22, 13, 10));
                    }
                    //Date are stored in UTC we need to convert them to europe timezone
                    else{
                        m.setStart(m.getStart() == null ? null : UIUtils.convertToEuropTimezone(m.getStart()));
                        m.setEnd(m.getEnd() == null ? null : UIUtils.convertToEuropTimezone(m.getEnd()));
                    }
                    talks.put(m.getIdSession(), m);
                }
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de la recuperation des talks", e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Impossible de fermer le fichier des talks", e);
                    }
                }
            }
        }
        return talks;
    }

    /**
     * Permet de recuperer la liste des talks
     */
    private LongSparseArray<Talk> getLightningtalks(Context context) {
        if (lightningtalks.size() == 0) {
            InputStream is = null;
            List<Talk> talkListe;
            JsonParser jp;
            try {
                //On regarde si fichier telecharge
                File myFile = FileUtils.getFileJson(context, TypeFile.lightningtalks);
                if (myFile == null) {
                    //On prend celui inclut dans l'archive
                    is = FileUtils.getRawFileJson(context, TypeFile.lightningtalks);
                } else {
                    is = new FileInputStream(myFile);
                }
                jp = this.jsonFactory.createJsonParser(is);
                talkListe = this.objectMapper.readValue(jp, new TypeReference<List<Talk>>() {
                });
                //On transforme la liste en Map
                for (Talk m : talkListe) {
                    m.setRoom(Salle.SALLE7.getNom());
                    m.setStart(UIUtils.createPlageHoraire(21, 13, 50));
                    m.setEnd(UIUtils.createPlageHoraire(21, 14, 20));
                    lightningtalks.put(m.getIdSession(), m);
                }
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de la recuperation des lightning talks", e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Impossible de fermer le fichier des lightnings talks", e);
                    }
                }
            }
        }
        return lightningtalks;
    }

    public Talk getTalk(Context context, Long key) {
        return getTalkAndWorkshops(context).get(key);
    }

    public Talk getWorkshop(Context context, Long key) {
        return getTalkAndWorkshops(context).get(key);
    }

    public Talk getLightningtalk(Context context, Long key) {
        return getLightningtalks(context).get(key);
    }

    /**
     * Filtre la liste des talks ou des workshops
     */
    private List<Talk> filtrerTalk(LongSparseArray<Talk> talks, final TypeFile type, final String filtre) {
        return FluentIterable.from(Utils.asList(talks)).filter(new Predicate<Talk>() {
            @Override
            public boolean apply(Talk input) {
                boolean retenu;
                if (type == null) {
                    retenu = true;
                } else if (type.equals(TypeFile.workshops)) {
                    retenu = "Workshop".equals(input.getFormat());
                } else {
                    retenu = !"Workshop".equals(input.getFormat());
                }
                return retenu &&
                        ((filtre == null ||
                                (input.getTitle() != null && input.getTitle().toLowerCase().contains(filtre.toLowerCase())) ||
                                (input.getSummary() != null && input.getSummary().toLowerCase().contains(filtre.toLowerCase()))));
            }
        }).toList();
    }

    /**
     * Filtre la liste des talks ou des workshops
     */
    private List<Talk> filtrerLightningTalk(LongSparseArray<Talk> talks, final String filtre) {
        return FluentIterable.from(Utils.asList(talks)).filter(new Predicate<Talk>() {
            @Override
            public boolean apply(Talk input) {
                return (filtre == null ||
                        (input.getTitle() != null && input.getTitle().toLowerCase().contains(filtre.toLowerCase())) ||
                        (input.getSummary() != null && input.getSummary().toLowerCase().contains(filtre.toLowerCase())));
            }
        }).toList();
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
                } else {
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
     * Renvoie le comparator permettant de trier des conf
     */
    private Comparator<Talk> getComparatorLightningtalk() {
        return new Comparator<Talk>() {
            @Override
            public int compare(Talk m1, Talk m2) {
                if (m1.getPositiveVotes() == m2.getPositiveVotes()) {
                    return m1.getTitle().compareTo(m2.getTitle());
                }
                if (m2.getPositiveVotes() > m1.getPositiveVotes()) {
                    return 1;
                }
                return -1;
            }
        };
    }

    /**
     * Renvoi la liste des membres attachés à une session
     */
    public List<Talk> getSessionMembre(Member membre, Context context) {
        List<Talk> sessions = new ArrayList<>();
        List<Talk> listetalks = Utils.asList(getTalkAndWorkshops(context));

        //On recherche les talks
        for(Long session : membre.getSessions()){
            for (Talk t : listetalks) {
                if (Long.valueOf(t.getIdSession()).equals(session)) {
                    sessions.add(t);
                }
            }
        }
        return sessions;
    }


}
