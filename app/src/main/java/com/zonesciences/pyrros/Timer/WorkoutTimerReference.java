package com.zonesciences.pyrros.Timer;

import com.zonesciences.pyrros.models.Workout;

/**
 * Created by Peter on 17/12/2016.
 */
public class WorkoutTimerReference {

    private WorkoutTimer mWorkoutTimer;

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
}
