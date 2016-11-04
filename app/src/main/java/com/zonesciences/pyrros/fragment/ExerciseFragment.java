package com.zonesciences.pyrros.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperCallback;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.adapters.SetsAdapter;
import com.zonesciences.pyrros.models.Exercise;

import java.util.HashMap;
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

    //Touch Helper
    ItemTouchHelper mItemTouchHelper;
    ItemTouchHelper.Callback mItemTouchHelperCallback;

    //Variables
    String mExerciseKey;
    String mWorkoutKey;
    String mUser;

    //Sets reps and weights
    double mWeight;
    int mReps;
    int mSets = 0;

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
        Log.i(TAG, "onCreate()");
        Bundle bundle = getArguments();
        mExerciseKey = bundle.getString(ARG_EXERCISE_KEY);
        mWorkoutKey = ((WorkoutActivity)this.getActivity()).getWorkoutKey();
        mUser = ((WorkoutActivity) this.getActivity()).getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mExerciseReference = mDatabase.child("workout-exercises").child(mWorkoutKey).child(mExerciseKey);

        //Create workout object from firebase for this fragment
        getExercise();

        //adapter created here
        mSetsAdapter = new SetsAdapter(this.getContext(), mExerciseReference);
        mSetsAdapter.setSetsListener(new SetsAdapter.SetsListener() {
            @Override
            public void onSetsChanged() {
                getExercise();
            }
        });


    }

    // Update fragment exercise object with latest firebase data on first load and if changes to set
    // are made via the adapter (e.g reorder/deleting sets)
    private void getExercise() {
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

        Log.i(TAG, "onCreateView()");
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



        mSetsAdapter.notifyDataSetChanged(); // this ensures that data is reloaded when recreating the view after swiping from other exercises
        mSetsRecycler = (RecyclerView) view.findViewById(R.id.recycler_sets);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mSetsRecycler.setLayoutManager(mLayoutManager);
        mDividerItemDecoration = new DividerItemDecoration(mSetsRecycler.getContext(), mLayoutManager.getOrientation());
        mSetsRecycler.addItemDecoration(mDividerItemDecoration);
        mSetsRecycler.setHasFixedSize(true);
        mSetsRecycler.setAdapter(mSetsAdapter);

        mItemTouchHelperCallback = new ItemTouchHelperCallback(mSetsAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
        mItemTouchHelper.attachToRecyclerView(mSetsRecycler);


        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG, "onStart()");


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

        mSetNumberTitle.setText("Set " + Integer.toString(mSets));

        mExercise.addWeight(mWeight);
        mExercise.addReps(mReps);
        Log.i(TAG, "Exercise object updated with sets. Sets: " + mExercise.getSets() + " Weights: " + mExercise.getWeight() + " Reps: " + mExercise.getReps());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseKey + "/", mExercise.toMap());
        childUpdates.put("/user-workout-exercises/" + mUser + "/" + mWorkoutKey + "/" + mExerciseKey + "/", mExercise.toMap());
        mDatabase.updateChildren(childUpdates);

    }

    private void adjustWeight(int id) {

        setWeight();

        if (id == R.id.button_increase_weight) {
            mWeight += 2.5;
        } else {
            if (mWeight >= 2.5) {
                mWeight -= 2.5;
            } else {
                Log.i(TAG, "Cannot have negative weight motherfucker");
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
