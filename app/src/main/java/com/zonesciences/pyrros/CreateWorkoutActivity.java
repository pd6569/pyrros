package com.zonesciences.pyrros;

import android.app.SearchManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.adapters.ExercisesFilterAdapter;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CreateWorkoutActivity extends BaseActivity {

    RecyclerView mExercisesFilterRecycler;
    ExercisesFilterAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    List<Exercise> mExercises = new ArrayList<>();
    DatabaseReference mDatabase;

    // Menu
    MenuItem mFilterExercisesMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        mExercisesFilterRecycler = (RecyclerView) findViewById(R.id.recycler_exercises_filter);
        mLayoutManager = new LinearLayoutManager(this);
        mExercisesFilterRecycler.setHasFixedSize(true);
        mExercisesFilterRecycler.setLayoutManager(mLayoutManager);

        mDatabase = Utils.getDatabase().getReference();
        mDatabase.child("exercises").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot exercise : dataSnapshot.getChildren()){
                    Exercise e = exercise.getValue(Exercise.class);
                    mExercises.add(e);
                }
                mAdapter = new ExercisesFilterAdapter(getApplicationContext(), mExercises);
                mExercisesFilterRecycler.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_create_workout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Workout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_workout, menu);

        mFilterExercisesMenuItem = menu.findItem(R.id.action_filter_exercises);


        return true;
    }
}
