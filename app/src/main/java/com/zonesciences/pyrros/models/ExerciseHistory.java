package com.zonesciences.pyrros.models;

/**
 * Created by Peter on 26/11/2016.
 */
public class ExerciseHistory {

    String date;
    String workoutId;
    Exercise exercise;

    public ExerciseHistory(String date, String workoutId, Exercise exercise){
        this.date = date;
        this.workoutId = workoutId;
        this.exercise = exercise;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(String workoutId) {
        this.workoutId = workoutId;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }
}
