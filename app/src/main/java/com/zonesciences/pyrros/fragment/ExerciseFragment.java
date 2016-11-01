package com.zonesciences.pyrros.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.zonesciences.pyrros.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseFragment extends Fragment {

    public static final String ARG_EXERCISE_KEY = "ExerciseKey";

    String mExerciseKey;

    public static ExerciseFragment newInstance(String exerciseKey){
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_KEY, exerciseKey);
        ExerciseFragment fragment = new ExerciseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ExerciseFragment() {
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
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);


        return view;
    }

}
