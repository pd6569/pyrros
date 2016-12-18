package com.zonesciences.pyrros.Timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zonesciences.pyrros.WorkoutActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Peter on 18/12/2016.
 */

public class WorkoutTimer extends CountDownTimer {

    private static final String TAG = "WorkoutTimer";

    // timer format constants
    public static final String MINUTES = "MinutesDisplay";
    public static final String SECONDS = "SecondsDisplay";

    private Context mContext;

    // Need to change menu item view in any activity
    MenuItem timerActionBarText;
    MenuItem timerAction;

    // Is dialog open
    boolean mIsDialogOpen;

    // Preferences
    private SharedPreferences mSharedPreferences;

    /*public WorkoutTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }*/

    public WorkoutTimer(long millisInFuture, long countDownInterval, Context context) {
        super(millisInFuture, countDownInterval);
        this.mContext = context;
    }

    @Override
    public void onTick(long millisRemaining) {
        int timeRemaining = (int) (millisRemaining / 1000) + 1;

        if (timerActionBarText != null && timerAction != null) {

            Log.i(TAG, "mIsDialogOpen = " + mIsDialogOpen);

            if (!mIsDialogOpen) {

                Log.i(TAG, "Timer dialog closed. Set timer icon invisible and timer toolbar text visible");
                timerAction.setVisible(false);
                timerActionBarText.setVisible(true);

                timerActionBarText.setTitle(timeToDisplay(millisRemaining).get(MINUTES) + ":" + timeToDisplay(millisRemaining).get(SECONDS));
            } else {
                timerActionBarText.setVisible(false);
            }
        }
    }

    @Override
    public void onFinish() {
        Log.i(TAG, "Workout Timer finished");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (!mSharedPreferences.getBoolean(WorkoutActivity.PREF_WORKOUT_ACTIVITY_STATE, false)){
            Toast.makeText(mContext, "Workout Timer has finished", Toast.LENGTH_SHORT).show();
        } else {
            timerActionBarText.setVisible(false);
            timerAction.setVisible(true);
        }

        long[] pattern = {0, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000};

        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern, -1);

    }


    public void setTimerActionBarText(MenuItem timerActionBarText) {
        this.timerActionBarText = timerActionBarText;
    }

    public void setTimerAction(MenuItem timerAction) {
        this.timerAction = timerAction;
    }

    public void setDialogOpen(boolean dialogOpen) {
        mIsDialogOpen = dialogOpen;
    }


    // Method for formatting countdown display
    public static Map<String, String> timeToDisplay (long timeRemainingMillis){

        Map<String, String> timeToDisplay = new HashMap<>();

        int timeRemainingSecs = (int) (timeRemainingMillis / 1000) + 1;

        int secondsRemaining;

        double minutesRemaining = timeRemainingSecs / 60;

        if (minutesRemaining >= 1){
            secondsRemaining = (timeRemainingSecs - ((int) minutesRemaining * 60));
        } else {
            secondsRemaining = timeRemainingSecs;
        }

        String minsToDisplay = new String();
        String secsToDisplay = new String();

        if (minutesRemaining < 10){
            minsToDisplay = 0 + Integer.toString((int) minutesRemaining);
        } else {
            minsToDisplay = Integer.toString((int) minutesRemaining);
        }
        if (secondsRemaining < 10){
            secsToDisplay = 0 + Integer.toString(secondsRemaining);
        } else {
            secsToDisplay = Integer.toString(secondsRemaining);
        }

        timeToDisplay.put(MINUTES, minsToDisplay);
        timeToDisplay.put(SECONDS, secsToDisplay);

        return timeToDisplay;

    }
}
