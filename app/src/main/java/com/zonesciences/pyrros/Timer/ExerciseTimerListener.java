package com.zonesciences.pyrros.Timer;

/**
 * Created by Peter on 14/12/2016.
 */
public interface ExerciseTimerListener {

    void onExerciseTimerCreated();
    void onExerciseTimerResumed();
    void onExerciseTimerPaused();
    void onExerciseTimerFinished();

}
