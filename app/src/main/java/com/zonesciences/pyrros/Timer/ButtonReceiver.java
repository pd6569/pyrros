package com.zonesciences.pyrros.Timer;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.zonesciences.pyrros.BaseActivity;
import com.zonesciences.pyrros.PyrrosApp;
import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.models.Workout;

/**
 * Created by Peter on 19/12/2016.
 */
public class ButtonReceiver extends BroadcastReceiver {

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mPrefEditor;
    TimerState mTimerState;


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.hasExtra(WorkoutTimer.EXTRA_DISMISS_NOTIFICATION)) {
            int notificationId = intent.getIntExtra(WorkoutTimer.EXTRA_DISMISS_NOTIFICATION, 0);
            System.out.println("onReceive. notification id " + notificationId);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);

        } else if (intent.hasExtra(WorkoutTimer.EXTRA_PAUSE_TIMER)){
            boolean pauseTimer = intent.getBooleanExtra(WorkoutTimer.EXTRA_PAUSE_TIMER, false);
            WorkoutTimerReference.getWorkoutTimerReference().getWorkoutTimer().cancel();

            System.out.println("onReceive. Pause timer: " + pauseTimer);


            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isWorkoutActivityRunning = mSharedPreferences.getBoolean(WorkoutActivity.PREF_WORKOUT_ACTIVITY_STATE, false);

            WorkoutTimerReference timerRef = WorkoutTimerReference.getWorkoutTimerReference();

            if (!isWorkoutActivityRunning) {
                Gson gson = new Gson();
                String json = mSharedPreferences.getString(WorkoutActivity.PREF_WORKOUT_TIMER_STATE, null);
                mTimerState = gson.fromJson(json, TimerState.class);


                // Pause timer
                timerRef.getWorkoutTimer().cancel();

                // New values:
                mTimerState.setTimerRunning(false);
                mTimerState.setTimeRemaining(timerRef.getWorkoutTimer().getTimeRemaining());

                mPrefEditor = mSharedPreferences.edit();
                json = gson.toJson(mTimerState);
                mPrefEditor.putString(WorkoutActivity.PREF_WORKOUT_TIMER_STATE, json);
                mPrefEditor.apply();
            } else {
                try {

                    WorkoutActivity workoutActivity = (WorkoutActivity) ((PyrrosApp) context.getApplicationContext()).getCurrentActivity();
                    workoutActivity.pauseTimer(timerRef.getWorkoutTimer().getTimeRemaining(), false);
                } catch (Exception e){
                    System.out.print("Error: " + e.toString());
                }

            }

        }
    }



}
