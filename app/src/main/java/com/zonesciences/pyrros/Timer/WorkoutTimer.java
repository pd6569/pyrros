package com.zonesciences.pyrros.Timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zonesciences.pyrros.WorkoutActivity;

/**
 * Created by Peter on 18/12/2016.
 */

public class WorkoutTimer extends CountDownTimer {

    private static final String TAG = "WorkoutTimer";

    private Context mContext;

    TextView timerOverlay;

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
        int timeRemaining = (int) (millisRemaining / 1000);

        if (timerOverlay != null) {
            timerOverlay.setVisibility(View.VISIBLE);
            timerOverlay.setText("" + (timeRemaining + 1));
        }
    }

    @Override
    public void onFinish() {
        Log.i(TAG, "Workout Timer finished");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (!mSharedPreferences.getBoolean(WorkoutActivity.PREF_WORKOUT_ACTIVITY_STATE, false)){
            Toast.makeText(mContext, "Workout Timer has finished", Toast.LENGTH_SHORT).show();
        } else {
            timerOverlay.setVisibility(View.GONE);
        }

        long[] pattern = {0, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000};

        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern, -1);

    }

    public void setTimerOverlay(TextView timerOverlay) {
        this.timerOverlay = timerOverlay;
    }
}
