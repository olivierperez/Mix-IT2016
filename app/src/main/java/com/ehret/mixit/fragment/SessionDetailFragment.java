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
package com.ehret.mixit.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ehret.mixit.HomeActivity;
import com.ehret.mixit.R;
import com.ehret.mixit.SalleActivity;
import com.ehret.mixit.builder.TextViewTableBuilder;
import com.ehret.mixit.domain.Salle;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.people.Member;
import com.ehret.mixit.domain.talk.Speaker;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.model.ConferenceFacade;
import com.ehret.mixit.model.MembreFacade;
import com.ehret.mixit.utils.FileUtils;
import com.ehret.mixit.utils.HtmlTagHandler;
import com.ehret.mixit.utils.UIUtils;
import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activité permettant d'afficher les informations sur un talk
 */
public class SessionDetailFragment extends Fragment {

    private TextView horaire;
    private TextView level;
    private TextView name;
    private TextView summary;
    private TextView descriptif;
    private TextView salle;
    private TextView track;
    private ImageView imageFavorite;
    private ImageView imageTrack;
    private ImageView langImage;
    private View talkView;
    private TextView speakersTitle;
    private LinearLayout sessionPersonList;
    private LayoutInflater mInflater;
    private HtmlTagHandler htmlTagHandler = new HtmlTagHandler();

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SessionDetailFragment newInstance(String typeAppel, String message, int sectionNumber) {
        SessionDetailFragment fragment = new SessionDetailFragment();
        Bundle args = new Bundle();
        args.putString(UIUtils.ARG_LIST_TYPE, typeAppel);
        args.putString(UIUtils.ARG_ID, message);
        args.putInt(UIUtils.ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_session, container, false);

        this.imageFavorite = (ImageView) rootView.findViewById(R.id.talk_image_favorite);
        this.imageTrack = (ImageView) rootView.findViewById(R.id.talk_image_track);
        this.horaire = (TextView) rootView.findViewById(R.id.talk_horaire);
        this.level = (TextView) rootView.findViewById(R.id.talk_level);
        this.name = (TextView) rootView.findViewById(R.id.talk_name);
        this.summary = (TextView) rootView.findViewById(R.id.talk_summary);
        this.descriptif = (TextView) rootView.findViewById(R.id.talk_desciptif);
        this.salle = (TextView) rootView.findViewById(R.id.talk_salle);
        this.talkView = rootView.findViewById(R.id.talkView);
        this.speakersTitle = (TextView) rootView.findViewById(R.id.speakersTitle);
        this.sessionPersonList = (LinearLayout) rootView.findViewById(R.id.sessionPersonList);
        this.langImage = (ImageView) rootView.findViewById(R.id.talk_image_language);
        this.track = (TextView) rootView.findViewById(R.id.talk_track);

        this.imageFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toggleFavorite()==Toggle.TRUE){
                    imageFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_important));
                }
                else{
                    imageFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_not_important));
                }
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HomeActivity) activity).onSectionAttached(
                "title_detail_" + getArguments().getString(UIUtils.ARG_LIST_TYPE),
                "color_primary");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    /**
     * Recuperation des marques de la partie en cours
     */
    @Override
    public void onResume() {
        super.onResume();

        Context context = getActivity().getBaseContext();

        //On commence par recuperer le Membre que l'on sohaite afficher
        String id = getArguments().getString(UIUtils.ARG_ID);
        String typeStr = getArguments().getString(UIUtils.ARG_LIST_TYPE);
        TypeFile type = TypeFile.getTypeFile(typeStr);

        Talk conference;
        if (type == TypeFile.special) {
            conference = ConferenceFacade.getInstance().getSpecial(context, id);
            addGeneralInfo(conference);
            hideSpeakerInfo();
        } else {
            conference = ConferenceFacade.getInstance().getTalk(context, id);
            addGeneralInfo(conference);
            addSpeakerInfo(conference);
        }

    }

    private void hideSpeakerInfo() {
        talkView.setVisibility(View.GONE);
        speakersTitle.setVisibility(View.GONE);
    }

    private void addGeneralInfo(Talk conference) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE");
        if (conference.getStart() != null && conference.getEnd() != null) {
            horaire.setText(String.format(getResources().getString(R.string.periode),
                    sdf.format(conference.getStart()),
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(conference.getStart()),
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(conference.getEnd())
            ));
        } else {
            horaire.setText(getResources().getString(R.string.pasdate));

        }
        name.setText(conference.getTitle());
        if(conference.getSummary()!=null) {
            summary.setText(Html.fromHtml(Processor.process(conference.getSummary()).trim()));
        }
        if(conference.getDescription()!=null) {
            descriptif.setText(
                    Html.fromHtml(
                            Processor.process(conference.getDescription(), Configuration.builder().forceExtentedProfile().build()).trim(), null, htmlTagHandler),
                    TextView.BufferType.SPANNABLE);
        }
        final Salle room = Salle.getSalle(conference.getRoom());

        if(conference.getTrack()!=null){
            switch (conference.getTrack()){
                case "aliens":
                    imageTrack.setImageDrawable(getResources().getDrawable(R.drawable.mxt_icon__aliens));
                    track.setText("Track Alien");
                    break;
                case "design":
                    imageTrack.setImageDrawable(getResources().getDrawable(R.drawable.mxt_icon__design));
                    track.setText("Track Design");
                    break;
                case "hacktivism":
                    imageTrack.setImageDrawable(getResources().getDrawable(R.drawable.mxt_icon__hack));
                    track.setText("Track Hacktivism");
                    break;
                case "tech":
                    imageTrack.setImageDrawable(getResources().getDrawable(R.drawable.mxt_icon__tech));
                    track.setText("Track Tech");
                    break;
                case "learn":
                    imageTrack.setImageDrawable(getResources().getDrawable(R.drawable.mxt_icon__learn));
                    track.setText("Track Learn");
                    break;
                case "makers":
                    imageTrack.setImageDrawable(getResources().getDrawable(R.drawable.mxt_icon__makers));
                    track.setText("Track Makers");
                    break;
                default:
                    imageTrack.setImageDrawable(null);
                    track.setText("");
            }

        }

        if (conference.getLang() != null && "ENGLISH".equals(conference.getLang())) {
            langImage.setImageDrawable(getResources().getDrawable(R.drawable.en));
        } else {
            langImage.setImageDrawable(getResources().getDrawable(R.drawable.fr));
        }
        if (Salle.INCONNU != room) {
            salle.setText(String.format(getString(R.string.Salle), room.getNom()));
            if (room.getDrawable() != 0) {
                salle.setBackgroundResource(room.getDrawable());
            } else {
                salle.setBackgroundColor(getActivity().getBaseContext().getResources().getColor(room.getColor()));
            }
            salle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> parametres = new HashMap<>();
                    parametres.put(UIUtils.ARG_KEY_ROOM, room.getEtage());
                    UIUtils.startActivity(SalleActivity.class, getActivity(), parametres);
                }
            });
        }
    }

    private void addSpeakerInfo(Talk conference) {
        //On vide les éléments
        sessionPersonList.removeAllViews();

        List<Member> speakers = new ArrayList<>();
        for (Speaker member : conference.getSpeakers()) {
            Member membre = MembreFacade.getInstance().getMembre(getActivity(), TypeFile.speaker.name(), member.getIdMember());

            if (membre != null) {
                speakers.add(membre);
            }
        }

        //On affiche les liens que si on a recuperer des choses
        if (!speakers.isEmpty()) {
            //On utilisait auparavant une liste pour afficher ces éléments dans la page mais cette liste
            //empêche d'avoir un ScrollView englobant pour toute la page. Nous utilisons donc un tableau

            //On ajoute un table layout
            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            TableLayout tableLayout = new TableLayout(getActivity().getBaseContext());
            tableLayout.setLayoutParams(tableParams);

            if (mInflater != null) {
                for (final Member membre : speakers) {
                    LinearLayout row = (LinearLayout) mInflater.inflate(R.layout.item_person, tableLayout, false);
                    row.setBackgroundResource(R.drawable.row_transparent_background);

                    //Dans lequel nous allons ajouter le contenu que nous faisons mappé dans
                    TextView userName = (TextView) row.findViewById(R.id.person_user_name);
                    TextView descriptif = (TextView) row.findViewById(R.id.person_shortdesciptif);
                    TextView level = (TextView) row.findViewById(R.id.person_level);
                    ImageView profileImage = (ImageView) row.findViewById(R.id.person_user_image);

                    userName.setText(membre.getCompleteName());

                    if (membre.getShortDescription() != null) {
                        descriptif.setText(membre.getShortDescription().trim());
                    }

                    //Recuperation de l'mage liee au profil
                    Bitmap image = FileUtils.getImageProfile(getActivity(), membre);
                    if (image == null) {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.person_image_empty));
                    } else {
                        //On regarde dans les images embarquees
                        profileImage.setImageBitmap(image);
                    }

                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HomeActivity) getActivity()).changeCurrentFragment(
                                    PeopleDetailFragment.newInstance(
                                            TypeFile.speaker.toString(),
                                            membre.getLogin(),
                                            7),
                                    TypeFile.speaker.toString());
                        }
                    });

                    tableLayout.addView(row);
                }
            }
            sessionPersonList.addView(tableLayout);
        }
    }

    /**
     * Icon change according to the session if it's present or not in the favorites
     */
    public void updateMenuItem(Boolean isFavorite) {
        if (isFavorite == null) {
            isFavorite = isTalkFavorite();
        }
        if (isFavorite) {
            imageFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_important));
        } else {
            imageFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_not_important));
        }
    }


    /**
     * Verifie si l'activité st dans les favoris
     */
    private boolean isTalkFavorite() {
        SharedPreferences settings = getActivity().getSharedPreferences(UIUtils.PREFS_FAVORITES_NAME, 0);
        return settings.getBoolean(getArguments().getString(UIUtils.ARG_ID), false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        updateMenuItem(null);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private enum Toggle {TRUE, FALSE, NOTHING}

    protected Toggle toggleFavorite() {
        //On recupere id
        String id = getArguments().getString(UIUtils.ARG_ID);
        Toggle toggle = Toggle.NOTHING;
        if (id != null) {
            //On sauvegarde le choix de l'utilsateur
            SharedPreferences settings = getActivity().getSharedPreferences(UIUtils.PREFS_FAVORITES_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            if (isTalkFavorite()) {
                //S'il l'est et on qu'on a cliquer sur le bouton on supprime
                editor.remove(String.valueOf(id));
                toggle = Toggle.FALSE;

            } else {
                editor.putBoolean(String.valueOf(id), Boolean.TRUE);
                toggle = Toggle.TRUE;
            }
            editor.apply();
        }
        return toggle;
    }
}
