package com.zonesciences.pyrros.Timer;

import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.Timer.WorkoutTimer;
import com.zonesciences.pyrros.models.Workout;

/**
 * Created by Peter on 17/12/2016.
 */

/**
 * Timer reference is singleton and holds reference to active timer
 */


public class WorkoutTimerReference {

    private WorkoutTimer mWorkoutTimer;

    private TimerState mTimerState;

    private static WorkoutTimerReference workoutTimerReference = null;

    protected WorkoutTimerReference () {

    }

    public static WorkoutTimerReference getWorkoutTimerReference(){
        if (workoutTimerReference == null){
            workoutTimerReference = new WorkoutTimerReference();
        }

        return workoutTimerReference;
    }

    public WorkoutTimer getWorkoutTimer(){
        return mWorkoutTimer;
    }

    public void setWorkoutTimer(WorkoutTimer workoutTimer) {
        mWorkoutTimer = workoutTimer;
    }

    public TimerState getTimerState() {
        return mTimerState;
    }

    public void setTimerState(TimerState timerState) {
        mTimerState = timerState;
    }
}
