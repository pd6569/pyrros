package com.zonesciences.pyrros.fragment.stats;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
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
    private static final String ARG_CURRENT_WORKOUT_KEY = "CurrentWorkoutKey";

    String mExerciseKey;
    String mCurrentWorkoutKey;
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

    // Filter
    int mCurrentFilter = DataTools.ALL_TIME; //defaults to all time

    public static StatsOverviewFragment newInstance(String exerciseKey, String userId, ArrayList<Exercise> exercises, ArrayList<String> workoutKeys, ArrayList<String> workoutDates, String currentWorkoutKey) {
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_KEY, exerciseKey);
        args.putString(ARG_USER_ID, userId);
        args.putSerializable(ARG_EXERCISES, exercises);
        args.putSerializable(ARG_WORKOUT_KEYS, workoutKeys);
        args.putSerializable(ARG_WORKOUT_DATES, workoutDates);
        args.putString(ARG_CURRENT_WORKOUT_KEY, currentWorkoutKey);

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
        mCurrentWorkoutKey = bundle.getString(ARG_CURRENT_WORKOUT_KEY);

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
                        DataTools previousDataTools = mDataTools; // if there are no workouts for date range, then datatools will not change
                        int filterRequested;
                        boolean filterNotChanged = false;

                        switch(item.getItemId()){
                            case R.id.stats_menu_today:
                                Log.i(TAG, "Stats for today requested");

                                filterRequested = DataTools.TODAY;
                                setDataTools(filterRequested);
                                setFilter(item.getTitle(), filterRequested, previousDataTools);

                                break;

                            case R.id.stats_menu_this_session:
                                Log.i(TAG, "Stats for this session");

                                filterRequested = DataTools.THIS_SESSION;
                                setDataTools(filterRequested);
                                setFilter(item.getTitle(), filterRequested, previousDataTools);

                                break;

                            case R.id.stats_menu_all_time:
                                Log.i(TAG, "Stats for all time requested");

                                filterRequested = DataTools.ALL_TIME;
                                setDataTools(filterRequested);
                                setFilter(item.getTitle(), filterRequested, previousDataTools);

                                break;

                            case R.id.stats_menu_month:
                                Log.i(TAG, "Stats for this month requested");

                                filterRequested = DataTools.THIS_MONTH;
                                setDataTools(filterRequested);
                                setFilter(item.getTitle(), filterRequested, previousDataTools);

                                break;

                            case R.id.stats_menu_week:
                                Log.i(TAG, "Stats for this week requested");

                                filterRequested = DataTools.THIS_WEEK;
                                setDataTools(filterRequested);
                                setFilter(item.getTitle(), filterRequested, previousDataTools);

                                break;

                            case R.id.stats_menu_28_days:
                                Log.i(TAG, "Stats for last 28 days requested");

                                filterRequested = DataTools.LAST_28_DAYS;
                                setDataTools(filterRequested);
                                setFilter(item.getTitle(), filterRequested, previousDataTools);

                                break;

                            case R.id.stats_menu_6_months:
                                Log.i(TAG, "Stats for last 6 months requested");

                                filterRequested = DataTools.LAST_6_MONTHS;
                                setDataTools(filterRequested);
                                setFilter(item.getTitle(), filterRequested, previousDataTools);

                                break;

                            case R.id.stats_menu_year:
                                Log.i(TAG, "Stats for this year requested");

                                filterRequested = DataTools.THIS_YEAR;
                                setDataTools(filterRequested);
                                setFilter(item.getTitle(), filterRequested, previousDataTools);

                                break;
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


    private void setDataTools(int filterRequested){

        if (filterRequested < mCurrentFilter) {
            Log.i(TAG, "Current filter will include all data necessary for the requested filter");

            // Filter for this session requires all exercise data, as the active ssession could be from ANY time range, however it only contains a single random workout, therefore
            // it is LOWEST in the hierarchy below today with value 0
            if (filterRequested == DataTools.THIS_SESSION) {
                Log.i(TAG, "Filter requested = THIS SESSION. Trying to set workout key: " + mCurrentWorkoutKey);
                resetDataTools();
                mDataTools = mDataTools.getToolsForSingleSession(mCurrentWorkoutKey);
                return;
            }
            mDataTools = mDataTools.getExercisesForDates(mDataTools, filterRequested);
        } else if (mCurrentFilter == filterRequested){
            Log.i(TAG, "Already viewing stats for this particular filter, dickhead");
            return;
        } else {
            Log.i(TAG, "Current filter requires more data than the current filter includes, reset datatools");
            resetDataTools();
            if (filterRequested == DataTools.ALL_TIME){
                return;
            } else {

                mDataTools = mDataTools.getExercisesForDates(mDataTools, filterRequested);

                Log.i(TAG, "mDataTools set: " + mDataTools.getExerciseDates() + " exercises : " + mDataTools.getExercises().size() + " workout keys: " + mDataTools.getWorkoutKeys());
            }
        }
    }

    private void resetDataTools() {
        Log.i(TAG, "Data tools reset to include all exercises from all time");
        mDataTools = new DataTools(mUserId, mExerciseKey, mExercises, mWorkoutKeys, mWorkoutDates);
        Log.i(TAG, "Number of exercises: " + mExercises.size());
    }

    private void setFilter(CharSequence filterTitle, int filterRequested, DataTools previousDataTools){

        boolean filterNotChanged = false;

        if(mCurrentFilter == filterRequested) { filterNotChanged = true; }

        if (mDataTools != previousDataTools) {
            mFilterButton.setText(filterTitle);
            Log.i(TAG, "new data tools set: " + mDataTools.getExercises() + " DATES: " + mDataTools.getExerciseDates());
            setStatsVariables();
            updateStatsVariableArray();
            mAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Showing stats for: " + filterTitle, Toast.LENGTH_SHORT).show();
            mCurrentFilter = filterRequested;
        } else {
            if (filterNotChanged){
                Toast.makeText(getContext(), "Filter not changed, dickhead", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No workouts for this filter", Toast.LENGTH_SHORT).show();
            }
        }

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
