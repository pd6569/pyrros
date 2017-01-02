package com.zonesciences.pyrros.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public String equipment;
    public boolean isSelected;
    public List<Integer> prescribedReps;
    public List<Double> prescribedWeight;
    public int restInterval;
    public int tempo;

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

    public Exercise (Exercise oldExercise, String uid){
        this.uid = uid;
        this.name = oldExercise.getName();
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
        result.put("equipment", equipment);
        result.put("prescribedReps", prescribedReps);
        result.put("prescribedWeight", prescribedWeight);
        result.put("restInterval", restInterval);
        result.put("tempo", tempo);
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
    public void setReps(List<Integer> reps) {
        this.reps = reps;
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
    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    @Exclude
    public void setExerciseId(){
        this.exerciseId = UUID.randomUUID().toString();
    }

    @Exclude
    public boolean isSelected() {
        return isSelected;
    }

    @Exclude
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Exclude
    public String getEquipment() {
        return equipment;
    }

    @Exclude
    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    @Exclude
    public List<Integer> getPrescribedReps() {
        return prescribedReps;
    }

    @Exclude
    public void setPrescribedReps(List<Integer> prescribedReps) {
        this.prescribedReps = prescribedReps;
    }

    @Exclude
    public List<Double> getPrescribedWeight() {
        return prescribedWeight;
    }

    @Exclude
    public void setPrescribedWeight(List<Double> prescribedWeight) {
        this.prescribedWeight = prescribedWeight;
    }

    @Exclude
    public int getRestInterval() {
        return restInterval;
    }

    @Exclude
    public void setRestInterval(int restInterval) {
        this.restInterval = restInterval;
    }

    @Exclude
    public int getTempo() {
        return tempo;
    }

    @Exclude
    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    @Exclude
    public boolean hasExerciseId(){
        if (exerciseId != null) {
            return true;
        } else {
            return false;
        }
    }

    @Exclude
    public boolean hasSets(){
        if (weight == null){
            return false;
        } else {
            return true;
        }
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
    public void addPrescribedReps (int newPrescribedReps){
        if (prescribedReps == null){
            prescribedReps = new ArrayList<>();
        }
        prescribedReps.add(newPrescribedReps);
    }

    @Exclude
    public void addPrescribedWeight (int newPrescribedWeight){
        if (prescribedWeight == null){
            prescribedWeight = new ArrayList<>();
        }
        prescribedReps.add(newPrescribedWeight);
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
        if (in.readByte() == 0x01) {
            prescribedReps = new ArrayList<Integer>();
            in.readList(prescribedReps, Integer.class.getClassLoader());
        } else {
            prescribedReps = null;
        }
        if (in.readByte() == 0x01) {
            prescribedWeight = new ArrayList<Double>();
            in.readList(prescribedWeight, Integer.class.getClassLoader());
        } else {
            prescribedWeight = null;
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
        if (prescribedReps == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(prescribedReps);
        }
        if (prescribedWeight == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(prescribedWeight);
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