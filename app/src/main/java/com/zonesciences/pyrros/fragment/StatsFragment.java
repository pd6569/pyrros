package com.zonesciences.pyrros.fragment;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.datatools.DataTools;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Record;
import com.zonesciences.pyrros.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    ViewPager mStatsViewPager;

    TextView mTitle;
    TextView mTotalSets;
    TextView mTotalReps;
    TextView mTotalVolume;
    TextView mHeaviestWeightLifted;
    TextView mOneRepMax;
    TextView mThreeRepMax;
    TextView mFiveRepMax;
    TextView mTenRepMax;
    TextView mNumSessions;
    TextView mSetsPerSession;

    //Data
    ArrayList<Exercise> mExercises = new ArrayList<>();
    ArrayList<String> mWorkoutKeys;
    ArrayList<String> mWorkoutDates;
    Map<String, Object> mHeaviestWeightMap = new HashMap<>();

    DataTools mDataTools;
    Record mRecord;

    //Units
    String mUnit;
    Double mConversionMultiple;

    //Heaviest weight for rep-max estimate
    double mHeaviestWeight;
    int mHeaviestWeightReps;

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
        Log.i(TAG, "onCreate");
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


        mHeaviestWeightMap = mDataTools.heaviestWeightLifted();
        mHeaviestWeight = (Double) mHeaviestWeightMap.get("weight");
        mHeaviestWeightReps = (Integer) mHeaviestWeightMap.get("reps");

        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_unit", null).equals("metric")){
            mUnit = " kgs";
            mConversionMultiple = 1.0;
        } else {
            mUnit = " lbs";
            mConversionMultiple = 2.20462;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View rootView =  inflater.inflate(R.layout.fragment_stats, container, false);


        mTitle = (TextView) rootView.findViewById(R.id.stats_title);
        mTitle.setText("Stats for: " + mExerciseKey);

        mTotalSets = (TextView) rootView.findViewById(R.id.stats_total_sets);
        mTotalSets.setText("" + mDataTools.totalSets());

        mTotalReps = (TextView) rootView.findViewById(R.id.stats_total_reps);
        mTotalReps.setText("" + mDataTools.totalReps());

        mTotalVolume = (TextView) rootView.findViewById(R.id.stats_total_volume);
        mTotalVolume.setText(Utils.formatWeight(mDataTools.totalVolume() * mConversionMultiple) + mUnit);

        int reps = (Integer) mHeaviestWeightMap.get("reps");

        mHeaviestWeightLifted = (TextView) rootView.findViewById(R.id.stats_heaviest_weight_lifted);
        mHeaviestWeightLifted.setText(Utils.formatWeight((Double) mHeaviestWeightMap.get("weight") * mConversionMultiple) + mUnit + " x " + reps);

        mNumSessions = (TextView) rootView.findViewById(R.id.stats_number_of_sessions);
        mNumSessions.setText("" + mDataTools.getExercises().size());

        mSetsPerSession = (TextView) rootView.findViewById(R.id.stats_sets_per_session);
        mSetsPerSession.setText("" + mDataTools.totalSets()/mDataTools.getExercises().size());

        mOneRepMax = (TextView) rootView.findViewById(R.id.stats_one_rep_max);
        mThreeRepMax = (TextView) rootView.findViewById(R.id.stats_three_rep_max);
        mFiveRepMax = (TextView) rootView.findViewById(R.id.stats_five_rep_max);
        mTenRepMax = (TextView) rootView.findViewById(R.id.stats_ten_rep_max);

        if (mRecord == null) {
            Log.i(TAG, "mRecord has not been initialised yet, load from datatools");
            mDataTools.loadRecord();
            mDataTools.setOnDataLoadCompleteListener(new DataTools.OnDataLoadCompleteListener() {
                @Override
                public void onExercisesLoadComplete() {

                }

                @Override
                public void onWorkoutDatesLoadComplete() {

                }

                @Override
                public void onWorkoutKeysLoadComplete() {

                }

                @Override
                public void onExerciseRecordLoadComplete() {
                    Log.i(TAG, "Exercise record loaded");
                    mRecord = mDataTools.getExerciseRecord();

                    setRepMaxStats();

                }
            });
        } else {
            setRepMaxStats();
        }

        return rootView;
    }

    private void setRepMaxStats() {

        String oneRepMax;
        String threeRepMax;
        String fiveRepMax;
        String tenRepMax;


        try{
            oneRepMax = Utils.formatWeight(mRecord.getRecords().get("1 rep-max").get(mRecord.getRecords().get("1 rep-max").size() - 1));
            mOneRepMax.setText(oneRepMax + mUnit);

        } catch (Exception e) {
            Log.i(TAG, "No data for 1 rep-max created. Error: " + e.toString());
            double estimatedOneRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 1));
            mOneRepMax.setText("Estimated: " + Utils.formatWeight(estimatedOneRep) + mUnit);
        };


        try{
            threeRepMax = Utils.formatWeight(mRecord.getRecords().get("3 rep-max").get(mRecord.getRecords().get("3 rep-max").size() - 1));
            mThreeRepMax.setText(threeRepMax + mUnit);

        } catch (Exception e) {
            Log.i(TAG, "No data for 3 rep-max created. Error: " + e.toString());
            double estimatedThreeRep = Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 3));
            mThreeRepMax.setText("Estimated: " + Utils.formatWeight(estimatedThreeRep) + mUnit);;
        };

        try{
            fiveRepMax = Utils.formatWeight(mRecord.getRecords().get("5 rep-max").get(mRecord.getRecords().get("5 rep-max").size() - 1));
            mFiveRepMax.setText(fiveRepMax + mUnit);

        } catch (Exception e) {
            Log.i(TAG, "No data for 5 rep-max created. Error: " + e.toString());
            double estimatedFiveRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 5));
            mFiveRepMax.setText("Estimated: " + Utils.formatWeight(estimatedFiveRep) + mUnit);
        };

        try{
            tenRepMax = Utils.formatWeight(mRecord.getRecords().get("10 rep-max").get(mRecord.getRecords().get("10 rep-max").size() - 1));
            mTenRepMax.setText(tenRepMax + mUnit);

        } catch (Exception e) {
            Log.i(TAG, "No data for 10 rep-max created. Error: " + e.toString());
            double estimatedTenRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 10));
            mTenRepMax.setText("Estimated: " + Utils.formatWeight(estimatedTenRep) + mUnit);
        };

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

}
