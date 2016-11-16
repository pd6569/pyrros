package com.zonesciences.pyrros.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter on 18/10/2016.
 */
public class Exercise implements Comparable<Exercise>, Parcelable {

    public String uid;
    public String name;
    public String muscleGroup;
    public List<Double> weight;
    public List<Integer> reps;
    public int sets;
    public int order;
    public String exerciseId;

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
        result.put("order", order);
        result.put("exerciseId", exerciseId);
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
    public int getOrder() {
        return order;
    }

    @Exclude
    public void setOrder(int order) {
        this.order = order;
    }

    @Exclude
    public String getExerciseId() {
        return exerciseId;
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

    @Exclude
    @Override
    public int compareTo(Exercise exercise) {
        int compareOrder =((Exercise) exercise).getOrder();

        //Ascending order
        return this.order - compareOrder;
    }

    protected Exercise(Parcel in) {
        uid = in.readString();
        name = in.readString();
        muscleGroup = in.readString();
        if (in.readByte() == 0x01) {
            weight = new ArrayList<Double>();
            in.readList(weight, Double.class.getClassLoader());
        } else {
            weight = null;
        }
        if (in.readByte() == 0x01) {
            reps = new ArrayList<Integer>();
            in.readList(reps, Integer.class.getClassLoader());
        } else {
            reps = null;
        }
        sets = in.readInt();
        order = in.readInt();
        exerciseId = in.readString();
    }

    @Exclude
    @Override
    public int describeContents() {
        return 0;
    }

    @Exclude
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(muscleGroup);
        if (weight == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(weight);
        }
        if (reps == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(reps);
        }
        dest.writeInt(sets);
        dest.writeInt(order);
        dest.writeString(exerciseId);
    }

    @Exclude
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Exercise> CREATOR = new Parcelable.Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };
}