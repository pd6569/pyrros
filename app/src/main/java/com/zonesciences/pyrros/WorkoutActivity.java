package com.zonesciences.pyrros;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.zonesciences.pyrros.Timer.ExerciseTimerListener;
import com.zonesciences.pyrros.Timer.TimerDialog;
import com.zonesciences.pyrros.Timer.TimerState;
import com.zonesciences.pyrros.Timer.WorkoutTimer;
import com.zonesciences.pyrros.Timer.WorkoutTimerReference;
import com.zonesciences.pyrros.fragment.ExerciseFragment;
import com.zonesciences.pyrros.fragment.ExerciseHistoryFragment;
import com.zonesciences.pyrros.fragment.FeedbackFragment;
import com.zonesciences.pyrros.fragment.stats.StatsFragment;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Record;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: Rotating screen to landscape crashes app - likely just fix to portrait orientation for this app

// The viewpager forms the main basis of this activity and is separate to the fragment container
// The fragment container switches in and out the exercise history, stats, and feedback fragments
// and the visibility of the viewpager is toggled on/off.

public class WorkoutActivity extends BaseActivity {

    private static String TAG = "WorkoutActivity";

    // Required data for starting a workout activity
    public static final String WORKOUT_EXERCISES = "Workout Exercises";
    public static final String WORKOUT_EXERCISE_OBJECTS = "WorkoutExerciseObjects";
    public static final String WORKOUT_ID = "Workout ID";

    // Workout Timer
    public static final String PREF_WORKOUT_TIMER_STATE = "WorkoutTimerState";
    public static final String PREF_WORKOUT_ACTIVITY_STATE = "WorkoutActivityState";
    public static final String PREF_WORKOUT_TIMER_SOUND = "WorkoutTimerSound";
    public static final String PREF_WORKOUT_TIMER_VIBRATE = "WorkoutTimerVibrate";
    public static final String PREF_WORKOUT_TIMER_AUTOSTART= "WorkoutTimerAutoStart";

    ViewPager mExercisesViewPager;
    WorkoutExercisesAdapter mWorkoutExercisesAdapter;
    TextView mTimerOverlayTextView;

    public Toolbar mToolbar;
    TabLayout mTabLayout;

    // Timer actions
    MenuItem mActiveTimerToolbarText;
    MenuItem mTimerAction;

    ArrayList<String> mExercisesList;
    ArrayList<Exercise> mExerciseObjects;

    String mWorkoutKey;
    String mExerciseKey;
    String mUserId;

    FragmentManager mFragmentManager;

    // Firebase
    DatabaseReference mDatabase;

    //Track exercise fragments
    Map<Integer, ExerciseFragment> mExerciseReferenceMap = new HashMap<>();

    // Fragments
    ExerciseHistoryFragment mExerciseHistoryFragment;
    StatsFragment mStatsFragment;
    FeedbackFragment mFeedbackFragment;

    //Fragment temporary reference
    Fragment mFragment;

    String mFragmentTag;

    // Timer
    TimerDialog mTimerDialog;
    WorkoutTimer mWorkoutTimer;
    TimerState mTimerState;
    WorkoutTimerReference mWorkoutTimerReference; // reference to single timer and timer state
    boolean mTimerDialogOpen;

    // Notification settings
    boolean mSound;
    boolean mVibrate;

    // Preferences
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mPrefEditor;

    // App reference
    protected PyrrosApp mPyrrosApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_workout);


        Intent i = getIntent();
        mExercisesList = (ArrayList<String>) i.getSerializableExtra(WORKOUT_EXERCISES);
        mExerciseObjects = (ArrayList<Exercise>) i.getSerializableExtra(WORKOUT_EXERCISE_OBJECTS);
        mWorkoutKey = i.getStringExtra(WORKOUT_ID);
        mUserId = getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Log.i(TAG, "Workout activity created. mExerciseList: " + mExercisesList.size());

        // Get preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        setTimerState();


        //TODO: THINK ABOUT CHANGING THIS
        mPyrrosApp = (PyrrosApp) this.getApplicationContext();


        Log.i(TAG, "timer start time: " + mTimerState.getTimerStartTime() + " timer duration: " + mTimerState.getTimerDuration() + "has active timer: " + mTimerState.hasActiveTimer() + " timer running: " + mTimerState.isTimerRunning() + " timer first start: " + mTimerState.isTimerFirstStart() + " TIME REMAINING: " + mTimerState.getTimeRemaining() + " current progress: " + mTimerState.getCurrentProgress() + " progress max " + mTimerState.getCurrentProgressMax());


        mExercisesViewPager = (ViewPager) findViewById(R.id.viewpager_exercises);

        mFragmentManager = getSupportFragmentManager();
        mWorkoutExercisesAdapter = new WorkoutExercisesAdapter(mFragmentManager);
        mExercisesViewPager.setAdapter(mWorkoutExercisesAdapter);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_workout);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Workout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs_workout);
        mTabLayout.setupWithViewPager(mExercisesViewPager, false);

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {

                switch (tabId) {
                    case R.id.tab_history:
                        mFragmentTag = "EXERCISE_HISTORY";
                        changeFragment(mFragmentTag);
                        mToolbar.setTitle(mExerciseKey);
                        break;

                    case R.id.tab_stats:
                        mFragmentTag = "STATS";
                        changeFragment(mFragmentTag);
                        mToolbar.setTitle(mExerciseKey);
                        break;

                    case R.id.tab_feedback:
                        mFragmentTag = "FEEDBACK";
                        mToolbar.setTitle(mExerciseKey);
                        changeFragment(mFragmentTag);
                        break;

                    case R.id.tab_workout:
                        mToolbar.setTitle("Workout");
                        returnToWorkout();
                        Log.i(TAG, "Backstack entry count: " + mFragmentManager.getBackStackEntryCount());
                        break;
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_workout, menu);

        Log.i(TAG, "onCreateOptionsMenu");

        mTimerAction = menu.findItem(R.id.action_timer);
        mActiveTimerToolbarText = menu.findItem(R.id.action_active_timer);

        if (mTimerState.isTimerRunning()){
            Log.i(TAG, "Timer is running");
            mWorkoutTimer.setDialogOpen(mTimerDialogOpen);
            mWorkoutTimer.setTimerActionBarText(mActiveTimerToolbarText);
            mWorkoutTimer.setTimerAction(mTimerAction);
        } else {
            mTimerAction.setVisible(true);
        }

        return true;
    }

    private void changeFragment(String fragmentTag) {
        mExercisesViewPager.setVisibility(View.GONE);
        mTabLayout.setVisibility(View.GONE);

        /*mFragmentTag = fragmentTag;*/

        final int index = mExercisesViewPager.getCurrentItem();
        mExerciseKey = mExercisesList.get(index);

        if (fragmentTag == "EXERCISE_HISTORY") {

            final ExerciseFragment currentFragment = mWorkoutExercisesAdapter.getFragment(index);

            if (currentFragment.isStatsDataLoaded()){
                Log.i(TAG, "Stats data loaded for this exercise, create exercise history fragment and pass in data lists");

                ArrayList<Exercise> exercises = (ArrayList)currentFragment.getStatsExercises();
                ArrayList<String> workoutDates = (ArrayList) currentFragment.getStatsExerciseDates();
                ArrayList<String> workoutKeys = (ArrayList) currentFragment.getStatsExerciseWorkoutKeys();

                mFragment = ExerciseHistoryFragment.newInstance(mExerciseKey, mUserId, workoutDates, exercises, workoutKeys, mWorkoutKey);
                setFragment();

            } else {

                Log.i(TAG, "Stats data has not finished loading, cannot load fragment");
                // show progress dialog and set a listener on the current fragment to notify when data is ready and then load fragment

                showProgressDialog();
                currentFragment.setOnStatsDataLoadedListener(new ExerciseFragment.OnStatsDataLoaded() {
                    @Override
                    public void statsDataLoaded() {
                        Log.i(TAG, "Callback now received from fragment, load complete, load stats");
                        ArrayList<Exercise> exercises = (ArrayList)currentFragment.getStatsExercises();
                        ArrayList<String> workoutDates = (ArrayList) currentFragment.getStatsExerciseDates();
                        ArrayList<String> workoutKeys = (ArrayList) currentFragment.getStatsExerciseWorkoutKeys();

                        mFragment = ExerciseHistoryFragment.newInstance(mExerciseKey, mUserId, workoutDates, exercises, workoutKeys, mWorkoutKey);
                        setFragment();
                        hideProgressDialog();
                    }
                });
            }

        }

        else if (fragmentTag == "STATS") {

            Log.i(TAG, "Stats fragment called");

            final ExerciseFragment currentFragment = mWorkoutExercisesAdapter.getFragment(index);

            if (currentFragment.isStatsDataLoaded()){
                Log.i(TAG, "Stats data loaded for this exercise, create stats fragment and pass in data lists");

                loadStatsFragment(currentFragment);


            } else {

                Log.i(TAG, "Stats data has not finished loading, cannot load fragment yet");

                // show progress dialog and set a listener on the current fragment to notify when data is ready and then load fragment
                showProgressDialog();
                currentFragment.setOnStatsDataLoadedListener(new ExerciseFragment.OnStatsDataLoaded() {
                    @Override
                    public void statsDataLoaded() {
                        Log.i(TAG, "Callback now received from fragment, load complete, load stats");
                        loadStatsFragment(currentFragment);
                        hideProgressDialog();
                    }
                });

            }

        } else if (fragmentTag == "FEEDBACK") {
                if (mFeedbackFragment == null) {
                    mFeedbackFragment = new FeedbackFragment();
                }
                mFragment = mFeedbackFragment;
                setFragment();
        } else {
            mFragment = new Fragment();
            setFragment();
        }

    }

    public void returnToWorkout(){

        mExercisesViewPager.setVisibility(View.VISIBLE);
        mTabLayout.setVisibility(View.VISIBLE);


        // backstack will be 1 if any bottom nav frag has been previously selected. If it is 0, then the
        // workout tab will be displayed, therefore do nothing.
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            Log.i(TAG, "Attempt to remove fragment: " + mFragmentManager.findFragmentByTag(mFragmentTag));
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.remove(mFragmentManager.findFragmentByTag(mFragmentTag)).commit();
        }
    }

    @Override
    public void onBackPressed(){
        Log.i(TAG, "Back button pressed, and I'm not gonna do shit about it, you stay right where you are motherfucker");
        // DO NOTHING
    }


    class WorkoutExercisesAdapter extends FragmentStatePagerAdapter {

        public WorkoutExercisesAdapter(FragmentManager fm) {
            super(fm);
            Log.i(TAG, "WorkoutExercisesAdapter constructor called. Size of mExercises: " + mExercisesList.size());
        }

        @Override
        public Fragment getItem(int position) {
            final ExerciseFragment exerciseFragment = ExerciseFragment.newInstance(mExercisesList.get(position), mExerciseObjects.get(position), mWorkoutKey, mUserId);

            mExerciseReferenceMap.put(position, exerciseFragment); // map exercise fragments so that variables can be obtained when switching fragments.
            return exerciseFragment;
        }

        @Override
        public int getCount() {
            return mExercisesList.size();
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mExercisesList.get(position);
        }

        public ExerciseFragment getFragment(int key){
            return mExerciseReferenceMap.get(key);
        }
    }

    public String getWorkoutKey(){
        return this.mWorkoutKey;
    }

    public void loadExerciseHistoryFragment(){
        /*mFragment = ExerciseHistoryFragment.newInstance(mExerciseKey, mUserId, mExerciseHistoryDates, mExerciseHistory);
        setFragment();*/
    }

    public void loadStatsFragment(ExerciseFragment fragment){

        ExerciseFragment currentFragment = fragment;


        // Get the currentExercise with up to date number of sets - this may be an old workout that is being viewed, or the latest workout
        // Replace this specific exercise in the exercise list with the up to date information



        Exercise currentExercise = currentFragment.getCurrentExercise();
        ArrayList<Exercise> exercises = (ArrayList)currentFragment.getStatsExercises();

        Log.i(TAG, "currentExercise: " + currentExercise.getName() + " reps: " + currentExercise.getReps() + " List of exercises size: " + exercises.size());

        for (int i = 0; i < exercises.size(); i++){
            Log.i(TAG, "Looping through exercises " + i + " exercise: " + exercises.get(i).getExerciseId() );
            Exercise e = exercises.get(i);
            if (e.getExerciseId().equals(currentExercise.getExerciseId())){
                Log.i(TAG, "Found matching exercise in list with id: " + e.getExerciseId() + " At index: " + i + " Weights lifted: " + e.getWeight());
                exercises.set(i, currentExercise);
                Log.i(TAG, "Replaced with current Exercise parameters: " + exercises.get(i).getWeight());
                break;
            }
        }

        ArrayList<String> workoutKeys = (ArrayList)currentFragment.getStatsExerciseWorkoutKeys();
        ArrayList<String> workoutDates = (ArrayList) currentFragment.getStatsExerciseDates();
        Record exerciseRecord = currentFragment.getRecord();
        String currentWorkoutKey = currentFragment.getWorkoutKey();
        Log.i(TAG, "Current workout key = " + currentWorkoutKey);

        mFragment = StatsFragment.newInstance(mExerciseKey, mUserId, exercises, workoutKeys, workoutDates, currentWorkoutKey);
        setFragment();

    }

    private void setFragment() {

        // only add to back stack if a any fragment has previously been added, otherwise just replace fragment.

        FragmentTransaction ft;
        ft = mFragmentManager.beginTransaction();
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.workout_fragment_container, mFragment, mFragmentTag).commit();
        } else {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.workout_fragment_container, mFragment, mFragmentTag).addToBackStack(null).commit();
        }

        Log.i(TAG, "Backstack entry count: " + mFragmentManager.getBackStackEntryCount());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_edit_workout) {
            Intent intent = new Intent(getApplicationContext(), EditWorkoutActivity.class);
            intent.putExtra(WORKOUT_EXERCISE_OBJECTS, mExerciseObjects);
            intent.putExtra(WORKOUT_ID, mWorkoutKey);
            startActivity(intent);
            return true;
        }

        if (i == R.id.action_pause_timer){
            pauseTimer(mWorkoutTimer.getTimeRemaining(), false);
        }

        if (i == R.id.action_resume_timer){
            resumeTimer((int) mTimerState.getTimeRemaining() / 1000, false);
        }

        if (i == R.id.action_reset_timer){
            resetTimer();
        }

        if (i == R.id.action_timer || i == R.id.action_active_timer){
            createTimerDialog();
        }


        if (i == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    private void createTimerDialog(){

        mTimerDialogOpen = true;

        mTimerDialog = new TimerDialog(this);
        if (mTimerState.hasActiveTimer()){


            if (mTimerState.isTimerRunning()){
                Log.i(TAG, "Timer is active and running");
                int timeNow = (int) ((Calendar.getInstance().getTimeInMillis() / 1000));
                int timeToStart = mTimerState.getTimerDuration() - (timeNow - (mTimerState.getTimerStartTime()));
                Log.i(TAG, "time now:  " + timeNow + "timer duration: " + mTimerState.getTimerDuration() + "timer started at :" + mTimerState.getTimerStartTime() + " TIME TO START: " + timeToStart);

                if (timeToStart <= 0){
                    Log.i(TAG, "Timer has expired");
                    mTimerState.reset();
                } else {
                    Log.i(TAG, "Timer is still going, so set time remaining");
                    mTimerState.setTimeRemaining(timeToStart * 1000);

                }

                setTimerProperties(mTimerDialog);



            } else {
                Log.i(TAG, "Timer is active and paused");

                setTimerProperties(mTimerDialog);
            }
        }


        mTimerDialog.setExerciseTimerListener(new ExerciseTimerListener() {
            @Override
            public void onExerciseTimerCreated(int timerDuration, boolean vibrate, boolean sound) {
                Log.i(TAG, "Timer created");
                mWorkoutTimer = new WorkoutTimer(timerDuration * 1000, 500, getApplicationContext(), mWorkoutKey, mExercisesList, mExerciseObjects);
                mWorkoutTimer.setTimerActionBarText(mActiveTimerToolbarText);
                mWorkoutTimer.setTimerAction(mTimerAction);
                mWorkoutTimer.setDialogOpen(true);
                mWorkoutTimer.setVibrate(vibrate);
                mWorkoutTimer.setSound(sound);
                mWorkoutTimer.start();

                mWorkoutTimerReference.setWorkoutTimer(mWorkoutTimer);

                mTimerState.setTimerDuration(timerDuration);
                mTimerState.setTimerFirstStart(false);
                mTimerState.setHasActiveTimer(true);

                // update notification settings
                mVibrate = vibrate;
                mSound = sound;

                // set start time for timer and max duration
                int startTime = (int) ((Calendar.getInstance().getTimeInMillis() / 1000));
                mTimerState.setTimerStartTime(startTime);

                Log.i(TAG, "TIMER CREATED TIME: " + startTime);
            }

            @Override
            public void onExerciseTimerPaused(long timeRemaining, boolean pauseFromDialog) {
                Log.i(TAG, "Timer paused. Time remaining: " + timeRemaining);
                pauseTimer(timeRemaining, pauseFromDialog);
            }

            @Override
            public void onExerciseTimerResumed(int timerDuration, boolean pausedFromDialog) {
                Log.i(TAG, "Timer resumed. Reset timer start time and timer duration: " + timerDuration);
                resumeTimer(timerDuration, pausedFromDialog);
            }

            @Override
            public void onExerciseTimerFinished() {
                Log.i(TAG, "Timer finished");
                mWorkoutTimer = null;
                mWorkoutTimerReference.setWorkoutTimer(null);

                mTimerAction.setVisible(true);

                mTimerState.reset();

                // remove time state info
                mPrefEditor = mSharedPreferences.edit();
                mPrefEditor.remove(PREF_WORKOUT_TIMER_STATE);
                mPrefEditor.commit();
            }

            @Override
            public void onExerciseTimerDismissed(boolean timerRunning, long timeRemaining, int currentProgress, int currentProgressMax){
                Log.i(TAG, "Timer dismissed. Current Progress: " + currentProgress);

                mTimerDialogOpen = false;

                if (mWorkoutTimer != null) {
                    mWorkoutTimer.setDialogOpen(false);
                }

                mTimerState.setTimerRunning(timerRunning);
                mTimerState.setTimeRemaining(timeRemaining);
                mTimerState.setCurrentProgress(currentProgress);
                mTimerState.setCurrentProgressMax(currentProgressMax);

            }

            @Override
            public void onExerciseTimerReset(){
                Log.i(TAG, "Timer reset.");
                resetTimer();
            }
        });
        mTimerDialog.createDialog();

        if (mWorkoutTimer != null){
            mWorkoutTimer.setDialogOpen(true);
        }
    }

    private void setTimerProperties(TimerDialog timerDialog){
        timerDialog.setHasActiveTimer(mTimerState.hasActiveTimer());
        timerDialog.setTimerFirstStart(mTimerState.isTimerFirstStart());
        timerDialog.setTimerRunning(mTimerState.isTimerRunning());
        timerDialog.setTimeRemaining(mTimerState.getTimeRemaining());
        timerDialog.setCurrentProgress(mTimerState.getCurrentProgress());
        timerDialog.setCurrentProgressMax(mTimerState.getCurrentProgressMax());
    }

    /****** TIMER CONTROLS ******/
    public void pauseTimer(long timeRemaining, boolean pausedFromDialog){
        Log.i(TAG, "Pause Timer");

        if (mTimerDialogOpen && !pausedFromDialog){
            Log.i(TAG, "Paused via notification. Dialog window open");
            // Update dialog view
            mTimerDialog.pauseTimer();
        } else {

            // update timer and reference
            mWorkoutTimer.cancel();
            /*mWorkoutTimer = null;
            mWorkoutTimerReference.setWorkoutTimer(null);*/

            // update timer state
            mTimerState.setTimerRunning(false);
            mTimerState.setTimeRemaining(timeRemaining);

            // update toolbar
            mTimerAction.setVisible(true);
            mActiveTimerToolbarText.setVisible(false);
        }
    }

    // timerDuration is duration of new timer in seconds
    public void resumeTimer(int timerDuration, boolean pausedFromDialog){
        Log.i(TAG, "Resume Timer");

        if (mTimerDialogOpen && !pausedFromDialog) {
            Log.i(TAG, "Resumed via notification. Dialog window open");
            // Update dialog view
            mTimerDialog.resumeTimer();
        } else {

            // update timer and reference
            mWorkoutTimer = new WorkoutTimer(timerDuration * 1000, 500, getApplicationContext(), mWorkoutKey, mExercisesList, mExerciseObjects);
            mWorkoutTimer.setTimerActionBarText(mActiveTimerToolbarText);
            mWorkoutTimer.setTimerAction(mTimerAction);
            mWorkoutTimer.setDialogOpen(mTimerDialogOpen);
            mWorkoutTimer.setSound(mSound);
            mWorkoutTimer.setVibrate(mVibrate);
            mWorkoutTimer.start();
            mWorkoutTimerReference.setWorkoutTimer(mWorkoutTimer);

            // update timer state
            mTimerState.setTimerStartTime((int) ((Calendar.getInstance().getTimeInMillis() / 1000)));
            mTimerState.setTimerDuration(timerDuration);
            mTimerState.setTimerRunning(true);
        }
    }

    public void resetTimer(){
        Log.i(TAG, "Reset Timer");

        // remove timer
        if (mWorkoutTimer != null) {
            mWorkoutTimer.cancel();
            mWorkoutTimer = null;
            mWorkoutTimerReference.setWorkoutTimer(null);
        }

        // update timer state
        mTimerState.reset();

        // update toolbar
        mTimerAction.setVisible(true);
        mActiveTimerToolbarText.setVisible(false);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(WorkoutTimer.NOTIFICATION_ID);
    }

    /****** END TIMER CONTROLS ******/


    private void setTimerState(){
        // Set timer state
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString(PREF_WORKOUT_TIMER_STATE, null);
        mTimerState = gson.fromJson(json, TimerState.class);
        mWorkoutTimerReference = WorkoutTimerReference.getWorkoutTimerReference();

        if (mTimerState == null){
            Log.i(TAG, "No timer state info available");
            mTimerState = new TimerState();
        } else {
            if (mTimerState.hasActiveTimer() && !mTimerState.isTimerRunning()) {
                // Has timer and it is paused, set timer state.
                Log.i(TAG, "Timer state info available. Timer active and paused.");
                mWorkoutTimer = new WorkoutTimer(mTimerState.getTimeRemaining(), 500, getApplicationContext(), mWorkoutKey, mExercisesList, mExerciseObjects);
            } else if (mTimerState.hasActiveTimer() && mTimerState.isTimerRunning()){
                // Has running timer. Check if timer has expired or not, if not get reference to active running timer.
                Log.i(TAG, "Timer state info available. Timer active and running.");
                int timeNow = (int) ((Calendar.getInstance().getTimeInMillis() / 1000));
                int timeToStart = mTimerState.getTimerDuration() - (timeNow - (mTimerState.getTimerStartTime()));

                if (timeToStart <= 0) {
                    // timer expired, reset timer state variables
                    mTimerState.reset();
                } else {
                    // timer is still going, update timerstate time remaining and get reference to the running timer if not destroyed
                    mTimerState.setTimeRemaining(timeToStart * 1000);
                    mWorkoutTimer = mWorkoutTimerReference.getWorkoutTimer();

                    // mWorkoutReference may be null if app is destroyed and restarted
                    if (mWorkoutTimer == null) {
                        // workout reference does not exist, so create new workout timer and set it as reference
                        mWorkoutTimer = new WorkoutTimer(mTimerState.getTimeRemaining(), 500, getApplicationContext(), mWorkoutKey, mExercisesList, mExerciseObjects);
                        mWorkoutTimer.start();
                        mWorkoutTimerReference.setWorkoutTimer(mWorkoutTimer);
                    }

                    if (mActiveTimerToolbarText != null && mTimerAction != null){
                        Log.i(TAG, "set action and text for timer");
                        mWorkoutTimer.setTimerAction(mTimerAction);
                        mWorkoutTimer.setTimerActionBarText(mActiveTimerToolbarText);
                    }

                    Log.i(TAG, "mWorkoutTimer: " + mWorkoutTimer);
                }
            } else if (!mTimerState.hasActiveTimer()){
                Log.i(TAG, "Timer is not active");
                mTimerState.reset();
            }
        }
    }

    private void clearReferences(){
        BaseActivity currentActivity = mPyrrosApp.getCurrentActivity();
        if (this.equals(currentActivity)) {
            mPyrrosApp.setCurrentActivity(null);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause");
        clearReferences();

        if (mTimerState.hasActiveTimer()){
            // if activity is paused while dialog open, need to get current timer state and store it.
            if (mTimerDialogOpen) {
                Log.i(TAG, "activity stopped while dialog open, get values from timer");
                mTimerState.setTimerRunning(mTimerDialog.isTimerRunning());
                mTimerState.setCurrentProgress(mTimerDialog.getCurrentProgress());
                mTimerState.setCurrentProgressMax(mTimerDialog.getCurrentProgressMax());

                // Dismiss the dialog and stop timer

                mTimerDialog.getAlertDialog().cancel();
            }
            mPrefEditor = mSharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(mTimerState);
            mPrefEditor.putString(PREF_WORKOUT_TIMER_STATE, json);
            mPrefEditor.putBoolean(PREF_WORKOUT_TIMER_SOUND, mSound);
            mPrefEditor.putBoolean(PREF_WORKOUT_TIMER_VIBRATE, mVibrate);
            mPrefEditor.apply();
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume");

        mPyrrosApp.setCurrentActivity(this);

        setTimerState();

        if (!mTimerState.isHasActiveTimer()) {
            if (mTimerAction != null && mActiveTimerToolbarText != null){
                mTimerAction.setVisible(true);
                mActiveTimerToolbarText.setVisible(false);
            }
        }

        // Load timer preferences
        mSound = mSharedPreferences.getBoolean(PREF_WORKOUT_TIMER_SOUND, false);
        mVibrate = mSharedPreferences.getBoolean(PREF_WORKOUT_TIMER_VIBRATE, false);

        /**
         * REFRESH WORKOUT EXERCISES - LOOK TO SEE IF CHANGES MADE FROM EDIT WORKOUT ACTIVITY AND WRITE NEW TABS AND EXERCISES
         */
        final List<Exercise> exercises = new ArrayList<>();
        mDatabase.child("/workout-exercises/").child(mWorkoutKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot exercise : dataSnapshot.getChildren()){
                    Exercise e = exercise.getValue(Exercise.class);
                    exercises.add(e);
                }

                Log.i(TAG, "exercises: " + exercises.size());
                Collections.sort(exercises);

                List<String> tabTitles = new ArrayList<String>();
                for (Exercise e : exercises){
                    tabTitles.add(e.getName());
                }

                mExerciseObjects.clear();
                mExerciseObjects.addAll(exercises);

                mExercisesList.clear();
                mExercisesList.addAll(tabTitles);

                mWorkoutExercisesAdapter = null;
                mWorkoutExercisesAdapter = new WorkoutExercisesAdapter(mFragmentManager);
                mExercisesViewPager.setAdapter(mWorkoutExercisesAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

        // Store state of activity for timer
        mPrefEditor = mSharedPreferences.edit();
        mPrefEditor.putBoolean(PREF_WORKOUT_ACTIVITY_STATE, true);
        mPrefEditor.apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");

        // Store state of activity for timer
        mPrefEditor = mSharedPreferences.edit();
        mPrefEditor.putBoolean(PREF_WORKOUT_ACTIVITY_STATE, false);
        mPrefEditor.apply();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        clearReferences();

        Log.i(TAG, "onDestroy");
    }


    /********* static methods ***********/

    public static int getTimerStartTime(){
        return (int) ((Calendar.getInstance().getTimeInMillis() / 1000));
    }

}
