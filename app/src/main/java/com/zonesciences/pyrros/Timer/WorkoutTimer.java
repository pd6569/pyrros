package com.zonesciences.pyrros.Timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
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


    // timer format constants
    public static final String MINUTES = "MinutesDisplay";
    public static final String SECONDS = "SecondsDisplay";

    // notifications extras
    public static final int NOTIFICATION_ID = 123;
    public static final String EXTRA_PAUSE_TIMER = "PauseTimer";
    public static final String EXTRA_RESUME_TIMER = "ResumeTimer";
    public static final String EXTRA_DISMISS_NOTIFICATION = "DismissNotification";


    //Context
    private Context mContext;

    // Need to change menu item view in any activity
    MenuItem timerActionBarText;
    MenuItem timerAction;

    // if dialog is open need to change progress bar and text
    TextView mCountDownText;
    ProgressBar mCountDownProgressBar;


    // Is dialog open
    boolean mIsDialogOpen;

    // timer variables
    long mTimeRemaining;
    int mProgress;

    // Preferences
    private SharedPreferences mSharedPreferences;

    // Workout data
    String mWorkoutKey;
    List<String> mExerciseList;
    List<Exercise> mExerciseObjects;

    // alarm/notification settings
    boolean mVibrate;
    boolean mSound;
    boolean mAutoStart;

    // Notifications
    NotificationCompat.Builder mNotificationBuilder;

    // Listener
    WorkoutTimerListener mWorkoutTimerListener;


    public WorkoutTimer(long millisInFuture, long countDownInterval, Context context, String workoutKey, List<String> exerciseList, List<Exercise> exerciseObjects) {
        super(millisInFuture, countDownInterval);
        this.mContext = context;
        this.mWorkoutKey = workoutKey;
        this.mExerciseList = exerciseList;
        this.mExerciseObjects = exerciseObjects;
        mProgress = (int) millisInFuture / 1000;
        mNotificationBuilder = new NotificationCompat.Builder(mContext);
        onStart(millisInFuture);
        Log.i(TAG, "exercise list : " + mExerciseList + " mWorkoutKey: " + mWorkoutKey + " Exercises: " + mExerciseObjects);
    }

    public void onStart(long millisInFuture){

        // Intent to resume workout
        Bundle extras = new Bundle();
        extras.putString(WorkoutActivity.WORKOUT_ID, mWorkoutKey);
        extras.putSerializable(WorkoutActivity.WORKOUT_EXERCISES, (ArrayList) mExerciseList);
        extras.putSerializable(WorkoutActivity.WORKOUT_EXERCISE_OBJECTS, (ArrayList) mExerciseObjects);
        Intent resumeWorkoutIntent = new Intent(mContext, WorkoutActivity.class);
        resumeWorkoutIntent.putExtras(extras);
        resumeWorkoutIntent.setAction("com.zonesciences.pyrros.intent.ACTION_RESUME_WORKOUT_ACTIVITY");

        resumeWorkoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent piResumeWorkout = PendingIntent.getActivity(mContext, 0, resumeWorkoutIntent, 0);

        // Intent to pause timer
        Intent buttonIntent = new Intent (mContext, ButtonReceiver.class);
        buttonIntent.putExtra(EXTRA_PAUSE_TIMER, true);
        buttonIntent.putExtras(extras);
        buttonIntent.setAction("com.zonesciences.pyrros.intent.ACTION_PAUSE_TIMER");
        PendingIntent btPendingIntent = PendingIntent.getBroadcast(mContext, 0, buttonIntent, 0);


        String timeRemaining = timeToDisplay(millisInFuture).get(MINUTES) + ":" + timeToDisplay(millisInFuture).get(SECONDS);

        mNotificationBuilder.setSmallIcon(R.drawable.ic_timer_gray_24dp)
                .setContentTitle("Workout Timer")
                .setContentText(timeRemaining)
                .setContentIntent(piResumeWorkout)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(R.drawable.ic_pause_gray_24dp, "Pause", btPendingIntent);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    @Override
    public void onTick(long millisRemaining) {
        mTimeRemaining = millisRemaining;
        String timeRemaining = timeToDisplay(millisRemaining).get(MINUTES) + ":" + timeToDisplay(millisRemaining).get(SECONDS);

        if (timerActionBarText != null && timerAction != null) {

            if (!mIsDialogOpen) {
                timerAction.setVisible(false);
                timerActionBarText.setVisible(true);

                timerActionBarText.setTitle(timeToDisplay(millisRemaining).get(MINUTES) + ":" + timeToDisplay(millisRemaining).get(SECONDS));
            } else {
                timerActionBarText.setVisible(false);
            }
        }

        int i = (int) (millisRemaining / 1000);

        if (i != mProgress) {
            mProgress = i;

            if (mIsDialogOpen){
                mCountDownText.setText(timeRemaining);
                updateNotificationBar(timeRemaining);
            } else {
                updateNotificationBar(timeRemaining);
            }
        }

        if (mIsDialogOpen) {

            int progressBarUpdate = (int) (millisRemaining / 10);
            mCountDownProgressBar.setProgress(mCountDownProgressBar.getMax() - progressBarUpdate);
        }

    }

    private void updateNotificationBar(String timeRemaining){
        // Update time remaining in notification bar
        mNotificationBuilder.setSmallIcon(R.drawable.ic_timer_gray_24dp)
                .setContentText(timeRemaining);
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

        if (mWorkoutTimerListener != null) {
            mWorkoutTimerListener.onFinish();
        }


        /*Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern, -1);*/

        /*Intent dismissAlarm = new Intent(mContext, WorkoutActivity.class);
        dismissAlarm.setAction("DismissAction");
        PendingIntent pendingIntentDismissAlarm = PendingIntent.getService(mContext, 0, dismissAlarm, 0);*/

        // reset timer state and set shared prefs
        TimerState timerState = new TimerState();
        timerState.setHasActiveTimer(false);
        timerState.setTimerRunning(false);
        timerState.setTimerFirstStart(true);

        Log.i(TAG, "timer active: " + timerState.hasActiveTimer() + " timer running: " + timerState.isTimerRunning() + " timer first start: " + timerState.isTimerFirstStart());
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        Gson gson = new Gson();
        String json = gson.toJson(timerState);
        editor.putString(WorkoutActivity.PREF_WORKOUT_TIMER_STATE, json);
        editor.apply();

        /*TimerState checkTimerState;
        String timerJson = mSharedPreferences.getString(WorkoutActivity.PREF_WORKOUT_TIMER_STATE, null);
        checkTimerState = gson.fromJson(timerJson, TimerState.class);
        Log.i(TAG, "timer active: " + checkTimerState.hasActiveTimer() + " timer running: " + checkTimerState.isTimerRunning() + " timer first start: " + checkTimerState.isTimerFirstStart());*/



        Intent buttonIntent = new Intent (mContext, ButtonReceiver.class);
        buttonIntent.putExtra(EXTRA_DISMISS_NOTIFICATION, NOTIFICATION_ID);
        buttonIntent.setAction("com.zonesciences.pyrros.intent.ACTION_DISMISS_TIMER");

        // Sound
        Uri alarmSound = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.timer);

        // Vibration
        long[] pattern = {500, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000};



        PendingIntent btPendingIntent = PendingIntent.getBroadcast(mContext, 0, buttonIntent, 0);
        mNotificationBuilder.mActions.clear();
        mNotificationBuilder.setSmallIcon(R.drawable.ic_timer_gray_24dp)
                .setContentTitle("Workout Timer")
                .setContentText("Timer finished, begin next set!")
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(R.drawable.ic_close_gray_24dp, "disimss", btPendingIntent);

        if (mVibrate){
            mNotificationBuilder.setVibrate(pattern);
        }

        if (mSound) {
            mNotificationBuilder.setSound(alarmSound);
        }

        Notification notification = mNotificationBuilder.build();
        notification.flags |= Notification.FLAG_INSISTENT;

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);

    }


    // getters
    public long getTimeRemaining() {
        return mTimeRemaining;
    }

    public boolean isVibrate() {
        return mVibrate;
    }

    public boolean isSound() {
        return mSound;
    }

    public boolean isAutoStart() {
        return mAutoStart;
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

    public void setVibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    public void setSound(boolean sound) {
        mSound = sound;
    }

    public void setAutoStart(boolean autoStart) {
        mAutoStart = autoStart;
    }

    public void setCountDownText(TextView countDownText) {
        mCountDownText = countDownText;
    }

    public void setCountDownProgressBar(ProgressBar countDownProgressBar) {
        mCountDownProgressBar = countDownProgressBar;
    }

    // listener

    public interface WorkoutTimerListener {
        void onFinish();
    }

    public void setWorkoutTimerListener(WorkoutTimerListener listener){
        this.mWorkoutTimerListener = listener;
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
