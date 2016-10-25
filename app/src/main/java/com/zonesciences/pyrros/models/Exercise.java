package com.zonesciences.pyrros.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Peter on 18/10/2016.
 */
public class Exercise {

    public String uid;
    public String name;
    public String muscleGroup;

    public Exercise(){
        // Default constructor required for calls to DataSnapshot.getValue(Exercise.class)
    }

    public Exercise (String uid, String name){
        this.uid = uid;
        this.name = name;
    }


    public Exercise (String uid, String name, String muscleGroup){
        this.uid = uid;
        this.name = name;
        this.muscleGroup = muscleGroup;
    }


    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("group", muscleGroup);

        return result;
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
    public String getMuscleGroup() {
        return muscleGroup;
    }


}
