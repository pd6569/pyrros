package com.zonesciences.pyrros.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter on 18/10/2016.
 */
public class Exercise {

    public String uid;
    public String name;
    public String muscleGroup;
    public List<Double> weight;
    public List<Integer> reps;
    public int sets;

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

    public Exercise (String uid, String name, String muscleGroup, List<Double> weight, List<Integer> reps, int sets){
        this.uid = uid;
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.weight = weight;
        this.reps = reps;
        this.sets = sets;
    }



    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("muscleGroup", muscleGroup);
        result.put("weight", weight);
        result.put("reps", reps);
        result.put("sets", sets);
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

    @Exclude
    public List<Double> getWeight() {
        return weight;
    }

    @Exclude
    public List<Integer> getReps() {
        return reps;
    }

    @Exclude
    public int getSets() {
        return sets;
    }

    @Exclude
    public void addWeight(Double newWeight){
        if(weight == null){
            weight = new ArrayList<>();
        }
        weight.add(newWeight);
        sets = weight.size();
    }

    @Exclude
    public void addReps (int newReps){
        if(reps == null){
            reps = new ArrayList<>();
        }
        reps.add(newReps);
        sets = reps.size();
    }
}
