package com.zonesciences.pyrros.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.SetsAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ExerciseFragment";
    public static final String ARG_EXERCISE_KEY = "ExerciseKey";

    String mExerciseKey;

    double mWeight;
    int mReps;

    //Views
    Button mIncreaseWeightButton;
    Button mDecreaseWeightButton;
    Button mIncreaseRepsButton;
    Button mDecreaseRepsButton;

    EditText mWeightField;
    EditText mRepsField;

    //Recycler view components
    RecyclerView mSetsRecycler;
    SetsAdapter mSetsAdapter;
    LinearLayoutManager mLayoutManager;

    public static ExerciseFragment newInstance(String exerciseKey) {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mExerciseKey = bundle.getString(ARG_EXERCISE_KEY);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);
        mDecreaseWeightButton = (Button) view.findViewById(R.id.button_decrease_weight);
        mDecreaseWeightButton.setOnClickListener(this);

        mIncreaseWeightButton = (Button) view.findViewById(R.id.button_increase_weight);
        mIncreaseWeightButton.setOnClickListener(this);

        mDecreaseRepsButton = (Button) view.findViewById(R.id.button_decrease_reps);
        mDecreaseRepsButton.setOnClickListener(this);

        mIncreaseRepsButton = (Button) view.findViewById(R.id.button_increase_reps);
        mIncreaseRepsButton.setOnClickListener(this);

        mWeightField = (EditText) view.findViewById(R.id.field_weight);
        mRepsField = (EditText) view.findViewById(R.id.field_reps);

        mSetsRecycler = (RecyclerView) view.findViewById(R.id.recycler_sets);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mSetsRecycler.setLayoutManager(mLayoutManager);
        mSetsRecycler.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        mSetsAdapter = new SetsAdapter();

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button_decrease_weight:
                adjustWeight(id);
                break;
            case R.id.button_increase_weight:
                adjustWeight(id);
                break;
            case R.id.button_decrease_reps:
                adjustReps(id);
                break;
            case R.id.button_increase_reps:
                adjustReps(id);
                break;

        }
    }

    private void adjustWeight(int id) {
        String s = mWeightField.getText().toString();
        if (s.isEmpty()){
            s = "0.0";
        }
        double weight = Double.parseDouble(s);
        mWeight = Math.round(weight * 100.0) / 100.0;

        if (id == R.id.button_increase_weight) {
            mWeight += 2.5;
        } else {
            if (mWeight >= 2.5) {
                mWeight -= 2.5;
            } else {
                Log.i(TAG, "Cannot have negative weight mothefucker");
            }
        }
        mWeightField.setText(Double.toString(mWeight));
    }

    private void adjustReps(int id) {

        String s = mRepsField.getText().toString();
        if (s.isEmpty()){
            s = "0";
        }
        mReps = Integer.parseInt(s);
        if (id == R.id.button_increase_reps) {
            mReps++;
        } else {
            if (mReps > 0){
                mReps--;
            }
            else {
                Log.i(TAG, "Cannot have negative reps, motherfucker");
            }
        }
        mRepsField.setText(Integer.toString(mReps));
    }

}
