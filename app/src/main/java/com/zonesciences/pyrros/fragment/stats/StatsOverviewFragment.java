package com.zonesciences.pyrros.fragment.stats;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
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

    //RecyclerView
    RecyclerView mStatsRecycler;
    StatsOverviewAdapter mAdapter;
    GridLayoutManager mGridLayoutManager;

    //Data
    ArrayList<Exercise> mExercises = new ArrayList<>();
    ArrayList<String> mWorkoutKeys;
    ArrayList<String> mWorkoutDates;

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
    Map<String, Object> mOneRepMax;
    Map<String, Object> mThreeRepMax;
    Map<String, Object> mFiveRepMax;
    Map<String, Object> mTenRepMax;
    private double mEstimatedOneRep;
    private double mEstimatedThreeRep;
    private double mEstimatedFiveRep;
    private double mEstimatedTenRep;

    String[] mStatsTitles = new String[]{
            "Sessions",
            "Sets",
            "Reps",
            "Volume",
            "Heaviest Weight",
            "Most reps",
            "Most volume (Single Set)",
            "1 rep-max",
            "3 rep-max",
            "5 rep-max",
            "10 rep-max"
    };

    String[] mStatsContentArray; // contains string representation of stats content

    DataTools mDataTools; // contains data tools for specified date range


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
            mUnit = Utils.UNIT_METRIC;
            mConversionMultiple = 1.0;
        } else {
            mUnit = Utils.UNIT_IMPERIAL;
            mConversionMultiple = 2.20462;
        }

        mDataTools = new DataTools(mUserId, mExerciseKey, mExercises, mWorkoutKeys, mWorkoutDates);

        setStats();
        setRepMaxes();

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
                        int dateRange;
                        boolean filterNotChanged = false;

                        switch(item.getItemId()){
                            case R.id.stats_menu_today:
                                Log.i(TAG, "Stats for today requested");

                                dateRange = DataTools.TODAY;
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_this_session:
                                Log.i(TAG, "Stats for this session");

                                dateRange = DataTools.THIS_SESSION;
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_all_time:
                                Log.i(TAG, "Stats for all time requested");

                                dateRange = DataTools.ALL_TIME;
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_month:
                                Log.i(TAG, "Stats for this month requested");

                                dateRange = DataTools.THIS_MONTH;
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_week:
                                Log.i(TAG, "Stats for this week requested");

                                dateRange = DataTools.THIS_WEEK;
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_28_days:
                                Log.i(TAG, "Stats for last 28 days requested");

                                dateRange = DataTools.LAST_28_DAYS;
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_6_months:
                                Log.i(TAG, "Stats for last 6 months requested");

                                dateRange = DataTools.LAST_6_MONTHS;
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_year:
                                Log.i(TAG, "Stats for this year requested");

                                dateRange = DataTools.THIS_YEAR;
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;
                        }

                        return true;
                    }


                });

                menu.show();
            }
        });

        mStatsRecycler = (RecyclerView) rootView.findViewById(R.id.recycler_stats_overview);

        updateStatsContentView();

        mAdapter = new StatsOverviewAdapter();
        mStatsRecycler.setAdapter(mAdapter);
        mStatsRecycler.setHasFixedSize(true);
        mGridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mStatsRecycler.setLayoutManager(mGridLayoutManager);

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
                mDataTools = mDataTools.getDataToolsForSingleSession(mCurrentWorkoutKey);
                return;
            }
            mDataTools = mDataTools.getDataToolsForDateRange(mDataTools, filterRequested);
        } else if (mCurrentFilter == filterRequested){
            Log.i(TAG, "Already viewing stats for this particular filter, dickhead");
            return;
        } else {
            Log.i(TAG, "Current filter requires more data than the current filter includes, reset datatools");
            resetDataTools();
            if (filterRequested == DataTools.ALL_TIME){
                return;
            } else {

                mDataTools = mDataTools.getDataToolsForDateRange(mDataTools, filterRequested);

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
            setStats();
            setRepMaxes();
            updateStatsContentView();
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


    private void setStats() {
        Log.i(TAG, "setStats");
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

    private void setRepMaxes() {

        try{
            mOneRepMax = mDataTools.getRepMax(1);
            Log.i(TAG, "One rep max for this date range: " + mDataTools.getRepMax(1));
            mEstimatedOneRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 1) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 1 rep-max created. Error: " + e.toString());
            mEstimatedOneRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 1) * mConversionMultiple);
        };


        try{
            mThreeRepMax = mDataTools.getRepMax(3);
            Log.i(TAG, "Three rep max for this date range: " + mDataTools.getRepMax(3));
            mEstimatedThreeRep = Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 3) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 3 rep-max created. Error: " + e.toString());
            mEstimatedThreeRep = Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 3) * mConversionMultiple);

        };

        try{
            mFiveRepMax = mDataTools.getRepMax(5);
            Log.i(TAG, "Five rep max for this date range: " + mDataTools.getRepMax(5));
            mEstimatedFiveRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 5) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 5 rep-max created. Error: " + e.toString());
            mEstimatedFiveRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 5) * mConversionMultiple);
        };

        try{
            mTenRepMax = mDataTools.getRepMax(10);
            Log.i(TAG, "Ten rep max for this date range: " + mDataTools.getRepMax(10));
            mEstimatedTenRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 10) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 10 rep-max created. Error: " + e.toString());
            mEstimatedTenRep =  Math.round(mDataTools.estimatedMax(mHeaviestWeight, mHeaviestWeightReps, 10) * mConversionMultiple);
        };

    }

    public void updateStatsContentView(){

        if(mStatsContentArray != null){
            mStatsContentArray = null;
        }

        mStatsContentArray = new String[]{
                Integer.toString(mNumSessions),
                Integer.toString(mTotalSets),
                Integer.toString(mTotalReps),
                Utils.formatWeight(mTotalVolume * mConversionMultiple) + mUnit,
                Utils.formatWeight(mHeaviestWeight * mConversionMultiple) + mUnit + " x " + mHeaviestWeightReps,
                Integer.toString(mMostReps) + " x " + Utils.formatWeight(mMostRepsWeight * mConversionMultiple) + mUnit,
                Utils.formatWeight(mMostVolume * mConversionMultiple) + mUnit + "\n (" + Utils.formatWeight((Double) mDataTools.mostVolume().get("weight") * mConversionMultiple) + mUnit + " x " + mDataTools.mostVolume().get("reps") + ")",
                Utils.formatWeight((double) mOneRepMax.get(DataTools.KEY_WEIGHT) * mConversionMultiple),
                Utils.formatWeight((double) mThreeRepMax.get(DataTools.KEY_WEIGHT) * mConversionMultiple),
                Utils.formatWeight((double) mFiveRepMax.get(DataTools.KEY_WEIGHT) * mConversionMultiple),
                Utils.formatWeight((double) mTenRepMax.get(DataTools.KEY_WEIGHT) * mConversionMultiple)
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
            holder.title.setText(mStatsTitles[position]);

            LinearLayout statsContentContainer = (LinearLayout) holder.itemView.findViewById(R.id.stats_overview_content_container);
            statsContentContainer.removeAllViews();

            View view = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.item_stats_overview_content, null);

            TextView statsContentText = (TextView) view.findViewById(R.id.stats_overview_content);
            LinearLayout maxContainer = (LinearLayout) view.findViewById(R.id.stats_overview_max_container);
            LinearLayout statsAdditionalInfoContainer = (LinearLayout) view.findViewById(R.id.stats_overview_additional_info_container);

            if (mStatsTitles[position].contains("max")) {

                statsContentText.setVisibility(View.GONE);

                maxContainer.setVisibility(View.VISIBLE);
                statsAdditionalInfoContainer.setVisibility(View.VISIBLE);


                TextView estimatedMax = (TextView) view.findViewById(R.id.stats_overview_estimated_max);
                TextView actualMax = (TextView) view.findViewById(R.id.stats_overview_actual_max);
                TextView date = (TextView) view.findViewById(R.id.stats_overview_workout_date);

                if (mStatsContentArray[position].equals("0")) {
                    actualMax.setText("Not set");
                } else {
                    actualMax.setText(mStatsContentArray[position] + mUnit);
                }

                switch (mStatsTitles[position]) {

                    case "1 rep-max":
                        Log.i(TAG, "1 rep-max");
                        estimatedMax.setText(Utils.formatWeight(mEstimatedOneRep) + mUnit);
                        date.setText(Utils.formatDate((String) mOneRepMax.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                        statsContentContainer.addView(view);
                        break;

                    case "3 rep-max":
                        Log.i(TAG, "3 rep-max");
                        estimatedMax.setText(Utils.formatWeight(mEstimatedThreeRep) + mUnit);
                        date.setText(Utils.formatDate((String) mThreeRepMax.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                        statsContentContainer.addView(view);
                        break;

                    case "5 rep-max":
                        Log.i(TAG, "5 rep-max");
                        estimatedMax.setText(Utils.formatWeight(mEstimatedFiveRep) + mUnit);
                        date.setText(Utils.formatDate((String) mFiveRepMax.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                        statsContentContainer.addView(view);
                        break;

                    case "10 rep-max":
                        Log.i(TAG, "10 rep-max");
                        estimatedMax.setText(Utils.formatWeight(mEstimatedTenRep) + mUnit);
                        date.setText(Utils.formatDate((String) mTenRepMax.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                        statsContentContainer.addView(view);
                        break;

                }
            } else {
                statsContentText.setText(mStatsContentArray[position]);
                statsContentContainer.addView(view);
            }
        }

        @Override
        public int getItemCount() {
            return mStatsContentArray.length;
        }
    }

}
