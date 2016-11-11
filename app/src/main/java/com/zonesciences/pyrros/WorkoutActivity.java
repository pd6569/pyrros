package com.zonesciences.pyrros;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.zonesciences.pyrros.datatools.ExerciseHistory;
import com.zonesciences.pyrros.fragment.ExerciseFragment;
import com.zonesciences.pyrros.fragment.ExerciseHistoryFragment;
import com.zonesciences.pyrros.fragment.FeedbackFragment;
import com.zonesciences.pyrros.fragment.StatsFragment;
import com.zonesciences.pyrros.models.Exercise;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: Rotating screen to landscape crashes app - likely just fix to portrait orientation for this app

// The viewpager forms the main basis of this activity and is separate to the fragment container
// The fragment container switches in and out the exercise history, stats, and feedback fragments
// and the visibility of the viewpager is toggled on/off.

public class WorkoutActivity extends BaseActivity {

    private static String TAG = "WorkoutActivity";

    private static final String WORKOUT_EXERCISES = "Workout Exercises";
    private static final String WORKOUT_EXERCISE_OBJECTS = "WorkoutExerciseObjects";
    private static final String WORKOUT_ID = "Workout ID";

    ViewPager mExercisesViewPager;
    WorkoutExercisesAdapter mWorkoutExercisesAdapter;

    Toolbar mToolbar;
    TabLayout mTabLayout;

    ArrayList<String> mExercisesList;
    ArrayList<Exercise> mExerciseObjects;

    String mWorkoutKey;
    String mExerciseKey;
    String mUserId;

    FragmentManager mFragmentManager;

    //Track exercise fragments
    Map<Integer, ExerciseFragment> mExerciseReferenceMap = new HashMap<>();

    // Fragments
    ExerciseHistoryFragment mExerciseHistoryFragment;
    StatsFragment mStatsFragment;
    FeedbackFragment mFeedbackFragment;

    //Fragment temporary reference
    Fragment mFragment;

    //Exercise History
    List<String> mExerciseHistoryDates;
    List<Exercise> mExerciseHistory;

    //Maps for storing exercise history for each exercise in the workout once initially loaded from firebase
    Map<Integer, List<String>> mExerciseHistoryDatesMap = new HashMap<>();
    Map<Integer, List<Exercise>> mExerciseHistoryMap = new HashMap<>();

    String mFragmentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);


        Intent i = getIntent();
        mExercisesList = (ArrayList<String>) i.getSerializableExtra(WORKOUT_EXERCISES);
        mExerciseObjects = (ArrayList<Exercise>) i.getSerializableExtra(WORKOUT_EXERCISE_OBJECTS);
        mWorkoutKey = i.getStringExtra(WORKOUT_ID);
        mUserId = getUid();

        Log.i(TAG, "Exercises : " + mExercisesList + " WorkoutKey: " + mWorkoutKey);

        mExercisesViewPager = (ViewPager) findViewById(R.id.viewpager_exercises);

        mFragmentManager = getSupportFragmentManager();
        mWorkoutExercisesAdapter = new WorkoutExercisesAdapter(mFragmentManager);
        mExercisesViewPager.setAdapter(mWorkoutExercisesAdapter);


        mToolbar = (Toolbar) findViewById(R.id.toolbar_workout);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Workout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs_workout);
        mTabLayout.setupWithViewPager(mExercisesViewPager, false);

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {

                switch (tabId) {
                    case R.id.tab_history:
                        mFragmentTag = "EXERCISE_HISTORY";
                        changeFragment(mFragmentTag);

                        break;

                    case R.id.tab_stats:
                        mFragmentTag = "STATS";
                        changeFragment(mFragmentTag);
                        break;

                    case R.id.tab_feedback:
                        mFragmentTag = "FEEDBACK";
                        changeFragment(mFragmentTag);
                        break;

                    case R.id.tab_workout:
                        returnToWorkout();
                        Log.i(TAG, "Backstack entry count: " + mFragmentManager.getBackStackEntryCount());
                        break;
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_workout, menu);
        return true;
    }

    private void changeFragment(String fragmentTag) {
        mExercisesViewPager.setVisibility(View.GONE);
        mTabLayout.setVisibility(View.GONE);

        mFragmentTag = fragmentTag;

        if (fragmentTag == "EXERCISE_HISTORY") {
            Log.i(TAG, "Current exercise item: " + mExercisesList.get(mExercisesViewPager.getCurrentItem()));

            final int index = mExercisesViewPager.getCurrentItem();
            mExerciseKey = mExercisesList.get(index);

            //check if the history has already been viewed, if not load from firebase and store in hashmaps
            if (!mExerciseHistoryMap.containsKey(index)) {
                Log.i(TAG, "This exercise has not been viewed. Load from firebase and update map");
                final ExerciseHistory exerciseHistory = new ExerciseHistory(getUid(), mExerciseKey);

                //start loading exercise history
                showProgressDialog();
                exerciseHistory.getHistory();
                exerciseHistory.setOnLoadCompleteListener(new ExerciseHistory.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete() {
                        Log.i(TAG, "Callback from exercise history loader received. Notified of completion.");
                        hideProgressDialog();
                        mExerciseHistoryDates = exerciseHistory.getExerciseDates();
                        mExerciseHistory = exerciseHistory.getExercises();
                        mExerciseHistoryDates.remove(mExerciseHistory.size()-1);
                        mExerciseHistory.remove(mExerciseHistory.size()-1);

                        List<String> dateformat = new ArrayList<String>();
                        for (int i = 0; i < mExerciseHistoryDates.size(); i++){
                            String oldDate = mExerciseHistoryDates.get(i);
                            String newDate;
                            DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
                            DateFormat targeFormat = new SimpleDateFormat("EEE, dd MMM");
                            try {
                                Date date = originalFormat.parse(oldDate);
                                DateFormat newDF = new SimpleDateFormat("EEE, dd MMM");


                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        //puts newest exercises first by default
                        Collections.reverse(mExerciseHistoryDates);
                        Collections.reverse(mExerciseHistory);

                        //store data to hashmap
                        mExerciseHistoryDatesMap.put(index, mExerciseHistoryDates);
                        mExerciseHistoryMap.put(index, mExerciseHistory);

                        loadExerciseHistoryFragment();
                    }
                });
            }

            else {
                Log.i(TAG, "This exercise has already been viewed. Load history from map, not firebase ");
                mExerciseHistory = mExerciseHistoryMap.get(index);
                mExerciseHistoryDates = mExerciseHistoryDatesMap.get(index);

                loadExerciseHistoryFragment();
            }
        }

        else {
            if (fragmentTag == "STATS") {
                if (mStatsFragment == null) {
                    mStatsFragment = new StatsFragment();
                }
                mFragment = mStatsFragment;
            } else if (fragmentTag == "FEEDBACK") {
                if (mFeedbackFragment == null) {
                    mFeedbackFragment = new FeedbackFragment();
                }
                mFragment = mFeedbackFragment;
            } else {
                mFragment = new Fragment();
            }

            // only add to back stack if a any fragment has previously been added, otherwise just replace fragment.
            FragmentTransaction ft;
            ft = mFragmentManager.beginTransaction();
            if (mFragmentManager.getBackStackEntryCount() > 0) {
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.workout_fragment_container, mFragment, mFragmentTag).commit();
            } else {
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.workout_fragment_container, mFragment, mFragmentTag).addToBackStack(null).commit();
            }
            Log.i(TAG, "Backstack entry count: " + mFragmentManager.getBackStackEntryCount());
        }

    }

    public void returnToWorkout(){

        mExercisesViewPager.setVisibility(View.VISIBLE);
        mTabLayout.setVisibility(View.VISIBLE);

        // backstack will be 1 if any bottom nav frag has been previously selected. If it is 0, then the
        // workout tab will be displayed, therefore do nothing.
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            Log.i(TAG, "Attempt to remove fragment: " + mFragmentManager.findFragmentByTag(mFragmentTag));
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.remove(mFragmentManager.findFragmentByTag(mFragmentTag)).commit();
        }
    }

    @Override
    public void onBackPressed(){
        Log.i(TAG, "Back button pressed, and I'm not gonna do shit about it, you stay right where you are motherfucker");
        // DO NOTHING
    }


    class WorkoutExercisesAdapter extends FragmentPagerAdapter {

        public WorkoutExercisesAdapter(FragmentManager fm) {
            super(fm);
            Log.i(TAG, "WorkoutExercisesAdapter constructor called. Size of mExercises: " + mExercisesList.size());
        }

        @Override
        public Fragment getItem(int position) {
            ExerciseFragment exerciseFragment = ExerciseFragment.newInstance(mExercisesList.get(position), mExerciseObjects.get(position), mWorkoutKey, mUserId);
            mExerciseReferenceMap.put(position, exerciseFragment); // map exercise fragments so that variables can be obtained when switching fragments.
            return exerciseFragment;
        }

        @Override
        public int getCount() {
            return mExercisesList.size();
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mExercisesList.get(position);
        }

        public ExerciseFragment getFragment(int key){
            return mExerciseReferenceMap.get(key);
        }
    }

    public String getWorkoutKey(){
        return this.mWorkoutKey;
    }

    public void loadExerciseHistoryFragment(){

        if (mExerciseHistoryFragment == null) {
            mExerciseHistoryFragment = ExerciseHistoryFragment.newInstance(mExerciseKey, mUserId, mExerciseHistoryDates, mExerciseHistory);
        }
        mFragment = mExerciseHistoryFragment.newInstance(mExerciseKey, mUserId, mExerciseHistoryDates, mExerciseHistory);

        FragmentTransaction ft;
        ft = mFragmentManager.beginTransaction();
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.workout_fragment_container, mFragment, mFragmentTag).commit();
        } else {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.workout_fragment_container, mFragment, mFragmentTag).addToBackStack(null).commit();
        }

        Log.i(TAG, "Backstack entry count: " + mFragmentManager.getBackStackEntryCount());

    }

}
