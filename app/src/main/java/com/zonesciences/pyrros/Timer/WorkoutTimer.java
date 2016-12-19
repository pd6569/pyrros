package com.zonesciences.pyrros.Timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter on 18/12/2016.
 */

public class WorkoutTimer extends CountDownTimer {

    private static final String TAG = "WorkoutTimer";
    private static final int NOTIFICATION_ID = 123;

    // timer format constants
    public static final String MINUTES = "MinutesDisplay";
    public static final String SECONDS = "SecondsDisplay";

    private Context mContext;

    // Need to change menu item view in any activity
    MenuItem timerActionBarText;
    MenuItem timerAction;

    // Is dialog open
    boolean mIsDialogOpen;

    // timer variables
    long mTimeRemaining;

    // Preferences
    private SharedPreferences mSharedPreferences;

    // Workout data
    String mWorkoutKey;
    List<String> mExerciseList;
    List<Exercise> mExerciseObjects;

    // Notifications
    NotificationCompat.Builder mNotificationBuilder;

    /*public WorkoutTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }*/

    public WorkoutTimer(long millisInFuture, long countDownInterval, Context context, String workoutKey, List<String> exerciseList, List<Exercise> exerciseObjects) {
        super(millisInFuture, countDownInterval);
        this.mContext = context;
        this.mWorkoutKey = workoutKey;
        this.mExerciseList = exerciseList;
        this.mExerciseObjects = exerciseObjects;
        mNotificationBuilder = new NotificationCompat.Builder(mContext);

        Log.i(TAG, "exercise list : " + mExerciseList + " mWorkoutKey: " + mWorkoutKey + " Exercises: " + mExerciseObjects);
    }

    @Override
    public void onTick(long millisRemaining) {
        mTimeRemaining = millisRemaining;

        if (timerActionBarText != null && timerAction != null) {

            if (!mIsDialogOpen) {
                timerAction.setVisible(false);
                timerActionBarText.setVisible(true);

                timerActionBarText.setTitle(timeToDisplay(millisRemaining).get(MINUTES) + ":" + timeToDisplay(millisRemaining).get(SECONDS));
            } else {
                timerActionBarText.setVisible(false);
            }
        }


        Bundle extras = new Bundle();
        extras.putString(WorkoutActivity.WORKOUT_ID, mWorkoutKey);
        extras.putSerializable(WorkoutActivity.WORKOUT_EXERCISES, (ArrayList) mExerciseList);
        extras.putSerializable(WorkoutActivity.WORKOUT_EXERCISE_OBJECTS, (ArrayList) mExerciseObjects);
        Intent resumeWorkoutIntent = new Intent(mContext, WorkoutActivity.class);
        resumeWorkoutIntent.putExtras(extras);
        resumeWorkoutIntent.setAction("com.zonesciences.pyrros.Timer.WorkoutTimer.ACTION_RESUME_WORKOUT_ACTIVITY");

        resumeWorkoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(mContext, 0, resumeWorkoutIntent, 0);


        String timeRemaining = timeToDisplay(millisRemaining).get(MINUTES) + ":" + timeToDisplay(millisRemaining).get(SECONDS);
        mNotificationBuilder.setSmallIcon(R.drawable.ic_timer_gray_24dp)
                .setContentTitle("Workout Timer")
                .setContentText(timeRemaining)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_LOW);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
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

        long[] pattern = {500, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000};

        /*Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern, -1);*/

        /*Intent dismissAlarm = new Intent(mContext, WorkoutActivity.class);
        dismissAlarm.setAction("DismissAction");
        PendingIntent pendingIntentDismissAlarm = PendingIntent.getService(mContext, 0, dismissAlarm, 0);*/



        Intent buttonIntent = new Intent (mContext, ButtonReceiver.class);
        buttonIntent.putExtra("notificationId", NOTIFICATION_ID);
        buttonIntent.setAction("com.zonesciences.pyrros.Timer.WorkoutTimer.FINISH_NOTIFICATION");
        Log.i(TAG, "buttonIntent: " + buttonIntent.getExtras());

        PendingIntent btPendingIntent = PendingIntent.getBroadcast(mContext, 0, buttonIntent, 0);

        mNotificationBuilder.setSmallIcon(R.drawable.ic_timer_gray_24dp)
                .setContentTitle("Workout Timer")
                .setContentText("Timer finished, begin next set!")
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(pattern)
                .addAction(R.drawable.ic_close_gray_24dp, "disimss", btPendingIntent);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());

    }


    // getters
    public long getTimeRemaining() {
        return mTimeRemaining;
    }

    // setters
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
