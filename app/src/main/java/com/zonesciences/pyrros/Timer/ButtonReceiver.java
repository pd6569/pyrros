package com.zonesciences.pyrros.Timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

import com.google.gson.Gson;
import com.zonesciences.pyrros.BaseActivity;
import com.zonesciences.pyrros.PyrrosApp;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 19/12/2016.
 */
public class ButtonReceiver extends BroadcastReceiver {

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mPrefEditor;
    TimerState mTimerState;

    // Workout data
    String mWorkoutKey;
    List<String> mExerciseList;
    List<Exercise> mExerciseObjects;


    @Override
    public void onReceive(Context context, Intent intent) {



        mExerciseList = (ArrayList<String>) intent.getSerializableExtra(WorkoutActivity.WORKOUT_EXERCISES);
        mExerciseObjects = (ArrayList<Exercise>) intent.getSerializableExtra(WorkoutActivity.WORKOUT_EXERCISE_OBJECTS);
        mWorkoutKey = intent.getStringExtra(WorkoutActivity.WORKOUT_ID);

        if (intent.hasExtra(WorkoutTimer.EXTRA_DISMISS_NOTIFICATION)) {
            int notificationId = intent.getIntExtra(WorkoutTimer.EXTRA_DISMISS_NOTIFICATION, 0);
            System.out.println("onReceive. notification id " + notificationId);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);

        } else if (intent.hasExtra(WorkoutTimer.EXTRA_PAUSE_TIMER)){
            boolean pauseTimer = intent.getBooleanExtra(WorkoutTimer.EXTRA_PAUSE_TIMER, false);

            WorkoutTimer timer = WorkoutTimerReference.getWorkoutTimerReference().getWorkoutTimer();
            timer.cancel();

            System.out.println("onReceive. Pause timer: " + pauseTimer);

            // set timer state and get workout state
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isWorkoutActivityRunning = mSharedPreferences.getBoolean(WorkoutActivity.PREF_WORKOUT_ACTIVITY_STATE, false);

            WorkoutTimerReference timerRef = WorkoutTimerReference.getWorkoutTimerReference();

            if (!isWorkoutActivityRunning) {
                Gson gson = new Gson();
                String json = mSharedPreferences.getString(WorkoutActivity.PREF_WORKOUT_TIMER_STATE, null);
                mTimerState = gson.fromJson(json, TimerState.class);

                // New values:
                mTimerState.setTimerRunning(false);
                mTimerState.setTimeRemaining(timerRef.getWorkoutTimer().getTimeRemaining());

                mPrefEditor = mSharedPreferences.edit();
                json = gson.toJson(mTimerState);
                mPrefEditor.putString(WorkoutActivity.PREF_WORKOUT_TIMER_STATE, json);
                mPrefEditor.apply();

                // Cancel timer
                timerRef.getWorkoutTimer().cancel();

            } else {
                try {
                    WorkoutActivity workoutActivity = (WorkoutActivity) ((PyrrosApp) context.getApplicationContext()).getCurrentActivity();
                    workoutActivity.pauseTimer(false);
                } catch (Exception e){
                    System.out.print("Error: " + e.toString());
                }

            }

            // Change pause button to resume button
            setActionButtonResume(context, timer);

        } else if (intent.hasExtra(WorkoutTimer.EXTRA_RESUME_TIMER)){
            System.out.println("Resume timer");

            // set timer state and get workout state
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isWorkoutActivityRunning = mSharedPreferences.getBoolean(WorkoutActivity.PREF_WORKOUT_ACTIVITY_STATE, false);

            WorkoutTimerReference timerRef = WorkoutTimerReference.getWorkoutTimerReference();

            if (!isWorkoutActivityRunning) {

                Gson gson = new Gson();
                String json = mSharedPreferences.getString(WorkoutActivity.PREF_WORKOUT_TIMER_STATE, null);
                mTimerState = gson.fromJson(json, TimerState.class);

                mTimerState.setHasActiveTimer(true);
                mTimerState.setTimerRunning(true);
                mTimerState.setTimerDuration((int) mTimerState.getTimeRemaining() / 1000);
                mTimerState.setTimerStartTime(WorkoutActivity.getTimerStartTime());

                mPrefEditor = mSharedPreferences.edit();
                json = gson.toJson(mTimerState);
                mPrefEditor.putString(WorkoutActivity.PREF_WORKOUT_TIMER_STATE, json);
                mPrefEditor.apply();

                // get notification settings
                boolean vibrate = mSharedPreferences.getBoolean(WorkoutActivity.PREF_WORKOUT_TIMER_VIBRATE, false);
                boolean sound = mSharedPreferences.getBoolean(WorkoutActivity.PREF_WORKOUT_TIMER_SOUND, false);

                // Start workout timer
                WorkoutTimer timer = new WorkoutTimer(mTimerState.getTimeRemaining(), 10, context, mWorkoutKey, mExerciseList, mExerciseObjects);
                timer.setVibrate(vibrate);
                timer.setSound(sound);
                timer.start();
                timerRef.setWorkoutTimer(timer);

            } else {
                try {
                    WorkoutActivity workoutActivity = (WorkoutActivity) ((PyrrosApp) context.getApplicationContext()).getCurrentActivity();
                    workoutActivity.resumeTimer(false);
                } catch (Exception e){
                    System.out.print("Error: " + e.toString());
                }

            }

        }
    }

    public void setActionButtonResume(Context context, WorkoutTimer timer){
        Intent resumeButtonIntent = new Intent (context, ButtonReceiver.class);
        resumeButtonIntent.putExtra(WorkoutTimer.EXTRA_RESUME_TIMER, true);
        resumeButtonIntent.setAction("com.zonesciences.pyrros.intent.ACTION_RESUME_TIMER");
        PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, 0, resumeButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String timeRemaining = WorkoutTimer.timeToDisplay(timer.getTimeRemaining()).get(WorkoutTimer.MINUTES) + ":" + WorkoutTimer.timeToDisplay(timer.getTimeRemaining()).get(WorkoutTimer.SECONDS);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_timer_gray_24dp)
                .setContentTitle("Workout Timer")
                .setContentText(timeRemaining)
                    /*.setContentIntent(piResumeWorkout)*/
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(R.drawable.ic_play_arrow_gray_24dp, "Resume", btPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(WorkoutTimer.NOTIFICATION_ID, builder.build());
    }



}
