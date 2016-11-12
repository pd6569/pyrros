package com.zonesciences.pyrros.datatools;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 12/11/2016.
 */
public class DataTools {

    private static final String TAG = "DataTools.class";

    DatabaseReference mUserWorkoutExercisesRef;

    String mExerciseKey;
    String mUserId;

    ArrayList<Exercise> mExercises = new ArrayList<>();
    ArrayList<String> mExerciseDates = new ArrayList<>();
    ArrayList<String> mWorkoutKeys = new ArrayList<>();

    int mSets;
    int mReps;

    OnDataLoadCompleteListener mListener;


    //Constructor without exercises list passed in - generates exercises from firebase call.
    public DataTools (String userId, String exerciseKey){
        mExerciseKey = exerciseKey;
        mUserId = userId;
        mUserWorkoutExercisesRef = FirebaseDatabase.getInstance().getReference().child("user-workout-exercises").child(mUserId);
    }

    //Constructor with exercises list passed in.
    public DataTools (String userId, String exerciseKey, ArrayList<Exercise> exercises){
        mExerciseKey = exerciseKey;
        mUserId = userId;
        mExercises = exercises;
        mUserWorkoutExercisesRef = FirebaseDatabase.getInstance().getReference().child("user-workout-exercises").child(mUserId);
    }

    // Methods for loading data from Firebase

    public void loadExercises() {

        mUserWorkoutExercisesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot workout : dataSnapshot.getChildren()) {

                    for (DataSnapshot exercise : workout.getChildren()) {

                        if (exercise.getKey().equals(mExerciseKey)) {

                            mWorkoutKeys.add(workout.getKey());
                            Log.i(TAG, "Workout key: " + workout.getKey());

                            Exercise e = exercise.getValue(Exercise.class);

                            mExercises.add(e);
                            Log.i(TAG, "Created exercises object for exercise and and it to list: " + e.getName() + "mExercises size: " + mExercises.size());
                        }
                    }

                }

                Log.i(TAG, "Exercises loaded. Number of exercises : " + mExercises.size());

                mListener.onExercisesLoadComplete();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void loadWorkoutDates(final List<String> workoutKeys) {

        mUserWorkoutExercisesRef.getRoot().child("user-workouts").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot workout : dataSnapshot.getChildren()) {
                    if (mWorkoutKeys.contains(workout.getKey())) {
                        Workout w = workout.getValue(Workout.class);
                        mExerciseDates.add(w.getClientTimeStamp());
                        Log.i(TAG, "Added date to exercise dates list: " + w.getClientTimeStamp());
                    }
                }
                Log.i(TAG, "Finished loading workout dates");

                mListener.onWorkoutDatesLoadComplete();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // Listener for load completion
    public interface OnDataLoadCompleteListener{
        public void onExercisesLoadComplete();
        public void onWorkoutDatesLoadComplete();
    }

    public void setOnDataLoadCompleteListener(OnDataLoadCompleteListener listener){
        this.mListener = listener;
    }

    // Getters

    public ArrayList<Exercise> getExercises() {
        return mExercises;
    }

    public ArrayList<String> getExerciseDates() { return mExerciseDates; }

    public ArrayList<String> getWorkoutKeys() {
        return mWorkoutKeys;
    }

    // Methods for calculations

    public int totalSets(){
        int totalSets = 0;
        for(Exercise exercise : mExercises){
            int sets = exercise.getSets();
            totalSets = totalSets + sets;
        }
        return totalSets;
    }

    public int totalReps(){
        int totalReps = 0;
        for(Exercise exercise : mExercises){
            try {
            List<Integer> repsList = exercise.getReps();
            Log.i(TAG, "repsList = " + repsList.size());

            for (int rep : repsList) {
                totalReps = totalReps + rep;
            }

            } catch (NullPointerException exception) {
                Log.i(TAG, "No reps recorded for this exercise: " + exception);
            }
        }
        return totalReps;
    }
}
