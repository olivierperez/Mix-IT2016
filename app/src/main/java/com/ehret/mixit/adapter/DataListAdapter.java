package com.ehret.mixit.adapter;

import android.content.Context;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author Olivier Perez
 */
public class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.DataViewHolder> {

    private final Set<String> favorites;
    private final OnTalkClickListener listener;

    private List<Talk> items;

    public DataListAdapter(Set<String> favorites, OnTalkClickListener listener) {
        this.favorites = favorites;
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

            if ("ENGLISH".equals(talk.getLang())) {
                langImage.setImageResource(R.drawable.en);
            } else {
                langImage.setImageResource(R.drawable.fr);
            }

            if (Salle.INCONNU != Salle.getSalle(talk.getRoom())) {
                Salle salle = Salle.getSalle(talk.getRoom());
                if (context.getResources().getBoolean(R.bool.small_screen)) {
                    talkSalle.setText(context.getResources().getString(R.string.Salle, salle.getTeenyName()));
                } else {
                    talkSalle.setText(context.getResources().getString(R.string.Salle, salle.getNom()));
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
                        imageTrack.setImageResource(R.drawable.mxt_icon__aliens);
                        break;
                    case "design":
                        imageTrack.setImageResource(R.drawable.mxt_icon__design);
                        break;
                    case "hacktivism":
                        imageTrack.setImageResource(R.drawable.mxt_icon__hack);
                        break;
                    case "tech":
                        imageTrack.setImageResource(R.drawable.mxt_icon__tech);
                        break;
                    case "learn":
                        imageTrack.setImageResource(R.drawable.mxt_icon__learn);
                        break;
                    case "makers":
                        imageTrack.setImageResource(R.drawable.mxt_icon__makers);
                        break;
                }
            }

            // On regarde si la conf fait partie des favoris
            imageFavorite.setImageResource(favorites.contains(talk.getIdSession())
                    ? R.drawable.ic_action_important
                    : R.drawable.ic_action_not_important);
        }

        public void listen(Talk talk) {
            itemView.setOnClickListener(v -> listener.onTalkClicked(talk));
        }
    }

    public interface OnTalkClickListener {
        void onTalkClicked(Talk talk);
    }
}
