package com.zonesciences.pyrros;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.User;
import com.zonesciences.pyrros.models.Workout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NewWorkoutActivity extends BaseActivity {
    private static final String REQUIRED = "Required";
    private static final String TAG = "NewWorkoutActivity";

    //need reference to database to read/write data.
    private DatabaseReference mDatabase;

    private EditText mExerciseField;
    private ListView mListView;
    private FloatingActionButton mSubmitExercise;

    private int numExercises = 0;
    private String mWorkoutKey;
    private Workout mCurrentWorkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_workout);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Workout");

        mExerciseField = (EditText) findViewById(R.id.field_new_exercise);

        mListView = (ListView) findViewById(R.id.listview_exercises);


        mSubmitExercise = (FloatingActionButton) findViewById(R.id.fab_new_workout);
        mSubmitExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExercise();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void addExercise(){
        final String exercise = mExerciseField.getText().toString();

        // Exercise name is required
        if (TextUtils.isEmpty(exercise)) {
            mExerciseField.setError(REQUIRED);
            return;
        }

        Toast.makeText(this, "Submitting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read
        final String userId = getUid();

        //ListenerForSingleValue event is useful for data that needs to be loaded once and isn't expected
        //to change frequently or require active listening.
        //Listener under "users" path - checks that user exists, if exists then writes new workout
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {

            // onDataChange() method reads static snapshot of contents as they exist at time of event
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
                    if (mCurrentWorkout == null) {
                        writeNewWorkout(userId, user.username, exercise);
                    } else {
                        addNewExercise(userId, user.username, mWorkoutKey, exercise);
                    }
                }


                // [END_EXCLUDE]

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, "getUser:onCancelled", databaseError.toException());
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }
            // [END single_value_read]
        });

    }


    // [START write_fan_out]
    private void writeNewWorkout(String userId, String username, String exercise) {
        //Create new workout at /user-workouts/$user-id/$workout-id and at
        // /workouts/$workoutid simultaneously


        mWorkoutKey = mDatabase.child("workouts").push().getKey();
        String exerciseKey = mDatabase.child("user-exercises").push().getKey();

        //Create new workout object and map values. Add exercise via exerciseKey.
        mCurrentWorkout = new Workout(userId, username, getClientTimeStamp(), "New Workout", new Boolean(true), exerciseKey);
        Map<String, Object> workoutValues = mCurrentWorkout.toMap();

        //Create new exercise object and map values to push to user-exercises directory
        Exercise exerciseName = new Exercise(exercise);
        Map<String, Object> exerciseValues = exerciseName.toMap();

        //Create map object to push multiple updates to multiple nodes
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workouts/" + mWorkoutKey, workoutValues);
        childUpdates.put("/user-workouts/" + userId + "/" + mWorkoutKey, workoutValues);
        childUpdates.put("/user-exercises/" + userId + "/" + exerciseKey, exerciseValues);
        childUpdates.put("/timestamps/workouts/" + mWorkoutKey + "/created/", ServerValue.TIMESTAMP);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]


    private void addNewExercise(String userId, String username, String workoutKey, String exercise) {
        //Add exercise to existing workout that has just been created

        //Create unique exercise key
        String exerciseKey = mDatabase.child("user-exercises").push().getKey();

        //Create new exercise object and add it to user-exercises directory
        Exercise exerciseName = new Exercise(exercise);
        mDatabase.child("user-exercises").child(userId).child(exerciseKey).setValue(exerciseName);

        //Add this exercise to current workout via unique exercise key
        mCurrentWorkout.addExercise(exerciseKey);


        //add new current workout object and to database)
        mDatabase.child("workouts").child(mWorkoutKey).child("exercises").setValue(mCurrentWorkout.exercises);


        //Get current workout object and add exercise


        //locations to update: user-exercises, user-workouts, workouts
    }

    private String getClientTimeStamp(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }
}
