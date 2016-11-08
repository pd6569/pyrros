package com.zonesciences.pyrros.datatools;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.fragment.ExerciseFragment;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    List<String> mWorkoutKeys = new ArrayList<>();

    OnLoadCompleteListener mListener;

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
                Log.i(TAG, "Datasnapshot instances: " + dataSnapshot.getChildrenCount() + " VALUE : " + dataSnapshot.getValue());
                for (DataSnapshot workout : dataSnapshot.getChildren()){
                    mWorkoutKeys.add(workout.getKey());
                    Log.i(TAG, "Workout key: " + workout.getKey());
                    for (DataSnapshot exercise : workout.getChildren()){

                        if (exercise.getKey().equals(mExerciseKey)){
                            Exercise e = exercise.getValue(Exercise.class);
                            mExercises.add(e);
                            Log.i(TAG, "Created exercises object for exercise and and it to list: " + e.getName());
                        }
                    }

                }
                getWorkoutDates(mWorkoutKeys);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getWorkoutDates(final List<String> workoutKeys) {

        mUserWorkoutExercisesRef.getRoot().child("user-workouts").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot workout : dataSnapshot.getChildren()){
                    if (mWorkoutKeys.contains(workout.getKey())){
                        Workout w = workout.getValue(Workout.class);
                        mExerciseDates.add(w.getClientTimeStamp());
                        Log.i(TAG, "Added date to exercise dates list: " + w.getClientTimeStamp());
                    }
                }
                Log.i(TAG, "Finished loading exercise history");

                mListener.onLoadComplete();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Map <String, Exercise> createExerciseHistoryMap(List<String> dates, List<Exercise> exercises){
        Map<String, Exercise> map = new HashMap<>();
        for (int i = 0; i < exercises.size(); i++){
            map.put(dates.get(i), exercises.get(i));
        }
        return map;
    }

    public List<Exercise> getExercises() {
        return mExercises;
    }

    public List<String> getExerciseDates() {
        return mExerciseDates;
    }

    public interface OnLoadCompleteListener{
        public void onLoadComplete();
    }

    public void setOnLoadCompleteListener(OnLoadCompleteListener listener){
        this.mListener = listener;
    }
}
