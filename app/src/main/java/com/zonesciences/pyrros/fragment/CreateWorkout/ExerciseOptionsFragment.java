package com.zonesciences.pyrros.fragment.CreateWorkout;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.Timer.WorkoutTimer;
import com.zonesciences.pyrros.adapters.AddSetExerciseOptionsAdapter;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseOptionsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ExerciseOptionsFragment";

    // Arguments
    private static final String ARG_EXERCISE = "ExerciseToEdit";

    // Data
    Exercise mExercise;
    List<Integer> mSets;
    int mNumReps;

    // Rep Tempo
    TextView mRepTempoTitle;
    TextView mRepTempoText;
    NumberPicker mEccentricNumberPicker;
    NumberPicker mPauseNumberPicker;
    NumberPicker mConcentricPicker;

    // Rest Interval
    TextView mRestIntervalTitle;
    TextView mRestIntervalText;
    NumberPicker mRestIntervalMinutesNumberPicker;
    NumberPicker mRestIntervalSecondsNumberPicker;


    // Recycler and add sets views
    RecyclerView mRecyclerView;
    AddSetExerciseOptionsAdapter mAdapter;
    TextView mNumSetsText;
    EditText mNumRepsField;
    Button mIncreaseReps;
    Button mDecreaseReps;
    Button mAddSet;


    public static ExerciseOptionsFragment newInstance(Exercise exercise){
        ExerciseOptionsFragment fragment = new ExerciseOptionsFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_EXERCISE, exercise);
        fragment.setArguments(args);

        return fragment;
    }

    public ExerciseOptionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        mExercise = getArguments().getParcelable(ARG_EXERCISE);
        mSets = mExercise.getPrescribedReps();
        Log.i(TAG, "Exercise: " + mExercise.getName() + " Prescribed reps: " + mSets);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_exercise_options, container, false);

        // Recycler for adding sets
        mRecyclerView = (RecyclerView)  view.findViewById(R.id.dialog_exercise_options_add_sets_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        if (mSets == null){
            mSets = new ArrayList<Integer>();
        }

        mAdapter = new AddSetExerciseOptionsAdapter(getContext(), mSets);
        mAdapter.setSetRemovedListener(new AddSetExerciseOptionsAdapter.SetRemovedListener() {
            @Override
            public void onSetRemoved() {
                updateSetsInfo();
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        // Rep Tempo
        mRepTempoTitle = (TextView) view.findViewById(R.id.exercise_options_rep_tempo_title_textview);
        mRepTempoTitle.setOnClickListener(this);
        mRepTempoText = (TextView) view.findViewById(R.id.exercise_options_rep_tempo_textview);
        if (mExercise.getRepTempo() != null) {
            mRepTempoText.setText(mExercise.getRepTempo());
        } else {
            mRepTempoText.setText("Not set");
        }
        mRepTempoText.setOnClickListener(this);

        // Rest Interval
        mRestIntervalTitle = (TextView) view.findViewById(R.id.exercise_options_rest_interval_title_textview);
        mRestIntervalTitle.setOnClickListener(this);

        mRestIntervalText = (TextView) view.findViewById(R.id.exercise_options_rest_interval_textview);
        if (mExercise.getRestInterval() == 0){
            mRestIntervalText.setText("Not set");
        } else {
            mRestIntervalText.setText(Utils.timeToDisplay(mExercise.getRestInterval() * 1000).get(Utils.MINUTES) + ":" + Utils.timeToDisplay(mExercise.getRestInterval() * 1000).get(Utils.SECONDS));
        }
        mRestIntervalText.setOnClickListener(this);

        // Add sets
        mNumSetsText = (TextView) view.findViewById(R.id.exercise_options_num_sets_textview);
        updateSetsInfo();

        mNumRepsField = (EditText) view.findViewById(R.id.exercise_options_num_reps_edit_text);
        mDecreaseReps = (Button) view.findViewById(R.id.exercise_options_decrease_reps_button);
        mDecreaseReps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNumRepsField.getText().toString().isEmpty()) {
                    mNumReps = Integer.parseInt(mNumRepsField.getText().toString());
                } else {
                    mNumReps = 0;
                }
                if (mNumReps > 0){
                    mNumReps--;
                }
                mNumRepsField.setText("" + mNumReps);
            }
        });
        mIncreaseReps = (Button) view.findViewById(R.id.exercise_options_increase_reps_button);
        mIncreaseReps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNumRepsField.getText().toString().isEmpty()) {
                    mNumReps = Integer.parseInt(mNumRepsField.getText().toString());
                } else {
                    mNumReps = 0;
                }
                mNumReps++;
                mNumRepsField.setText("" + mNumReps);
            }
        });

        mAddSet = (Button) view.findViewById(R.id.exercise_options_add_set);
        mAddSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSets == null){
                    mSets = new ArrayList<Integer>();
                }
                mSets.add(Integer.parseInt(mNumRepsField.getText().toString()));
                mExercise.setPrescribedReps(mSets);
                mAdapter.notifyDataSetChanged();
                updateSetsInfo();
            }
        });

        return view;
    }

    private void updateSetsInfo() {
        if (mSets == null || mSets.size() == 0) {
            mNumSetsText.setText("No sets added");
        } else {
            int numSets = mSets.size();
            String setsInfo;
            boolean sameReps = false;
            int repsLow = Collections.min(mSets);
            int repsHigh = Collections.max(mSets);
            if (repsLow == repsHigh) sameReps = true;
            if (sameReps) {
                setsInfo = numSets + " sets of " + repsHigh + " reps";
            } else {
                setsInfo = numSets + " sets of " + repsLow + "-" + repsHigh + " reps";
            }
            mNumSetsText.setText(setsInfo);
        }
    }

    public void showRepTempoDialog(){

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_rep_tempo, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        // Get tempo values if already exist
        String oldRepTempo = mExercise.getRepTempo();
        Log.i(TAG, "OLD REP TEMPO: " + oldRepTempo);
        int eccentric = 0;
        int pause = 0;
        int concentric = 0;

        if (oldRepTempo != null){
            String[] tempo = oldRepTempo.split("-");
            Log.i(TAG, "TEMPO AFTER SPLIT: " + tempo[0] + tempo [1] + tempo[2]);
            eccentric = Integer.parseInt(tempo[0]);
            pause = Integer.parseInt(tempo[1]);
            concentric = Integer.parseInt(tempo[2]);
            Log.i(TAG, "TEMPO AFTER SPLIT: " + eccentric + pause + concentric);
        }

        // Eccentric picker
        mEccentricNumberPicker = (NumberPicker) dialogView.findViewById(R.id.eccentric_number_picker);
        mEccentricNumberPicker.setMinValue(0);
        mEccentricNumberPicker.setMaxValue(30);
        mEccentricNumberPicker.setValue(eccentric);

        // Pause picker
        mPauseNumberPicker = (NumberPicker) dialogView.findViewById(R.id.pause_number_picker);
        mPauseNumberPicker.setMinValue(0);
        mPauseNumberPicker.setMaxValue(30);
        mPauseNumberPicker.setValue(pause);

        // Concentric picker
        mConcentricPicker = (NumberPicker) dialogView.findViewById(R.id.concentric_number_picker);
        mConcentricPicker.setMinValue(0);
        mConcentricPicker.setMaxValue(30);
        mConcentricPicker.setValue(concentric);


        builder
                .setTitle("Rep Tempo")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialogBox, int id){
                        String eccentric = Integer.toString(mEccentricNumberPicker.getValue());
                        String pause = Integer.toString(mPauseNumberPicker.getValue());
                        String concentric = Integer.toString(mConcentricPicker.getValue());
                        String newRepTempo = eccentric + "-" + pause + "-" + concentric;
                        Log.i(TAG, "Rep Tempo: " + newRepTempo);
                        mExercise.setRepTempo(newRepTempo);
                        mRepTempoText.setText(newRepTempo);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.i(TAG, "onDismiss");
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialogBox, int id){
                        dialogBox.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showRestIntervalDialog(){

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_rest_interval, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        int mins;
        int secs;

        mins = (int) (mExercise.getRestInterval() / 60);
        secs = (int) mExercise.getRestInterval() - (mins * 60);

        // Minutes number picker
        mRestIntervalMinutesNumberPicker = (NumberPicker) dialogView.findViewById(R.id.rest_interval_mins_number_picker);
        mRestIntervalMinutesNumberPicker.setMinValue(0);
        mRestIntervalMinutesNumberPicker.setMaxValue(15);
        mRestIntervalMinutesNumberPicker.setValue(mins);

        // Seconds number picker
        mRestIntervalSecondsNumberPicker = (NumberPicker) dialogView.findViewById(R.id.rest_interval__secs_number_picker);
        mRestIntervalSecondsNumberPicker.setMinValue(0);
        mRestIntervalSecondsNumberPicker.setMaxValue(59);
        mRestIntervalSecondsNumberPicker.setValue(secs);

        builder
                .setTitle("Rest interval")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialogBox, int id){
                        int mins = mRestIntervalMinutesNumberPicker.getValue();
                        int secs = mRestIntervalSecondsNumberPicker.getValue();
                        int restInterval = (mins * 60) + secs;
                        mExercise.setRestInterval(restInterval);
                        Log.i(TAG, "Rest interval: " + restInterval);
                        mRestIntervalText.setText(Utils.timeToDisplay(mExercise.getRestInterval() * 1000).get(Utils.MINUTES) + ":" + Utils.timeToDisplay(mExercise.getRestInterval() * 1000).get(Utils.SECONDS));
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.i(TAG, "onDismiss");
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialogBox, int id){
                        dialogBox.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    // Click listener methods
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.exercise_options_rep_tempo_textview || view.getId() == R.id.exercise_options_rep_tempo_title_textview) {
            showRepTempoDialog();
        }
        if (view.getId() == R.id.exercise_options_rest_interval_textview || view.getId() == R.id.exercise_options_rest_interval_title_textview) {
            showRestIntervalDialog();
        }
    }
}
