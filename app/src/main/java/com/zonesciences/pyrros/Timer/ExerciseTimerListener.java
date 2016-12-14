package com.zonesciences.pyrros.Timer;

/**
 * Created by Peter on 14/12/2016.
 */
public interface ExerciseTimerListener {

    void onExerciseTimerCreated(TimerDialog.WorkoutTimer workoutTimer);
    void onExerciseTimerResumed(TimerDialog.WorkoutTimer newWorkoutTimer);
    void onExerciseTimerPaused(long timeRemaining);
    void onExerciseTimerFinished();
    void onExerciseTimerDismissed(boolean timerRunning, TimerDialog.WorkoutTimer workoutTimer, int currentProgress, int currentProgressMax);
}
