package com.zonesciences.pyrros.fragment.stats;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetBehavior;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.datatools.DataTools;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    //To pass to new workout
    private static final String WORKOUT_EXERCISES = "Workout Exercises";
    private static final String WORKOUT_ID = "Workout ID";
    private static final String WORKOUT_EXERCISES_OBJECTS = "WorkoutExerciseObjects";

    // prefs
    private static final String PREFS_STATS = "StatsPrefs";
    private static final String PREFS_CURRENT_FILTER = "StatsPrefsCurrentFilter";
    private static final String PREFS_CURRENT_FILTER_NAME = "StatsPrefsCurrentFilterName";

    String mExerciseKey;
    String mCurrentWorkoutKey;
    String mUserId;

    //View
    Button mFilterButton;

    // Bottomsheet View
    private View mBottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private RelativeLayout mTitleContainer;
    private TextView mTitle;
    private ImageView mLaunchWorkoutImage;
    private LinearLayout mExercisesContainer;

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
    Map<String, Object> mHeaviestWeight;
    Map<String, Object>  mMostReps;
    Map<String, Object>  mMostVolume;
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

    //Database
    DatabaseReference mDatabase;

    //Units
    String mUnit;
    Double mConversionMultiple;

    // Filter
    int mCurrentFilter = DataTools.ALL_TIME; //defaults to all time
    String mCurrentFilterName = "ALL TIME"; //defaults  to all time

    // Preferences
    SharedPreferences mFilterPreferences;
    SharedPreferences.Editor mEditor;

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

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_unit", null).equals("metric")){
            mUnit = Utils.UNIT_METRIC;
            mConversionMultiple = 1.0;
        } else {
            mUnit = Utils.UNIT_IMPERIAL;
            mConversionMultiple = 2.20462;
        }

        mDataTools = new DataTools(mUserId, mExerciseKey, mExercises, mWorkoutKeys, mWorkoutDates);

        mFilterPreferences = getContext().getSharedPreferences(PREFS_STATS, Context.MODE_PRIVATE);

        int filterRequested = mFilterPreferences.getInt(PREFS_CURRENT_FILTER, Context.MODE_PRIVATE);
        mCurrentFilterName = mFilterPreferences.getString(PREFS_CURRENT_FILTER_NAME, null);

        Log.i(TAG, "Filter set from prefs: " + mCurrentFilterName);

        if (mCurrentFilterName == null){
            mCurrentFilter = DataTools.ALL_TIME;
            mCurrentFilterName = "ALL TIME";
        } else {
            DataTools previousDataTools = mDataTools;
            setDataTools(filterRequested);

            // if DataTools is unchanged this means that workouts are not found for this date range OR viewing same filter as before
            if (mDataTools == previousDataTools){
                Log.i(TAG, "Datatools is unchanged");
                mCurrentFilter = DataTools.ALL_TIME;
                mCurrentFilterName = "ALL TIME";
            } else {
                Log.i(TAG, "Datatools has changed");
                setStats();
                setRepMaxes();
                updateStatsContentView();
                mCurrentFilter = filterRequested;
                Log.i(TAG, "Filter set from prefs: " + mCurrentFilterName + " Filter value: " + filterRequested);
            }
        }

        setStats();
        setRepMaxes();

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        final View rootView =  inflater.inflate(R.layout.fragment_stats_overview, container, false);

        mFilterButton = (Button) rootView.findViewById(R.id.stats_overview_filter_button);
        mFilterButton.setText(mCurrentFilterName);
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
                        mEditor = mFilterPreferences.edit();

                        switch(item.getItemId()){
                            case R.id.stats_menu_today:
                                Log.i(TAG, "Stats for today requested");

                                dateRange = DataTools.TODAY;
                                mEditor.putInt(PREFS_CURRENT_FILTER, dateRange);
                                mEditor.putString(PREFS_CURRENT_FILTER_NAME, (String) item.getTitle());
                                mEditor.commit();
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_this_session:
                                Log.i(TAG, "Stats for this session");

                                dateRange = DataTools.THIS_SESSION;
                                mEditor.putInt(PREFS_CURRENT_FILTER, dateRange);
                                mEditor.putString(PREFS_CURRENT_FILTER_NAME, (String) item.getTitle());
                                mEditor.commit();
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_all_time:
                                Log.i(TAG, "Stats for all time requested");

                                dateRange = DataTools.ALL_TIME;
                                mEditor.putInt(PREFS_CURRENT_FILTER, dateRange);
                                mEditor.putString(PREFS_CURRENT_FILTER_NAME, (String) item.getTitle());
                                mEditor.commit();
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_month:
                                Log.i(TAG, "Stats for this month requested");

                                dateRange = DataTools.THIS_MONTH;
                                mEditor.putInt(PREFS_CURRENT_FILTER, dateRange);
                                mEditor.putString(PREFS_CURRENT_FILTER_NAME, (String) item.getTitle());
                                mEditor.commit();
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_week:
                                Log.i(TAG, "Stats for this week requested");

                                dateRange = DataTools.THIS_WEEK;
                                mEditor.putInt(PREFS_CURRENT_FILTER, dateRange);
                                mEditor.putString(PREFS_CURRENT_FILTER_NAME, (String) item.getTitle());
                                mEditor.commit();
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_28_days:
                                Log.i(TAG, "Stats for last 28 days requested");

                                dateRange = DataTools.LAST_28_DAYS;
                                mEditor.putInt(PREFS_CURRENT_FILTER, dateRange);
                                mEditor.putString(PREFS_CURRENT_FILTER_NAME, (String) item.getTitle());
                                mEditor.commit();
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_6_months:
                                Log.i(TAG, "Stats for last 6 months requested");

                                dateRange = DataTools.LAST_6_MONTHS;
                                mEditor.putInt(PREFS_CURRENT_FILTER, dateRange);
                                mEditor.putString(PREFS_CURRENT_FILTER_NAME, (String) item.getTitle());
                                mEditor.commit();
                                setDataTools(dateRange);
                                setFilter(item.getTitle(), dateRange, previousDataTools);

                                break;

                            case R.id.stats_menu_year:
                                Log.i(TAG, "Stats for this year requested");

                                dateRange = DataTools.THIS_YEAR;
                                mEditor.putInt(PREFS_CURRENT_FILTER, dateRange);
                                mEditor.putString(PREFS_CURRENT_FILTER_NAME, (String) item.getTitle());
                                mEditor.commit();
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

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet_stats_overview);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setPeekHeight(0);

        mTitleContainer = (RelativeLayout) rootView.findViewById(R.id.bottom_sheet_calendar_title_container);
        mTitleContainer.setVisibility(View.VISIBLE);

        mTitle = (TextView) rootView.findViewById(R.id.bottom_sheet_calendar_title);
        mLaunchWorkoutImage = (ImageView) rootView.findViewById(R.id.bottom_sheet_calendar_go_to_workout);
        mExercisesContainer = (LinearLayout) rootView.findViewById(R.id.workout_exercises_container);

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
        } else if (mCurrentFilter == filterRequested) {
            Log.i(TAG, "Already viewing stats for this particular filter, dickhead");
            return;
        } else {
            Log.i(TAG, "Current filter requires more data than the current filter includes, reset datatools");
            resetDataTools();
            if (filterRequested == DataTools.ALL_TIME) {
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
            Log.i(TAG, "date tools not changed");
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
        mHeaviestWeight = mDataTools.heaviestWeightLifted();
        mMostReps = mDataTools.mostReps();
        mMostVolume = mDataTools.mostVolume();
    }

    private void setRepMaxes() {

        try{
            mOneRepMax = mDataTools.getRepMax(1);
            Log.i(TAG, "One rep max for this date range: " + mDataTools.getRepMax(1));
            mEstimatedOneRep =  Math.round(mDataTools.estimatedMax((double) mHeaviestWeight.get(DataTools.KEY_WEIGHT), (int) mHeaviestWeight.get(DataTools.KEY_REPS), 1) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 1 rep-max created. Error: " + e.toString());
            mEstimatedOneRep =  Math.round(mDataTools.estimatedMax((double) mHeaviestWeight.get(DataTools.KEY_WEIGHT), (int) mHeaviestWeight.get(DataTools.KEY_REPS), 1) * mConversionMultiple);
        };


        try{
            mThreeRepMax = mDataTools.getRepMax(3);
            Log.i(TAG, "Three rep max for this date range: " + mDataTools.getRepMax(3));
            mEstimatedThreeRep = Math.round(mDataTools.estimatedMax((double) mHeaviestWeight.get(DataTools.KEY_WEIGHT), (int) mHeaviestWeight.get(DataTools.KEY_REPS), 3) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 3 rep-max created. Error: " + e.toString());
            mEstimatedThreeRep = Math.round(mDataTools.estimatedMax((double) mHeaviestWeight.get(DataTools.KEY_WEIGHT), (int) mHeaviestWeight.get(DataTools.KEY_REPS), 3) * mConversionMultiple);

        };

        try{
            mFiveRepMax = mDataTools.getRepMax(5);
            Log.i(TAG, "Five rep max for this date range: " + mDataTools.getRepMax(5));
            mEstimatedFiveRep =  Math.round(mDataTools.estimatedMax((double) mHeaviestWeight.get(DataTools.KEY_WEIGHT), (int) mHeaviestWeight.get(DataTools.KEY_REPS), 5) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 5 rep-max created. Error: " + e.toString());
            mEstimatedFiveRep =  Math.round(mDataTools.estimatedMax((double) mHeaviestWeight.get(DataTools.KEY_WEIGHT), (int) mHeaviestWeight.get(DataTools.KEY_REPS), 5) * mConversionMultiple);
        };

        try{
            mTenRepMax = mDataTools.getRepMax(10);
            Log.i(TAG, "Ten rep max for this date range: " + mDataTools.getRepMax(10));
            mEstimatedTenRep =  Math.round(mDataTools.estimatedMax((double) mHeaviestWeight.get(DataTools.KEY_WEIGHT), (int) mHeaviestWeight.get(DataTools.KEY_REPS), 10) * mConversionMultiple);

        } catch (Exception e) {
            Log.i(TAG, "No data for 10 rep-max created. Error: " + e.toString());
            mEstimatedTenRep =  Math.round(mDataTools.estimatedMax((double) mHeaviestWeight.get(DataTools.KEY_WEIGHT), (int) mHeaviestWeight.get(DataTools.KEY_REPS), 10) * mConversionMultiple);
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
                Utils.formatWeight((double) mHeaviestWeight.get(DataTools.KEY_WEIGHT) * mConversionMultiple) + mUnit,
                Integer.toString((int) mMostReps.get(DataTools.KEY_REPS)),
                Utils.formatWeight((double) mMostVolume.get(DataTools.KEY_VOLUME) * mConversionMultiple) + mUnit,
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
    public void onStart() {
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

    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG, "onStop");

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "onDestroy");

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
        public void onBindViewHolder(StatsOverviewAdapter.ViewHolder holder, final int position) {
            holder.title.setText(mStatsTitles[position]);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "Just a normal stats title: " + mStatsTitles[position]);
                }
            };

            LinearLayout statsContentContainer = (LinearLayout) holder.itemView.findViewById(R.id.stats_overview_content_container);
            statsContentContainer.removeAllViews();

            View view = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.item_stats_overview_content, null);

            TextView statsContentText = (TextView) view.findViewById(R.id.stats_overview_content);
            LinearLayout maxContainer = (LinearLayout) view.findViewById(R.id.stats_overview_max_container);
            LinearLayout statsAdditionalInfoContainer = (LinearLayout) view.findViewById(R.id.stats_overview_additional_info_container);

            TextView date = (TextView) view.findViewById(R.id.stats_overview_workout_date);
            TextView setInfo = (TextView) view.findViewById(R.id.stats_overview_set_info);

            if (mStatsTitles[position].contains("max")) {

                statsContentText.setVisibility(View.GONE);

                maxContainer.setVisibility(View.VISIBLE);
                statsAdditionalInfoContainer.setVisibility(View.VISIBLE);


                TextView estimatedMax = (TextView) view.findViewById(R.id.stats_overview_estimated_max);
                TextView actualMax = (TextView) view.findViewById(R.id.stats_overview_actual_max);


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
                        listener = setClickListener(position, (String) mOneRepMax.get(DataTools.KEY_WORKOUT_KEY), Utils.formatDate((String) mOneRepMax.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                        break;

                    case "3 rep-max":
                        Log.i(TAG, "3 rep-max");
                        estimatedMax.setText(Utils.formatWeight(mEstimatedThreeRep) + mUnit);
                        date.setText(Utils.formatDate((String) mThreeRepMax.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                        statsContentContainer.addView(view);
                        listener = setClickListener(position, (String) mThreeRepMax.get(DataTools.KEY_WORKOUT_KEY), Utils.formatDate((String) mThreeRepMax.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                        break;

                    case "5 rep-max":
                        Log.i(TAG, "5 rep-max");
                        estimatedMax.setText(Utils.formatWeight(mEstimatedFiveRep) + mUnit);
                        date.setText(Utils.formatDate((String) mFiveRepMax.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                        statsContentContainer.addView(view);
                        listener = setClickListener(position, (String) mFiveRepMax.get(DataTools.KEY_WORKOUT_KEY), Utils.formatDate((String) mFiveRepMax.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                        break;

                    case "10 rep-max":
                        Log.i(TAG, "10 rep-max");
                        estimatedMax.setText(Utils.formatWeight(mEstimatedTenRep) + mUnit);
                        date.setText(Utils.formatDate((String) mTenRepMax.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                        statsContentContainer.addView(view);
                        listener = setClickListener(position, (String) mTenRepMax.get(DataTools.KEY_WORKOUT_KEY), Utils.formatDate((String) mTenRepMax.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                        break;

                }
            } else {
                statsContentText.setText(mStatsContentArray[position]);
                statsContentContainer.addView(view);
            }

            if (mStatsTitles[position].contains("Heaviest Weight")) {
                statsAdditionalInfoContainer.setVisibility(View.VISIBLE);
                setInfo.setText(Utils.formatWeight((double) mHeaviestWeight.get(DataTools.KEY_WEIGHT) * mConversionMultiple) + mUnit + " x " + mHeaviestWeight.get(DataTools.KEY_REPS));
                setInfo.setVisibility(View.VISIBLE);
                date.setText(Utils.formatDate((String) mHeaviestWeight.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                listener = setClickListener(position, (String) mHeaviestWeight.get(DataTools.KEY_WORKOUT_KEY), Utils.formatDate((String) mHeaviestWeight.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
            }

            if (mStatsTitles[position].contains("Most reps")) {
                statsAdditionalInfoContainer.setVisibility(View.VISIBLE);
                setInfo.setText(Utils.formatWeight((double) mMostReps.get(DataTools.KEY_WEIGHT) * mConversionMultiple) + mUnit + " x " + mMostReps.get(DataTools.KEY_REPS));
                setInfo.setVisibility(View.VISIBLE);
                date.setText(Utils.formatDate((String) mMostReps.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                listener = setClickListener(position, (String) mMostReps.get(DataTools.KEY_WORKOUT_KEY), Utils.formatDate((String) mMostReps.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
            }

            if (mStatsTitles[position].contains("Most volume (Single Set)")) {
                statsAdditionalInfoContainer.setVisibility(View.VISIBLE);
                setInfo.setText(Utils.formatWeight((double) mMostVolume.get(DataTools.KEY_WEIGHT) * mConversionMultiple) + mUnit + " x " + mMostVolume.get(DataTools.KEY_REPS));
                setInfo.setVisibility(View.VISIBLE);
                date.setText(Utils.formatDate((String) mMostVolume.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));
                listener = setClickListener(position, (String) mMostVolume.get(DataTools.KEY_WORKOUT_KEY), Utils.formatDate((String) mMostVolume.get(DataTools.KEY_DATE), Utils.DATE_FORMAT_FULL, 1));

            }

            holder.itemView.setOnClickListener(listener);

        }

        @Override
        public int getItemCount() {
            return mStatsContentArray.length;
        }
    }

    private View.OnClickListener setClickListener(final int position, final String workoutKey, final String date){
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Statistic selected: " + mStatsTitles[position] + " Workout key: " + workoutKey);

                final List<Exercise> exercises = new ArrayList<>();
                final List<String> exerciseNames = new ArrayList<>();

                mDatabase.child("user-workout-exercises").child(mUserId).child(workoutKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot exercise : dataSnapshot.getChildren()){
                            Exercise e = exercise.getValue(Exercise.class);
                            exercises.add(e);
                        }
                        Collections.sort(exercises);

                        for (Exercise e : exercises){
                            Log.i(TAG, "Exercises for this workout: " + e.getName());
                            String name = e.getName();
                            exerciseNames.add(name);
                        }

                        Log.i(TAG, "Exercise names: " + exerciseNames);

                        createBottomSheet(workoutKey, date, exercises, exerciseNames);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        return listener;
    }


    private void createBottomSheet(final String workoutKey, final String date, final List<Exercise> exercises, final List<String> exerciseNames){

        int numExercises = exercises.size();

        mTitle.setText(date);

        mLaunchWorkoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle extras = new Bundle();
                extras.putSerializable(WORKOUT_EXERCISES, (ArrayList) exerciseNames);
                extras.putString(WORKOUT_ID, workoutKey);
                extras.putSerializable(WORKOUT_EXERCISES_OBJECTS, (ArrayList) exercises);
                Intent i = new Intent (getActivity(), WorkoutActivity.class);
                i.putExtras(extras);
                startActivity(i);
            }
        });

        mExercisesContainer.removeAllViews();

        for (int i = 0; i < numExercises; i++) {

            Exercise currentExercise = exercises.get(i);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_workout_exercises, null);
            TextView exerciseText = (TextView) view.findViewById(R.id.workout_exercise_name);
            LinearLayout setsContainer = (LinearLayout) view.findViewById(R.id.workout_sets_container);
            exerciseText.setText(currentExercise.getName());

            if (currentExercise.getSets() == 0){
                TextView noSets = (TextView) view.findViewById(R.id.workout_no_sets);
                noSets.setVisibility(View.VISIBLE);
            }

            for (int j = 0; j < currentExercise.getSets(); j++){

                Log.i(TAG, "GETTING SETS FOR: currentExercise = " + currentExercise.getName());
                View setsView = LayoutInflater.from(getContext()).inflate(R.layout.item_sets, null);
                TextView setNumber = (TextView) setsView.findViewById(R.id.textview_set_number);
                TextView setWeight = (TextView) setsView.findViewById(R.id.textview_set_weight);
                TextView setReps = (TextView) setsView.findViewById(R.id.textview_set_reps);

                setNumber.setVisibility(View.GONE);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f);
                double weight = currentExercise.getWeight().get(j) * mConversionMultiple;
                String s = Utils.formatWeight(weight);
                setWeight.setText(s + mUnit);
                setWeight.setLayoutParams(params);
                setReps.setText("" + currentExercise.getReps().get(j) + " reps");
                setReps.setLayoutParams(params);

                setsContainer.addView(setsView);

            }

            mExercisesContainer.addView(view);
        }

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

}
