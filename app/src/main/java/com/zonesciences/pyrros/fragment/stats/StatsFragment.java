package com.zonesciences.pyrros.fragment.stats;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    private static final String TAG = "StatsFragment";

    private static final String ARG_EXERCISE_KEY = "ExerciseKey";
    private static final String ARG_USER_ID = "UserId";
    private static final String ARG_EXERCISES = "Exercises";
    private static final String ARG_WORKOUT_KEYS = "WorkoutKeys";
    private static final String ARG_WORKOUT_DATES = "WorkoutDates";
    private static final String ARG_CURRENT_WORKOUT_KEY = "CurrentWorkoutKey";

    private ViewPager mViewPager;
    private FragmentPagerAdapter mPagerAdapter;

    TabLayout mTabLayout;

    //Data for stats
    String mExerciseKey;
    String mUserId;
    List<Exercise> mExercises;
    List<String> mWorkoutKeys;
    List<String> mWorkoutDates;
    String mCurrentWorkoutKey;

    // Data for child fragments
    int mCurrentFilter;

    // Fragments
    StatsOverviewFragment mStatsOverviewFragment;
    StatsRepMaxFragment mStatsRepMaxFragment;
    StatsGraphFragment mStatsGraphFragment;
    StatsCompareFragment mStatsCompareFragment;

    // State information
    private static final String STATE_CURRENT_FILTER = "CurrentFilter";

    public static StatsFragment newInstance(String exerciseKey, String userId, ArrayList<Exercise> exercises, ArrayList<String> workoutKeys, ArrayList<String> workoutDates, String currentWorkoutKey) {
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_KEY, exerciseKey);
        args.putString(ARG_USER_ID, userId);
        args.putSerializable(ARG_EXERCISES, exercises);
        args.putSerializable(ARG_WORKOUT_KEYS, workoutKeys);
        args.putSerializable(ARG_WORKOUT_DATES, workoutDates);
        args.putString(ARG_CURRENT_WORKOUT_KEY, currentWorkoutKey);

        StatsFragment fragment = new StatsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        Bundle bundle = getArguments();
        mExerciseKey = bundle.getString(ARG_EXERCISE_KEY);
        mUserId = bundle.getString(ARG_USER_ID);
        mExercises = (ArrayList) bundle.getSerializable(ARG_EXERCISES);
        mWorkoutKeys = (ArrayList) bundle.getSerializable(ARG_WORKOUT_KEYS);
        mWorkoutDates = (ArrayList) bundle.getSerializable(ARG_WORKOUT_DATES);
        mCurrentWorkoutKey = bundle.getString(ARG_CURRENT_WORKOUT_KEY);


        // Create fragments
        mStatsOverviewFragment = StatsOverviewFragment.newInstance(mExerciseKey, mUserId, (ArrayList) mExercises, (ArrayList) mWorkoutKeys, (ArrayList) mWorkoutDates, mCurrentWorkoutKey);
        mStatsRepMaxFragment = new StatsRepMaxFragment();
        mStatsGraphFragment = new StatsGraphFragment();
        mStatsCompareFragment = new StatsCompareFragment();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View rootView =  inflater.inflate(R.layout.fragment_stats, container, false);

        mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_stats);

        mPagerAdapter = new FragmentStatsPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        mTabLayout = (TabLayout) rootView.findViewById(R.id.sliding_tabs_stats);
        mTabLayout.setupWithViewPager(mViewPager, false);

        return rootView;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    public class FragmentStatsPagerAdapter extends FragmentPagerAdapter {

        public Fragment[] mFragments = new Fragment[] {
                mStatsOverviewFragment,
                mStatsRepMaxFragment,
                mStatsGraphFragment,
                mStatsCompareFragment
        };

        String tabTitles[] = new String[]{
                "Overview",
                "Rep-max",
                "Graph",
                "Compare"
        };

        public FragmentStatsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return tabTitles[position];
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState){
        Log.i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

}
