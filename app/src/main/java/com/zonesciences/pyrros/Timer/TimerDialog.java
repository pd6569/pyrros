package com.zonesciences.pyrros.Timer;

import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.WorkoutActivity;

/**
 * Created by Peter on 14/12/2016.
 */

public class TimerDialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "TimerDialog";

    WorkoutActivity mActivity;

    // View
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
    ImageView mDeleteTimerImageView;

    //Check box
    CheckBox mSoundCheckBox;
    CheckBox mVibrateCheckBox;
    CheckBox mAutoStartCheckBox;

    // Notification settings
    boolean mSound;
    boolean mVibrate;

    // Timer variables
    WorkoutTimer mCountDownTimer;
    int mTimeToSet;
    TimerState mTimerState;
    WorkoutTimerReference mWorkoutTimerReference;

    // Dialog
    AlertDialog mAlertDialog;


    // Listener
    ExerciseTimerListener mExerciseTimerListener;


    public TimerDialog(WorkoutActivity activity) {
        this.mActivity = activity;
        mWorkoutTimerReference = WorkoutTimerReference.getWorkoutTimerReference();
    }

    public void createDialog() {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View dialogView = inflater.inflate(R.layout.dialog_timer, null);

        // timer state set
        mTimerState = mActivity.getTimerState();
        mCountDownTimer = mWorkoutTimerReference.getWorkoutTimer();

        mSetTimerField = (EditText) dialogView.findViewById(R.id.timer_edit_text);

        mStartTimerImageView = (ImageView) dialogView.findViewById(R.id.timer_start);
        mStartTimerImageView.setOnClickListener(this);

        mPauseTimerImageView = (ImageView) dialogView.findViewById(R.id.timer_pause);
        mPauseTimerImageView.setOnClickListener(this);

        mCountDownText = (TextView) dialogView.findViewById(R.id.timer_countdown_text);
        mCountDownProgressBar = (ProgressBar) dialogView.findViewById(R.id.timer_progress_bar);

        mIncreaseTimeButton = (Button) dialogView.findViewById(R.id.timer_increase_time_button);
        mIncreaseTimeButton.setOnClickListener(this);

        mDecreaseTimeButton = (Button) dialogView.findViewById(R.id.timer_decrease_time_button);
        mDecreaseTimeButton.setOnClickListener(this);

        mDeleteTimerImageView = (ImageView) dialogView.findViewById(R.id.timer_delete);
        mDeleteTimerImageView.setOnClickListener(this);

        mLayoutTimerSetTimer = (RelativeLayout) dialogView.findViewById(R.id.timer_layout_set_timer);
        mLayoutTimerOptions = (LinearLayout) dialogView.findViewById(R.id.timer_layout_set_options);
        mLayoutTimerProgress = (RelativeLayout) dialogView.findViewById(R.id.timer_layout_circular_timer);

        mSoundCheckBox = (CheckBox) dialogView.findViewById(R.id.timer_sound_checkbox);
        mSoundCheckBox.setChecked(mSound);
        mSoundCheckBox.setOnCheckedChangeListener(this);

        mVibrateCheckBox = (CheckBox) dialogView.findViewById(R.id.timer_vibrate_checkbox);
        mVibrateCheckBox.setChecked(mVibrate);
        mVibrateCheckBox.setOnCheckedChangeListener(this);

        mAutoStartCheckBox = (CheckBox) dialogView.findViewById(R.id.timer_autostart_checkbox);
        mAutoStartCheckBox.setOnCheckedChangeListener(this);

        if (!mTimerState.isTimerFirstStart()) {

            // timer has been started before, timer has been resumed to progress view
            Log.i(TAG, "Timer has been started before. Time remaining: " + mTimerState.getTimeRemaining());
            setTimerOptionsVisible(false);
            mCountDownProgressBar.setMax(mTimerState.getCurrentProgressMax());

            if (mCountDownTimer != null){
                mCountDownText.setText(WorkoutTimer.timeToDisplay(mCountDownTimer.getTimeRemaining()).get(WorkoutTimer.MINUTES) + ":" + WorkoutTimer.timeToDisplay(mCountDownTimer.getTimeRemaining()).get(WorkoutTimer.SECONDS));
            } else {
                mCountDownText.setText(WorkoutTimer.timeToDisplay(mTimerState.getTimeRemaining()).get(WorkoutTimer.MINUTES) + ":" + WorkoutTimer.timeToDisplay(mTimerState.getTimeRemaining()).get(WorkoutTimer.SECONDS));
            }


            if (mTimerState.isTimerRunning()) {
                Log.i(TAG, "Timer resumed, timer running. Time remaning: " + mTimerState.getTimeRemaining());

                // pass countdown text and progress views to timer to update
                mCountDownTimer.setCountDownText(mCountDownText);
                mCountDownTimer.setCountDownProgressBar(mCountDownProgressBar);
                mCountDownTimer.setDialogOpen(true);

                mStartTimerImageView.setVisibility(View.GONE);
                mPauseTimerImageView.setVisibility(View.VISIBLE);
            } else {
                Log.i(TAG, "Timer resumed, timer paused. Current Progress: " + mTimerState.getCurrentProgress());

                // set correct progress bar value to display progress in paused state
                mCountDownProgressBar.setProgress(mTimerState.getCurrentProgress());

                mStartTimerImageView.setVisibility(View.VISIBLE);
                mPauseTimerImageView.setVisibility(View.GONE);
            }
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setView(dialogView);
        builder.setCancelable(true);
        mAlertDialog = builder.create();
        mAlertDialog.show();
        mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.i(TAG, "Count Down Dialog dismissed");
                // Notify activity and update with variables to store when timer is resumed.
                mExerciseTimerListener.onExerciseTimerDismissed();

            }
        });

    }

    private void setTimerOptionsVisible(boolean visible) {
        if (visible) {
            mLayoutTimerOptions.setVisibility(View.VISIBLE);
            mLayoutTimerSetTimer.setVisibility(View.VISIBLE);

            mLayoutTimerProgress.setVisibility(View.GONE);
            mStartTimerImageView.setVisibility(View.GONE);
            mPauseTimerImageView.setVisibility(View.GONE);
            mDeleteTimerImageView.setVisibility(View.GONE);

        } else {
            mLayoutTimerOptions.setVisibility(View.GONE);
            mLayoutTimerSetTimer.setVisibility(View.GONE);

            mLayoutTimerProgress.setVisibility(View.VISIBLE);
            mDeleteTimerImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Click listener methods
     */

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.timer_start:
                Log.i(TAG, "Start timer. Timer already started: " + !mTimerState.isTimerRunning());

                if (mTimerState.isTimerFirstStart()) {

                    if (!mSetTimerField.getText().toString().isEmpty()) {

                        setTimerOptionsVisible(false);
                        mStartTimerImageView.setVisibility(View.GONE);
                        mPauseTimerImageView.setVisibility(View.VISIBLE);

                        int timerDuration = Integer.parseInt(mSetTimerField.getText().toString());

                        mSetTimerField.setEnabled(false);

                        Log.i(TAG, "mCountDown text: " +mCountDownText + " mprogressbar: " + mCountDownProgressBar);

                        mCountDownProgressBar.setMax(timerDuration * 100);

                        mTimerState.setTimerFirstStart(false);

                        // Notify activity of timer creation and pass timer instance to activity
                        mExerciseTimerListener.onExerciseTimerCreated(timerDuration, mVibrate, mSound);

                    } else {
                        Log.i(TAG, "Error: Enter number, dickhead");
                        Toast.makeText(mActivity, "Enter a number for rest timer", Toast.LENGTH_SHORT).show();
                        return;
                    }

                } else {
                    // There is an existing timer active, destroy old timer and create new one, to resume where left off.

                    resumeTimer();

                }
                break;

            case R.id.timer_pause:
                pauseTimer();

                break;
            case R.id.timer_increase_time_button:
                if (mSetTimerField.getText().toString().equals("")) {
                    mTimeToSet = 0;
                } else {
                    mTimeToSet = Integer.parseInt(mSetTimerField.getText().toString());
                }
                mTimeToSet++;
                mSetTimerField.setText("" + mTimeToSet);
                break;
            case R.id.timer_decrease_time_button:
                if (mSetTimerField.getText().toString().equals("")) {
                    mTimeToSet = 0;
                } else {
                    mTimeToSet = Integer.parseInt(mSetTimerField.getText().toString());
                }
                if (mTimeToSet > 0) mTimeToSet--;

                mSetTimerField.setText("" + mTimeToSet);
                break;
            default:
                break;

            case R.id.timer_delete:

                // Update View
                setTimerOptionsVisible(true);
                mSetTimerField.setEnabled(true);
                mStartTimerImageView.setVisibility(View.VISIBLE);

                // Reset timer variables
                mActivity.resetTimer();

                // Notify activity
                mExerciseTimerListener.onExerciseTimerReset();

                break;
        }
    }

    /*
     * Checkbox listener actions
     */

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        String onOff;
        if (isChecked){
            onOff = "on";
        } else {
            onOff = "off";
        }
        switch (compoundButton.getId()){
            case R.id.timer_sound_checkbox:
                Toast.makeText(mActivity, "Sound is " + onOff, Toast.LENGTH_SHORT).show();
                mSound = isChecked;
                break;
            case R.id.timer_vibrate_checkbox:
                Toast.makeText(mActivity, "Vibrate is " + onOff, Toast.LENGTH_SHORT).show();
                mVibrate = isChecked;
                break;
            case R.id.timer_autostart_checkbox:
                Toast.makeText(mActivity, "Autostart is " + onOff, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // Timer actions:

    public void pauseTimer(){

        // update view
        mSetTimerField.setEnabled(true);
        mStartTimerImageView.setVisibility(View.VISIBLE);
        mPauseTimerImageView.setVisibility(View.GONE);

        // Notify activity
        mExerciseTimerListener.onExerciseTimerPaused(true);
    }

    public void resumeTimer(){

        mStartTimerImageView.setVisibility(View.GONE);
        mPauseTimerImageView.setVisibility(View.VISIBLE);

        // notify activity that timer has been resumed:
        mExerciseTimerListener.onExerciseTimerResumed(true);
    }

    // Listener

    public void setExerciseTimerListener (ExerciseTimerListener listener){
        this.mExerciseTimerListener = listener;
    }

    // Getters

    public int getCurrentProgress() {
        return mCountDownProgressBar.getProgress();
    }

    public int getCurrentProgressMax() {
        return mCountDownProgressBar.getMax();
    }


    public AlertDialog getAlertDialog() {
        return mAlertDialog;
    }

    public ProgressBar getCountDownProgressBar() {
        return mCountDownProgressBar;
    }

    public TextView getCountDownText() {
        return mCountDownText;
    }

    // Setters

    public void setSound(boolean sound) {
        mSound = sound;
    }

    public void setVibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    // other methods

    private void resetTimer(){

    }

    public void displayTimerOptions(){
        mSetTimerField.setText("");
        mSetTimerField.setEnabled(true);
        setTimerOptionsVisible(true);
        mStartTimerImageView.setVisibility(View.VISIBLE);
        mPauseTimerImageView.setVisibility(View.GONE);
    }




    /**
     * Workout Timer Class
     */

   /* public class WorkoutTimerDialog extends CountDownTimer {


        int progress;
        long timeRemaining;
        int timerDurationSecs;


        public WorkoutTimerDialog(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            progress = (int) millisInFuture / 1000;
            timerDurationSecs = (int)millisInFuture / 1000;
        }


        @Override
        public void onTick(long millisUntilFinished) {

            timeRemaining = millisUntilFinished;
            int timeRemainingSecs = (int) (millisUntilFinished / 1000) + 1;

            int i = (int) (millisUntilFinished / 1000);
            if (i != progress) {
                progress = i;
                mCountDownText.setText(WorkoutTimer.timeToDisplay(millisUntilFinished).get(WorkoutTimer.MINUTES) + ":" + WorkoutTimer.timeToDisplay(millisUntilFinished).get(WorkoutTimer.SECONDS));

            }

            int progressBarUpdate = (int) (millisUntilFinished / 10);
            mCountDownProgressBar.setProgress(mCountDownProgressBar.getMax() - progressBarUpdate);

        }

        @Override
        public void onFinish() {
            mTimerFirstStart = true;
            mTimerRunning = false;
            mHasActiveTimer = false;
            Log.i(TAG, "Timer finished");
            mSetTimerField.setText("");
            mSetTimerField.setEnabled(true);
            setTimerOptionsVisible(true);
            mStartTimerImageView.setVisibility(View.VISIBLE);
            mPauseTimerImageView.setVisibility(View.GONE);

            mExerciseTimerListener.onExerciseTimerFinished();
        }

        public long getTimeRemaining() {
            return timeRemaining;
        }

        public void setTimeRemaining(long timeRemaining){
            this.timeRemaining = timeRemaining;
        }

        public int getTimerDurationSecs() {
            return timerDurationSecs;
        }

        public int getProgress() {
            return progress;
        }
    }*/
}



