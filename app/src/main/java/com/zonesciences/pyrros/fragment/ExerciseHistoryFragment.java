package com.zonesciences.pyrros.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExerciseHistoryFragment extends Fragment {

    private static final String ARG_EXERCISE_KEY = "ExerciseKey";
    private static final String ARG_USER_ID = "UserId";
    private static final String TAG = "ExerciseHistoryFragment";

    DatabaseReference mUserWorkoutExercisesRef;

    String mExerciseKey;
    String mUserId;

    List<Exercise> mExercises = new ArrayList<>();
    List<String> mExerciseDates = new ArrayList<>();

    public static ExerciseHistoryFragment newInstance(String exerciseKey, String userId) {
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_KEY, exerciseKey);
        args.putString(ARG_USER_ID, userId);
        ExerciseHistoryFragment fragment = new ExerciseHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ExerciseHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mExerciseKey = bundle.getString(ARG_EXERCISE_KEY);
        mUserId = bundle.getString(ARG_USER_ID);

        mUserWorkoutExercisesRef = FirebaseDatabase.getInstance().getReference().child("user-workout-exercises").child(mUserId);
        //Firebase deep path query returns all workouts containing mExerciseKey
        Query query = mUserWorkoutExercisesRef.orderByChild(mExerciseKey+"/name").equalTo(mExerciseKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String workoutKey;

                Log.i(TAG, "Datasnapshot instances: " + dataSnapshot.getChildrenCount() + " VALUE : " + dataSnapshot.getValue());
                for (DataSnapshot workout : dataSnapshot.getChildren()){
                    getExerciseDate(workout.getKey());
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

    private void getExerciseDate(final String workoutKey) {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_exercise_history, container, false);

        TextView tv = (TextView) rootView.findViewById(R.id.textview_exercise_history);
        tv.setText("Exercise history for: " + mExerciseKey);

        return rootView;
    }

}
