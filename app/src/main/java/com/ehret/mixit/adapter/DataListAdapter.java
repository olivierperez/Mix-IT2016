package com.ehret.mixit.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ehret.mixit.R;
import com.ehret.mixit.domain.Salle;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.utils.UIUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * @author Olivier Perez
 */
public class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.DataViewHolder> {

    private final OnTalkClickListener listener;

    private List<Talk> items;

    public DataListAdapter(OnTalkClickListener listener) {
        this.listener = listener;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_talk, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        holder.bind(items.get(position));
        holder.listen(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void setItems(List<Talk> items) {
        this.items = items;
        notifyItemRangeInserted(0, items.size());
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView descriptif;
        private TextView horaire;
        private TextView talkImageText;
        private TextView talkSalle;
        private ImageView imageFavorite;
        private ImageView imageTrack;
        private ImageView langImage;

        public DataViewHolder(View view) {
            super(view);
            imageFavorite = (ImageView) view.findViewById(R.id.talk_image_favorite);
            langImage = (ImageView) view.findViewById(R.id.talk_image_language);
            name = (TextView) view.findViewById(R.id.talk_name);
            descriptif = (TextView) view.findViewById(R.id.talk_shortdesciptif);
            horaire = (TextView) view.findViewById(R.id.talk_horaire);
            talkImageText = (TextView) view.findViewById(R.id.talkImageText);
            imageTrack = (ImageView) view.findViewById(R.id.talk_image_track);
            talkSalle = (TextView) view.findViewById(R.id.talk_salle);
        }

        public void bind(Talk talk) {
            Context context = itemView.getContext();

            name.setText(talk.getTitle());
            if (talk.getSummary() != null) {
                descriptif.setText(Html.fromHtml(talk.getSummary().trim()));
            }
            DateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
            if (talk.getStart() != null && talk.getEnd() != null) {
                horaire.setText(String.format(context.getResources().getString(R.string.periode),
                        sdf.format(talk.getStart()),
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(talk.getStart()),
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(talk.getEnd())
                ));
            } else {
                horaire.setText(context.getResources().getString(R.string.pasdate));

            }
            if (talk.getLang() != null && "ENGLISH".equals(talk.getLang())) {
                langImage.setImageDrawable(context.getResources().getDrawable(R.drawable.en));
            } else {
                langImage.setImageDrawable(context.getResources().getDrawable(R.drawable.fr));
            }
            Salle salle;
            if (Salle.INCONNU != Salle.getSalle(talk.getRoom())) {
                salle = Salle.getSalle(talk.getRoom());
                if (context.getResources().getBoolean(R.bool.small_screen)) {
                    talkSalle.setText(String.format(context.getResources().getString(R.string.Salle), salle.getTeenyName()));
                } else {
                    talkSalle.setText(String.format(context.getResources().getString(R.string.Salle), salle.getNom()));
                }
                talkSalle.setBackgroundColor(context.getResources().getColor(salle.getColor()));

            } else {
                talkSalle.setText("");
            }

            if ("WORKSHOP".equals(talk.getFormat())) {
                talkImageText.setText("Workshop");
            } else if ("KEYNOTE".equals(talk.getFormat())) {
                talkImageText.setText("Keynote");
            } else if ("RANDOM".equals(talk.getFormat())) {
                talkImageText.setText("Random");
            } else if ("TALK".equals(talk.getFormat())) {
                talkImageText.setText("Talk");
            }

            if (talk.getTrack() != null) {
                switch (talk.getTrack()) {
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
            //On regarde si la conf fait partie des favoris
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
        }

        public void listen(Talk talk) {
            itemView.setOnClickListener(v -> listener.onTalkClicked(talk));
        }
    }

    public interface OnTalkClickListener {
        void onTalkClicked(Talk talk);
    }
}
