package com.zonesciences.pyrros.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;


public class ExerciseHistoryFragment extends Fragment {

    private static final String ARG_EXERCISE_KEY = "ExerciseKey";

    String mExerciseKey;

    public static ExerciseHistoryFragment newInstance(String exerciseKey) {
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_KEY, exerciseKey);
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
