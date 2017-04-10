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
package com.ehret.mixit.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Toast;
import com.ehret.mixit.R;
import com.ehret.mixit.domain.Salle;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.utils.UIUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Adapter permettant de gérer l'affichage dans la liste des talks
 */
public class ListTalkAdapter extends BaseAdapter {
    
    private List<Talk> datas;
    private Context context;
    private TypeFile typeFile;

    public ListTalkAdapter(Context context, List<Talk> datas, TypeFile typeFile) {
        this.datas = datas;
        this.context = context;
        this.typeFile = typeFile;
        if(datas.isEmpty() && typeFile == TypeFile.favorites){
            Toast.makeText(context, "Aucun favori pour le moment. Pour en ajouter, allez sur un talk et cliquez sur une étoile", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Talk getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_talk, parent, false);
            holder = new ViewHolder();
            holder.imageFavorite = (ImageView) convertView.findViewById(R.id.talk_image_favorite);
            holder.langImage = (ImageView) convertView.findViewById(R.id.talk_image_language);
            holder.name = (TextView) convertView.findViewById(R.id.talk_name);
            holder.descriptif = (TextView) convertView.findViewById(R.id.talk_shortdesciptif);
            holder.horaire = (TextView) convertView.findViewById(R.id.talk_horaire);
            holder.talkImageText = (TextView) convertView.findViewById(R.id.talkImageText);
            holder.imageTrack = (ImageView) convertView.findViewById(R.id.talk_image_track);
            holder.talkSalle = (TextView) convertView.findViewById(R.id.talk_salle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Talk conf = datas.get(position);
        holder.name.setText(conf.getTitle());
        if(conf.getSummary()!=null) {
            holder.descriptif.setText(Html.fromHtml(conf.getSummary().trim()));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEE");
        if (conf.getStart() != null && conf.getEnd() != null) {
            holder.horaire.setText(String.format(context.getResources().getString(R.string.periode),
                    sdf.format(conf.getStart()),
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(conf.getStart()),
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(conf.getEnd())
            ));
        } else {
            holder.horaire.setText(context.getResources().getString(R.string.pasdate));

        }
        if(conf.getLang()!=null && "ENGLISH".equals(conf.getLang())){
            holder.langImage.setImageDrawable(context.getResources().getDrawable(R.drawable.en));
        }
        else{
            holder.langImage.setImageDrawable(context.getResources().getDrawable(R.drawable.fr));
        }
        Salle salle;
        if (Salle.INCONNU != Salle.getSalle(conf.getRoom())) {
            salle = Salle.getSalle(conf.getRoom());
            if(context.getResources().getBoolean(R.bool.small_screen)){
                holder.talkSalle.setText(String.format(context.getResources().getString(R.string.Salle), salle.getTeenyName()));
            }
            else {
                holder.talkSalle.setText(String.format(context.getResources().getString(R.string.Salle), salle.getNom()));
            }
            holder.talkSalle.setBackgroundColor(context.getResources().getColor(salle.getColor()));

        }
        else{
            holder.talkSalle.setText("");
        }

        if ("WORKSHOP".equals(conf.getFormat())) {
                holder.talkImageText.setText("Workshop");
        }
        else if ("KEYNOTE".equals(((Talk) conf).getFormat())) {
            holder.talkImageText.setText("Keynote");
        }
        else if ("RANDOM".equals(((Talk) conf).getFormat())) {
            holder.talkImageText.setText("Random");
        }
        else if ("TALK".equals(((Talk) conf).getFormat())) {
            holder.talkImageText.setText("Talk");
        }

        if(conf.getTrack()!=null){
            switch (conf.getTrack()){
                case "aliens":
                    holder.imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__aliens));
                    break;
                case "design":
                    holder.imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__design));
                    break;
                case "hacktivism":
                    holder.imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__hack));
                    break;
                case "tech":
                    holder.imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__tech));
                    break;
                case "learn":
                    holder.imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__learn));
                    break;
                case "makers":
                    holder.imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__makers));
                    break;
            }
        }
        //On regarde si la conf fait partie des favoris
        SharedPreferences settings = context.getSharedPreferences(UIUtils.PREFS_FAVORITES_NAME, 0);
        boolean trouve = false;
        for (String key : settings.getAll().keySet()) {
            if (key.equals(String.valueOf(conf.getIdSession()))) {
                trouve = true;
                holder.imageFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_important));
                break;
            }
        }
        if (!trouve) {
            holder.imageFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_not_important));
        }
        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView descriptif;
        TextView horaire;
        TextView talkImageText;
        TextView talkSalle;
        ImageView imageFavorite;
        ImageView imageTrack;
        ImageView langImage;
    }

}
