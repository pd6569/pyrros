package com.zonesciences.pyrros.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 27/12/2016.
 */

public class Routine {

    //variable names must match key name in hashmap method below
    public String uid;
    public String creator;
    public String name;
    public Boolean shared;
    public int userCount;
    public String clientTimeStamp;
    public Map<String, Boolean> users = new HashMap<>();
    public Map<String, Boolean> workouts = new HashMap<>();
    public int numWorkouts;

    public Routine(){

    }

    public Routine (String uid, String clientTimeStamp, boolean shared){
        this.uid = uid;
        this.clientTimeStamp = clientTimeStamp;
        this.shared = shared;
    }

    public Routine (String uid, String creator, String clientTimeStamp, String name, Boolean shared){
        this.uid = uid;
        this.creator = creator;
        this.clientTimeStamp = clientTimeStamp;
        this.name = name;
        this.shared = shared;
    }

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
        result.put("workouts", workouts);
        result.put("numWorkouts", numWorkouts);
        return result;
    }


    // Getters and setters

    @Exclude
    public String getUid() {
        return uid;
    }

    @Exclude
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Exclude
    public String getCreator() {
        return creator;
    }

    @Exclude
    public void setCreator(String creator) {
        this.creator = creator;
    }

    @Exclude
    public String getName() {
        return name;
    }

    @Exclude
    public void setName(String name) {
        this.name = name;
    }

    @Exclude
    public Boolean getShared() {
        return shared;
    }

    @Exclude
    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    @Exclude
    public int getUserCount() {
        return userCount;
    }

    @Exclude
    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    @Exclude
    public String getClientTimeStamp() {
        return clientTimeStamp;
    }

    @Exclude
    public void setClientTimeStamp(String clientTimeStamp) {
        this.clientTimeStamp = clientTimeStamp;
    }

    @Exclude
    public Map<String, Boolean> getUsers() {
        return users;
    }

    @Exclude
    public void setUsers(Map<String, Boolean> users) {
        this.users = users;
    }

    @Exclude
    public Map<String, Boolean> getWorkouts() {
        return workouts;
    }

    @Exclude
    public void setWorkouts(Map<String, Boolean> workouts) {
        this.workouts = workouts;
    }

    @Exclude
    public int getNumWorkouts() {
        return numWorkouts;
    }

    @Exclude
    public void setNumWorkouts(int numWorkouts) {
        this.numWorkouts = numWorkouts;
    }
}
