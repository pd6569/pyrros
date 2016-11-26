package com.zonesciences.pyrros.models;

import org.joda.time.DateTime;

/**
 * Created by Peter on 26/11/2016.
 */
public class ExerciseHistory implements Comparable<ExerciseHistory> {

    DateTime date;
    String workoutId;
    Exercise exercise;

    public ExerciseHistory(DateTime date, String workoutId, Exercise exercise){
        this.date = date;
        this.workoutId = workoutId;
        this.exercise = exercise;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
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

    @Override
    public int compareTo(ExerciseHistory exerciseHistory) {
        return getDate().compareTo(exerciseHistory.getDate());
    }
}
