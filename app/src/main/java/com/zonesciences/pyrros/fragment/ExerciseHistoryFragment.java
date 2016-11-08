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

import java.util.ArrayList;
import java.util.List;


public class ExerciseHistoryFragment extends Fragment {

    private static final String ARG_EXERCISE_KEY = "ExerciseKey";
    private static final String ARG_USER_ID = "UserId";
    private static final String TAG = "ExerciseHistoryFragment";

    DatabaseReference mUserWorkoutExercisesRef;

    String mExerciseKey;
    String mUserId;

    List<Exercise> mExercises = new ArrayList<>();

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
        //Firebase deep path query
        Query query = mUserWorkoutExercisesRef.orderByChild(mExerciseKey+"/name").equalTo(mExerciseKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i(TAG, "Datasnapshot instances: " + dataSnapshot.getChildrenCount() + " VALUE : " + dataSnapshot.getValue());

                /*Log.i(TAG, "Trying to find all occurences of exercise " + mExerciseKey + " DataSnapshot children count: " + dataSnapshot.getChildrenCount() + " Value: " + dataSnapshot.getValue());
                for(DataSnapshot workout : dataSnapshot.getChildren()){
                    for (DataSnapshot exercise : workout.getChildren()){
                        Log.i(TAG, "Exercise: " + exercise.getKey() + " in workout: " + workout.getKey());
                        Exercise e = exercise.getValue(Exercise.class);
                        if (e.getName().equals(mExerciseKey)){
                            Log.i(TAG, "New exercise object created and added to list: " + e.getName());
                            mExercises.add(e);
                        }
                    }
                }
                Log.i(TAG, mExerciseKey + " has been performed " + mExercises.size() + " times. " + mExercises);*/
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
