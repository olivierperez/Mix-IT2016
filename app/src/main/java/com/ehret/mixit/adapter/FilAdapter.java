package com.ehret.mixit.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author Olivier Perez
 */
public class FilAdapter extends RecyclerView.Adapter<FilAdapter.FilViewHolder> {

    private final Set<String> favorites;
    private final OnTalkClickListener listener;

    private List<Talk> items;

    public SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());

    public FilAdapter(Set<String> favorites, OnTalkClickListener listener) {
        this.favorites = favorites;
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
        private ImageView langImage;
        private View timeContainer;

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
            timeContainer = itemView.findViewById(R.id.time_container);
        }

        public void bind(Talk talk) {
            Context context = itemView.getContext();

            Log.d("FilAdapter", "Title: " + talk.getTitle());

            if (talk.getTitle() == null) {
                // In this case this just a time marker
                imageFavorite.setVisibility(View.GONE);
                imageTrack.setVisibility(View.GONE);
                talkImageText.setVisibility(View.GONE);
                timeContainer.setVisibility(View.GONE);
                descriptif.setVisibility(View.GONE);

                if ("day1".equals(talk.getFormat())) {
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.color_home));
                    name.setText(context.getString(R.string.calendrier_jour1));
                    name.setTextColor(context.getResources().getColor(R.color.white));
                } else if ("day2".equals(talk.getFormat())) {
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.color_home));
                    name.setText(context.getString(R.string.calendrier_jour2));
                    name.setTextColor(context.getResources().getColor(R.color.white));
                } else {
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.color_planning_time));
                    name.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(talk.getEnd()));
                    name.setTextColor(context.getResources().getColor(R.color.black));
                }
            } else {
                imageFavorite.setVisibility(View.VISIBLE);
                imageTrack.setVisibility(View.VISIBLE);
                talkImageText.setVisibility(View.VISIBLE);
                timeContainer.setVisibility(View.VISIBLE);

                // We control hour
                if (talk.getEnd() == null || talk.getEnd().compareTo(new Date()) < 0) {
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                    itemView.setBackground(context.getResources().getDrawable(R.drawable.selector_fildeleau_oldsession));
                } else {
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                    itemView.setBackground(context.getResources().getDrawable(R.drawable.selector_fildeleau_session));
                }

                langImage.setImageResource(
                        "ENGLISH".equals(talk.getLang())
                                ? R.drawable.en
                                : R.drawable.fr);

                if (talk.getStart() != null && talk.getEnd() != null) {
                    horaire.setText(String.format(context.getString(R.string.periode),
                            sdf.format(talk.getStart()),
                            DateFormat.getTimeInstance(DateFormat.SHORT).format(talk.getStart()),
                            DateFormat.getTimeInstance(DateFormat.SHORT).format(talk.getEnd())
                    ));
                } else {
                    horaire.setText(context.getString(R.string.pasdate));

                }

                name.setTextColor(context.getResources().getColor(R.color.black));
                name.setText(talk.getTitle());
                if (talk.getSummary() != null) {
                    descriptif.setVisibility(View.VISIBLE);
                    descriptif.setText(Html.fromHtml(talk.getSummary().trim()));
                } else {
                    descriptif.setVisibility(View.GONE);
                    descriptif.setText("");
                }

                // On regarde si la talk fait partie des favoris
                imageFavorite.setImageResource(
                        favorites.contains(talk.getIdSession())
                                ? R.drawable.ic_action_important
                                : R.drawable.ic_action_not_important);

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

                Salle salle;
                if (Salle.INCONNU != Salle.getSalle(talk.getRoom())) {
                    salle = Salle.getSalle(talk.getRoom());
                    if (context.getResources().getBoolean(R.bool.small_screen)) {
                        talkSalle.setText(context.getString(R.string.Salle, salle.getTeenyName()));
                    } else {
                        talkSalle.setText(context.getString(R.string.Salle, salle.getNom()));
                    }
                    talkSalle.setBackgroundResource(salle.getColor());

                } else {
                    talkSalle.setText("");
                }

                if ("Special".equals(talk.getFormat())) {
                    imageFavorite.setVisibility(View.GONE);
                    imageTrack.setVisibility(View.GONE);
                    talkImageText.setVisibility(View.GONE);
                } else {
                    imageFavorite.setVisibility(View.VISIBLE);
                    imageTrack.setVisibility(View.VISIBLE);
                    talkImageText.setVisibility(View.VISIBLE);
                }

                if ("WORKSHOP".equals(talk.getFormat())) {
                    talkImageText.setText("Atelier");
                    talkImageText.setTextColor(context.getResources().getColor(R.color.color_workshops));
                } else if ("KEYNOTE".equals(talk.getFormat())) {
                    talkImageText.setText("Keynote");
                } else if ("RANDOM".equals(talk.getFormat())) {
                    talkImageText.setText("Random");
                } else if ("TALK".equals(talk.getFormat())) {
                    talkImageText.setText("Talk");
                } else {
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
