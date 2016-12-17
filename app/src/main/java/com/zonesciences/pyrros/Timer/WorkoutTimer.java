package com.zonesciences.pyrros.Timer;

import android.os.CountDownTimer;
import android.util.Log;

/**
 * Created by Peter on 17/12/2016.
 */
public class WorkoutTimer extends CountDownTimer {

    private static final String TAG = "WorkoutTimer";

    public WorkoutTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long l) {
        Log.i(TAG, "Timer: " + (int) l / 1000);
    }

    @Override
    public void onFinish() {
        Log.i(TAG, "Workout Timer finished");
    }
}
