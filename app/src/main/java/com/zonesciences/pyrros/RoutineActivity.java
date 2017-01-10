package com.zonesciences.pyrros;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ViewStubCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.zonesciences.pyrros.fragment.Routine.RoutineDetailsFragment;
import com.zonesciences.pyrros.fragment.Routine.RoutineLoadListener;
import com.zonesciences.pyrros.fragment.Routine.RoutineSelectedListener;
import com.zonesciences.pyrros.fragment.Routine.ViewRoutinesFragment;
import com.zonesciences.pyrros.fragment.Routine.WorkoutChangedListener;
import com.zonesciences.pyrros.models.Routine;
import com.zonesciences.pyrros.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: touch listener for reordering workouts


public class RoutineActivity extends BaseActivity {

    private static final String TAG = "CreateRoutineAcctivity";

    // Fragment constants
    public static final int FRAGMENT_CREATE_ROUTINE = 1;
    public static final int FRAGMENT_VIEW_ROUTINES = 2;

    // Extas
    public static final String EXTRA_FRAGMENT_TO_LOAD = "RoutineFragmentToLoad";

    // Fragments
    RoutineDetailsFragment mRoutineDetailsFragment;
    ViewRoutinesFragment mViewRoutineFragment;

    // View
    TextView mRoutineNameTextView;
    EditText mRoutineNameEditText;

    // Menu items / toolbar
    Toolbar mToolbar;
    MenuItem mEditRoutineMenuItem;
    MenuItem mSaveRoutineMenuItem;

    // Fragment to load
    int mFragmentToLoad = FRAGMENT_CREATE_ROUTINE;

    // Routines
    List<Routine> mRoutinesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_routine);

        Intent intent = getIntent();
        mFragmentToLoad = intent.getIntExtra(EXTRA_FRAGMENT_TO_LOAD, FRAGMENT_CREATE_ROUTINE);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_create_routine);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        createViewRoutinesFragment();
        createRoutineDetailFragment();


        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (mFragmentToLoad == FRAGMENT_CREATE_ROUTINE) {
            ft.add(R.id.routine_fragment_container, mRoutineDetailsFragment);
        } else if (mFragmentToLoad == FRAGMENT_VIEW_ROUTINES){
            ft.add(R.id.routine_fragment_container, mViewRoutineFragment);
        }
        ft.commit();

        mRoutineNameTextView = (TextView) findViewById(R.id.toolbar_title_create_routine);
        mRoutineNameEditText = (EditText) findViewById(R.id.toolbar_title_edit_text);

        if (mFragmentToLoad == FRAGMENT_CREATE_ROUTINE) {
            setToolbarTitleClickListener();
        } else {
            mRoutineNameTextView.setText("Routines");
        }

    }
    private void createRoutineDetailFragment(){
        mRoutineDetailsFragment = RoutineDetailsFragment.newInstance();
        mRoutineDetailsFragment.setOnWorkoutChangedListener(new WorkoutChangedListener() {
            @Override
            public void onWorkoutAdded() {
                Log.i(TAG, "Workout added");
            }

            @Override
            public void onWorkoutRemoved() {
                Log.i(TAG, "Workout removed");
            }

            @Override
            public void onWorkoutChanged() {
                Log.i(TAG, "Workout changed");
            }
        });
        mRoutineDetailsFragment.setNewRoutine(true);
    }

    private void createViewRoutinesFragment(){
        mViewRoutineFragment = ViewRoutinesFragment.newInstance(new RoutineLoadListener() {
            @Override
            public void onLoadStart() {
                Log.i(TAG, "Start loading routines");
                showProgressDialog();
            }

            @Override
            public void onLoadComplete(List<Routine> routinesList) {
                hideProgressDialog();
                mRoutinesList = routinesList;
                Log.i(TAG, "Complete loading routines. Number of routines: " + mRoutinesList.size());
            }
        }, new RoutineSelectedListener() {
            @Override
            public void onRoutineSelected(Routine routine) {
                Log.i(TAG, "Routine selected: " + routine.getName() + " Number of workouts: " + routine.getWorkoutsList().size());

                if (mRoutineDetailsFragment == null){
                    createRoutineDetailFragment();
                }

                // Change toolbar to display routine name and edit icons
                mRoutineNameTextView.setText(routine.getName());
                mEditRoutineMenuItem.setVisible(true);
                setToolbarTitleClickListener();

                mRoutineDetailsFragment.setRoutine(routine);
                mRoutineDetailsFragment.setShowAddWorkoutPanel(false);
                mRoutineDetailsFragment.setNewRoutine(false);
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.routine_fragment_container, mRoutineDetailsFragment).addToBackStack(null).commit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_routine, menu);
        mEditRoutineMenuItem = menu.findItem(R.id.action_edit_routine);
        mSaveRoutineMenuItem = menu.findItem(R.id.action_save_routine);

        if (mFragmentToLoad == FRAGMENT_VIEW_ROUTINES){
            mEditRoutineMenuItem.setVisible(false);
            mSaveRoutineMenuItem.setVisible(false);
            menu.findItem(R.id.action_clear_routines).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_edit_routine:
                Log.i(TAG, "Edit workout");
                editRoutineName();
                return true;

            case R.id.action_save_routine:
                Log.i(TAG, "Save routine");
                saveRoutine();
                return true;
            case R.id.action_clear_routines:
                DatabaseReference database = Utils.getDatabase().getReference();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/routines/", null);
                childUpdates.put("/user-routines/", null);
                childUpdates.put("/routine-workouts/", null);
                childUpdates.put("/user-routine-workouts/", null);
                childUpdates.put("/routine-workout-exercises/", null);
                childUpdates.put("/user-routine-workout-exercises/", null);
                database.updateChildren(childUpdates);

            default:
                break;
        }
        return false;
    }

    private void editRoutineName(){
        mRoutineNameTextView.setVisibility(View.GONE);
        mRoutineNameEditText.setVisibility(View.VISIBLE);
        mRoutineNameEditText.setText(mRoutineNameTextView.getText());
        mRoutineNameEditText.requestFocus();
        mEditRoutineMenuItem.setVisible(false);
        mSaveRoutineMenuItem.setVisible(true);
    }

    private void saveRoutine(){
        String routineName = mRoutineNameEditText.getText().toString();
        mRoutineNameTextView.setText(routineName);
        mRoutineNameTextView.setVisibility(View.VISIBLE);
        mRoutineNameEditText.setVisibility(View.GONE);
        mEditRoutineMenuItem.setVisible(true);
        mSaveRoutineMenuItem.setVisible(false);

        // update routine object and notify that change has been made
        mRoutineDetailsFragment.getRoutine().setName(routineName);
        mRoutineDetailsFragment.setRoutineChanged(true);
    }

    private void setToolbarTitleClickListener(){
        mRoutineNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editRoutineName();

            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");

        if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            // Revert toolbar and remove click listener
            mRoutineNameTextView.setOnClickListener(null);
            if (mRoutineNameEditText.getVisibility() == View.VISIBLE){
                mRoutineNameEditText.setVisibility(View.GONE);
                mSaveRoutineMenuItem.setVisible(false);
                mRoutineNameTextView.setVisibility(View.VISIBLE);
            }
            mRoutineNameTextView.setText("Routines");
            mEditRoutineMenuItem.setVisible(false);
        }

        super.onBackPressed();
    }
}
