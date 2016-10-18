package com.zonesciences.pyrros;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;

public class NewWorkoutActivity extends AppCompatActivity {
    private static final String REQUIRED = "Required";

    private DatabaseReference mDatabase;

    private EditText mExerciseField;
    private FloatingActionButton mSubmitExercise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_workout);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Workout");

        mExerciseField = (EditText) findViewById(R.id.field_new_exercise);

        mSubmitExercise = (FloatingActionButton) findViewById(R.id.fab_new_workout);
        mSubmitExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitExercise();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void submitExercise(){
        final String exercise = mExerciseField.getText().toString();

        // Exercise name is required
        if (TextUtils.isEmpty(exercise)) {
            mExerciseField.setError(REQUIRED);
            return;
        }

        setEditingEnabled(false);
        Toast.makeText(this, "Submitting...", Toast.LENGTH_SHORT).show();



    }

    private void setEditingEnabled(boolean enabled) {
        mExerciseField.setEnabled(enabled);
        if (enabled) {
            mExerciseField.setVisibility(View.VISIBLE);
        } else {
            mSubmitExercise.setVisibility(View.GONE);
        }
    }
}
