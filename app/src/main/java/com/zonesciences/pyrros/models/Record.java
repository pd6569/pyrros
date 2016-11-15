package com.zonesciences.pyrros.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter on 12/11/2016.
 */
public class Record {

    //key for maps is the rep-max record, e.g. "1 rep-max"

    public String exerciseKey;
    public Map<String, List<String>> workoutKey = new HashMap<>();
    public String userId;
    public Map<String, List<Double>> records = new HashMap<>();
    public Map<String, List<String>> date = new HashMap<>();
    public Map<String, List<Boolean>> verified = new HashMap<>();

    public Record(){
        // Default constructor required for calls to DataSnapshot.getValue(Record.class)
    }

    public Record (String exerciseKey, String userId){
        this.exerciseKey = exerciseKey;
        this.userId = userId;
    }

    public Record (String exerciseKey, Map<String, List<String>> workoutKey, String userId, Map<String, List<Double>> records, Map<String, List<String>> date, Map<String, List<Boolean>> verified){
        this.exerciseKey = exerciseKey;
        this.workoutKey = workoutKey;
        this.userId = userId;
        this.records = records;
        this.date = date;
        this.verified = verified;
    }

    @Exclude
    public String getExerciseKey() {
        return exerciseKey;
    }

    @Exclude
    public Map<String, List<String>> getWorkoutKey() {
        return workoutKey;
    }

    @Exclude
    public String getUserId() {
        return userId;
    }

    @Exclude
    public Map<String, List<Double>> getRecords() {
        return records;
    }

    @Exclude
    public Map<String, List<String>> getDate() {
        return date;
    }

    @Exclude
    public Map<String, List<Boolean>> getVerified() {
        return verified;
    }

}