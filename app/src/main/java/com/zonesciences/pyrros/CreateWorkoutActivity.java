package com.zonesciences.pyrros;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.adapters.SortWorkoutAdapter;
import com.zonesciences.pyrros.fragment.CreateWorkout.CreateWorkoutFragment;
import com.zonesciences.pyrros.fragment.CreateWorkout.ExerciseOptionsFragment;
import com.zonesciences.pyrros.fragment.CreateWorkout.ExercisesListener;
import com.zonesciences.pyrros.fragment.CreateWorkout.SortWorkoutFragment;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.User;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateWorkoutActivity extends BaseActivity {

    private static final String TAG = "CreateWorkoutActivity";

    // Args
    public static final String ARG_WORKOUT_DATE = "WorkoutDate";
    public static final String ARG_CREATE_WORKOUT_FOR_ROUTINE = "CreateWorkoutForRoutine";

    // Extras
    public static final String EXTRA_WORKOUT_EXERCISES = "WorkoutExercises";
    public static final String EXTRA_EXERCISES_CHANGED = "WorkoutExercisesChanged";
    public static final String EXTRA_WORKOUT_TITLE = "WorkoutTitle";


    // Context
    Context mContext;

    // Database, workout and user details
    DatabaseReference mDatabase;
    String mWorkoutKey;
    String mUserId;
    String mUsername;
    String mWorkoutDate;

    // Toolbar, tabs and pager
    Toolbar mToolbar;
    TextView mToolbarTitleText;
    TabLayout mTabLayout;
    ViewPager mViewPager;
    CreateWorkoutPagerAdapter mPagerAdapter;

    // Fragment reference
    Map<Integer, Fragment> mFragmentReferenceMap = new HashMap<>();

    // Current workout list
    ArrayList<Exercise> mWorkoutExercises = new ArrayList<>();

    // Preselected exercises e.g. from Routine Activity
    ArrayList<Exercise> mPreselectedExercises = new ArrayList<>();
    boolean mExercisesChanged = false;
    String mWorkoutTitle = "New Workout";

    // Is this activity to create a workout for a routine?
    boolean mCreateWorkoutForRoutine = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        Intent intent = getIntent();

        mContext = getApplicationContext();

        // if creating new workout from calendar view, need to create on specific date
        if (intent.hasExtra(ARG_WORKOUT_DATE)) {
            mWorkoutDate = intent.getExtras().getString(ARG_WORKOUT_DATE);
        }

        if (intent.hasExtra(ARG_CREATE_WORKOUT_FOR_ROUTINE)){
            mCreateWorkoutForRoutine = intent.getBooleanExtra(ARG_CREATE_WORKOUT_FOR_ROUTINE, false);
            Log.i(TAG, "Create for routine" + mCreateWorkoutForRoutine);
        }

        if (intent.hasExtra(EXTRA_WORKOUT_EXERCISES)){
            mPreselectedExercises = (ArrayList<Exercise>) intent.getSerializableExtra(EXTRA_WORKOUT_EXERCISES);
            Log.i(TAG, "Exercises received: " + mPreselectedExercises.size());

        }

        if (intent.hasExtra(EXTRA_WORKOUT_TITLE)){
            mWorkoutTitle = intent.getStringExtra(EXTRA_WORKOUT_TITLE);

        }



        // Initialise database and get user details
        mDatabase = Utils.getDatabase().getReference();
        mUserId = getUid();
        mDatabase.child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mUsername = user.getUsername();
                if (mFragmentReferenceMap.get(0) != null){
                    CreateWorkoutFragment frag = (CreateWorkoutFragment) mFragmentReferenceMap.get(0);
                    frag.setUsername(mUsername);
                }
                Log.i(TAG, "Username: " + mUsername);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mViewPager = (ViewPager) findViewById(R.id.viewpager_create_workout);
        mPagerAdapter = new CreateWorkoutPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        // If create workout is being opened via Routine Activity and there are already exercises
        // selected, then load straight up into the SortWorkoutFragment view
        if (mCreateWorkoutForRoutine && mPreselectedExercises.size() > 0) {
            mViewPager.setCurrentItem(1);
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar_create_workout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbarTitleText = (TextView) mToolbar.findViewById(R.id.toolbar_title_create_workout);
        mToolbarTitleText.setText(mWorkoutTitle);
        mToolbarTitleText.findViewById(R.id.toolbar_title_create_workout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Title clicked");
            }
        });

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs_create_workout);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "Back Pressed");

        ExerciseOptionsFragment fragment = (ExerciseOptionsFragment) getSupportFragmentManager().findFragmentByTag("exerciseOptionsFragment");
        if (fragment != null) {
            Log.i(TAG, "Exercise Options is Open");
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            mViewPager.setVisibility(View.VISIBLE);
            mTabLayout.setVisibility(View.VISIBLE);

            // reset start workout / save changes action
            SortWorkoutFragment frag = (SortWorkoutFragment) mFragmentReferenceMap.get(1);
            frag.getStartWorkoutAction().setVisible(true);
            frag.getAdapter().notifyDataSetChanged();

            // reset title
            if (mWorkoutTitle != null) {
                mToolbarTitleText.setText(mWorkoutTitle);
            } else {
                mToolbarTitleText.setText("New Workout");
            }
        };

        super.onBackPressed();

    }

    @Override
    public void finish(){
        if (mCreateWorkoutForRoutine){
            Log.i(TAG, "Finish activity and return to routine, pass exercises");

            for (Exercise e : mWorkoutExercises){
                Log.i(TAG, "Exercise: " + e.getName() + " Prescribed Reps : " + e.getPrescribedReps());
            }

            Intent i = new Intent();
            i.putExtra(EXTRA_WORKOUT_EXERCISES, mWorkoutExercises);
            i.putExtra(EXTRA_EXERCISES_CHANGED, mExercisesChanged);
            this.setResult(RESULT_OK, i);
        }
        super.finish();
    }

    public void showExerciseOptionsFragment(final Exercise exercise) {

        mViewPager.setVisibility(View.GONE);
        mTabLayout.setVisibility(View.GONE);

        // Setup toolbar
        mToolbarTitleText.setText("Edit: " + exercise.getName());
        SortWorkoutFragment fragment = (SortWorkoutFragment) mFragmentReferenceMap.get(1);
        fragment.getStartWorkoutAction().setVisible(false);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ExerciseOptionsFragment newFrag = ExerciseOptionsFragment.newInstance(exercise);

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.exercise_options_fragment_container, newFrag, "exerciseOptionsFragment").addToBackStack(null).commit();

        Log.i(TAG, "BackStackCount: " + fm.getBackStackEntryCount());
    }

    // GETTERS AND SETTERS
    public Fragment getFragment (int position){
        return mFragmentReferenceMap.get(position);
    }

    public String getWorkoutDate() {
        return mWorkoutDate;
    }


    public void setWorkoutExercises(ArrayList<Exercise> workoutExercises) {
        this.mWorkoutExercises = workoutExercises;
    }

    /***
     * PAGER ADAPTER CLASS
     */

    class CreateWorkoutPagerAdapter extends FragmentPagerAdapter {

        String[] tabTitles = new String[]{
                "Select Exercises",
                "My workout"
        };

        Fragment[] fragments = new Fragment[]{
                CreateWorkoutFragment.newInstance(mUserId),
                new SortWorkoutFragment()
        };

        public CreateWorkoutPagerAdapter (FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0){
                CreateWorkoutFragment frag = CreateWorkoutFragment.newInstance(mUserId);
                frag.setExercisesListener(new ExercisesListener() {
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
                        mExercisesChanged = true;
                        mWorkoutExercises = exerciseList;
                        boolean isFirstExercise;
                        SortWorkoutFragment sortWorkoutFragment = (SortWorkoutFragment) mFragmentReferenceMap.get(1);
                        sortWorkoutFragment.setWorkoutExercises(mWorkoutExercises);
                        sortWorkoutFragment.getAdapter().notifyDataSetChanged();
                        if (mCreateWorkoutForRoutine) {
                            sortWorkoutFragment.setNewAdapter();
                        }
                        Log.i(TAG, "Exercise changed in adapter, activity notified: " + mWorkoutExercises.size());
                    }

                    @Override
                    public void onExerciseSelected(Exercise exercise) {
                        Log.i(TAG, "onExerciseSelected. Selected: " + exercise.getName());
                        showExerciseOptionsFragment(exercise);

                    }
                });
                frag.setCreateWorkoutForRoutine(mCreateWorkoutForRoutine);

                if (mCreateWorkoutForRoutine){
                    frag.setPreselectedExercises(mPreselectedExercises);
                }

                mFragmentReferenceMap.put(position, frag);
                return frag;
            } else {
                SortWorkoutFragment frag = new SortWorkoutFragment();
                frag.setExercisesListener(new ExercisesListener() {
                    @Override
                    public void onExerciseAdded(Exercise exercise) {
                        Log.i(TAG, "Exercises added");
                    }

                    @Override
                    public void onExercisesEmpty() {
                        Log.i(TAG, "Exercises empty");
                    }

                    @Override
                    public void onExerciseRemoved(Exercise exercise) {
                        Log.i(TAG, "Exercise removed");
                    }

                    @Override
                    public void onExercisesChanged(ArrayList<Exercise> exerciseList) {
                        mExercisesChanged = true;
                        mWorkoutExercises = exerciseList;
                        CreateWorkoutFragment createWorkoutFragment = (CreateWorkoutFragment) mFragmentReferenceMap.get(0);
                        createWorkoutFragment.getAdapter().notifyDataSetChanged();

                        Log.i(TAG, "Exercises changed. New workout exercises list size: " + mWorkoutExercises.size());

                    }

                    @Override
                    public void onExerciseSelected(Exercise exercise) {
                        Log.i(TAG, "onExerciseSelected. Selected: " + exercise.getName());
                        showExerciseOptionsFragment(exercise);
                    }
                });
                frag.setCreateWorkoutForRoutine(mCreateWorkoutForRoutine);
                mFragmentReferenceMap.put(position, frag);
                return frag;
            }
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return tabTitles[position];
        }

        public Fragment getFragment (int position){
            return mFragmentReferenceMap.get(position);
        }
    }

}
