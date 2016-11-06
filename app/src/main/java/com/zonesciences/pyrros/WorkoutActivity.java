package com.zonesciences.pyrros;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.zonesciences.pyrros.fragment.ExerciseFragment;

import java.util.ArrayList;
import java.util.List;

//TODO: Rotating screen to landscape crashes app - likely just fix to portrait orientation for this app

public class WorkoutActivity extends BaseActivity {
    private static String TAG = "WorkoutActivity";

    private static final String WORKOUT_EXERCISES = "Workout Exercises";
    private static final String WORKOUT_ID = "Workout ID";

    ViewPager mExercisesViewPager;
    WorkoutExercisesAdapter mWorkoutExercisesAdapter;

    ArrayList<String> mExercisesList;
    String mWorkoutKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);


        Intent i = getIntent();
        mExercisesList = (ArrayList<String>) i.getSerializableExtra(WORKOUT_EXERCISES);
        mWorkoutKey = i.getStringExtra(WORKOUT_ID);

        Log.i(TAG, "Exercises : " + mExercisesList + " WorkoutKey: " + mWorkoutKey);

        mExercisesViewPager = (ViewPager) findViewById(R.id.viewpager_exercises);
        mWorkoutExercisesAdapter = new WorkoutExercisesAdapter(getSupportFragmentManager());
        mExercisesViewPager.setAdapter(mWorkoutExercisesAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_workout);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Workout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs_workout);
        tabLayout.setupWithViewPager(mExercisesViewPager, false);
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

}
