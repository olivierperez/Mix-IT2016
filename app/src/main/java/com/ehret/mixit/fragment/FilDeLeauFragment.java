package com.ehret.mixit.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ehret.mixit.HomeActivity;
import com.ehret.mixit.R;
import com.ehret.mixit.adapter.FilAdapter;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.model.ConferenceFacade;

import java.util.List;


public class FilDeLeauFragment extends Fragment {

    private RecyclerView talksListView;
    private FilAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new FilAdapter(talk -> {
            if ("Special".equals(talk.getFormat())) {
                ((HomeActivity) getActivity()).changeCurrentFragment(
                        SessionDetailFragment.newInstance(TypeFile.special.toString(), talk.getIdSession(), -1), TypeFile.special.toString());
            } else if ("WORKSHOP".equals(talk.getFormat())) {
                ((HomeActivity) getActivity()).changeCurrentFragment(
                        SessionDetailFragment.newInstance(TypeFile.workshops.toString(), talk.getIdSession(), 4), TypeFile.workshops.toString());
            } else {
                ((HomeActivity) getActivity()).changeCurrentFragment(
                        SessionDetailFragment.newInstance(TypeFile.talks.toString(), talk.getIdSession(), 3), TypeFile.talks.toString());
            }
        });

        talksListView = (RecyclerView) view.findViewById(R.id.recyclerView);
        talksListView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((HomeActivity) context).onSectionAttached("title_fildeleau", "color_primary");
    }

    /**
     * Recuperation des marques de la partie en cours
     */
    @Override
    public void onResume() {
        super.onResume();

        if (talksListView.getAdapter() == null || talksListView.getAdapter().getItemCount() == 0) {
            new Loading(getContext()).execute();
        }
    }

    /**
     * Load talks asynchronously
     */
    private class Loading extends AsyncTask<Void, Void, List<Talk>> {

        private final Context context;

        private Loading(Context context) {
            this.context = context;
        }

        @Override
        protected List<Talk> doInBackground(Void... voids) {
            return ConferenceFacade.getInstance().getWorkshopsAndTalks(context);
        }

        @Override
        protected void onPostExecute(List<Talk> talks) {
            adapter.setItems(talks);
            talksListView.setAdapter(adapter);
        }
    }
}
