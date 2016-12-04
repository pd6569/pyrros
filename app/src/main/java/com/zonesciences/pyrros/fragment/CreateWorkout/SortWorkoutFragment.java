package com.zonesciences.pyrros.fragment.CreateWorkout;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.SortWorkoutAdapter;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class SortWorkoutFragment extends Fragment {

    private static final String TAG = "SortWorkoutFrag";

    private static final String ARG_EXERCISES = "WorkoutExerciseList";

    // View
    RecyclerView mRecyclerView;

    // Data
    List<Exercise> mWorkoutExercises;

    public static SortWorkoutFragment newInstance(List<Exercise> exerciseList){
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_EXERCISES, (ArrayList) exerciseList);
        SortWorkoutFragment fragment = new SortWorkoutFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    public SortWorkoutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");

        Bundle bundle = getArguments();
        mWorkoutExercises = (ArrayList) bundle.getSerializable(ARG_EXERCISES);
        Log.i(TAG, "mWorkoutExercises received, size: " + mWorkoutExercises.size());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_sort_workout, container, false);

        final TextView textView = (TextView) rootView.findViewById(R.id.text_workout_exercises);
        textView.setText("Number of exercises: " + mWorkoutExercises.size());

        Button button = (Button) rootView.findViewById(R.id.button_num_exercises);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("Number of exercises: " + mWorkoutExercises.size());
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG, "onStop");
    }

    public List<Exercise> getWorkoutExercises() {
        return mWorkoutExercises;
    }

    public void setWorkoutExercises(List<Exercise> workoutExercises) {
        mWorkoutExercises = workoutExercises;
    }
}
