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

@IgnoreExtraProperties
public class Workout {

    public String uid;
    public String creator;
    public String name;
    public boolean isPublic;
    public int userCount;
    public Map<String, Boolean> users = new HashMap<>();
    public Map<String, Boolean> exercises = new HashMap<>();

    public Workout(){
        //Default constructor required for calls to DataSnapshot.getValue(Workout.class);
    }

    public Workout(String uid, String creator, String name, boolean isPublic, String exerciseKey){
        this.uid = uid;
        this.creator = creator;
        this.name = name;
        this.isPublic = isPublic;
        addExercise(exerciseKey);
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("creator", creator);
        result.put("name", name);
        result.put("shared", isPublic);
        result.put("userCount", userCount);
        result.put("users", users);
        result.put("exercises", exercises);

        return result;
    }

    //adds exercise to workout object via Key
    @Exclude
    public Map<String, Boolean> addExercise (String exerciseKey){
        HashMap<String, Boolean> map = new HashMap<>();
        map.put(exerciseKey, true);
        this.exercises = map;
        return this.exercises;
    }

    // [END post_to_map]
}

//[END workout_class]