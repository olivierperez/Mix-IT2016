package com.ehret.mixit.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ehret.mixit.R;
import com.ehret.mixit.domain.people.Member;
import com.ehret.mixit.utils.FileUtils;
import com.github.rjeschke.txtmark.Processor;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * @author Olivier Perez
 */
public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.MemberViewHolder> {

    private final OnMemberClickListener listener;

    private List<Member> items;

    public MemberListAdapter(OnMemberClickListener listener) {
        this.listener = listener;
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        holder.bind(items.get(position));
        holder.listen(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void setItems(List<Member> items) {
        this.items = items;
        notifyItemRangeInserted(0, items.size());
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private TextView userName;
        private TextView descriptif;
        private TextView level;
        private ImageView profile_image;

        public MemberViewHolder(View view) {
            super(view);
            profile_image = (ImageView) view.findViewById(R.id.person_user_image);
            userName = (TextView) view.findViewById(R.id.person_user_name);
            descriptif = (TextView) view.findViewById(R.id.person_shortdesciptif);
            level = (TextView) view.findViewById(R.id.person_level);
        }

        public void bind(Member member) {
            Context context = itemView.getContext();

            userName.setText(member.getCompleteName());
            if (member.getShortDescription() != null) {
                descriptif.setText(Html.fromHtml(Processor.process(member.getShortDescription().trim())).toString().replaceAll("\n", ""));
            }

            File sponsorImage = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), "logo" + member.getLogin() + "."  + member.getExtension());
            File profileImage = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), "membre" + member.getLogin() + "." + member.getExtension());

            // Si on est un sponsor on affiche le logo
            if ("svg".equals(member.getExtension())) {
                profile_image.setAdjustViewBounds(true);
                profile_image.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                Drawable imageSvg = FileUtils.getImageSvg(context, member);
                if (imageSvg != null) {
                    profile_image.setImageDrawable(imageSvg);
                } else {
                    profile_image.setImageResource(R.drawable.person_image_empty);
                }
            } else if (member.getLogo() != null && !member.getLogo().isEmpty() && sponsorImage.exists()) {
                Picasso
                        .with(context)
                        .load(sponsorImage)
                        .resizeDimen(R.dimen.speaker_image_size, R.dimen.speaker_image_size)
                        .placeholder(R.drawable.person_image_empty)
                        .into(profile_image);
            } else {
                // Recuperation de l'image liee au profil
                Picasso
                        .with(context)
                        .load(profileImage)
                        .resizeDimen(R.dimen.speaker_image_size, R.dimen.speaker_image_size)
                        .placeholder(R.drawable.person_image_empty)
                        .into(profile_image);
            }

        }

        public void listen(Member member) {
            itemView.setOnClickListener(v -> listener.onTalkClicked(member));
        }
    }

    public interface OnMemberClickListener {
        void onTalkClicked(Member member);
    }
}
