package com.zonesciences.pyrros.models;

/**
 * Created by Peter on 18/10/2016.
 */
public class Exercise {

    public String name;
    public String muscleGroup;

    public Exercise(){

    }

    public Exercise (String name){
        this.name = name;
    }

    public Exercise (String name, String muscleGroup){
        this.name = name;
        this.muscleGroup = muscleGroup;
    }


}
