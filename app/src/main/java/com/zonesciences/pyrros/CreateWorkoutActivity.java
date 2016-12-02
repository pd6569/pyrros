package com.zonesciences.pyrros;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.adapters.ExercisesFilterAdapter;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CreateWorkoutActivity extends BaseActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "CreateWorkout";

    Context mContext;

    // Data
    List<Exercise> mAllExercises = new ArrayList<>();
    List<Exercise> mFilteredExercises = new ArrayList<>();
    List<List<Exercise>> mFilterHistory = new ArrayList<>();
    List<Exercise> mWorkoutExercises = new ArrayList<>();

    // Database
    DatabaseReference mDatabase;

    // View
    Spinner mBodypartSpinner;
    Spinner mEquipmentSpinner;
    RecyclerView mExercisesFilterRecycler;
    LinearLayoutManager mLayoutManager;
    DividerItemDecoration mDivider;


    // Adapter
    ExercisesFilterAdapter mAdapter;

    // Menu
    MenuItem mStartWorkoutAction;


    String[] mBodyPartsArray;
    String[] mEquipmentArray;

    // Search Filter
    int mPreviousSearchStringLength;

    // Spinner Filter
    int mCurrentBodyPartFilterIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        mContext = getApplicationContext();
        mExercisesFilterRecycler = (RecyclerView) findViewById(R.id.recycler_exercises_filter);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mExercisesFilterRecycler.setHasFixedSize(true);
        mExercisesFilterRecycler.setLayoutManager(mLayoutManager);
        mDivider = new DividerItemDecoration(mExercisesFilterRecycler.getContext(), mLayoutManager.getOrientation());
        mExercisesFilterRecycler.addItemDecoration(mDivider);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_create_workout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Workout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBodyPartsArray = getResources().getStringArray(R.array.bodyparts);
        mEquipmentArray = getResources().getStringArray(R.array.equipment);

        mDatabase = Utils.getDatabase().getReference();
        mDatabase.child("exercises").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot exercise : dataSnapshot.getChildren()){
                    Exercise e = exercise.getValue(Exercise.class);
                    mAllExercises.add(e);
                }
                mFilteredExercises.addAll(mAllExercises);
                mAdapter = new ExercisesFilterAdapter(mContext, mFilteredExercises);
                mAdapter.setExercisesListener(new ExercisesFilterAdapter.ExercisesListener() {
                    @Override
                    public void onExercisesAdded() {
                        Log.i(TAG, "Exercise Added");
                        if (!mStartWorkoutAction.isVisible()){
                            mStartWorkoutAction.setVisible(true);
                        }
                    }

                    @Override
                    public void onExercisesEmpty() {
                        Log.i(TAG, "Exercises Empty");
                        mStartWorkoutAction.setVisible(false);
                    }

                    @Override
                    public void onExerciseRemoved() {

                    }
                });
                mExercisesFilterRecycler.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mBodypartSpinner = (Spinner) findViewById(R.id.spinner_bodypart_filter);
        ArrayAdapter<CharSequence> bodypartAdapter = ArrayAdapter.createFromResource(this, R.array.bodyparts, R.layout.simple_spinner_item);
        bodypartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBodypartSpinner.setAdapter(bodypartAdapter);
        mBodypartSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mFilterHistory.clear();
                mCurrentBodyPartFilterIndex = pos;
                Toast.makeText(getApplicationContext(), "Filter selected: " + mBodyPartsArray[pos], Toast.LENGTH_SHORT).show();
                if (pos == 0) {
                    Log.i(TAG, "Filter: " + mBodyPartsArray[pos].toLowerCase());
                    mFilteredExercises.clear();
                    mFilteredExercises.addAll(mAllExercises);
                } else {
                    Log.i(TAG, "Filter: " + mBodyPartsArray[pos].toLowerCase() + " mAllExercises size: " + mAllExercises.size());

                    List<Exercise> list = getExercisesForFilter(pos);

                    Log.i(TAG, "mFilteredExercises SIZE: " + mFilteredExercises.size() + " list size: " + list.size() + " mAllExercises: " + mAllExercises.size());

                    mFilteredExercises.clear();
                    mFilteredExercises.addAll(list);
                }

                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mEquipmentSpinner = (Spinner) findViewById(R.id.spinner_equipment_filter);
        ArrayAdapter<CharSequence> equipmentAdapter = ArrayAdapter.createFromResource(this, R.array.equipment, R.layout.simple_spinner_item);
        equipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEquipmentSpinner.setAdapter(equipmentAdapter);
        mEquipmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Toast.makeText(getApplicationContext(), "Filter selected: " + mEquipmentArray[pos], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_workout, menu);
        mStartWorkoutAction = menu.findItem(R.id.action_start_workout);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        boolean addHistory = true;
        int length = newText.length();
        List<Exercise> exercisesToSearch = new ArrayList<>();

        if (length == 0){
            mFilterHistory.clear();
        }

        if(mFilterHistory.isEmpty()){
            if (mCurrentBodyPartFilterIndex == 0) {
                mFilterHistory.add(mAllExercises);
            } else {
                mFilterHistory.add(getExercisesForFilter(mCurrentBodyPartFilterIndex));
            }
            mAdapter.notifyDataSetChanged();
        }

        Log.i(TAG, "onQueryTextChange. newText Length: " + length + " previous Text Length: " + mPreviousSearchStringLength);
        if (length < mPreviousSearchStringLength) {
            addHistory = false;
        }

        exercisesToSearch = mFilterHistory.get(mFilterHistory.size()-1);

        newText = newText.toLowerCase();
        ArrayList<Exercise> newList = new ArrayList<>();
        for (Exercise exercise : exercisesToSearch){
            String name = exercise.getName().toLowerCase();
            if(name.contains(newText)){
                newList.add(exercise);
            }
        }

        if (addHistory) {
            mFilterHistory.add(newList);
        } else {
            if(mFilterHistory.size()>1){
                mFilterHistory.remove(mFilterHistory.size()-1);
            }
        }

        setFilter(newList);
        mPreviousSearchStringLength = newText.length();
        return true;
    }

    public void setFilter(ArrayList<Exercise> newList){

        mFilteredExercises.clear();
        mFilteredExercises.addAll(newList);

        Log.i(TAG, "mFilter history added: " + mFilterHistory.size());

        mAdapter.notifyDataSetChanged();
    }

    public List<Exercise> getExercisesForFilter(int index){
        List<Exercise> filteredList = new ArrayList<>();
        for (Exercise e : mAllExercises) {
            Log.i(TAG, "Filter: " + mBodyPartsArray[index].toLowerCase() + " Exercise: " + e.getName() + " bodypart: " + e.getMuscleGroup());
            if (e.getMuscleGroup().toLowerCase().equals(mBodyPartsArray[index].toLowerCase())) {
                Log.i(TAG, "Found exercises for " + mBodyPartsArray[index]);
                filteredList.add(e);
            }
        }
        return filteredList;
    }


    /*public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_filter_exercises:
               *//* PopupMenu menu = new PopupMenu(getApplicationContext(), mFilterExercisesMenuItem);
                menu.getMenuInflater().inflate(R.menu.menu_stats_overview_filter_popup, menu.getMenu());

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return true;
                    }
                });
                menu.show();*//*
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }

    }*/

}
