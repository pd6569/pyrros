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
    public ArrayList<Exercise> exercises = new ArrayList<>();

    public Workout(){
        //Default constructor required for calls to DataSnapshot.getValue(Workout.class);
    }

    public Workout(String uid, String creator, String name, boolean isPublic){
        this.uid = uid;
        this.creator = creator;
        this.name = name;
        this.isPublic = isPublic;
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

    @Exclude
    public ArrayList<Exercise> addExercise (String name){
        ArrayList<Exercise> list = this.exercises;
        list.add(new Exercise(name));
        return list;
    }

    // [END post_to_map]
}

//[END workout_class]