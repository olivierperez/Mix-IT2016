package com.ehret.mixit.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ehret.mixit.R;
import com.ehret.mixit.domain.Salle;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.utils.UIUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Olivier Perez
 */
public class FilAdapter extends RecyclerView.Adapter<FilAdapter.FilViewHolder> {

    private final OnTalkClickListener listener;

    private List<Talk> items;

    public SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());

    public FilAdapter(OnTalkClickListener listener) {
        this.listener = listener;
    }

    @Override
    public FilViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_talk, parent, false);
        return new FilViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FilViewHolder holder, int position) {
        Talk talk = items.get(position);
        holder.bind(talk);
        holder.listen(talk);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void setItems(List<Talk> items) {
        this.items = items;
        notifyItemRangeInserted(0, items.size());
    }

    public class FilViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView descriptif;
        private TextView horaire;
        private TextView talkImageText;
        private TextView talkSalle;
        private ImageView imageFavorite;
        private ImageView imageTrack;
        private FrameLayout container2;
        private RelativeLayout container3;
        private ImageView langImage;
        
        public FilViewHolder(View itemView) {
            super(itemView);

            imageFavorite = (ImageView) itemView.findViewById(R.id.talk_image_favorite);
            imageTrack = (ImageView) itemView.findViewById(R.id.talk_image_track);
            langImage = (ImageView) itemView.findViewById(R.id.talk_image_language);
            name = (TextView) itemView.findViewById(R.id.talk_name);
            descriptif = (TextView) itemView.findViewById(R.id.talk_shortdesciptif);
            horaire = (TextView) itemView.findViewById(R.id.talk_horaire);
            talkImageText = (TextView) itemView.findViewById(R.id.talkImageText);
            talkSalle = (TextView) itemView.findViewById(R.id.talk_salle);
            container2 = (FrameLayout) itemView.findViewById(R.id.container2);
            container3 = (RelativeLayout) itemView.findViewById(R.id.container3);
        }

        public void bind(Talk talk) {
            Context context = itemView.getContext();

            int size = 0;
            int sizeHoraire = 0;
            int sizeLang = 0;

            if (container2.getLayoutParams().height > 0) {
                size = container2.getLayoutParams().height;
                sizeHoraire = horaire.getLayoutParams().height;
                sizeLang = langImage.getLayoutParams().height;
            }

            if (talk.getTitle() == null) {
                //In this case this just a time marker
                name.setText(null);
                descriptif.setText(null);
                talkImageText.setText(null);
                talkSalle.setText(null);
                imageFavorite.setImageDrawable(null);
                imageTrack.setImageDrawable(null);
                horaire.setText(null);

                if (talk.getFormat() != null && "day1".equals(talk.getFormat())) {
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.color_home));
                    name.setText(context.getResources().getString(R.string.calendrier_jour1));
                    name.setTextColor(context.getResources().getColor(R.color.white));
                } else if (talk.getFormat() != null && "day2".equals(talk.getFormat())) {
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.color_home));
                    name.setText(context.getResources().getString(R.string.calendrier_jour2));
                    name.setTextColor(context.getResources().getColor(R.color.white));
                } else {
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.color_planning_time));
                    name.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(talk.getEnd()));
                    name.setTextColor(context.getResources().getColor(R.color.black));
                }

                horaire.getLayoutParams().height = 0;
                talkSalle.getLayoutParams().height = 0;
                container2.getLayoutParams().height = 0;
                descriptif.getLayoutParams().height = 0;
                langImage.getLayoutParams().height = 0;
            } else {
                //We control hour
                if(talk.getEnd()==null || talk.getEnd().compareTo(new Date()) < 0){
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                    itemView.setBackground(context.getResources().getDrawable(R.drawable.selector_fildeleau_oldsession));
                }
                else{
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                    itemView.setBackground(context.getResources().getDrawable(R.drawable.selector_fildeleau_session));
                }

                if(talk.getLang()!=null && "ENGLISH".equals(talk.getLang())){
                    langImage.setImageDrawable(context.getResources().getDrawable(R.drawable.en));
                }
                else{
                    langImage.setImageDrawable(context.getResources().getDrawable(R.drawable.fr));
                }
                horaire.getLayoutParams().height = sizeHoraire;
                talkSalle.getLayoutParams().height = sizeHoraire;
                container2.getLayoutParams().height = size;
                descriptif.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                name.setTextColor(context.getResources().getColor(R.color.black));
                langImage.getLayoutParams().height = sizeLang;
                if (talk.getStart() != null && talk.getEnd() != null) {
                    horaire.setText(String.format(context.getResources().getString(R.string.periode),
                            sdf.format(talk.getStart()),
                            DateFormat.getTimeInstance(DateFormat.SHORT).format(talk.getStart()),
                            DateFormat.getTimeInstance(DateFormat.SHORT).format(talk.getEnd())
                    ));
                } else {
                    horaire.setText(context.getResources().getString(R.string.pasdate));

                }

                name.setText(talk.getTitle());
                if (talk.getSummary() != null) {
                    descriptif.setText(Html.fromHtml(talk.getSummary().trim()));
                }
                else{
                    descriptif.setText("");
                }

                //On regarde si la talk fait partie des favoris
                SharedPreferences settings = context.getSharedPreferences(UIUtils.PREFS_FAVORITES_NAME, 0);
                boolean trouve = false;
                for (String key : settings.getAll().keySet()) {
                    if (key.equals(String.valueOf(talk.getIdSession()))) {
                        trouve = true;
                        imageFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_important));
                        break;
                    }
                }
                if (!trouve) {
                    imageFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_not_important));
                }
                if(talk.getTrack()!=null){
                    switch (talk.getTrack()){
                        case "aliens":
                            imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__aliens));
                            break;
                        case "design":
                            imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__design));
                            break;
                        case "hacktivism":
                            imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__hack));
                            break;
                        case "tech":
                            imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__tech));
                            break;
                        case "learn":
                            imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__learn));
                            break;
                        case "makers":
                            imageTrack.setImageDrawable(context.getResources().getDrawable(R.drawable.mxt_icon__makers));
                            break;
                    }

                }

                Salle salle;
                if (Salle.INCONNU != Salle.getSalle(talk.getRoom())) {
                    salle = Salle.getSalle(talk.getRoom());
                    if(context.getResources().getBoolean(R.bool.small_screen)){
                        talkSalle.setText(String.format(context.getResources().getString(R.string.Salle), salle.getTeenyName()));
                    }
                    else {
                        talkSalle.setText(String.format(context.getResources().getString(R.string.Salle), salle.getNom()));
                    }
                    talkSalle.setBackgroundColor(context.getResources().getColor(salle.getColor()));

                }
                else{
                    talkSalle.setText("");
                }

                if ("WORKSHOP".equals(talk.getFormat())) {
                    talkImageText.setText("Atelier");
                    talkImageText.setTextColor(context.getResources().getColor(R.color.color_workshops));
                }
                else if ("KEYNOTE".equals(((Talk) talk).getFormat())) {
                    talkImageText.setText("Keynote");
                }
                else if ("RANDOM".equals(((Talk) talk).getFormat())) {
                    talkImageText.setText("Random");
                }
                else if ("TALK".equals(((Talk) talk).getFormat())) {
                    talkImageText.setText("Talk");
                }
                else if(("Special".equals(talk.getFormat()))) {
                    imageFavorite.setImageDrawable(null);
                    imageTrack.setImageDrawable(null);
                    talkImageText.setText("");
                }
                else {
                    talkImageText.setText(talk.getFormat());
                    talkImageText.setTextColor(context.getResources().getColor(R.color.color_talks));
                }

            }
        }

        public void listen(Talk talk) {
            itemView.setOnClickListener(v -> listener.onTalkClicked(talk));
        }
    }

    public interface OnTalkClickListener {
        void onTalkClicked(Talk talk);
    }
}
