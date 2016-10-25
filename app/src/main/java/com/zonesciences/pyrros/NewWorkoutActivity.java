package com.zonesciences.pyrros;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.adapters.ExercisesAdapter;
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
    private DatabaseReference mExercisesReference;

    // setup for recyclerview to display exercises in current workout

    private RecyclerView mExercisesRecycler;
    private ExercisesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private EditText mExerciseField;
    private TextView mNoExercises;
    private FloatingActionButton mSubmitExercise;

    private String mWorkoutKey;
    private Workout mCurrentWorkout;
    private Exercise mExercise;

    private List<String> mUserExercises = new ArrayList<String>();
    private List<String> mCurrentExercises = new ArrayList<String>();

    private String mExerciseKey = new String();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_workout);

        //Initialise Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Create unique workout key
        mWorkoutKey = mDatabase.child("workouts").push().getKey();

        mExercisesReference = mDatabase.child("workout-exercises").child(mWorkoutKey).child(mExerciseKey);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Workout");

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

    @Override
    public void onStart(){
        super.onStart();


        mAdapter = new ExercisesAdapter(mCurrentExercises);
        mExercisesRecycler.setAdapter(mAdapter);

        //add listener for new exercises added/removed from this workout
        mDatabase.child("workouts").child(mWorkoutKey).child("exercises").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildAdded(). Exercise added to workout: " + dataSnapshot.getKey());
                String exerciseKey = dataSnapshot.getKey();
                mCurrentExercises.add(exerciseKey);
                mAdapter.notifyDataSetChanged();
                Log.i(TAG, "exercise added using key: " + exerciseKey + " to mCurrentExercise. Number of exercises in workout: " + mCurrentExercises.size());
                Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout_new_workout), "Added " + exerciseKey + " to  workout", Snackbar.LENGTH_LONG);
                snackbar.show();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildChanged");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

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
        } else {
            if (exerciseInCurrentWorkout(exercise)){
                Toast.makeText(this, "This workout already contains this exercise", Toast.LENGTH_SHORT).show();
                return;
            }
            mNoExercises.setVisibility(View.INVISIBLE);
        }

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
                    //check if there is an existing workout being added to, or if this is a brand new workout.
                    // and check if the exercise being added already exists in user-exercise or not.
                    //if exists call method getExerciseKey
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

        Map<String, Object> exerciseValues;

        //if the exercise already exists, exercise key is passed in, if not a brand new exercise key
        //is generated

        if (exerciseKey == null) {
            mExerciseKey = exercise;
            mUserExercises.add(exercise); // add exercise to list for tracking purposes

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

        //Create new workout object and map values. Add exercise via exerciseKey.
        mCurrentWorkout = new Workout(userId, username, getClientTimeStamp(), "New Workout", new Boolean(true), mExerciseKey);
        Map<String, Object> workoutValues = mCurrentWorkout.toMap();

        //Create map object to push multiple updates to multiple nodes
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/timestamps/workouts/" + mWorkoutKey + "/created/", ServerValue.TIMESTAMP);
        childUpdates.put("/workouts/" + mWorkoutKey, workoutValues);
        childUpdates.put("/user-workouts/" + userId + "/" + mWorkoutKey, workoutValues);
        if(exerciseKey == null) { // create new exercise if it does not already exist in user-exercises and add it to workout-exercises
            childUpdates.put("/user-exercises/" + userId + "/" + mExerciseKey, mExercise);
            childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseKey, mExercise);
        }
        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]

    //Add exercise to existing workout that has just been created
    private void addNewExercise(String userId, String username, String workoutKey, final String exercise, String exerciseKey) {


        //Create unique exercise key if the exercise is new, otherwise use key for existing exercise
        if (exerciseKey == null){
            mExerciseKey = exercise;
            Log.d(TAG, "exercise does not already exist, created new exercise key: " + mExerciseKey);
            mUserExercises.add(exercise);
            mExercise = new Exercise(userId, exercise);
            mDatabase.child("user-exercises").child(userId).child(mExerciseKey).setValue(mExercise);
        } else {
            Log.d(TAG, "exercise already exists with exercise key: " + exerciseKey);
            mExerciseKey = exerciseKey;
            mDatabase.child("user-exercises").child(userId).child(mExerciseKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mExercise = dataSnapshot.getValue(Exercise.class);
                    Log.i(TAG, "Adding existing exercise to current workout, setting exercise object to existing exercise values: " + mExercise.getName());
                    mDatabase.child("workout-exercises/").child(mWorkoutKey).child(mExerciseKey).updateChildren(mExercise.toMap());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        Log.i(TAG, "mExercise = " + mExercise);

        //Add this exercise to current workout via exercise key
        mCurrentWorkout.addExercise(mExerciseKey);

        //add new exercises from current workout object to workouts and user-workouts directory
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workouts/" + mWorkoutKey + "/exercises/", mCurrentWorkout.exercises);
        childUpdates.put("/user-workouts/" + userId + "/" + mWorkoutKey + "/exercises/", mCurrentWorkout.exercises);
        if (exerciseKey == null) {
            childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseKey, mExercise);
        }
        mDatabase.updateChildren(childUpdates);
        /*mDatabase.child("workouts").child(mWorkoutKey).child("exercises").setValue(mCurrentWorkout.exercises);*/


        //locations to update: user-exercises, user-workouts, workouts
    }

    private void updateWorkoutExercises(Exercise exercise) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseKey, mExercise);
        mDatabase.updateChildren(childUpdates);
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

    private boolean exerciseInCurrentWorkout(String exercise) {
        if(mCurrentExercises.contains(exercise)){
            return true;
        }
        return false;
    }


    private String getClientTimeStamp(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    //get exercise key for existing exercises
    public void getExerciseKey(final String userId, final String username, final String exercise, final boolean newWorkout) {
        Log.i(TAG, "getExerciseKey()");

        Query queryRef = mDatabase.child("user-exercises").child(userId).orderByChild("name").equalTo(exercise);
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
