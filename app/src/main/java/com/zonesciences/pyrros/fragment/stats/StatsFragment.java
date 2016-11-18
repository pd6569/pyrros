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

    private ViewPager mViewPager;
    private FragmentPagerAdapter mPagerAdapter;

    TabLayout mTabLayout;

    //Data for stats
    String mExerciseKey;
    String mUserId;
    List<Exercise> mExercises;
    List<String> mWorkoutKeys;
    List<String> mWorkoutDates;


    public static StatsFragment newInstance(String exerciseKey, String userId, ArrayList<Exercise> exercises, ArrayList<String> workoutKeys, ArrayList<String> workoutDates) {
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_KEY, exerciseKey);
        args.putString(ARG_USER_ID, userId);
        args.putSerializable(ARG_EXERCISES, exercises);
        args.putSerializable(ARG_WORKOUT_KEYS, workoutKeys);
        args.putSerializable(ARG_WORKOUT_DATES, workoutDates);

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

    private class FragmentStatsPagerAdapter extends FragmentPagerAdapter {

        StatsOverviewFragment mStatsOverviewFragment = StatsOverviewFragment.newInstance(mExerciseKey, mUserId, (ArrayList) mExercises, (ArrayList) mWorkoutKeys, (ArrayList) mWorkoutDates);
        StatsRepMaxFragment mStatsRepMaxFragment = new StatsRepMaxFragment();
        StatsGraphFragment mStatsGraphFragment = new StatsGraphFragment();

        private final Fragment[] mFragments = new Fragment[] {
                mStatsOverviewFragment,
                new StatsRepMaxFragment(),
                new StatsGraphFragment()
        };

        String tabTitles[] = new String[]{
                "Overview",
                "Rep-max",
                "Graph"
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
}
