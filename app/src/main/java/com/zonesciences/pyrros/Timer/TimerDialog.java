package com.zonesciences.pyrros.Timer;

import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zonesciences.pyrros.R;

/**
 * Created by Peter on 14/12/2016.
 */

public class TimerDialog implements View.OnClickListener {

    public static final String TAG = "TimerDialog";

    AppCompatActivity mActivity;

    // View
    WorkoutTimer mCountDownTimer;
    WorkoutTimer mExistingTimer;

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

    // Timer variables
    long mTimeRemaining;
    boolean mTimerFirstStart = true;
    boolean mTimerRunning;
    int mCurrentProgress;
    int mCurrentProgressMax;
    boolean mHasActiveTimer;
    int mTimeToSet;

    // Listener
    ExerciseTimerListener mExerciseTimerListener;


    public TimerDialog(AppCompatActivity activity) {
        this.mActivity = activity;
    }

    public void createDialog() {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View dialogView = inflater.inflate(R.layout.dialog_timer, null);

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

        mLayoutTimerSetTimer = (RelativeLayout) dialogView.findViewById(R.id.timer_layout_set_timer);
        mLayoutTimerOptions = (LinearLayout) dialogView.findViewById(R.id.timer_layout_set_options);
        mLayoutTimerProgress = (RelativeLayout) dialogView.findViewById(R.id.timer_layout_circular_timer);

        if (!mTimerFirstStart) {

            // timer has been started before, timer has been resumed to progress view
            Log.i(TAG, "Timer has been started before");
            setTimerOptionsVisible(false);
            mCountDownProgressBar.setMax(mCurrentProgressMax);
            mCountDownText.setText("" + (int)((mExistingTimer.timeRemaining/1000) + 1));

            if (mTimerRunning) {
                Log.i(TAG, "Timer resumed, timer running. Time remaning: " + mExistingTimer.getTimeRemaining());
                mCountDownTimer = new WorkoutTimer(mExistingTimer.getTimeRemaining(), 10);
                mCountDownTimer.start();
                mExistingTimer = null;
                mStartTimerImageView.setVisibility(View.GONE);
                mPauseTimerImageView.setVisibility(View.VISIBLE);
            } else {
                Log.i(TAG, "Timer resumed, timer paused. Current Progress: " + mCurrentProgress);
                mCountDownProgressBar.setProgress(mCurrentProgress);
                mStartTimerImageView.setVisibility(View.VISIBLE);
                mPauseTimerImageView.setVisibility(View.GONE);
            }
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setView(dialogView);
        builder.setCancelable(true);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.i(TAG, "count down timer" + mCountDownTimer);
                if (mCountDownTimer != null) {
                    mTimeRemaining = mCountDownTimer.getTimeRemaining();
                    mCurrentProgress = mCountDownProgressBar.getProgress();
                    mCurrentProgressMax = mCountDownProgressBar.getMax();

                    // Notify activity and update with variables to store when timer is resumed.
                    /*mExerciseTimerListener.onExerciseTimerDismissed(mTimerRunning, mTimeRemaining, mTimeRemainingToDisplay, mCurrentProgress, mCurrentProgressMax);*/
                    mExerciseTimerListener.onExerciseTimerDismissed(mTimerRunning, mCountDownTimer, mCurrentProgress, mCurrentProgressMax);

                    // Cancel timer otherwise will get multiple onFinish notifications from multiple timer objects.
                    mCountDownTimer.cancel();
                }

            }
        });
    }

    public void setTimerOptionsVisible(boolean visible) {
        if (visible) {
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

    /**
     * Click listener methods
     */

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.timer_start:
                Log.i(TAG, "Start timer. Timer already started: " + !mTimerFirstStart);

                mTimerRunning = true;
                mHasActiveTimer = true;


                if (mTimerFirstStart) {

                    if (!mSetTimerField.getText().toString().isEmpty()) {

                        setTimerOptionsVisible(false);
                        mStartTimerImageView.setVisibility(View.GONE);
                        mPauseTimerImageView.setVisibility(View.VISIBLE);

                        int timer = Integer.parseInt(mSetTimerField.getText().toString());
                        int milliseconds = timer * 1000;

                        mSetTimerField.setEnabled(false);

                        mCountDownProgressBar.setMax(timer * 100);
                        mCountDownTimer = new WorkoutTimer(milliseconds, 10);
                        mCountDownTimer.start();

                        mTimerFirstStart = false;

                        // Notify activity of timer creation and pass timer instance to activity
                        mExerciseTimerListener.onExerciseTimerCreated(mCountDownTimer);

                    } else {
                        Log.i(TAG, "Error: Enter number, dickhead");
                        Toast.makeText(mActivity, "Enter a number for rest timer", Toast.LENGTH_SHORT).show();
                        return;
                    }

                } else {
                    // There is an existing timer active, destroy old timer and create new one, to resume where left off.

                    mCountDownTimer = null;
                    mCountDownTimer = new WorkoutTimer(mTimeRemaining, 10);
                    mCountDownTimer.start();

                    mStartTimerImageView.setVisibility(View.GONE);
                    mPauseTimerImageView.setVisibility(View.VISIBLE);

                    // notify activity that timer has been resumed:
                    mExerciseTimerListener.onExerciseTimerResumed(mCountDownTimer);
                }
                break;

            case R.id.timer_pause:
                mTimerRunning = false;
                mCountDownTimer.cancel();
                mSetTimerField.setEnabled(true);
                mStartTimerImageView.setVisibility(View.VISIBLE);
                mPauseTimerImageView.setVisibility(View.GONE);

                mTimeRemaining = mCountDownTimer.getTimeRemaining();

                // Notify activity
                mExerciseTimerListener.onExerciseTimerPaused(mTimeRemaining);
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
        }
    }

    // Listener

    public void setExerciseTimerListener (ExerciseTimerListener listener){
        this.mExerciseTimerListener = listener;
    }

    // Setters


    public void setCountDownTimer(WorkoutTimer countDownTimer) {
        mCountDownTimer = countDownTimer;
    }

    public void setExistingTimer(WorkoutTimer existingTimer) {
        mExistingTimer = existingTimer;
    }

    public void setHasActiveTimer(boolean hasActiveTimer) {
        mHasActiveTimer = hasActiveTimer;
    }

    public void setCurrentProgressMax(int currentProgressMax) {
        mCurrentProgressMax = currentProgressMax;
    }

    public void setCurrentProgress(int currentProgress) {
        mCurrentProgress = currentProgress;
    }

    public void setTimerRunning(boolean timerRunning) {
        mTimerRunning = timerRunning;
    }

    public void setTimerFirstStart(boolean timerFirstStart) {
        mTimerFirstStart = timerFirstStart;
    }

    public void setTimeRemaining(long timeRemaining) {
        mTimeRemaining = timeRemaining;
    }

    /**
     * Workout Timer Class
     */

    public class WorkoutTimer extends CountDownTimer {


        int progress;
        long timeRemaining;
        int timerDurationSecs;


        public WorkoutTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            progress = (int) millisInFuture / 1000;
            timerDurationSecs = (int)millisInFuture / 1000;
        }


        @Override
        public void onTick(long millisUntilFinished) {

            timeRemaining = millisUntilFinished;

            int i = (int) (millisUntilFinished / 1000);
            if (i != progress) {
                progress = i;
                mCountDownText.setText("" + (progress + 1));
                Log.i(TAG, "Countdown progress: " + progress);
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
    }
}


