package com.zonesciences.pyrros.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.adapters.SetsAdapter;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExerciseFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ExerciseFragment";
    public static final String ARG_EXERCISE_KEY = "ExerciseKey";

    //Views
    TextView mSetNumberTitle;
    Button mIncreaseWeightButton;
    Button mDecreaseWeightButton;
    Button mIncreaseRepsButton;
    Button mDecreaseRepsButton;
    Button mAddSet;

    EditText mWeightField;
    EditText mRepsField;

    //Recycler view components
    RecyclerView mSetsRecycler;
    SetsAdapter mSetsAdapter;
    LinearLayoutManager mLayoutManager;
    DividerItemDecoration mDividerItemDecoration;

    //Variables
    String mExerciseKey;
    String mWorkoutKey;

    //Sets reps and weights
    double mWeight;
    int mReps;
    int mSets = 0; // acts as index for lists below
    List<Double> mWeightList = new ArrayList<>(); // stores weights for each set
    List<Integer> mRepsList = new ArrayList<>(); // stores reps for each set

    //Firebase
    private DatabaseReference mDatabase;
    private DatabaseReference mExerciseReference;

    //Exercise object
    Exercise mExercise;

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
        mWorkoutKey = ((WorkoutActivity)this.getActivity()).getWorkoutKey();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mExerciseReference = mDatabase.child("workout-exercises").child(mWorkoutKey).child(mExerciseKey);

        //Create workout object from firebase for this fragment
        mExerciseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mExercise = dataSnapshot.getValue(Exercise.class);
                Log.i(TAG, "Fragment loaded for this exercise: " + mExercise.getName());
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
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        mSetNumberTitle = (TextView) view.findViewById(R.id.textview_set_number_title);

        mDecreaseWeightButton = (Button) view.findViewById(R.id.button_decrease_weight);
        mDecreaseWeightButton.setOnClickListener(this);

        mIncreaseWeightButton = (Button) view.findViewById(R.id.button_increase_weight);
        mIncreaseWeightButton.setOnClickListener(this);

        mDecreaseRepsButton = (Button) view.findViewById(R.id.button_decrease_reps);
        mDecreaseRepsButton.setOnClickListener(this);

        mIncreaseRepsButton = (Button) view.findViewById(R.id.button_increase_reps);
        mIncreaseRepsButton.setOnClickListener(this);

        mAddSet = (Button) view.findViewById(R.id.button_add_set);
        mAddSet.setOnClickListener(this);

        mWeightField = (EditText) view.findViewById(R.id.field_weight);
        mRepsField = (EditText) view.findViewById(R.id.field_reps);



        mSetsRecycler = (RecyclerView) view.findViewById(R.id.recycler_sets);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mSetsRecycler.setLayoutManager(mLayoutManager);
        mDividerItemDecoration = new DividerItemDecoration(mSetsRecycler.getContext(), mLayoutManager.getOrientation());
        mSetsRecycler.addItemDecoration(mDividerItemDecoration);
        mSetsRecycler.setHasFixedSize(true);



        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        mSetsAdapter = new SetsAdapter(this.getContext(), mExerciseReference);
        mSetsRecycler.setAdapter(mSetsAdapter);
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
            case R.id.button_add_set:
                addSet();
                break;
        }
    }

    private void addSet() {
        setWeight();
        setReps();
        mSets++;

        if (mWeightList == null) {
            mWeightList = new ArrayList<>();
        }

        if (mRepsList == null) {
            mRepsList = new ArrayList<>();
        }


        mSetNumberTitle.setText("Set " + Integer.toString(mSets));

        mWeightList.add(mWeight);
        mRepsList.add(mReps);

        mExercise.addWeight(mWeight);
        mExercise.addReps(mReps);
        Log.i(TAG, "Exercise object updated with sets. Sets: " + mExercise.getSets() + " Weights: " + mExercise.getWeight() + " Reps: " + mExercise.getReps());

        mDatabase.child("workout-exercises").child(mWorkoutKey).child(mExerciseKey).setValue(mExercise);

        Log.i(TAG, "Set: " + mSets + " Weight: " + mWeightList.get(mSets-1) + " Reps: " + mRepsList.get(mSets-1));
    }

    private void adjustWeight(int id) {

        setWeight();

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
        setReps();
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



    private void setWeight() {
        String s = mWeightField.getText().toString();
        if (s.isEmpty()){
            s = "0.0";
        }
        double weight = Double.parseDouble(s);
        mWeight = Math.round(weight * 100.0) / 100.0;
    }

    private void setReps() {
        String s = mRepsField.getText().toString();
        if (s.isEmpty()){
            s = "0";
        }
        mReps = Integer.parseInt(s);
    }
}
