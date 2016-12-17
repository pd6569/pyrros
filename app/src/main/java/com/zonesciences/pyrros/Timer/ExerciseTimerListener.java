package com.zonesciences.pyrros.Timer;

/**
 * Created by Peter on 14/12/2016.
 */
public interface ExerciseTimerListener {

    void onExerciseTimerCreated(int timerDuration);
    void onExerciseTimerResumed(int timerDuration);
    void onExerciseTimerPaused(long timeRemaining);
    void onExerciseTimerFinished();
    void onExerciseTimerDismissed(boolean timerRunning, TimerDialog.WorkoutTimer workoutTimer, long timeRemaining, int currentProgress, int currentProgressMax);
}
