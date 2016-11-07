package com.zonesciences.pyrros;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.adapters.ExercisesAdapter;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.User;
import com.zonesciences.pyrros.models.Workout;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewWorkoutActivity extends BaseActivity {
    private static final String REQUIRED = "Required";
    private static final String TAG = "NewWorkoutActivity";

    public static final String WORKOUT_EXERCISES = "Workout Exercises";
    public static final String WORKOUT_ID = "Workout ID";

    //need reference to database to read/write data.
    private DatabaseReference mDatabase;
    private DatabaseReference mExercisesReference;

    // setup for recyclerview to display exercises in current workout

    private RecyclerView mExercisesRecycler;
    private ExercisesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private CoordinatorLayout mCoordinatorLayout;
    private EditText mExerciseField;
    private TextView mNoExercises;
    private FloatingActionButton mSubmitExercise;

    private MenuItem mStartWorkoutAction;


    private String mWorkoutKey;
    private Workout mCurrentWorkout;
    private Exercise mExercise;
    private int mOrder;
    private int mNumExercises = 0;

    private List<String> mUserExerciseKeys = new ArrayList<>();
    private ArrayList<String> mExerciseKeysList = new ArrayList<>();

    private String mExerciseKey = new String();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_workout);

        //Initialise Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Create unique workout key
        mWorkoutKey = mDatabase.child("workouts").push().getKey();

        mExercisesReference = mDatabase.child("workout-exercises").child(mWorkoutKey);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Workout");

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout_new_workout);

        mExerciseField = (EditText) findViewById(R.id.field_new_exercise);
        mNoExercises = (TextView) findViewById(R.id.textview_no_exercises);


        mExercisesRecycler = (RecyclerView) findViewById(R.id.recycler_exercises);
        mExercisesRecycler.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mExercisesRecycler.setLayoutManager(mLayoutManager);

        mSubmitExercise = (FloatingActionButton) findViewById(R.id.fab_new_workout);
        mSubmitExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExercise();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get list of current exercises for user on startup of this activity and create working list.
        mDatabase.child("user-exercises").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot exerciseSnapshot : dataSnapshot.getChildren()) {
                    String s = exerciseSnapshot.getKey();
                    mUserExerciseKeys.add(s);
                    Log.d(TAG, "Exercise " + s + " added to UserExerciseKeys List");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Remove listener from previous activity


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_new_workout, menu);
        mStartWorkoutAction = menu.findItem(R.id.action_start_workout);
        return true;
    }


    @Override
    public void onStart(){
        super.onStart();

        // Adapter sets listener on workout-exercises directory detecting exercises that are added/removed/moved and updates the
        // recycler view as appropriate
        mAdapter = new ExercisesAdapter(this, mExercisesReference, mWorkoutKey, getUid());
        mAdapter.setExercisesListener(new ExercisesAdapter.ExercisesListener() {
            @Override
            public void onExercisesEmpty() {
                Log.i(TAG, "onExercisesEmpty() called");
                mNoExercises.setVisibility(View.VISIBLE);
                mStartWorkoutAction.setVisible(false);

            }

            @Override
            public void onExerciseRemoved() {
                mNumExercises--;
                Log.i(TAG, "onExerciseRemoved() Number of exercises" + mNumExercises);
            }
        });
        mExercisesRecycler.setAdapter(mAdapter);

    }

    @Override
    public void onPause(){
        super.onPause();
        updateOrder();
        updateNumExercises();
    }

    
    private void addExercise(){
        final String exercise = mExerciseField.getText().toString();

        // Exercise name is required
        if (TextUtils.isEmpty(exercise)) {
            mExerciseField.setError(REQUIRED);
            return;
        } else {
            mExerciseField.setText("");
            mNoExercises.setVisibility(View.INVISIBLE);
            mStartWorkoutAction.setVisible(true);
        }

        // [START single_value_read
        final String userId = getUid();

        //Listener under "users" path - checks that user exists, if exists then writes new workout
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get user value
                User user = dataSnapshot.getValue(User.class);

                // [START_EXCLUDE]
                if (user == null) {
                    // User is null, error out
                    Log.e(TAG, "User " + userId + " is unexpectedly null");
                    Toast.makeText(NewWorkoutActivity.this, "Error: could not fetch user.", Toast.LENGTH_SHORT).show();
                } else {

                    //create new workout
                    //check if there is an existing workout being added to, or if this is a brand new workout.
                    // and check if the exercise being added already exists in user-exercise or not.
                    //if exists call method getExerciseKey
                    String exerciseKey;
                    boolean newWorkout;
                    if (mCurrentWorkout == null) {
                        newWorkout = true;
                        if (checkExerciseExists(exercise)) {
                            //Exercise already exists, set exercise key
                            exerciseKey = exercise;
                            Log.i(TAG, "Creating brand new workout, with existing exercise");

                        } else {
                            // Exercise does not exist, set exercise key null
                            exerciseKey = null;
                            Log.i(TAG, "Creating brand new workout, with new exercise");
                        }
                        writeNewWorkout(userId, user.username, exercise, exerciseKey);
                    } else {
                        newWorkout = false;
                        if (checkExerciseExists(exercise)) {
                            // Exercise already exists, set exercise key
                            exerciseKey = exercise;
                            Log.i(TAG, "Adding existing exercise to current workout");
                        } else {
                            // Exercise does not exist, set exercise key null
                            exerciseKey = null;
                            Log.i(TAG, "Adding new exercise to current workout");
                        }
                        addNewExercise(userId, user.username, mWorkoutKey, exercise, exerciseKey);
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
        mNumExercises++;
        Log.i(TAG, "Number of exercises: " + mNumExercises);

        Map<String, Object> exerciseValues;

        //if the exercise already exists, exercise key is passed in, if not a brand new exercise key
        //is generated

        if (exerciseKey == null) {
            mExerciseKey = exercise;
            mUserExerciseKeys.add(exercise); // add exercise to list for tracking purposes

            //create new exercise to add to user-exercises
            mExercise = new Exercise(userId, exercise);
        } else {
            mExerciseKey = exerciseKey;

            //Exercise already exists in user-exercise. Pass existing values from this exercise into the current exercise object for this workout
            mDatabase.child("user-exercises").child(userId).child(mExerciseKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mExercise = dataSnapshot.getValue(Exercise.class);
                    Log.i(TAG, "Adding existing exercise to new workout, setting exercise object to existing exercise values: " + mExercise.getName());
                    updateWorkoutExercises(mExercise);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        //Set Default workout title
        String title = new String("" + getClientTimeStamp(false));

        mCurrentWorkout = new Workout(userId, username, getClientTimeStamp(true), title, new Boolean(true));
        Map<String, Object> workoutValues = mCurrentWorkout.toMap();

        //Create map object to push multiple updates to multiple nodes
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/timestamps/workouts/" + mWorkoutKey + "/created/", ServerValue.TIMESTAMP);
        childUpdates.put("/workouts/" + mWorkoutKey, workoutValues);
        childUpdates.put("/user-workouts/" + userId + "/" + mWorkoutKey, workoutValues);
        if(exerciseKey == null) { // create new exercise if it does not already exist in user-exercises and add it to workout-exercises
            childUpdates.put("/user-exercises/" + userId + "/" + mExerciseKey, mExercise);
            childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseKey, mExercise);
            childUpdates.put("/user-workout-exercises/" + userId + "/" + mWorkoutKey +"/" + mExerciseKey, mExercise);
        }
        mDatabase.updateChildren(childUpdates);

        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "Added exercise: " + mExerciseKey, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
    // [END write_fan_out]

    //Add exercise to existing workout that has just been created
    private void addNewExercise(final String userId, String username, String workoutKey, final String exercise, String exerciseKey) {

        if(mAdapter.getExerciseKeys().contains(exercise)) {
            Log.i(TAG, "You have already added this exercise to the workout, dickhead");
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "Already added this one, dickhead", Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            mNumExercises++;
            Log.i(TAG, "Number of exercises: " + mNumExercises);

            //Create unique exercise key if the exercise is new, otherwise use key for existing exercise
            if (exerciseKey == null) {
                mExerciseKey = exercise;
                Log.d(TAG, "exercise does not already exist, created new exercise key: " + mExerciseKey);
                mUserExerciseKeys.add(exercise);
                mExercise = new Exercise(userId, exercise);
                mDatabase.child("user-exercises").child(userId).child(mExerciseKey).setValue(mExercise);
                mDatabase.child("workout-exercises/").child(mWorkoutKey).child(mExerciseKey).updateChildren(mExercise.toMap());
                mDatabase.child("user-workout-exercises/").child(userId).child(mWorkoutKey).child(mExerciseKey).updateChildren(mExercise.toMap());
            } else {
                Log.d(TAG, "exercise already exists with exercise key: " + exerciseKey);
                mExerciseKey = exerciseKey;
                //grab this exercise from the user-exercises directory
                mDatabase.child("user-exercises").child(userId).child(mExerciseKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mExercise = dataSnapshot.getValue(Exercise.class);
                        Log.i(TAG, "Adding existing exercise to current workout, setting exercise object to existing exercise values: " + mExercise.getName());
                        mDatabase.child("workout-exercises/").child(mWorkoutKey).child(mExerciseKey).updateChildren(mExercise.toMap());
                        mDatabase.child("user-workout-exercises/").child(userId).child(mWorkoutKey).child(mExerciseKey).updateChildren(mExercise.toMap());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: " + databaseError);
                    }
                });
            }

            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "Added exercise: " + mExerciseKey, Snackbar.LENGTH_LONG);
            snackbar.show();
        }

    }

    private void updateWorkoutExercises(Exercise exercise) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseKey, mExercise);
        childUpdates.put("/user-workout-exercises/" + getUid() + "/" + mWorkoutKey + "/" + mExerciseKey, mExercise);
        mDatabase.updateChildren(childUpdates);
    }

    private boolean checkExerciseExists(String exercise) {
        if (mUserExerciseKeys.contains(exercise)){
            Log.i(TAG, "Exercise already exists");
            return true;
        } else {
            Log.i(TAG, "Exercise does not exist");
            return false;
        }
    }


    private String getClientTimeStamp(boolean includeTime){
        SimpleDateFormat df;
        if (includeTime) {
            df = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
        } else {
            df = new SimpleDateFormat("EEE, dd MMM");
        }
            String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    private void updateNumExercises() {
        Map<String, Object> childUpdates = new HashMap<String, Object>();
        if (mNumExercises > 0) {
            childUpdates.put("/workouts/" + mWorkoutKey + "/numExercises/", mNumExercises);
            childUpdates.put("/user-workouts/" + getUid() + "/" + mWorkoutKey + "/" + "/numExercises/", mNumExercises);
        } else {
            childUpdates.put("/workouts/" + mWorkoutKey, null);
            childUpdates.put("/user-workouts/" + getUid() + "/" + mWorkoutKey, null);
        }
        mDatabase.updateChildren(childUpdates);
    }

    private void updateOrder() {
        //Get ordered arraylist from adapter
        mExerciseKeysList = (ArrayList) mAdapter.getExerciseKeys();
        Log.i(TAG, "exerise keys ordered by adapter: " + mExerciseKeysList);

        //write the exercise order as an exercise property
        Map<String, Object> childUpdates = new HashMap<String, Object>();
        for (int i = 0; i < mExerciseKeysList.size(); i++){
            childUpdates.put("/workout-exercises/" + mWorkoutKey + "/"  + mExerciseKeysList.get(i) + "/order/", i + 1);
            childUpdates.put("/user-workout-exercises/" + getUid() + "/" + mWorkoutKey + "/" + mExerciseKeysList.get(i) + "/order/", i + 1);
        }
        mDatabase.updateChildren(childUpdates);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start_workout:
                //Gets ordered arraylist from adapter and writes order as property of exercise
                updateOrder();

                //write number of exercises to workout
                updateNumExercises();

                Bundle extras = new Bundle();
                Log.i(TAG, "Exercises to pass to new activity " + mExerciseKeysList);
                extras.putSerializable(WORKOUT_EXERCISES, mExerciseKeysList);
                extras.putString(WORKOUT_ID, mWorkoutKey);
                Intent i = new Intent (NewWorkoutActivity.this, WorkoutActivity.class);
                i.putExtras(extras);
                startActivity(i);
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
