package com.ehret.mixit.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ehret.mixit.HomeActivity;
import com.ehret.mixit.R;
import com.ehret.mixit.adapter.DataListAdapter;
import com.ehret.mixit.adapter.MemberListAdapter;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.model.ConferenceFacade;
import com.ehret.mixit.model.MembreFacade;
import com.ehret.mixit.utils.UIUtils;

import java.util.List;
import java.util.Set;

public class DataListFragment extends Fragment {

    private RecyclerView recyclerView;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DataListFragment newInstance(String typeAppel, String filterQuery, int sectionNumber) {
        DataListFragment fragment = new DataListFragment();
        Bundle args = new Bundle();
        args.putString(UIUtils.ARG_LIST_TYPE, typeAppel);
        args.putString(UIUtils.ARG_LIST_FILTER, filterQuery);
        args.putInt(UIUtils.ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putAll(getArguments());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context != null && getArguments().getString(UIUtils.ARG_LIST_TYPE) != null) {
            ((HomeActivity) getActivity()).onSectionAttached(
                    "title_" + getArguments().getString(UIUtils.ARG_LIST_TYPE),
                    "color_primary");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && getArguments() != null && getArguments().getInt(UIUtils.ARG_SECTION_NUMBER) == 0) {
            getArguments().putString(UIUtils.ARG_LIST_TYPE, getArguments().getString(UIUtils.ARG_LIST_TYPE));
            getArguments().putString(UIUtils.ARG_LIST_FILTER, getArguments().getString(UIUtils.ARG_LIST_FILTER));
            getArguments().putInt(UIUtils.ARG_SECTION_NUMBER, getArguments().getInt(UIUtils.ARG_SECTION_NUMBER));
            setArguments(savedInstanceState);
        }

    }

    /**
     * Recuperation des marques de la partie en cours
     */
    @Override
    public void onResume() {
        super.onResume();
        if (recyclerView.getAdapter() == null) {
            TypeFile typeFile = TypeFile.getTypeFile(getArguments().getString(UIUtils.ARG_LIST_TYPE));
            if (typeFile != null) {
                switch (typeFile) {
                    case members:
                    case staff:
                    case sponsor:
                        showMembers();
                        break;
                    case talks:
                    case workshops:
                    case favorites:
                        showTalks();
                        break;
                    default:
                        // Par defaut on affiche les speakers
                        showMembers();
                }
            }
        }
    }


    /**
     * Affichage des conferences
     */
    private void showMembers() {
        Context context = getContext();

        MemberListAdapter adapter = new MemberListAdapter(
                member -> ((HomeActivity) getActivity()).changeCurrentFragment(
                        PeopleDetailFragment.newInstance(
                                getArguments().getString(UIUtils.ARG_LIST_TYPE),
                                member.getLogin(),
                                getArguments().getInt(UIUtils.ARG_SECTION_NUMBER)),
                        getArguments().getString(UIUtils.ARG_LIST_TYPE)));

        recyclerView.setAdapter(adapter);

        adapter.setItems(MembreFacade.getInstance().getMembres(
                context,
                getArguments().getString(UIUtils.ARG_LIST_TYPE),
                getArguments().getString(UIUtils.ARG_LIST_FILTER)));
    }


    /**
     * Affichage des confs
     */
    private void showTalks() {
        Context context = getContext();

        String filter = getArguments().getString(UIUtils.ARG_LIST_FILTER);
        TypeFile typeFile = TypeFile.getTypeFile(getArguments().getString(UIUtils.ARG_LIST_TYPE));

        SharedPreferences settings = context.getSharedPreferences(UIUtils.PREFS_FAVORITES_NAME, 0);
        Set<String> favorites = settings.getAll().keySet();

        DataListAdapter adapter = new DataListAdapter(favorites,
                talk ->
                        ((HomeActivity) getActivity()).changeCurrentFragment(
                                SessionDetailFragment.newInstance(
                                        getArguments().getString(UIUtils.ARG_LIST_TYPE),
                                        talk.getIdSession(),
                                        getArguments().getInt(UIUtils.ARG_SECTION_NUMBER)),
                                getArguments().getString(UIUtils.ARG_LIST_TYPE)));
        recyclerView.setAdapter(adapter);

        if (typeFile != null) {
            switch (typeFile) {
                case workshops:
                    adapter.setItems(ConferenceFacade.getInstance().getWorkshops(context, filter));
                    break;
                case talks:
                    adapter.setItems(ConferenceFacade.getInstance().getTalks(context, filter));
                    break;
                default:
                    List<Talk> favTalks = ConferenceFacade.getInstance().getFavorites(context, filter);
                    if (favTalks != null && !favTalks.isEmpty()) {
                        adapter.setItems(favTalks);
                    } else {
                        Toast.makeText(context, "Aucun favori pour le moment. Pour en ajouter, allez sur un talk et cliquez sur une étoile.", Toast.LENGTH_LONG).show();
                    }
            }
        }

    }

}
