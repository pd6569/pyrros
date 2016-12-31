package com.zonesciences.pyrros.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Peter on 18/10/2016.
 */

// [START workout_class]

@IgnoreExtraProperties
public class Workout implements Comparable<Workout> {

    //variable names must match key name in hashmap method below
    public String uid;
    public String creator;
    public String name;
    public Boolean shared;
    public int userCount;
    public String clientTimeStamp;
    public Map<String, Boolean> users = new HashMap<>();
    public List<Exercise> exercises;
    public int numExercises;
    public String workoutKey;
    public int workoutOrder;

    public Workout(){
        //Default constructor required for calls to DataSnapshot.getValue(Workout.class);
    }

    public Workout(String uid, String creator, String clientTimeStamp, String name, Boolean shared){
        this.uid = uid;
        this.creator = creator;
        this.clientTimeStamp = clientTimeStamp;
        this.name = name;
        this.shared = shared;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("clientTimeStamp", clientTimeStamp);
        result.put("creator", creator);
        result.put("name", name);
        result.put("shared", shared);
        result.put("userCount", userCount);
        result.put("users", users);
        result.put("numExercises", numExercises);
        result.put("workoutKey", workoutKey);
        result.put("workoutOrder", workoutOrder);
        return result;
    }
    // [END post_to_map]

    //adds exercise to workout object via Key


    @Exclude
    public Boolean getShared() {
        return shared;
    }

    @Exclude
    public String getCreator() {
        return creator;
    }

    @Exclude
    public String getClientTimeStamp() {
        return clientTimeStamp;
    }

    @Exclude
    public String getUid() {
        return uid;
    }

    @Exclude
    public String getName() {
        return name;
    }

    @Exclude
    public List<Exercise> getExercises() {
        return exercises;
    }

    @Exclude
    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    @Exclude
    public void addExercise (Exercise exercise){
        exercises.add(exercise);
    }

    @Exclude
    public int getNumExercises() {
        return numExercises;
    }

    @Exclude
    public int getUserCount() {
        return userCount;
    }


    @Exclude
    public void setName(String name) {
        this.name = name;
    }

    @Exclude
    public void setShared(Boolean shared) {
        this.shared = shared;
    }


    @Exclude
    public void setNumExercises(int numExercises) {
        this.numExercises = numExercises;
    }


    @Exclude
    public void setClientTimeStamp(String clientTimeStamp) {
        this.clientTimeStamp = clientTimeStamp;
    }

    @Exclude
    public String getWorkoutKey() {
        return workoutKey;
    }

    @Exclude
    public void setWorkoutKey(String workoutKey) {
        this.workoutKey = workoutKey;
    }

    @Exclude
    public int getWorkoutOrder() {
        return workoutOrder;
    }

    @Exclude
    public void setWorkoutOrder(int workoutOrder) {
        this.workoutOrder = workoutOrder;
    }


    @Override
    public int compareTo(Workout workout) {
        int compareOrder =((Workout) workout).getWorkoutOrder();

        //Ascending order
        return this.workoutOrder - compareOrder;
    }
}
//[END workout_class]