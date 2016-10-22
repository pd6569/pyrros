package com.zonesciences.pyrros.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Peter on 18/10/2016.
 */
public class Exercise {

    public String name;
    public String muscleGroup;

    public Exercise(){
        // Default constructor required for calls to DataSnapshot.getValue(Exercise.class)
    }

    public Exercise (String name){
        this.name = name;
    }

    public Exercise (String name, String muscleGroup){
        this.name = name;
        this.muscleGroup = muscleGroup;
    }


    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("exercise", name);
        result.put("group", muscleGroup);

        return result;
    }
}
