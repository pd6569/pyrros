package com.zonesciences.pyrros;

import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zonesciences.pyrros.fragment.CreateWorkout.ExercisesListener;
import com.zonesciences.pyrros.fragment.CreateWorkout.SortWorkoutFragment;
import com.zonesciences.pyrros.fragment.EditWorkout.WorkoutOrderFragment;
import com.zonesciences.pyrros.fragment.EditWorkout.WorkoutPropertiesFragment;
import com.zonesciences.pyrros.fragment.ExerciseFragment;
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
                        Map<String, Object> childUpdates = new HashMap<String, Object>();
                        for (Exercise e : mExercises){
                            childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + e.getName() + "/order/", e.getOrder());
                            childUpdates.put("/user-workout-exercises/" + mUserId + "/" + mWorkoutKey + "/" + e.getName() + "/order/", e.getOrder());
                        }
                        mDatabase.updateChildren(childUpdates);
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
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
