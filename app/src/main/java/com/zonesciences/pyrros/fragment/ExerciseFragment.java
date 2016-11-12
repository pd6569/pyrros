package com.zonesciences.pyrros.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.zonesciences.pyrros.adapters.SetsAdapter;
import com.zonesciences.pyrros.models.Exercise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExerciseFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ExerciseFragment";
    public static final String ARG_EXERCISE_KEY = "ExerciseKey";
    public static final String ARG_WORKOUT_KEY = "WorkoutKey";
    public static final String ARG_USER_ID = "UserId";
    private static final String ARG_EXERCISE_OBJECT = "ExerciseObject";

    //Views
    TextView mSetNumberTitle;
    TextView mWeightTextView;
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
    int mCurrentSet;

    //Firebase
    private DatabaseReference mDatabase;
    private DatabaseReference mExerciseReference;

    //Exercise object
    Exercise mExercise;

    //Exercise history data
    List<String> mExerciseHistoryDates;
    List<Exercise> mExerciseHistory;
    Map<String, Exercise> mExerciseHistoryMap;

    //Units and conversion
    String mUnitSystem;
    String mUnits;
    double mConversionMultiple;

    public static ExerciseFragment newInstance(String exerciseKey, Exercise exercise, String workoutKey, String userId) {
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_KEY, exerciseKey);
        args.putString(ARG_WORKOUT_KEY, workoutKey);
        args.putString(ARG_USER_ID, userId);
        args.putParcelable(ARG_EXERCISE_OBJECT, exercise);
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
        mWorkoutKey = bundle.getString(ARG_WORKOUT_KEY);
        mExercise = bundle.getParcelable(ARG_EXERCISE_OBJECT);
        mUser = bundle.getString(ARG_USER_ID);
        mCurrentSet = mExercise.getSets() + 1;

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mExerciseReference = mDatabase.child("workout-exercises").child(mWorkoutKey).child(mExerciseKey);

        /*getExercise();*/

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUnitSystem = sharedPref.getString("pref_unit", null);

        setUnitSystem();

        //adapter created here
        mSetsAdapter = new SetsAdapter(this.getContext(), mExerciseReference, mWorkoutKey, mUser);
        mSetsAdapter.setSetsListener(new SetsAdapter.SetsListener() {
            @Override
            public void onSetsChanged() {
                getExercise();
            }
        });

        mSetsAdapter.notifyDataSetChanged();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        mSetNumberTitle = (TextView) view.findViewById(R.id.textview_set_number_title);

        mWeightTextView = (TextView) view.findViewById(R.id.textview_weight);
        mWeightTextView.setText("Weight (" + mUnits + ")");

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

        mSetNumberTitle.setText("Set " + mCurrentSet);


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
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause()");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume()");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        mCurrentSet++;

        double convertedWeight = mWeight * mConversionMultiple;

        mSetNumberTitle.setText("Set " + Integer.toString(mCurrentSet));

        mExercise.addWeight(convertedWeight);
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

    // Update fragment exercise object with latest firebase data on first load and if changes to set
    // are made via the adapter (e.g reorder/deleting sets)
    private void getExercise() {
        Log.i(TAG, "getExercise called()");
        mExerciseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mExercise = dataSnapshot.getValue(Exercise.class);
                Log.i(TAG, "Fragment loaded for this exercise: " + mExercise.getName());
                mCurrentSet = mExercise.getSets() + 1;
                mSetNumberTitle.setText("Set " + mCurrentSet);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setUnitSystem(){
        if (mUnitSystem.equals("metric")){
            mUnits = "kgs";
            mConversionMultiple = 1.0;
        } else {
            mUnits = "lbs";
            mConversionMultiple = 0.453592;
        }
    }

    public List<String> getExerciseHistoryDates() {

        return mExerciseHistoryDates;
    }

    public List<Exercise> getExerciseHistory() {

        return mExerciseHistory;
    }

    public Exercise getCurrentExercise(){
        return mExercise;
    }
}
