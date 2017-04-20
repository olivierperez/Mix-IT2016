package com.ehret.mixit.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ehret.mixit.HomeActivity;
import com.ehret.mixit.R;
import com.ehret.mixit.adapter.FilAdapter;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.model.ConferenceFacade;
import com.ehret.mixit.utils.UIUtils;
import com.ehret.mixit.view.LinearLayoutManagerWithSmoothScroller;

import java.util.Date;
import java.util.List;


public class FilDeLeauFragment extends Fragment {

    private RecyclerView talksListView;
    private FilAdapter adapter;
    private boolean alreadyLoaded = false;
    private List<Talk> talks;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        SharedPreferences settings = getContext().getSharedPreferences(UIUtils.PREFS_FAVORITES_NAME, 0);

        adapter = new FilAdapter(settings.getAll().keySet(),
                talk -> {
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
        talksListView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(getContext()));
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, R.string.option_next_talk, 65535, R.string.option_next_talk);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.string.option_next_talk) {
            talksListView.smoothScrollToPosition(getCurrentTalk(talks));
            return false;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method helps to scroll to the next incoming talk.
     *
     * @param talks List of all talks
     * @return The index of the next incoming talk
     */
    public int getCurrentTalk(List<Talk> talks) {
        Date date = new Date();
        for (int i = 0; i < talks.size(); i++) {
            Talk talk = talks.get(i);
            if (talk.getEnd() != null && talk.getEnd().after(date)) {
                if (i >= 2 && talks.get(i - 2).getFormat().startsWith("day")) {
                    return i - 2;
                } else if (i >= 1 && talks.get(i - 1).getTitle() == null) {
                    return i - 1;
                } else {
                    return i;
                }
            }
        }
        return 0;
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
            if (!alreadyLoaded) {
                FilDeLeauFragment.this.talks = talks;
                talksListView.scrollToPosition(getCurrentTalk(talks));
                alreadyLoaded = true;
            }
        }
    }
}
