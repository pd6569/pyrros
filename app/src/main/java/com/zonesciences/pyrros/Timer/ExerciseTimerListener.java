package com.zonesciences.pyrros.Timer;

/**
 * Created by Peter on 14/12/2016.
 */
public interface ExerciseTimerListener {

    void onExerciseTimerCreated(int timerDuration, boolean vibrate, boolean sound);
    void onExerciseTimerResumed(int timerDuration, boolean pausedFromDialog);
    void onExerciseTimerPaused(long timeRemaining, boolean pausedFromDialog);
    void onExerciseTimerFinished();
    void onExerciseTimerDismissed(boolean timerRunning, long timeRemaining, int currentProgress, int currentProgressMax);
    void onExerciseTimerReset();
}
