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
    int mTimeRemainingToDisplay;
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
            setTimerOptionsVisible(false);
            mCountDownProgressBar.setMax(mCurrentProgressMax);
            mCountDownProgressBar.setProgress(mCurrentProgress);
            mCountDownText.setText("" + mTimeRemainingToDisplay);

            /*mPauseTimerImageView.setOnClickListener(this);*/

            if (mTimerRunning) {
                mStartTimerImageView.setVisibility(View.GONE);
                mPauseTimerImageView.setVisibility(View.VISIBLE);
            } else {
                mStartTimerImageView.setVisibility(View.VISIBLE);
                mPauseTimerImageView.setVisibility(View.GONE);
            }
        }


        /*mStartTimerImageView.setOnClickListener(this);*/


        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setView(dialogView);
        builder.setCancelable(true);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (mCountDownTimer != null) {
                    mTimeRemaining = mCountDownTimer.getTimeRemaining();
                    mTimeRemainingToDisplay = mCountDownTimer.getProgress() + 1;
                    mCurrentProgress = mCountDownProgressBar.getProgress();
                    mCurrentProgressMax = mCountDownProgressBar.getMax();
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

                    // Notify activity of timer creation
                    mExerciseTimerListener.onExerciseTimerCreated();

                    if (!mSetTimerField.getText().toString().isEmpty()) {

                        setTimerOptionsVisible(false);
                        mStartTimerImageView.setVisibility(View.GONE);
                        mPauseTimerImageView.setVisibility(View.VISIBLE);

                        /*mPauseTimerImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Notify activity
                                mExerciseTimerListener.onExerciseTimerPaused();

                                mTimerRunning = false;
                                mCountDownTimer.cancel();
                                mSetTimerField.setEnabled(true);
                                mStartTimerImageView.setVisibility(View.VISIBLE);
                                mPauseTimerImageView.setVisibility(View.GONE);

                                mTimeRemaining = mCountDownTimer.getTimeRemaining();
                            }
                        });*/


                        int timer = Integer.parseInt(mSetTimerField.getText().toString());
                        int milliseconds = timer * 1000;

                        mSetTimerField.setEnabled(false);

                        mCountDownProgressBar.setMax(timer * 100);
                        mCountDownTimer = new WorkoutTimer(milliseconds, 10);
                        mCountDownTimer.start();

                        mTimerFirstStart = false;
                    } else {
                        Log.i(TAG, "Error: Enter number, dickhead");
                        Toast.makeText(mActivity, "Enter a number for rest timer", Toast.LENGTH_SHORT).show();
                        return;
                    }

                } else {
                    // There is an existing timer active, destroy old timer and create new one, to resume where left off.

                    // notify activity that timer has been resumed:
                    mExerciseTimerListener.onExerciseTimerResumed();

                    mCountDownTimer = null;
                    mCountDownTimer = new WorkoutTimer(mTimeRemaining, 10);
                    mCountDownTimer.start();

                    mStartTimerImageView.setVisibility(View.GONE);
                    mPauseTimerImageView.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.timer_pause:
                // Notify activity
                mExerciseTimerListener.onExerciseTimerPaused();

                mTimerRunning = false;
                mCountDownTimer.cancel();
                mSetTimerField.setEnabled(true);
                mStartTimerImageView.setVisibility(View.VISIBLE);
                mPauseTimerImageView.setVisibility(View.GONE);

                mTimeRemaining = mCountDownTimer.getTimeRemaining();
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

    /**
     * Workout Timer Class
     */

    public class WorkoutTimer extends CountDownTimer {

        int progress;
        long timeRemaining;

        public WorkoutTimer(long millisInFuture, long countDownInterval) {
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

        public int getProgress() {
            return progress;
        }
    }
}


