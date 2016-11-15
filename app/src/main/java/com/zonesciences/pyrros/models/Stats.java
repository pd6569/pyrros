package com.zonesciences.pyrros.models;

/**
 * Created by Peter on 15/11/2016.
 */
public class Stats {

    public String exerciseKey;
    public String workoutKey;
    public String workoutDate;
    public double heaviestWeightLifted;
    public double volume;
    public int numReps;
    public int numSets;
    public int numSessions;

    public Stats(String exerciseKey, String workoutKey, String workoutDate, double heaviestWeightLifted, double volume, int numReps, int numSets, int numSessions){
        this.exerciseKey = exerciseKey;
        this.workoutKey = workoutKey;
        this.workoutDate = workoutDate;
        this.heaviestWeightLifted = heaviestWeightLifted;
        this.volume = volume;
        this.numReps = numReps;
        this.numSets = numSets;
        this.numSessions = numSessions;
    }

    public String getExerciseKey() {
        return exerciseKey;
    }

    public String getWorkoutKey() {
        return workoutKey;
    }

    public String getWorkoutDate() {
        return workoutDate;
    }

    public double getHeaviestWeightLifted() {
        return heaviestWeightLifted;
    }

    public double getVolume() {
        return volume;
    }

    public int getNumReps() {
        return numReps;
    }

    public int getNumSets() {
        return numSets;
    }

    public int getNumSessions() {
        return numSessions;
    }
}
