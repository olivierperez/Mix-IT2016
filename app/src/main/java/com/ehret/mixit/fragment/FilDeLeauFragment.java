package com.ehret.mixit.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ehret.mixit.HomeActivity;
import com.ehret.mixit.R;
import com.ehret.mixit.adapter.ListTalkForFilAdapter;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.model.ConferenceFacade;

import java.util.List;


public class FilDeLeauFragment extends Fragment {

    private ListView talksListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_datalist, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        talksListView = (ListView) view.findViewById(R.id.liste_content);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((HomeActivity) context).onSectionAttached("title_fildeleau","color_primary");
    }

    /**
     * Recuperation des marques de la partie en cours
     */
    @Override
    public void onResume() {
        super.onResume();
        Context context = getContext();

        talksListView.setOnItemClickListener((parent, view, position, id) -> {
            Talk conf = (Talk) talksListView.getItemAtPosition(position);

            if("Special".equals(conf.getFormat())){
                ((HomeActivity) getActivity()).changeCurrentFragment(
                        SessionDetailFragment.newInstance(TypeFile.special.toString(), conf.getIdSession(),-1), TypeFile.special.toString());
            } else if("WORKSHOP".equals(conf.getFormat())){
                ((HomeActivity) getActivity()).changeCurrentFragment(
                        SessionDetailFragment.newInstance(TypeFile.workshops.toString(), conf.getIdSession(),4), TypeFile.workshops.toString());
            } else {
                ((HomeActivity) getActivity()).changeCurrentFragment(
                        SessionDetailFragment.newInstance(TypeFile.talks.toString(), conf.getIdSession(),3), TypeFile.talks.toString());
            }
        });

        if (talksListView.getAdapter() == null) {
            new Loading(context).execute();
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
            ListTalkForFilAdapter adapter = new ListTalkForFilAdapter(context, talks);
            talksListView.setAdapter(adapter);
            talksListView.setSelection(adapter.getCurrentSelection());
        }
    }
}
