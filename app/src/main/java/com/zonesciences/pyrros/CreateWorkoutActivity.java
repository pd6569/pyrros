package com.zonesciences.pyrros;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
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

    // Database
    DatabaseReference mDatabase;

    // View
    Spinner mBodypartSpinner;
    Spinner mEquipmentSpinner;
    RecyclerView mExercisesFilterRecycler;
    LinearLayoutManager mLayoutManager;


    // Adapter
    ExercisesFilterAdapter mAdapter;

    // Menu


    String[] mBodyPartsArray;
    String[] mEquipmentArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        mContext = getApplicationContext();
        mExercisesFilterRecycler = (RecyclerView) findViewById(R.id.recycler_exercises_filter);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mExercisesFilterRecycler.setHasFixedSize(true);
        mExercisesFilterRecycler.setLayoutManager(mLayoutManager);

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
                Toast.makeText(getApplicationContext(), "Filter selected: " + mBodyPartsArray[pos], Toast.LENGTH_SHORT).show();
                if (pos == 0) {
                    Log.i(TAG, "Filter: " + mBodyPartsArray[pos].toLowerCase());
                    mFilteredExercises.clear();
                    mFilteredExercises.addAll(mAllExercises);
                } else {
                    Log.i(TAG, "Filter: " + mBodyPartsArray[pos].toLowerCase() + " mAllExercises size: " + mAllExercises.size());
                    List<Exercise> list = new ArrayList<Exercise>();
                    for (Exercise e : mAllExercises) {
                        Log.i(TAG, "Filter: " + mBodyPartsArray[pos].toLowerCase() + " Exercise: " + e.getName() + " bodypart: " + e.getMuscleGroup());
                        if (e.getMuscleGroup().toLowerCase().equals(mBodyPartsArray[pos].toLowerCase())) {
                            Log.i(TAG, "Found exercises for " + mBodyPartsArray[pos]);
                            list.add(e);
                        }
                    }
                    Log.i(TAG, "mFilteredExercises SIZE: " + mFilteredExercises.size() + " list size: " + list.size() + " mAllExercises: " + mAllExercises.size());
                    mFilteredExercises.clear();
                    mFilteredExercises.addAll(list);
                    /*mFilteredExercises = list;*/

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
        Log.i(TAG, "onQueryTextChange");
        newText = newText.toLowerCase();
        ArrayList<Exercise> newList = new ArrayList<>();
        for (Exercise exercise : mFilteredExercises){
            String name = exercise.getName().toLowerCase();
            if(name.contains(newText)){
                newList.add(exercise);
            }
        }

        setFilter(newList);
        return true;
    }

    public void setFilter(ArrayList<Exercise> newList){
        mFilteredExercises.clear();
        mFilteredExercises.addAll(newList);
        mAdapter.notifyDataSetChanged();
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
