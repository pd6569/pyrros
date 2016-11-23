package com.zonesciences.pyrros.fragment.stats;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.datatools.DataTools;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Record;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatsOverviewFragment extends Fragment {


    private static final String TAG = "StatsOverviewFragment";

    private static final String ARG_EXERCISE_KEY = "ExerciseKey";
    private static final String ARG_USER_ID = "UserId";
    private static final String ARG_EXERCISES = "Exercises";
    private static final String ARG_WORKOUT_KEYS = "WorkoutKeys";
    private static final String ARG_WORKOUT_DATES = "WorkoutDates";

    String mExerciseKey;
    String mUserId;

    //View
    Button mFilterButton;
    ViewPager mStatsViewPager;

    TextView mTotalSetsTextView;
    TextView mTotalRepsTextView;
    TextView mTotalVolumeTextView;
    TextView mHeaviestWeightLiftedTextView;
    TextView mOneRepMaxTextView;
    TextView mThreeRepMaxTextView;
    TextView mFiveRepMaxTextView;
    TextView mTenRepMaxTextView;
    TextView mNumSessionsTextView;
    TextView mSetsPerSessionTextView;

    //RecyclerView
    RecyclerView mStatsRecycler;
    StatsOverviewAdapter mAdapter;
    GridLayoutManager mGridLayoutManager;

    //Data
    ArrayList<Exercise> mExercises = new ArrayList<>();
    ArrayList<String> mWorkoutKeys;
    ArrayList<String> mWorkoutDates;
    Map<String, Object> mHeaviestWeightMap = new HashMap<>();

    // Stats variables
    int mTotalSets;
    int mTotalReps;
    double mTotalVolume;
    int mNumSessions;
    int mSetsPerSession;
    double mHeaviestWeight;
    int mHeaviestWeightReps;
    double mMostRepsWeight;
    int mMostReps;
    double mMostVolume;
    double mOneRepMax;
    double mThreeRepMax;
    double mFiveRepMax;
    double mTenRepMax;
    private double mEstimatedOneRep;
    private double mEstimatedThreeRep;
    private double mEstimatedFiveRep;
    private double mEstimatedTenRep;

    String[] mStatsVariableIndex = new String[]{
            "Total Sets",
            "Total Reps",
            "Total Volume",
            "Heaviest Weight",
            "1 rep-max",
            "3 rep-max",
            "5 rep-max",
            "10 rep-max",
            "Sessions",
            "Sets per session",
            "Most reps",
            "Most volume"};

    String[] mStatsVariables;

    DataTools mDataTools;
    Record mRecord;

    //Units
    String mUnit;
    Double mConversionMultiple;


    public static StatsOverviewFragment newInstance(String exerciseKey, String userId, ArrayList<Exercise> exercises, ArrayList<String> workoutKeys, ArrayList<String> workoutDates) {
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_KEY, exerciseKey);
        args.putString(ARG_USER_ID, userId);
        args.putSerializable(ARG_EXERCISES, exercises);
        args.putSerializable(ARG_WORKOUT_KEYS, workoutKeys);
        args.putSerializable(ARG_WORKOUT_DATES, workoutDates);

        StatsOverviewFragment fragment = new StatsOverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public StatsOverviewFragment() {
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
        mWorkoutKeys = (ArrayList) bundle.getSerializable(ARG_WORKOUT_KEYS);
        mWorkoutDates = (ArrayList) bundle.getSerializable(ARG_WORKOUT_DATES);

        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_unit", null).equals("metric")){
            mUnit = " kgs";
            mConversionMultiple = 1.0;
        } else {
            mUnit = " lbs";
            mConversionMultiple = 2.20462;
        }

        mDataTools = new DataTools(mUserId, mExerciseKey, mExercises, mWorkoutKeys, mWorkoutDates);

        setStatsVariables();

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        final View rootView =  inflater.inflate(R.layout.fragment_stats_overview, container, false);

        mFilterButton = (Button) rootView.findViewById(R.id.stats_overview_filter_button);
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(getContext(), mFilterButton);
                menu.getMenuInflater().inflate(R.menu.menu_stats_overview_filter_popup, menu.getMenu());

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == R.id.stats_menu_today){
                            Log.i(TAG, "Stats for today requested");

                            DataTools previousDataTools = mDataTools; // if there are no workouts performed today, then datatools will not change
                            mDataTools = mDataTools.getExercisesForDates(mDataTools, DataTools.TODAY);
                            if (mDataTools != previousDataTools) {
                                Log.i(TAG, "mData tools set for today. Dates: " + mDataTools.getExerciseDates() + " Workout Ids: " + mDataTools.getWorkoutKeys() + " Exercises: " + mDataTools.getExercises());
                                mFilterButton.setText("TODAY");
                                setStatsVariables();
                                updateStatsVariableArray();
                                mAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getContext(), "No workouts performed today", Toast.LENGTH_SHORT).show();
                            }
                        } else if (item.getItemId() == R.id.stats_menu_all_time){
                            Log.i(TAG, "Stats for all time requested");
                            mFilterButton.setText("ALL TIME");
                            mDataTools = new DataTools(mUserId, mExerciseKey, mExercises, mWorkoutKeys, mWorkoutDates);
                            setStatsVariables();
                            updateStatsVariableArray();
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "Showing stats for: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        }

                        return true;
                    }
                });

                menu.show();
            }
        });

        mStatsViewPager = (ViewPager) rootView.findViewById(R.id.viewpager_stats);
        mStatsRecycler = (RecyclerView) rootView.findViewById(R.id.recycler_stats_overview);


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
                    setVariablesAndAdapter();

                }
            });
        } else {
            setRepMaxStats();
            setVariablesAndAdapter();
        }

        return rootView;
    }

    private void setStatsVariables() {
        Log.i(TAG, "setStatsVariables");
        mTotalSets = mDataTools.totalSets();
        mTotalReps = mDataTools.totalReps();
        mTotalVolume = mDataTools.totalVolume() * mConversionMultiple;
        mNumSessions = mDataTools.getExercises().size();
        mSetsPerSession = mDataTools.totalSets() / mDataTools.getExercises().size();
        mHeaviestWeight = (Double) mDataTools.heaviestWeightLifted().get("weight");
        mHeaviestWeightReps = (Integer) mDataTools.heaviestWeightLifted().get("reps");
        mMostRepsWeight = (Double) mDataTools.mostReps().get("weight");
        mMostReps = (Integer) mDataTools.mostReps().get("reps");
        mMostVolume = (Double) mDataTools.mostVolume().get("volume");

        Log.i(TAG, "sets: " + mTotalSets + "reps" + mTotalReps + " volume" + mTotalVolume + " sessions: " + mNumSessions + " heaviest weight: " + mHeaviestWeight + " heaviest weigh reps "+ mHeaviestWeightReps );
    }

    private void setVariablesAndAdapter() {

        updateStatsVariableArray();

        mAdapter = new StatsOverviewAdapter();
        mStatsRecycler.setAdapter(mAdapter);
        mStatsRecycler.setHasFixedSize(true);
        mGridLayoutManager = new GridLayoutManager(getActivity(), 2);
        mStatsRecycler.setLayoutManager(mGridLayoutManager);

    }


    private void setRepMaxStats() {

        try{
            mOneRepMax = mRecord.getRecords().get("1 rep-max").get(mRecord.getRecords().get("1 rep-max").size() - 1);
            mEstimatedOneRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 1) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 1 rep-max created. Error: " + e.toString());
            mEstimatedOneRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 1) * mConversionMultiple);
        };


        try{
            mThreeRepMax = (mRecord.getRecords().get("3 rep-max").get(mRecord.getRecords().get("3 rep-max").size() - 1));
            mEstimatedThreeRep = Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 3) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 3 rep-max created. Error: " + e.toString());
            mEstimatedThreeRep = Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 3) * mConversionMultiple);

        };

        try{
            mFiveRepMax = (mRecord.getRecords().get("5 rep-max").get(mRecord.getRecords().get("5 rep-max").size() - 1));
            mEstimatedFiveRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 5) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 5 rep-max created. Error: " + e.toString());
            mEstimatedFiveRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 5) * mConversionMultiple);
        };

        try{
            mTenRepMax = (mRecord.getRecords().get("10 rep-max").get(mRecord.getRecords().get("10 rep-max").size() - 1));
            mEstimatedTenRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 10) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 10 rep-max created. Error: " + e.toString());
            mEstimatedTenRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 10) * mConversionMultiple);
        };

    }

    public void updateStatsVariableArray(){

        if(mStatsVariables != null){
            mStatsVariables = null;
        }

        mStatsVariables = new String[]{
                Integer.toString(mTotalSets),
                Integer.toString(mTotalReps),
                Utils.formatWeight(mTotalVolume * mConversionMultiple) + mUnit,
                Utils.formatWeight(mHeaviestWeight * mConversionMultiple) + mUnit + " x " + mHeaviestWeightReps,
                Utils.formatWeight(mOneRepMax * mConversionMultiple),
                Utils.formatWeight(mThreeRepMax * mConversionMultiple),
                Utils.formatWeight(mFiveRepMax * mConversionMultiple),
                Utils.formatWeight(mTenRepMax * mConversionMultiple),
                Integer.toString(mNumSessions),
                Integer.toString(mSetsPerSession),
                Integer.toString(mMostReps) + " x " + Utils.formatWeight(mMostRepsWeight * mConversionMultiple) + mUnit,
                Utils.formatWeight(mMostVolume * mConversionMultiple) + mUnit + "\n (" + Utils.formatWeight((Double) mDataTools.mostVolume().get("weight") * mConversionMultiple) + mUnit + " x " + mDataTools.mostVolume().get("reps") + ")"
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

    public class StatsOverviewAdapter extends RecyclerView.Adapter<StatsOverviewAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView title;
            TextView content;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.stats_overview_title);
                content = (TextView) itemView.findViewById(R.id.stats_overview_content);
            }
        }

        public StatsOverviewAdapter(){

        }

        @Override
        public StatsOverviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.item_stats_overview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(StatsOverviewAdapter.ViewHolder holder, int position) {
            holder.title.setText(mStatsVariableIndex[position]);

            LinearLayout statsContentContainer = (LinearLayout) holder.itemView.findViewById(R.id.stats_overview_content_container);
            statsContentContainer.removeAllViews();

            View view = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.item_stats_overview_content, null);

            TextView statsContentText = (TextView) view.findViewById(R.id.stats_overview_content);
            LinearLayout maxContainer = (LinearLayout) view.findViewById(R.id.stats_overview_max_container);

            if (mStatsVariableIndex[position].contains("max")) {

                statsContentText.setVisibility(View.GONE);

                maxContainer.setVisibility(View.VISIBLE);

                TextView estimatedMax = (TextView) view.findViewById(R.id.stats_overview_estimated_max);
                TextView actualMax = (TextView) view.findViewById(R.id.stats_overview_actual_max);

                if (mStatsVariables[position].equals("0")) {
                    actualMax.setText("Not set");
                } else {
                    actualMax.setText(mStatsVariables[position] + mUnit + " (actual)");
                }

                switch (mStatsVariableIndex[position]) {

                    case "1 rep-max":
                        Log.i(TAG, "1 rep-max");
                        estimatedMax.setText(Utils.formatWeight(mEstimatedOneRep) + mUnit + " (estimated)");
                        statsContentContainer.addView(view);
                        break;

                    case "3 rep-max":
                        Log.i(TAG, "3 rep-max");
                        estimatedMax.setText(Utils.formatWeight(mEstimatedThreeRep) + mUnit + " (estimated)");
                        statsContentContainer.addView(view);
                        break;

                    case "5 rep-max":
                        Log.i(TAG, "5 rep-max");
                        estimatedMax.setText(Utils.formatWeight(mEstimatedFiveRep) + mUnit + " (estimated)");
                        statsContentContainer.addView(view);
                        break;

                    case "10 rep-max":
                        Log.i(TAG, "10 rep-max");
                        estimatedMax.setText(Utils.formatWeight(mEstimatedTenRep) + mUnit + " (estimated)");
                        statsContentContainer.addView(view);
                        break;

                }
            } else {
                statsContentText.setText(mStatsVariables[position]);
                statsContentContainer.addView(view);
            }
        }

        @Override
        public int getItemCount() {
            return mStatsVariables.length;
        }
    }

}
