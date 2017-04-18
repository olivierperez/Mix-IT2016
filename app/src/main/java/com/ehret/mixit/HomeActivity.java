package com.ehret.mixit;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.ehret.mixit.domain.JsonFile;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.people.Member;
import com.ehret.mixit.fragment.DataListFragment;
import com.ehret.mixit.fragment.DialogAboutFragment;
import com.ehret.mixit.fragment.FilDeLeauFragment;
import com.ehret.mixit.fragment.HomeFragment;
import com.ehret.mixit.fragment.NavigationDrawerFragment;
import com.ehret.mixit.fragment.PeopleDetailFragment;
import com.ehret.mixit.fragment.SessionDetailFragment;
import com.ehret.mixit.model.ConferenceFacade;
import com.ehret.mixit.model.MembreFacade;
import com.ehret.mixit.model.Synchronizer;
import com.ehret.mixit.utils.FileUtils;
import com.ehret.mixit.utils.UIUtils;

import java.util.List;


public class HomeActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String ARG_SECTION_NUMBER = "section_number";
    private Fragment mContent;
    private ProgressDialog progressDialog;
    private DrawerLayout drawerLayout;
    protected int progressStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreferences settings = this.getSharedPreferences(UIUtils.ARG_FILE_SAV, 0);
        Boolean test = settings.getBoolean(UIUtils.ARG_KEY_FIRST_TIME, true);
        if(test){
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(UIUtils.ARG_KEY_FIRST_TIME, false);
            appelerSynchronizer(TypeAppel.TALK);
            editor.apply();
        }

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
        }


        // Set up the drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                drawerLayout);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null && fragment.equals(mContent)) {
                getSupportFragmentManager().putFragment(outState, "mContent", mContent);
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            String filterQuery = getIntent().getStringExtra(SearchManager.QUERY);
            int current = PreferenceManager.getDefaultSharedPreferences(this).getInt(ARG_SECTION_NUMBER, 0);
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.container,
                    DataListFragment.newInstance(getTypeFile(current).toString(), filterQuery,
                            current + 1))
                    .commit();
        }
    }

    private TypeFile getTypeFile(int position) {
        if (position > 1) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(ARG_SECTION_NUMBER, position);
            editor.apply();
        }

        TypeFile typeFile = null;
        switch (position) {
            case 2:
                typeFile = TypeFile.favorites;
                break;
            case 3:
                typeFile = TypeFile.talks;
                break;
            case 4:
                typeFile = TypeFile.workshops;
                break;
            case 5:
                typeFile = TypeFile.speaker;
                break;
            case 6:
                typeFile = TypeFile.sponsor;
                break;
            case 7:
                typeFile = TypeFile.staff;
                break;
        }
        return typeFile;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        switch (position) {
            case 0:
                mContent = new HomeFragment();
                break;
//            case 1:
//                mContent = new PlanningFragment();
//                break;
            case 1:
                mContent = new FilDeLeauFragment();
                break;
            default:
                mContent = DataListFragment.newInstance(getTypeFile(position).toString(), null, position + 1);
        }
        changeCurrentFragment(mContent, null);
    }

    public void changeCurrentFragment(Fragment fragment, String backable) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (backable != null) {
            fragmentTransaction.addToBackStack(backable);
        } else {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        fragmentTransaction.replace(R.id.container, fragment).commit();
    }

    /**
     * On back pressed we want to exit only if home page is the current
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            boolean home = false;
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof HomeFragment) {
                    home = true;
                }
            }
            if(home){
                super.onBackPressed();
            } else{
                changeCurrentFragment(new HomeFragment(), null);
            }
        } else {
            super.onBackPressed();
        }
    }

    public void onSectionAttached(String title, String color) {
        int nbtitle = getResources().getIdentifier(title, "string", HomeActivity.this.getPackageName());

        if (nbtitle > 0) {
            ActionBar actionBar = getSupportActionBar();

            if (actionBar != null) {
                actionBar.setTitle(getString(nbtitle));
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setHomeAsUpIndicator(nbtitle == R.string.title_section_home ? R.drawable.ic_menu : R.drawable.ic_drawer);
                actionBar.setBackgroundDrawable(
                        new ColorDrawable(
                                getResources().getColor(getResources().getIdentifier(color, "color", HomeActivity.this.getPackageName()))));
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);

        //We have to know which fragment is used
        boolean found = false;
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof DataListFragment) {
                found = true;
                menu.findItem(R.id.menu_search).setVisible(true);
                // Get the SearchView and set the searchable configuration
                SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            }
            //The search buuton is not displayed if detail is displayed
            if ((fragment instanceof SessionDetailFragment) || (fragment instanceof PeopleDetailFragment)) {
                found = false;
            }
        }
        if (!found) {
            menu.findItem(R.id.menu_search).setVisible(false);
        }
        menu.findItem(R.id.menu_profile).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                DialogAboutFragment dial = new DialogAboutFragment();
                dial.show(getFragmentManager(), getResources().getString(R.string.about_titre));
                return true;
//            case R.id.menu_compose_google:
//                UIUtils.sendMessage(this, SendSocial.plus);
//                return true;
//            case R.id.menu_compose_twitter:
//                UIUtils.sendMessage(this, SendSocial.twitter);
//                return true;
            case R.id.menu_sync_talk:
                chargementDonnees(TypeAppel.TALK);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public enum TypeAppel {MEMBRE, TALK, FAVORITE}

    /**
     * Affichage d'un message pour savoir quelle données récupérer
     */
    protected void chargementDonnees(final TypeAppel type) {
        if (UIUtils.isNetworkAvailable(getBaseContext())) {
            if (FileUtils.isExternalStorageWritable()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(
                        getString(type == TypeAppel.MEMBRE ? R.string.dial_message_membre : R.string.dial_message_talk))
                        .setPositiveButton(R.string.dial_oui, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                appelerSynchronizer(type);
                            }
                        })
                        .setNeutralButton(R.string.dial_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //On ne fait rien
                            }
                        });
                builder.create();
                builder.show();
            } else {
                Toast.makeText(getBaseContext(), getText(R.string.sync_erreur), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getBaseContext(), getText(R.string.sync_erreur_reseau), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Lancement de la synchro
     */
    public void appelerSynchronizer(TypeAppel type) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setCancelable(true);
        int nbMax;
        if (type.equals(TypeAppel.TALK)) {
            nbMax = 135;
        } else if (type.equals(TypeAppel.MEMBRE)) {
            nbMax = 135;
        } else {
            nbMax = 135;
        }

        progressDialog.setMax(nbMax);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(getResources().getString(R.string.sync_message));
        progressDialog.show();
        new SynchronizeMixitAsync().execute(type);
    }

    /**
     * Lance en asynchrone la recuperation des fichiers
     */
    private class SynchronizeMixitAsync extends AsyncTask<TypeAppel, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressStatus = 0;
        }

        @Override
        protected Void doInBackground(TypeAppel... params) {
            TypeAppel type = params[0];

            // For this version we don't used anymore the favorites
            for (JsonFile json : JsonFile.values()) {
                try {
                    if (json.isReadRemote() && !Synchronizer.downloadJsonFile(getBaseContext(), json.getUrl(), json.getType())) {
                        //Si une erreur de chargement on sort
                        break;
                    }
                    publishProgress(progressStatus++);
                } catch (RuntimeException e) {
                    Log.w("DialogSynchronize", "Impossible de synchroniser", e);
                }
            }


            //TODO certainement à revoir
            //Une fois finie on supprime le cache
            List<Member> membres;
            if (type.equals(TypeAppel.TALK)) {
                MembreFacade.getInstance().viderCacheSpeakerStaffSponsor();
                ConferenceFacade.getInstance().viderCache();
                membres = MembreFacade.getInstance().getMembres(getBaseContext(), TypeFile.speaker.name(), null);
                membres.addAll(MembreFacade.getInstance().getMembres(getBaseContext(), TypeFile.staff.name(), null));
            }
            else {
                MembreFacade.getInstance().viderCacheMembres();
                membres = MembreFacade.getInstance().getMembres(getBaseContext(), TypeFile.members.name(), null);
            }

            //L'action d'après consiste à charger les images
            for (Member membre : membres) {
                if (membre.getUrlImage() != null && membre.isSpeaker()) {
                    Synchronizer.downloadImage(getBaseContext(), membre.getUrlImage(), "membre" + membre.getLogin(), membre.getExtension());
                    publishProgress(progressStatus++);
                }
            }
            //L'action d'après consiste à charger les images
            for (Member membre : MembreFacade.getInstance().getMembres(getBaseContext(), TypeFile.staff.name(), null)) {
                if (membre.getUrlImage() != null) {
                    Synchronizer.downloadImage(getBaseContext(), membre.getUrlImage(), "membre" + membre.getLogin(), membre.getExtension());
                    publishProgress(progressStatus++);
                }
            }
            //Pour les sponsors on s'interesse au logo
            for (Member membre : MembreFacade.getInstance().getMembres(getBaseContext(), TypeFile.sponsor.name(), null)) {
                if (membre.getLogo() != null && membre.isSponsor()) {
                    Synchronizer.downloadImage(getBaseContext(), membre.getUrlImage(), "membre" + membre.getLogin(), membre.getExtension());
                    publishProgress(progressStatus++);
                }
            }
            return null;
        }

        /**
         * This callback method is invoked when publishProgress()
         * method is called
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(progressStatus);
        }

        /**
         * This callback method is invoked when the background function
         * doInBackground() is executed completely
         */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                progressDialog.dismiss();
            } catch (IllegalArgumentException e) {
                //Si la vue n'est plus attachée (changement d'orientation on évite de faire planter)
                Log.w("AbstractActivity", "Erreur à la fin du chargement lors de la notification de la vue");

            }
        }
    }
}
