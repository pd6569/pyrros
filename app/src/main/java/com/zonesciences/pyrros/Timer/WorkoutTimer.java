package com.zonesciences.pyrros.Timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.zonesciences.pyrros.WorkoutActivity;

/**
 * Created by Peter on 17/12/2016.
 */
public class WorkoutTimer extends CountDownTimer {

    private static final String TAG = "WorkoutTimer";

    private Context mContext;


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
    public void onTick(long l) {
        Log.i(TAG, "Timer: " + (int) l / 1000);
    }

    @Override
    public void onFinish() {
        Log.i(TAG, "Workout Timer finished");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (!mSharedPreferences.getBoolean(WorkoutActivity.PREF_WORKOUT_ACTIVITY_STATE, false)){
            Toast.makeText(mContext, "Workout Timer has finished", Toast.LENGTH_SHORT).show();
        }

        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

    }
}
