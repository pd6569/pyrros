package com.zonesciences.pyrros.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.datatools.ExerciseStats;
import com.zonesciences.pyrros.models.Exercise;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatsFragment extends Fragment {

    private static final String TAG = "StatsFragment";

    private static final String ARG_EXERCISE_KEY = "ExerciseKey";
    private static final String ARG_USER_ID = "UserId";
    private static final String ARG_EXERCISES = "Exercises";

    String mExerciseKey;
    String mUserId;

    //Data
    ArrayList<Exercise> mExercises = new ArrayList<>();

    //ExerciseStats
    ExerciseStats mExerciseStats;

    public static StatsFragment newInstance(String exerciseKey, String userId, ArrayList<Exercise> exercises) {
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_KEY, exerciseKey);
        args.putString(ARG_USER_ID, userId);
        args.putSerializable(ARG_EXERCISES, (Serializable) exercises);
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

        mExerciseStats = new ExerciseStats(mExerciseKey, mUserId, mExercises);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_stats, container, false);

        TextView title = (TextView) rootView.findViewById(R.id.stats_title);
        title.setText("Showing stats for " + mExerciseKey);

        TextView totalSets = (TextView) rootView.findViewById(R.id.stats_total_sets);
        totalSets.setText("Total for this exercise: " + mExerciseStats.totalSets());

        return rootView;
    }

}
