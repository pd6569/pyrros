package com.zonesciences.pyrros.datatools;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 08/11/2016.
 */
public class ExerciseHistory {

    private static final String TAG = "ExerciseHistory.class";

    DatabaseReference mUserWorkoutExercisesRef;

    String mExerciseKey;
    String mUserId;

    List<Exercise> mExercises = new ArrayList<>();
    List<String> mExerciseDates = new ArrayList<>();

    public ExerciseHistory(String userId, String exerciseKey){
        mExerciseKey = exerciseKey;
        mUserId = userId;
    }

    public void getHistory(){
        mUserWorkoutExercisesRef = FirebaseDatabase.getInstance().getReference().child("user-workout-exercises").child(mUserId);
        //Firebase deep path query returns all workouts containing mExerciseKey
        Query query = mUserWorkoutExercisesRef.orderByChild(mExerciseKey+"/name").equalTo(mExerciseKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String workoutKey;

                Log.i(TAG, "Datasnapshot instances: " + dataSnapshot.getChildrenCount() + " VALUE : " + dataSnapshot.getValue());
                for (DataSnapshot workout : dataSnapshot.getChildren()){
                    getWorkoutDates(workout.getKey());
                    Log.i(TAG, "Workout key: " + workout.getKey());
                    for (DataSnapshot exercise : workout.getChildren()){

                        if (exercise.getKey().equals(mExerciseKey)){
                            Exercise e = exercise.getValue(Exercise.class);
                            mExercises.add(e);
                            Log.i(TAG, "Created exercises object for exercise and and it to list: " + e.getName());
                        }
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getWorkoutDates(final String workoutKey) {
        mUserWorkoutExercisesRef.getRoot().child("user-workouts").child(mUserId).child(workoutKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot workout) {
                Workout w = workout.getValue(Workout.class);
                Log.i(TAG, "Workout key: " + workout.getKey() + " was created on: " + w.getClientTimeStamp());
                mExerciseDates.add(w.getClientTimeStamp());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public List<Exercise> getExercises() {
        return mExercises;
    }

    public List<String> getExerciseDates() {
        return mExerciseDates;
    }
}
