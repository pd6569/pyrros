package com.zonesciences.pyrros;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zonesciences.pyrros.fragment.CreateWorkout.CreateWorkoutFragment;
import com.zonesciences.pyrros.fragment.CreateWorkout.ExercisesListener;
import com.zonesciences.pyrros.fragment.CreateWorkout.SortWorkoutFragment;
import com.zonesciences.pyrros.fragment.EditWorkout.WorkoutPropertiesFragment;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditWorkoutActivity extends BaseActivity {

    private static String TAG = "EditWorkoutActivity";

    private static final String WORKOUT_EXERCISE_OBJECTS = "WorkoutExerciseObjects";
    private static final String WORKOUT_ID = "Workout ID";

    // Viewpager
    ViewPager mViewPager;
    EditWorkoutAdapter mAdapter;

    // View
    public Toolbar mToolbar;
    TabLayout mTabLayout;
    FloatingActionButton mFab;

    // Data
    ArrayList<Exercise> mExercises;
    String mWorkoutKey;
    String mUserId;

    // Firebase
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout);

        Intent i = getIntent();

        mExercises= (ArrayList<Exercise>) i.getSerializableExtra(WORKOUT_EXERCISE_OBJECTS);
        mWorkoutKey = i.getStringExtra(WORKOUT_ID);
        mUserId = getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_edit_workout);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Edit Workout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.viewpager_edit_workout);

        mAdapter = new EditWorkoutAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs_edit_workout);
        mTabLayout.setupWithViewPager(mViewPager, false);

        mFab = (FloatingActionButton) findViewById(R.id.fab_add_exercise);
        mFab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                FragmentManager fm = getSupportFragmentManager();

                mViewPager.setVisibility(View.GONE);

                CreateWorkoutFragment createWorkoutFragment = CreateWorkoutFragment.newInstance(mUserId);


                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.edit_workout_fragment_container, createWorkoutFragment, null);
                ft.commit();

            }
        });

    }

    class EditWorkoutAdapter extends FragmentPagerAdapter {

        String [] mTitles = new String[] {
                "Order",
                "Properties"
        };

        public EditWorkoutAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new Fragment();
            if (position == 0){
                SortWorkoutFragment sortWorkoutFragment = SortWorkoutFragment.newInstance(mExercises, true);
                sortWorkoutFragment.setExercisesListener(new ExercisesListener() {
                    @Override
                    public void onExerciseAdded(Exercise exercise) {

                    }

                    @Override
                    public void onExercisesEmpty() {

                    }

                    @Override
                    public void onExerciseRemoved(Exercise exercise) {

                    }

                    @Override
                    public void onExercisesChanged(ArrayList<Exercise> exerciseList) {
                        mExercises = exerciseList;
                    }
                });
                fragment = sortWorkoutFragment;
            } else if (position == 1){
                WorkoutPropertiesFragment workoutPropertiesFragment = WorkoutPropertiesFragment.newInstance(mUserId, mWorkoutKey);
                fragment = workoutPropertiesFragment;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mTitles[position];
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                writeWorkoutChanges();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause called. Write workout changes");
        writeWorkoutChanges();
    }

    private void writeWorkoutChanges(){
        // Clear workout information
        mDatabase.child("workout-exercises").child(mWorkoutKey).setValue(null);
        mDatabase.child("user-workout-exercises").child(mUserId).child(mWorkoutKey).setValue(null);

        // Write new values
        Map<String, Object> childUpdates = new HashMap<String, Object>();
        for (Exercise e : mExercises){
            childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + e.getName(), e.toMap());
            childUpdates.put("/user-workout-exercises/" + mUserId + "/" + mWorkoutKey + "/" + e.getName(), e.toMap());
        }
        mDatabase.updateChildren(childUpdates);

        finish();
    }


}
