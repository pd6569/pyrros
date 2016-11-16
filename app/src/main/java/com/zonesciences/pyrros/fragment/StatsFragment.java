package com.zonesciences.pyrros.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.datatools.DataTools;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatsFragment extends Fragment {

    private static final String TAG = "StatsFragment";

    private static final String ARG_EXERCISE_KEY = "ExerciseKey";
    private static final String ARG_USER_ID = "UserId";
    private static final String ARG_EXERCISES = "Exercises";
    private static final String ARG_WORKOUT_KEYS = "WorkoutKeys";
    private static final String ARG_WORKOUT_DATES = "WorkoutDates";

    String mExerciseKey;
    String mUserId;

    //View
    TextView mTitle;
    TextView mTotalSets;
    TextView mTotalReps;
    TextView mTotalVolume;
    TextView mHeaviestWeightLifted;

    //Data
    ArrayList<Exercise> mExercises = new ArrayList<>();
    ArrayList<String> mWorkoutKeys;
    ArrayList<String> mWorkoutDates;


    DataTools mDataTools;

    public static StatsFragment newInstance(String exerciseKey, String userId, ArrayList<Exercise> exercises, ArrayList<String> workoutKeys, ArrayList<String> workoutDates) {
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_KEY, exerciseKey);
        args.putString(ARG_USER_ID, userId);
        args.putSerializable(ARG_EXERCISES, exercises);
        args.putSerializable(ARG_WORKOUT_KEYS, workoutKeys);
        args.putSerializable(ARG_WORKOUT_DATES, workoutDates);

        StatsFragment fragment = new StatsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        mExerciseKey = bundle.getString(ARG_EXERCISE_KEY);
        mUserId = bundle.getString(ARG_USER_ID);
        mExercises = (ArrayList) bundle.getSerializable(ARG_EXERCISES);

        for (Exercise e : mExercises){
            Log.i(TAG, "Exercise " + e.getWeight());
        }

        mWorkoutKeys = (ArrayList) bundle.getSerializable(ARG_WORKOUT_KEYS);
        mWorkoutDates = (ArrayList) bundle.getSerializable(ARG_WORKOUT_DATES);

        for (String key : mWorkoutKeys){
            Log.i(TAG, "Exercise " + key);
        }


        mDataTools = new DataTools(mUserId, mExerciseKey, mExercises, mWorkoutKeys, mWorkoutDates);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_stats, container, false);

        mTitle = (TextView) rootView.findViewById(R.id.stats_title);
        mTitle.setText("Showing stats for " + mExerciseKey);

        mTotalSets = (TextView) rootView.findViewById(R.id.stats_total_sets);
        mTotalSets.setText("Total number of sets: " + mDataTools.totalSets());

        mTotalReps = (TextView) rootView.findViewById(R.id.stats_total_reps);
        mTotalReps.setText("Total reps performed: " + mDataTools.totalReps());

        mTotalVolume = (TextView) rootView.findViewById(R.id.stats_total_volume);
        mTotalVolume.setText("Total volume: " + Utils.formatWeight(mDataTools.totalVolume()));

        /*mHeaviestWeightLifted = (TextView) rootView.findViewById(R.id.stats_heaviest_weight_lifted);
        mHeaviestWeightLifted.setText("Heaviest weight lifted " + Utils.formatWeight(mDataTools.heaviestWeightLifted()));*/

        return rootView;
    }

}
