package com.zonesciences.pyrros.models;

import java.util.List;

/**
 * Created by Peter on 12/11/2016.
 */
public class Record {

    public String exerciseKey;
    public List<Double> weight;
    public List<Integer> reps;
    public List<String> workoutKey;
    public List<String> date;


    public Record (String exerciseKey, List<Double> weight, List<Integer> reps, List<String> workoutKey, List<String> date){
        this.exerciseKey = exerciseKey;
        this.weight = weight;
        this.reps = reps;
        this.workoutKey = workoutKey;
        this.date = date;
    }

    public String getExerciseKey() {
        return exerciseKey;
    }

    public List<Double> getWeight() {
        return weight;
    }

    public List<Integer> getReps() {
        return reps;
    }

    public List<String> getWorkoutKey() {
        return workoutKey;
    }

    public List<String> getDate() {
        return date;
    }
}