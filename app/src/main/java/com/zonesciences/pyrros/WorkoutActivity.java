package com.zonesciences.pyrros;

import android.content.Intent;
import android.graphics.Color;
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
import android.view.View;
import android.view.ViewGroup;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.zonesciences.pyrros.fragment.ExerciseFragment;
import com.zonesciences.pyrros.fragment.ExerciseHistoryFragment;
import com.zonesciences.pyrros.fragment.FeedbackFragment;
import com.zonesciences.pyrros.fragment.StatsFragment;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
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
    private static final String WORKOUT_ID = "Workout ID";

    ViewPager mExercisesViewPager;
    WorkoutExercisesAdapter mWorkoutExercisesAdapter;

    Toolbar mToolbar;
    TabLayout mTabLayout;

    ArrayList<String> mExercisesList;
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

    //Exercise History
    List<String> mExerciseHistoryDates;
    List<Exercise> mExerciseHistory;

    String mFragmentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);


        Intent i = getIntent();
        mExercisesList = (ArrayList<String>) i.getSerializableExtra(WORKOUT_EXERCISES);
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

    private void changeFragment(String fragmentTag) {
        mExercisesViewPager.setVisibility(View.GONE);
        mTabLayout.setVisibility(View.GONE);

        mFragmentTag = fragmentTag;
        Fragment fragment;
        if (fragmentTag == "EXERCISE_HISTORY"){
            Log.i(TAG, "Current exercise item: " + mExercisesList.get(mExercisesViewPager.getCurrentItem()));

            int index = mExercisesViewPager.getCurrentItem();
            ExerciseFragment currentFragment = mWorkoutExercisesAdapter.getFragment(index);

            //If the history tab is clicked before the fragment has obtained exercise history,
            // then this will cause a null pointer exception in ExerciseHistoryFragment

            mExerciseHistoryDates = currentFragment.getExerciseHistoryDates();
            mExerciseHistory = currentFragment.getExerciseHistory();

            if (mExerciseHistory == null){
                Log.i(TAG, "History tab clicked, but mExerciseHistory is null, there do not allow fragment to load");
                return;
            } else {

                mExerciseKey = mExercisesList.get(mExercisesViewPager.getCurrentItem());
                if (mExerciseHistoryFragment == null) {
                    mExerciseHistoryFragment = ExerciseHistoryFragment.newInstance(mExerciseKey, mUserId, mExerciseHistoryDates, mExerciseHistory);
                }
                fragment = mExerciseHistoryFragment.newInstance(mExerciseKey, mUserId, mExerciseHistoryDates, mExerciseHistory);
            }
        } else if (fragmentTag == "STATS"){
            if (mStatsFragment == null) {
                mStatsFragment = new StatsFragment();
            }
            fragment = mStatsFragment;
        } else if (fragmentTag == "FEEDBACK"){
            if(mFeedbackFragment == null){
                mFeedbackFragment = new FeedbackFragment();
            }
            fragment = mFeedbackFragment;
        } else {
            fragment = new Fragment();
        }

        // only add to back stack if a any fragment has previously been added, otherwise just replace fragment.
        FragmentTransaction ft;
        ft = mFragmentManager.beginTransaction();
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.workout_fragment_container, fragment, mFragmentTag).commit();
        } else {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.workout_fragment_container, fragment, mFragmentTag).addToBackStack(null).commit();
        }
        Log.i(TAG, "Backstack entry count: " + mFragmentManager.getBackStackEntryCount());

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
            ExerciseFragment exerciseFragment = ExerciseFragment.newInstance(mExercisesList.get(position), mWorkoutKey, mUserId);
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


}
