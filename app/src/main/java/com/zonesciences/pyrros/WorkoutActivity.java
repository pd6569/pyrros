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

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.zonesciences.pyrros.fragment.ExerciseFragment;
import com.zonesciences.pyrros.fragment.ExerciseHistoryFragment;
import com.zonesciences.pyrros.fragment.FeedbackFragment;
import com.zonesciences.pyrros.fragment.StatsFragment;

import java.util.ArrayList;

//TODO: Rotating screen to landscape crashes app - likely just fix to portrait orientation for this app

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

    FragmentManager mFragmentManager;

    // Fragments
    ExerciseHistoryFragment mExerciseHistoryFragment;
    StatsFragment mStatsFragment;
    FeedbackFragment mFeedbackFragment;

    String mActiveFragmentTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);


        Intent i = getIntent();
        mExercisesList = (ArrayList<String>) i.getSerializableExtra(WORKOUT_EXERCISES);
        mWorkoutKey = i.getStringExtra(WORKOUT_ID);

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
                FragmentTransaction ft = null;
                switch (tabId) {
                    case R.id.tab_history:
                        mExercisesViewPager.setVisibility(View.GONE);
                        mTabLayout.setVisibility(View.GONE);

                        if (mExerciseHistoryFragment == null) {
                            Log.i(TAG, "New exercise history fragment created");
                            mExerciseHistoryFragment = new ExerciseHistoryFragment();

                        }
                        ft = mFragmentManager.beginTransaction();
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.workout_fragment_container, mExerciseHistoryFragment, "EXERCISE_HISTORY").addToBackStack(null).commit();
                        Log.i(TAG, "Backstack entry count: " + mFragmentManager.getBackStackEntryCount());
                        mActiveFragmentTag = "EXERCISE_HISTORY";
                        break;

                    case R.id.tab_stats:
                        mExercisesViewPager.setVisibility(View.GONE);
                        mTabLayout.setVisibility(View.GONE);

                        if (mStatsFragment == null){
                            Log.i(TAG, "New stats fragment created");
                            mStatsFragment = new StatsFragment();
                        }

                        ft = mFragmentManager.beginTransaction();
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.workout_fragment_container, mStatsFragment, "STATS").addToBackStack(null).commit();
                        Log.i(TAG, "Backstack entry count: " + mFragmentManager.getBackStackEntryCount());
                        mActiveFragmentTag = "STATS";
                        break;

                    case R.id.tab_feedback:
                        mExercisesViewPager.setVisibility(View.GONE);
                        mTabLayout.setVisibility(View.GONE);

                        if (mFeedbackFragment == null){
                            Log.i(TAG, "New feedback fragment created");
                            mFeedbackFragment = new FeedbackFragment();
                        }

                        ft = mFragmentManager.beginTransaction();
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.workout_fragment_container, mFeedbackFragment, "FEEDBACK").addToBackStack(null).commit();
                        Log.i(TAG, "Backstack entry count: " + mFragmentManager.getBackStackEntryCount());
                        mActiveFragmentTag = "FEEDBACK";

                        break;

                    case R.id.tab_workout:

                        mExercisesViewPager.setVisibility(View.VISIBLE);
                        mTabLayout.setVisibility(View.VISIBLE);

                        if (mFragmentManager.getBackStackEntryCount() > 0) {
                            Log.i(TAG, "Attempt to remove fragment: " + mFragmentManager.findFragmentByTag(mActiveFragmentTag));
                            ft = mFragmentManager.beginTransaction();
                            ft.remove(mFragmentManager.findFragmentByTag(mActiveFragmentTag)).commit();
                        }



                        Log.i(TAG, "Backstack entry count: " + mFragmentManager.getBackStackEntryCount());
                        break;
                }
            }
        });
    }

    class WorkoutExercisesAdapter extends FragmentPagerAdapter {

        public WorkoutExercisesAdapter(FragmentManager fm) {
            super(fm);
            Log.i(TAG, "WorkoutExercisesAdapter constructor called. Size of mExercises: " + mExercisesList.size());
        }

        @Override
        public Fragment getItem(int position) {
            return ExerciseFragment.newInstance(mExercisesList.get(position));
        }

        @Override
        public int getCount() {
            return mExercisesList.size();
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mExercisesList.get(position);
        }
    }

    public String getWorkoutKey(){
        return this.mWorkoutKey;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Log.i(TAG, "Back pressed");
        if (mFragmentManager.getBackStackEntryCount() == 0){
            mExercisesViewPager.setVisibility(View.VISIBLE);
        }
    }

}
