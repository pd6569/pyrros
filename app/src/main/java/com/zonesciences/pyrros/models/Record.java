package com.zonesciences.pyrros.models;

/**
 * Created by Peter on 12/11/2016.
 */
public class Record {

    public Exercise exercise;
    public double weight;
    public double reps;
    public String workoutId;
    public String date;


    public Record (Exercise exercise, double weight, double reps, String workoutId, String date){
        this.exercise = exercise;
        this.weight = weight;
        this.reps = reps;
        this.workoutId = workoutId;
        this.date = date;

    }

    public double getWeight() {
        return weight;
    }

    public double getReps() {
        return reps;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public String getWorkoutId() {
        return workoutId;
    }

    public String getDate() {
        return date;
    }
}
