package com.zonesciences.pyrros.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter on 12/11/2016.
 */
public class Record {

    public String exerciseKey;
    public List<String> workoutKey;
    public String userId;
    public Map<String, Double> records = new HashMap<>();
    public List<String> date;
    public List<Boolean> verified;

    public Record(){
        // Default constructor required for calls to DataSnapshot.getValue(Record.class)
    }

    public Record (String exerciseKey, String userId){
        this.exerciseKey = exerciseKey;
        this.userId = userId;
    }

    public Record (String exerciseKey, List<String> workoutKey, String userId, Map <String, Double> records, List<String> date, List<Boolean> verified){
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
    public List<String> getWorkoutKey() {
        return workoutKey;
    }

    @Exclude
    public String getUserId() {
        return userId;
    }

    @Exclude
    public Map<String, Double> getRecords() {
        return records;
    }

    @Exclude
    public List<String> getDate() {
        return date;
    }

    @Exclude
    public List<Boolean> isVerified() {
        return verified;
    }

    @Exclude
    public void addSet(String reps, double weight){
        if (records == null){
            records = new HashMap<>();
        }
        records.put(reps, weight);
    }
}