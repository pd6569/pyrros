package com.zonesciences.pyrros;

import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.User;
import com.zonesciences.pyrros.models.Workout;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class NewWorkoutActivity extends BaseActivity {
    private static final String REQUIRED = "Required";
    private static final String TAG = "NewWorkoutActivity";

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

        // [START single_value_read
        final String userId = getUid();

        //ListenerForSingleValue event is useful for data that needs to be loaded once and isn't expected
        //to change frequently or require active listening.

        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get user value
                User user = dataSnapshot.getValue(User.class);

                // [START_EXCLUDE]
                if (user==null){
                    // User is null, error out
                    Log.e(TAG, "User " + userId + " is unexpectedly null");
                    Toast.makeText(NewWorkoutActivity.this, "Error: could not fetch user.", Toast.LENGTH_SHORT).show();
                } else {
                    //create new workout
                    writeNewWorkout(userId, user.username, exercise);
                }

                //Finish this Activity, back to the stream
                setEditingEnabled(true);
                finish();
                // [END_EXCLUDE]

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, "getUser:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                setEditingEnabled(true);
                // [END_EXCLUDE]
            }
            // [END single_value_read]
        });

    }

    // [START write_fan_out]
    private void writeNewWorkout(String userId, String username, String exercise) {
        //Create new workout at /user-workouts/$user-id/$postid and at
        // /workouts/$workoutid simultaneously


        String workoutKey = mDatabase.child("workouts").push().getKey();
        String exerciseKey = mDatabase.child("user-exercises").push().getKey();

        Workout workout = new Workout(userId, username, "No title", true, exerciseKey);

        Map<String, Object> workoutValues = workout.toMap();

        //Create new exercise object
        Exercise exerciseName = new Exercise(exercise);
        Map<String, Object> exerciseValues = exerciseName.toMap();

        //Create map object to push updates to nodes
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workouts/" + workoutKey, workoutValues);
        childUpdates.put("/user-workouts/" + userId + "/" + workoutKey, workoutValues);
        childUpdates.put("/user-exercises/" + userId + "/" + exerciseKey, exerciseValues);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]

    private void setEditingEnabled(boolean enabled) {
        mExerciseField.setEnabled(enabled);
        if (enabled) {
            mExerciseField.setVisibility(View.VISIBLE);
        } else {
            mSubmitExercise.setVisibility(View.GONE);
        }
    }
}
