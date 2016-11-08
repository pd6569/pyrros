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
import com.zonesciences.pyrros.helper.ExerciseHistory;
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

    List<Exercise> mExercises;
    List<String> mExerciseDates;

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
        Log.i(TAG, "onCreate()");
        Bundle bundle = getArguments();
        mExerciseKey = bundle.getString(ARG_EXERCISE_KEY);
        mUserId = bundle.getString(ARG_USER_ID);

        ExerciseHistory exerciseHistory = new ExerciseHistory(mUserId, mExerciseKey);
        exerciseHistory.getHistory();
        this.mExercises = exerciseHistory.getExercises();
        this.mExerciseDates = exerciseHistory.getExerciseDates();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");

        View rootView =  inflater.inflate(R.layout.fragment_exercise_history, container, false);

        TextView tv = (TextView) rootView.findViewById(R.id.textview_exercise_history);
        tv.setText("Exercise history for: " + mExerciseKey);

        return rootView;
    }

}
