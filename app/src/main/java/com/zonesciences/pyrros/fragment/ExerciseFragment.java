package com.zonesciences.pyrros.fragment;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;
import com.zonesciences.pyrros.ActionMode.ActionModeCallback;
import com.zonesciences.pyrros.ActionMode.ActionModeInterface;
import com.zonesciences.pyrros.ActionMode.RecyclerClickListener;
import com.zonesciences.pyrros.ActionMode.RecyclerTouchListener;
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperCallback;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.SetsAdapter;
import com.zonesciences.pyrros.datatools.DataTools;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExerciseFragment extends Fragment implements View.OnClickListener, ActionModeInterface {

    public static final String TAG = "ExerciseFragment";
    public static final String ARG_EXERCISE_KEY = "ExerciseKey";
    public static final String ARG_WORKOUT_KEY = "WorkoutKey";
    public static final String ARG_USER_ID = "UserId";
    private static final String ARG_EXERCISE_OBJECT = "ExerciseObject";

    //Views
    LinearLayout mAddSetsContainerLinearLayout;
    ImageView mCloseEditSetImageView;
    TextView mSetNumberTitle;
    TextView mWeightTextView;
    Button mIncreaseWeightButton;
    Button mDecreaseWeightButton;
    Button mIncreaseRepsButton;
    Button mDecreaseRepsButton;
    Button mAddSet;
    Button mSaveSet;
    EditText mWeightField;
    EditText mRepsField;
    ImageView mTimer;

    // Timer
    CustomCountDownTimer mCountDownTimer;
    ProgressBar mCountDownProgressBar;
    EditText mSetTimerField;
    TextView mCountDownText;
    Button mIncreaseTimeButton;
    Button mDecreaseTimeButton;
    ImageView mStartTimerImageView;
    ImageView mPauseTimerImageView;
    RelativeLayout mLayoutTimerSetTimer;
    LinearLayout mLayoutTimerOptions;
    RelativeLayout mLayoutTimerProgress;




    //Recycler view components
    RecyclerView mSetsRecycler;
    SetsAdapter mSetsAdapter;
    LinearLayoutManager mLayoutManager;
    DividerItemDecoration mDividerItemDecoration;

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

    //Exercise history and stats
    List<String> mStatsExerciseWorkoutKeys;
    List<String> mStatsExerciseDates;
    List<Exercise> mStatsExercises;
    Map<String, Exercise> mExerciseHistoryMap;
    boolean mStatsDataLoaded;

    //Exercise records
    Record mRecord;

    //Units and conversion
    String mUnitSystem;
    String mUnits;
    double mConversionMultiple;

    //DataTools
    DataTools mDataTools;

    //Listener
    OnStatsDataLoaded mStatsDataLoadedListener;
    ExerciseTimerListener mExerciseTimerListener;

    // ActionMode
    ActionMode mActionMode;

    // Edit sets mode
    boolean mEditSetMode;
    int mSetSelected = -1;
    int mPreviousSetSelected = -1;

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

        Log.i(TAG, "mExercise obtained. Exercise name: " + mExercise.getName() + "mExercise order: " + mExercise.getOrder());

        mUser = bundle.getString(ARG_USER_ID);
        mCurrentSet = mExercise.getSets() + 1;

        //Load data for exercise history and for statistics
        mDataTools = new DataTools(mUser, mExerciseKey);
        mDataTools.loadExercises(); //loads exercises AND workout keys
        mDataTools.loadRecord();
        mDataTools.setOnDataLoadCompleteListener(new DataTools.OnDataLoadCompleteListener() {
            @Override
            public void onExercisesLoadComplete() {
                Log.i(TAG, "Exercises and workout keys loaded");
                mStatsExercises = mDataTools.getExercises();
                mStatsExerciseWorkoutKeys = mDataTools.getWorkoutKeys();
                mDataTools.loadWorkoutDates(mStatsExerciseWorkoutKeys);
            }

            @Override
            public void onWorkoutDatesLoadComplete() {
                Log.i(TAG, "Workout dates loaded");
                mStatsExerciseDates = mDataTools.getExerciseDates();
                mStatsDataLoaded = true;

                // only need to notify workout activity that data is loaded, if exercise fragments
                // methods are being called before this data is loaded. In this case the listener
                // will be set by the activity and will not be null.

                if (mStatsDataLoadedListener != null) {
                    mStatsDataLoadedListener.statsDataLoaded();
                }

            }
            @Override
            public void onWorkoutKeysLoadComplete() {

            }

            @Override
            public void onExerciseRecordLoadComplete() {
                Log.i(TAG, "Exercise record loaded");
                mRecord = mDataTools.getExerciseRecord();
                if (mRecord == null){
                    Log.i(TAG, "mRecord is null");
                    mRecord = new Record(mExerciseKey, mUser);
                    mDataTools.setExerciseRecord(mRecord);
                }
            }
        });

        //Load records for exercise


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
            public void onSetSelected(final int setIndex){
                Log.i(TAG, "Set has been clicked. Weight: " + mExercise.getWeight().get(setIndex) + " Reps: " + mExercise.getReps().get(setIndex));
                mWeightField.setText(mExercise.getWeight().get(setIndex).toString());
                mRepsField.setText(mExercise.getReps().get(setIndex).toString());
                mSetSelected = setIndex;

                mLayoutManager.findViewByPosition(mSetSelected).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

                if (mEditSetMode == true && mPreviousSetSelected == mSetSelected){
                    // we are in edit mode, and user has clicked on the highlighted set. Turn edit mode OFF
                    mEditSetMode = false;
                    mSetsAdapter.setSetBeingEdited(-1); // next time recycler is adapter it will NOT highlight the set
                    mLayoutManager.findViewByPosition(mPreviousSetSelected).setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
                } else if (mEditSetMode == false && mPreviousSetSelected == mSetSelected){
                    // edit mode is off, but set that is selected is the same as the last set that was selected, set edit mode true and highlight the set
                    mEditSetMode = true;
                    mLayoutManager.findViewByPosition(mPreviousSetSelected).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                } else if (mPreviousSetSelected > -1){
                    // turn edit mode on and deselect the previous set
                    mEditSetMode = true;

                    // if sets are deleted then previous set selected may no longer exist and will throw null pointer exception, therefore check
                    if (mPreviousSetSelected < mExercise.getReps().size()) {
                        mLayoutManager.findViewByPosition(mPreviousSetSelected).setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
                    }
                } else {
                    mEditSetMode = true;
                }


                if (mEditSetMode){

                    mSetNumberTitle.setText("Set " + (setIndex + 1));
                    mSetNumberTitle.setTypeface(null, Typeface.BOLD_ITALIC);
                    mCloseEditSetImageView.setVisibility(View.VISIBLE);
                    mCloseEditSetImageView.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            closeEditMode();
                        }
                    });

                    mAddSet.setVisibility(View.GONE);
                    mSaveSet.setVisibility(View.VISIBLE);
                } else {
                    closeEditMode();
                }

                mPreviousSetSelected = mSetSelected;
            }


            @Override
            public void onSetsChanged() {
                Log.i(TAG, "Set changed in adapter. Exercise Fragment received callback");
                getExercise();
            }

            @Override
            public void onRecordChanged(){
                Log.i(TAG, "Callback received from sets adapter, record has been changed, so update record variable with latest data");
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
                        mRecord = mDataTools.getExerciseRecord();
                    }
                });
            }
        });

        mSetsAdapter.notifyDataSetChanged();

    }

    public void closeEditMode(){
        mEditSetMode = false;
        mSetNumberTitle.setText("Set " + (mExercise.getReps().size() + 1));
        mSetNumberTitle.setTypeface(null, Typeface.BOLD);
        mAddSet.setVisibility(View.VISIBLE);
        mSaveSet.setVisibility(View.GONE);
        mCloseEditSetImageView.setVisibility(View.INVISIBLE);
        mLayoutManager.findViewByPosition(mPreviousSetSelected).setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        mAddSetsContainerLinearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_add_set_container);
        mCloseEditSetImageView = (ImageView) view.findViewById(R.id.imagview_edit_set_close);

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

        mSaveSet = (Button) view.findViewById(R.id.button_save_set);
        mSaveSet.setOnClickListener(this);

        mAddSet = (Button) view.findViewById(R.id.button_add_set);
        mAddSet.setOnClickListener(this);


        mWeightField = (EditText) view.findViewById(R.id.field_weight);
        mRepsField = (EditText) view.findViewById(R.id.field_reps);

        mSetNumberTitle.setText("Set " + mCurrentSet);

        mTimer = (ImageView) view.findViewById(R.id.image_timer);
        mTimer.setOnClickListener(this);


        mSetsAdapter.notifyDataSetChanged(); // this ensures that data is reloaded when recreating the view after swiping from other exercises
        mSetsRecycler = (RecyclerView) view.findViewById(R.id.recycler_sets);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mSetsRecycler.setLayoutManager(mLayoutManager);
        mDividerItemDecoration = new DividerItemDecoration(mSetsRecycler.getContext(), mLayoutManager.getOrientation());
        mSetsRecycler.addItemDecoration(mDividerItemDecoration);
        mSetsRecycler.setHasFixedSize(true);
        mSetsRecycler.setAdapter(mSetsAdapter);
        mSetsRecycler.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mSetsRecycler, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (mActionMode != null){
                    // select with single click if action mode is active
                    onItemSelected(position);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                onItemSelected(position);
            }
        }));

        return view;
    }


    /****** START ACTION MODE METHODS ******/

    @Override
    public void onItemSelected(int position) {
        mSetsAdapter.toggleSelection(position);

        if (mEditSetMode){
            closeEditMode();
        }

        boolean hasSelectedSets = mSetsAdapter.getSelectedCount() > 0;

        if (hasSelectedSets && mActionMode == null){
            // there are some selected items, start the action mode
            Log.i(TAG, "Start action mode");
            ActionModeCallback actionModeCallback = new ActionModeCallback();
            actionModeCallback.setSetsAdapter(mSetsAdapter);
            actionModeCallback.setOnFinishedActionModeListener(new ActionModeCallback.onFinishedActionMode() {
                @Override
                public void onActionModeFinished() {
                    if (mActionMode != null){
                        mActionMode.finish();
                        setActionModeNull();
                    }
                }
            });
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
        }
        else if (!hasSelectedSets && mActionMode != null){
            // no selected items, finish action mode
            Log.i(TAG, "End action mode");
            mActionMode.finish();
            setActionModeNull();
            mSetsAdapter.notifyDataSetChanged();
        }
        if (mActionMode != null){
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(mSetsAdapter.getSelectedCount()) + " selected");

        }
    }

    @Override
    public void setActionModeNull() {
        if (mActionMode != null){
            mActionMode = null;
        }
    }

    /****** END ACTION MODE ******/

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
            case R.id.button_save_set:
                replaceSet(mSetSelected, view);
                break;
            case R.id.image_timer:
                setTimer();
                break;
        }
    }


    /***
     * TODO:// Sort out this shit (but working) code
     *
     * TIMER FUNCTIONALITY HERE. VERY MESSY SHIT CODE. NEEDS NEATENING UP.
     */

    long timeRemaining;
    int timeRemainingToDisplay;
    boolean timerFirstStart = true;
    boolean timerRunning;
    int currentProgress;
    int currentProgressMax;
    boolean hasActiveTimer;


    private void setTimer(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_timer, null);

        mSetTimerField = (EditText) dialogView.findViewById(R.id.timer_edit_text);

        mStartTimerImageView = (ImageView) dialogView.findViewById(R.id.timer_start);
        mPauseTimerImageView = (ImageView) dialogView.findViewById(R.id.timer_pause);
        mCountDownText = (TextView) dialogView.findViewById(R.id.timer_countdown_text);
        mCountDownProgressBar = (ProgressBar) dialogView.findViewById(R.id.timer_progress_bar);

        mIncreaseTimeButton = (Button) dialogView.findViewById(R.id.timer_increase_time_button);
        mIncreaseTimeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                int timeToSet;
                if(mSetTimerField.getText().toString().equals("")) {
                    timeToSet = 0;
                } else {
                    timeToSet = Integer.parseInt(mSetTimerField.getText().toString());
                }
                timeToSet++;
                mSetTimerField.setText("" + timeToSet);
            }
        });

        mDecreaseTimeButton = (Button) dialogView.findViewById(R.id.timer_decrease_time_button);
        mDecreaseTimeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                int timeToSet;
                if(mSetTimerField.getText().toString().equals("")) {
                    timeToSet = 0;
                } else {
                    timeToSet = Integer.parseInt(mSetTimerField.getText().toString());
                }
                if (timeToSet > 0) timeToSet--;

                mSetTimerField.setText("" + timeToSet);
            }
        });

        mLayoutTimerSetTimer = (RelativeLayout) dialogView.findViewById(R.id.timer_layout_set_timer);
        mLayoutTimerOptions = (LinearLayout) dialogView.findViewById(R.id.timer_layout_set_options);
        mLayoutTimerProgress = (RelativeLayout) dialogView.findViewById(R.id.timer_layout_circular_timer);

        if (!timerFirstStart){
            // timer has been started before, timer has been resumed to progress view

            setTimerOptionsVisible(false);
            mCountDownProgressBar.setMax(currentProgressMax);
            mCountDownProgressBar.setProgress(currentProgress);
            mCountDownText.setText("" + timeRemainingToDisplay);

            mPauseTimerImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Notify activity
                    mExerciseTimerListener.onExerciseTimerPaused();

                    timerRunning = false;
                    mCountDownTimer.cancel();
                    mSetTimerField.setEnabled(true);
                    mStartTimerImageView.setVisibility(View.VISIBLE);
                    mPauseTimerImageView.setVisibility(View.GONE);

                    timeRemaining = mCountDownTimer.getTimeRemaining();
                }
            });

            if (timerRunning){
                mStartTimerImageView.setVisibility(View.GONE);
                mPauseTimerImageView.setVisibility(View.VISIBLE);
            } else {
                mStartTimerImageView.setVisibility(View.VISIBLE);
                mPauseTimerImageView.setVisibility(View.GONE);
            }
        }


        mStartTimerImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Start timer. Timer already started: " + !timerFirstStart);

                timerRunning = true;
                hasActiveTimer = true;



                if (timerFirstStart) {

                    // Notify activity of timer creation
                    mExerciseTimerListener.onExerciseTimerCreated();

                    if (!mSetTimerField.getText().toString().isEmpty()) {

                        setTimerOptionsVisible(false);
                        mStartTimerImageView.setVisibility(View.GONE);
                        mPauseTimerImageView.setVisibility(View.VISIBLE);

                        mPauseTimerImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Notify activity
                                mExerciseTimerListener.onExerciseTimerPaused();

                                timerRunning = false;
                                mCountDownTimer.cancel();
                                mSetTimerField.setEnabled(true);
                                mStartTimerImageView.setVisibility(View.VISIBLE);
                                mPauseTimerImageView.setVisibility(View.GONE);

                                timeRemaining = mCountDownTimer.getTimeRemaining();
                            }
                        });


                        int timer = Integer.parseInt(mSetTimerField.getText().toString());
                        int milliseconds = timer * 1000;

                        mSetTimerField.setEnabled(false);

                        mCountDownProgressBar.setMax(timer * 100);
                        mCountDownTimer = new CustomCountDownTimer(milliseconds, 10);
                        mCountDownTimer.start();

                        timerFirstStart = false;
                    } else {
                        Log.i(TAG, "Error: Enter number, dickhead");
                        Toast.makeText(getContext(), "Enter a number for rest timer", Toast.LENGTH_SHORT).show();
                        return;
                    }

                } else {
                    // There is an existing timer active, destroy old timer and create new one, to resume where left off.

                    // notify activity that timer has been resumed:
                    mExerciseTimerListener.onExerciseTimerResumed();

                    mCountDownTimer = null;
                    mCountDownTimer = new CustomCountDownTimer(timeRemaining, 10);
                    mCountDownTimer.start();

                    mStartTimerImageView.setVisibility(View.GONE);
                    mPauseTimerImageView.setVisibility(View.VISIBLE);
                }
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setCancelable(true);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (mCountDownTimer != null) {
                    timeRemaining = mCountDownTimer.getTimeRemaining();
                    timeRemainingToDisplay = mCountDownTimer.getProgress() + 1;
                    currentProgress = mCountDownProgressBar.getProgress();
                    currentProgressMax = mCountDownProgressBar.getMax();
                }

            }
        });
    }

    public void setTimerOptionsVisible (boolean visible){
        if (visible){
            mLayoutTimerOptions.setVisibility(View.VISIBLE);
            mLayoutTimerSetTimer.setVisibility(View.VISIBLE);

            mLayoutTimerProgress.setVisibility(View.GONE);
            mStartTimerImageView.setVisibility(View.GONE);
            mStartTimerImageView.setVisibility(View.GONE);

        } else {
            mLayoutTimerOptions.setVisibility(View.GONE);
            mLayoutTimerSetTimer.setVisibility(View.GONE);

            mLayoutTimerProgress.setVisibility(View.VISIBLE);
        }
    }


    public class CustomCountDownTimer extends CountDownTimer {

        int progress;
        long timeRemaining;

        public CustomCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            progress = (int) millisInFuture / 1000;
        }

        @Override
        public void onTick(long millisUntilFinished) {

            timeRemaining = millisUntilFinished;

            int i = (int) (millisUntilFinished / 1000);
            if (i != progress) {
                progress = i;
                mCountDownText.setText("" + (progress + 1));
            }

            int progressBarUpdate = (int) (millisUntilFinished / 10);
            mCountDownProgressBar.setProgress(mCountDownProgressBar.getMax() - progressBarUpdate);

        }

        @Override
        public void onFinish() {
            timerFirstStart = true;
            timerRunning = false;
            hasActiveTimer = false;
            Log.i(TAG, "Timer finished");
            mSetTimerField.setText("");
            mSetTimerField.setEnabled(true);
            setTimerOptionsVisible(true);
            mStartTimerImageView.setVisibility(View.VISIBLE);
            mPauseTimerImageView.setVisibility(View.GONE);

            mExerciseTimerListener.onExerciseTimerFinished();
        }

        public long getTimeRemaining (){
            return timeRemaining;
        }

        public int getProgress(){
            return progress;
        }
    }

    /*********** END TIMER FUNCTIONS ***********/


    private void addSet() {
        setWeight();
        setReps();
        mCurrentSet++;

        double convertedWeight = mWeight * mConversionMultiple;

        mSetNumberTitle.setText("Set " + Integer.toString(mCurrentSet));

        mExercise.addWeight(convertedWeight);
        mExercise.addReps(mReps);

        /*boolean record = false;
       if (mDataTools.isRecord(convertedWeight, Integer.toString(mReps), mWorkoutKey)){
           mRecord = mDataTools.getExerciseRecord();
           record = true;
        }*/

        Log.i(TAG, "Exercise object updated with sets. Sets: " + mExercise.getSets() + " Weights: " + mExercise.getWeight() + " Reps: " + mExercise.getReps());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseKey + "/", mExercise);
        childUpdates.put("/user-workout-exercises/" + mUser + "/" + mWorkoutKey + "/" + mExerciseKey + "/", mExercise);
        /*if (record){
            Log.i(TAG, "Record set, write to database");
            childUpdates.put("/user-records/" + mUser + "/" + mExerciseKey, mRecord);
            childUpdates.put("/records/" + mExerciseKey + "/" + mUser, mRecord);
        }*/
        mDatabase.updateChildren(childUpdates);
    }


    // EDIT METHOD TO REPLACE SET WHEN SAVE SET IS CLICKED

    private void replaceSet(int setIndex, View view) {
        setWeight();
        setReps();

        double convertedWeight = mWeight * mConversionMultiple;

        mExercise.getWeight().set(setIndex, convertedWeight);
        mExercise.getReps().set(setIndex, mReps);


        /*boolean record = false;
        if (mDataTools.isRecord(convertedWeight, Integer.toString(mReps), mWorkoutKey)){
            mRecord = mDataTools.getExerciseRecord();
            record = true;
        }*/

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + mExerciseKey + "/", mExercise);
        childUpdates.put("/user-workout-exercises/" + mUser + "/" + mWorkoutKey + "/" + mExerciseKey + "/", mExercise);
        /*if (record){
            Log.i(TAG, "Record set, write to database");
            childUpdates.put("/user-records/" + mUser + "/" + mExerciseKey, mRecord);
            childUpdates.put("/records/" + mExerciseKey + "/" + mUser, mRecord);
        }*/
        mDatabase.updateChildren(childUpdates);
        mSetsAdapter.setSetBeingEdited(setIndex); // makes sure set is still highlighted after dataset refreshed

        Snackbar snackbar = Snackbar.make(view, "Set updated", Snackbar.LENGTH_SHORT);
        snackbar.show();
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

    // Getters

    public Exercise getCurrentExercise(){
        return mExercise;
    }

    public List<String> getStatsExerciseWorkoutKeys() {
        return mStatsExerciseWorkoutKeys;
    }

    public List<String> getStatsExerciseDates() {
        return mStatsExerciseDates;
    }

    public List<Exercise> getStatsExercises() {
        return mStatsExercises;
    }

    public Record getRecord() {
        return mRecord;
    }

    public boolean isStatsDataLoaded() {
        return mStatsDataLoaded;
    }

    public String getWorkoutKey() {
        return mWorkoutKey;
    }



    // Listener

    public interface OnStatsDataLoaded {
        void statsDataLoaded();
    }

    public void setOnStatsDataLoadedListener (OnStatsDataLoaded listener){
        this.mStatsDataLoadedListener = listener;
    }

    public interface ExerciseTimerListener {
        void onExerciseTimerCreated();
        void onExerciseTimerResumed();
        void onExerciseTimerPaused();
        void onExerciseTimerFinished();
    }

    public void setExerciseTimerListener (ExerciseTimerListener listener){
        this.mExerciseTimerListener = listener;
    }
}
