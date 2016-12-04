package com.zonesciences.pyrros.fragment.CreateWorkout;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperCallback;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.SortWorkoutAdapter;
import com.zonesciences.pyrros.models.Exercise;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class SortWorkoutFragment extends Fragment {

    private static final String TAG = "SortWorkoutFrag";

    private static final String ARG_EXERCISES = "WorkoutExerciseList";

    // RecyclerView components
    RecyclerView mRecyclerView;
    SortWorkoutAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    TextView mTextView;

    // Context
    Context mContext;

    // Data
    ArrayList<Exercise> mWorkoutExercises = new ArrayList<>();
    boolean mExercisesAdded;

    // Touch Helper
    ItemTouchHelper mItemTouchHelper;
    ItemTouchHelper.Callback mItemTouchHelperCallback;

    public static SortWorkoutFragment newInstance(){
        SortWorkoutFragment fragment = new SortWorkoutFragment();
        return fragment;
    }


    public SortWorkoutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        mContext = getContext();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_sort_workout, container, false);

        mTextView = (TextView) rootView.findViewById(R.id.text_workout_exercises);
        mTextView.setText("Number of exercises: " + mWorkoutExercises.size());

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_sort_workout);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new SortWorkoutAdapter(mContext, mWorkoutExercises);
        mRecyclerView.setAdapter(mAdapter);

        mItemTouchHelperCallback = new ItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        if (isVisibleToUser) {

            Log.i(TAG, "SortWorkout Fragment is now visible");
            mTextView.setText("Number of exercises: " + mWorkoutExercises.size());
            if (!mExercisesAdded) {
                if (!mWorkoutExercises.isEmpty()) {
                    mExercisesAdded = true;
                    mAdapter = new SortWorkoutAdapter(mContext, mWorkoutExercises);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        }
    }


    public List<Exercise> getWorkoutExercises() {
        return mWorkoutExercises;
    }

    public void setWorkoutExercises(List<Exercise> workoutExercises) {
        mWorkoutExercises = (ArrayList) workoutExercises;
    }

    public SortWorkoutAdapter getAdapter() {
        return mAdapter;
    }
}
