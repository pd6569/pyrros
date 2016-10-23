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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.User;
import com.zonesciences.pyrros.models.Workout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private List<String> mUserExercises = new ArrayList<String>();
    private boolean mFirstLoad = true;
    private String mExerciseKey = new String();

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

        //Get list of current exercises for user and creates array.
        mDatabase.child("user-exercises").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot exerciseSnapshot : dataSnapshot.getChildren()) {
                    Exercise exercise = exerciseSnapshot.getValue(Exercise.class);
                    String s = exercise.getName();
                    mUserExercises.add(s);
                    Log.d(TAG, "Exercise " + s + " added to userExercise List");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                    boolean newWorkout;
                        if (mCurrentWorkout == null) {
                            newWorkout = true;
                            if (checkExerciseExists(exercise)){
                                Log.i(TAG, "Creating brand new workout, with existing exercise");
                                getExerciseKey(userId, user.username, exercise, newWorkout);
                            } else {
                                Log.i(TAG, "Creating brand new workout, with new exercise");
                                writeNewWorkout(userId, user.username, exercise, null);
                            }
                        } else {
                            newWorkout = false;
                            if (checkExerciseExists(exercise)){
                                Log.i(TAG, "Adding existing exercise to current workout");
                                getExerciseKey(userId, user.username, exercise, newWorkout);
                            } else {
                                Log.i(TAG, "Adding new exercise to current workout");
                                addNewExercise(userId, user.username, mWorkoutKey, exercise, null);
                            }
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
    private void writeNewWorkout(String userId, String username, String exercise, String exerciseKey) {

        mWorkoutKey = mDatabase.child("workouts").push().getKey();
        if (exerciseKey == null) {
            mExerciseKey = mDatabase.child("user-exercises").push().getKey();
        } else {
            mExerciseKey = exerciseKey;
        }

        //Create new workout object and map values. Add exercise via exerciseKey.
        mCurrentWorkout = new Workout(userId, username, getClientTimeStamp(), "New Workout", new Boolean(true), mExerciseKey);
        Map<String, Object> workoutValues = mCurrentWorkout.toMap();

        //Create new exercise object and map values to push to user-exercises directory
        Exercise exerciseName = new Exercise(exercise);
        Map<String, Object> exerciseValues = exerciseName.toMap();

        //Create map object to push multiple updates to multiple nodes
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workouts/" + mWorkoutKey, workoutValues);
        childUpdates.put("/user-workouts/" + userId + "/" + mWorkoutKey, workoutValues);
        childUpdates.put("/user-exercises/" + userId + "/" + mExerciseKey, exerciseValues);
        childUpdates.put("/timestamps/workouts/" + mWorkoutKey + "/created/", ServerValue.TIMESTAMP);

        mDatabase.updateChildren(childUpdates);
    }


    // [END write_fan_out]


    private void addNewExercise(String userId, String username, String workoutKey, final String exercise, String exerciseKey) {
        //Add exercise to existing workout that has just been created

        //Create unique exercise key
        if (exerciseKey == null){
            mExerciseKey = mDatabase.child("user-exercises").push().getKey();
        } else {
            mExerciseKey = exerciseKey;
        }

        //Create new exercise object and add it to user-exercises directory
        Exercise exerciseName = new Exercise(exercise);
        mDatabase.child("user-exercises").child(userId).child(mExerciseKey).setValue(exerciseName);

        //Add this exercise to current workout via unique exercise key
        mCurrentWorkout.addExercise(mExerciseKey);


        //add new current workout object and to database)
        mDatabase.child("workouts").child(mWorkoutKey).child("exercises").setValue(mCurrentWorkout.exercises);


        //Get current workout object and add exercise


        //locations to update: user-exercises, user-workouts, workouts
    }

    private boolean checkExerciseExists(String exercise) {
        if (mUserExercises.contains(exercise)){
            Log.i(TAG, "Exercise already exists");
            return true;
        } else {
            Log.i(TAG, "Exercise does not exist");
            return false;
        }
    }


    private String getClientTimeStamp(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    public void getExerciseKey(final String userId, final String username, final String exercise, final boolean newWorkout) {
        Log.i(TAG, "getExerciseKey called. userId: " + userId + " exercise: " + exercise);

        Query queryRef = mDatabase.child("user-exercises").child(userId).orderByChild("name").equalTo(exercise).limitToFirst(1);
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "Exercise exists with the following keys: " + dataSnapshot.getKey());
                mExerciseKey = dataSnapshot.getKey();
                Log.i(TAG, "mExerciseKey = " + mExerciseKey);
                if (newWorkout){
                    writeNewWorkout(userId, username, exercise, mExerciseKey);
                } else {
                    addNewExercise(userId, username, mWorkoutKey, exercise, mExerciseKey);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onchildchanged called");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onchildremoved called");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onchildmoved called");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "onchildcancelled called");
            }
        });

    }

}
