package com.zonesciences.pyrros;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.fragment.CreateWorkout.CreateWorkoutFragment;
import com.zonesciences.pyrros.fragment.CreateWorkout.ExercisesListener;
import com.zonesciences.pyrros.fragment.CreateWorkout.SortWorkoutFragment;
import com.zonesciences.pyrros.fragment.EditWorkout.WorkoutPropertiesFragment;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditWorkoutActivity extends BaseActivity {

    private static String TAG = "EditWorkoutActivity";

    private static final String WORKOUT_EXERCISE_OBJECTS = "WorkoutExerciseObjects";
    private static final String WORKOUT_ID = "Workout ID";

    // Viewpager
    ViewPager mViewPager;
    EditWorkoutAdapter mAdapter;

    // View

    TabLayout mTabLayout;
    FloatingActionButton mFab;

    // Toolbar
    public Toolbar mToolbar;
    MenuItem mUndoAction;
    MenuItem mRedoAction;

    // Data
    ArrayList<Exercise> mExercises;
    String mWorkoutKey;
    String mUserId;

    // Firebase
    DatabaseReference mDatabase;

    // Fragment/fragment reference
    SortWorkoutFragment sortWorkoutFragment;
    Map<Integer, Fragment> mFragmentMap = new HashMap<>();

    // Track workout changes map, store change history
    int mNumExercises;
    int mChangeHisoryIndex = 0;
    List<Exercise> mInitialExercises = new ArrayList<>();
    Map<Integer, List<Exercise>> mWorkoutChangesHistoryMap = new HashMap<>();

    // MenuItem listeners
    MenuItem.OnMenuItemClickListener mUndoListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (mChangeHisoryIndex == 0){
                Snackbar snackbar = Snackbar.make(sortWorkoutFragment.getView(), R.string.all_changes_undone, Snackbar.LENGTH_SHORT);
                snackbar.show();
                return true;
            } else {
                undoWorkoutChanges(sortWorkoutFragment);
                if (!mRedoAction.isVisible()) {
                    mRedoAction.setVisible(true);
                    mRedoAction.setOnMenuItemClickListener(mRedoListener);
                    return true;
                }
                return true;
            }
        }
    };
    MenuItem.OnMenuItemClickListener mRedoListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (mChangeHisoryIndex == mWorkoutChangesHistoryMap.size()) {
                Snackbar snackbar = Snackbar.make(sortWorkoutFragment.getView(), R.string.all_changes_redone, Snackbar.LENGTH_SHORT);
                snackbar.show();
                return true;
            } else {
                redoWorkoutChanges(sortWorkoutFragment);
                return true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_workout);

        Intent i = getIntent();

        mExercises = (ArrayList<Exercise>) i.getSerializableExtra(WORKOUT_EXERCISE_OBJECTS);
        for (Exercise e : mExercises){
            e.setSelected(true);
            mInitialExercises.add(e);
        }

        // Variables to track/store exercise history
        mNumExercises = mExercises.size();

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

                mToolbar.setTitle("Add Exercises");
                mViewPager.setVisibility(View.GONE);
                mTabLayout.setVisibility(View.GONE);
                mFab.setVisibility(View.GONE);

                final CreateWorkoutFragment createWorkoutFragment = CreateWorkoutFragment.newInstance(mUserId);
                createWorkoutFragment.setInEditWorkout(true);
                createWorkoutFragment.setExercisesListener(new ExercisesListener() {


                    @Override
                    public void onExerciseAdded(final Exercise exercise) {
                        Log.i(TAG, "Exercise added");
                        mExercises.add(exercise);
                        mChangeHisoryIndex++;
                        mNumExercises++;

                        List<Exercise> newList = new ArrayList<>();
                        newList.addAll(mExercises);
                        mWorkoutChangesHistoryMap.put(mChangeHisoryIndex, newList);

                    }

                    @Override
                    public void onExercisesEmpty() {

                    }

                    @Override
                    public void onExerciseRemoved(final Exercise exercise) {

                        Log.i(TAG, "Remove exercise: " + exercise.getName());
                        int indexRemove = 0;
                        for (int i = 0; i < mExercises.size(); i++) {
                            if (mExercises.get(i).getName().equals(exercise.getName())) {
                                indexRemove = i;
                                break;
                            }
                        }

                        mExercises.remove(indexRemove);
                        mChangeHisoryIndex++;
                        mNumExercises--;

                        List<Exercise> newList = new ArrayList<>();
                        newList.addAll(mExercises);
                        mWorkoutChangesHistoryMap.put(mChangeHisoryIndex, newList);

                    }

                    @Override
                    public void onExercisesChanged(ArrayList<Exercise> exerciseList) {

                    }
                });

                mDatabase.child("exercises").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Exercise> allExercises = new ArrayList<Exercise>();
                        for (DataSnapshot exercise : dataSnapshot.getChildren()){
                            Exercise e = exercise.getValue(Exercise.class);
                            allExercises.add(e);
                        }
                        for (Exercise e : allExercises){
                            for (Exercise exercise : mExercises){
                                if (exercise.getName().equals(e.getName())){
                                    e.setSelected(true);
                                    Log.i(TAG, "Found exercise: " + e.getName() + " selected: " + e.isSelected);
                                }
                            }
                        }

                        createWorkoutFragment.setAllExercises(allExercises);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.edit_workout_fragment_container, createWorkoutFragment, null).addToBackStack(null);
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
                sortWorkoutFragment = SortWorkoutFragment.newInstance(mExercises, true);
                sortWorkoutFragment.setMultiSelectDisabled(true);
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
                        mChangeHisoryIndex++;
                        if (!mUndoAction.isVisible()) {
                            mUndoAction.setVisible(true);
                            mUndoAction.setOnMenuItemClickListener(mUndoListener);
                        }
                        List<Exercise> newList = new ArrayList<>();
                        newList.addAll(mExercises);
                        mWorkoutChangesHistoryMap.put(mChangeHisoryIndex, newList);


                        if (mNumExercises > exerciseList.size()){
                            Log.i(TAG, "Exercise(s) removed. Store new exercise list in map");

                            Snackbar snackbar = Snackbar.make(sortWorkoutFragment.getView(), R.string.exercise_deleted, Snackbar.LENGTH_LONG).setAction(R.string.action_undo, new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    undoWorkoutChanges(sortWorkoutFragment);
                                }
                            });
                            snackbar.show();
                            mNumExercises = mExercises.size();
                        } else if (mNumExercises == exerciseList.size()){
                            Log.i(TAG, "Exercises reordered");

                            Snackbar snackbar = Snackbar.make(sortWorkoutFragment.getView(), R.string.exercises_changed, Snackbar.LENGTH_LONG).setAction(R.string.action_undo, new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    undoWorkoutChanges(sortWorkoutFragment);
                                }
                            });
                            snackbar.show();
                            mNumExercises = mExercises.size();

                            }


                        mExercises = exerciseList;

                    }
                });
                fragment = sortWorkoutFragment;
            } else if (position == 1){
                WorkoutPropertiesFragment workoutPropertiesFragment = WorkoutPropertiesFragment.newInstance(mUserId, mWorkoutKey);
                fragment = workoutPropertiesFragment;
            }
            mFragmentMap.put(position, fragment);
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

        public Fragment getFragment(int position){
            return mFragmentMap.get(position);
        }



    }


    private void undoWorkoutChanges(SortWorkoutFragment sortWorkoutFragment){
        if (mChangeHisoryIndex > 1){
            Log.i(TAG, "Not the first change. Step backwards. Map contains changes: " + mWorkoutChangesHistoryMap.size());
            mExercises.clear();
            mExercises.addAll(mWorkoutChangesHistoryMap.get(mChangeHisoryIndex - 1));
        } else {
            Log.i(TAG, "First change. Reset exercises. Map contains changes: " + mWorkoutChangesHistoryMap.size());
            mExercises.clear();
            mExercises.addAll(mInitialExercises);
        }
        if (!mRedoAction.isVisible()){
            mRedoAction.setVisible(true);
            mRedoAction.setOnMenuItemClickListener(mRedoListener);
        }
        sortWorkoutFragment.setWorkoutExercises(mExercises);
        sortWorkoutFragment.getAdapter().notifyDataSetChanged();
        mNumExercises = mExercises.size();
        if (mChangeHisoryIndex > 0) {
            mChangeHisoryIndex--;
        }
    }

    private void redoWorkoutChanges (SortWorkoutFragment sortWorkoutFragment){

        Log.i(TAG, "Step forwards. Map contains changes: " + mWorkoutChangesHistoryMap.size());
        if (mChangeHisoryIndex == mWorkoutChangesHistoryMap.size()){
            Log.i(TAG, "Cannot go further, reached limit");
            return;
        }
        mExercises.clear();
        mExercises.addAll(mWorkoutChangesHistoryMap.get(mChangeHisoryIndex + 1));

        sortWorkoutFragment.setWorkoutExercises(mExercises);
        sortWorkoutFragment.getAdapter().notifyDataSetChanged();
        mNumExercises = mExercises.size();
        mChangeHisoryIndex++;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.i(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_edit_workout, menu);
        mUndoAction = menu.findItem(R.id.action_undo);
        mRedoAction = menu.findItem(R.id.action_redo);
        if (mWorkoutChangesHistoryMap.size() > 0 && getSupportFragmentManager().getBackStackEntryCount() < 1){
            mUndoAction.setVisible(true);
            mUndoAction.setOnMenuItemClickListener(mUndoListener);

            mRedoAction.setVisible(true);
            mRedoAction.setOnMenuItemClickListener(mRedoListener);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0){
                    Log.i(TAG, "In add exercise view");
                    onBackPressed();
                } else {
                    writeWorkoutChanges();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause called. Write workout changes");
        for (Exercise e: mExercises){
            if (!e.hasExerciseId()){
                e.setExerciseId(UUID.randomUUID().toString());
            }
        }
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

    @Override
    public void onBackPressed(){
        if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            mTabLayout.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.VISIBLE);

            mToolbar.setTitle("Edit Workout");

            SortWorkoutFragment sortWorkoutFragment = (SortWorkoutFragment) mFragmentMap.get(0);
            sortWorkoutFragment.setWorkoutExercises(mExercises);
            sortWorkoutFragment.getAdapter().notifyDataSetChanged();
        }
        super.onBackPressed();
    }

}
