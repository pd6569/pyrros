package com.zonesciences.pyrros.datatools;

import android.provider.ContactsContract;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Record;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter on 12/11/2016.
 */
public class DataTools {

    private static final String TAG = "DataTools.class";

    // Date Range
    public static final int THIS_SESSION = 0;
    public static final int TODAY = 1;
    public static final int THIS_WEEK = 2;
    public static final int THIS_MONTH = 3;
    public static final int LAST_28_DAYS = 4;
    public static final int LAST_6_MONTHS = 5;
    public static final int THIS_YEAR = 6;
    public static final int ALL_TIME = 7;

    // Keys for maps
    public static final String KEY_VOLUME = "volume";
    public static final String KEY_WEIGHT = "weight";
    public static final String KEY_REPS = "reps";
    public static final String KEY_DATE = "date";
    public static final String KEY_WORKOUT_KEY = "workoutKey";


    DatabaseReference mUserWorkoutExercisesRef;

    String mRequestorWorkoutKey;
    String mExerciseKey;
    String mUserId;


    ArrayList<Exercise> mExercises = new ArrayList<>();
    ArrayList<String> mExerciseDates = new ArrayList<>();
    ArrayList<String> mWorkoutKeys = new ArrayList<>();
    Record mExerciseRecord;


    int mSets;
    int mReps;

    Map<Exercise, List<Double>> mWeightsMap = new HashMap<>();
    Map<Exercise, List<Integer>> mRepsMap = new HashMap<>();
    Map<Exercise, List<Double>> mVolumeMap = new HashMap<>();
    Map<String, String> mDatesMap = new HashMap<>();

    OnDataLoadCompleteListener mListener;

    // Anonymous constructor
    public DataTools (ArrayList<Exercise> exercises){
        mExercises = exercises;
    }


    //Constructor without exercises list passed in - generates exercises from firebase call.
    public DataTools (String userId, String exerciseKey){
        mExerciseKey = exerciseKey;
        mUserId = userId;
        mUserWorkoutExercisesRef = FirebaseDatabase.getInstance().getReference().child("user-workout-exercises").child(mUserId);
    }

    //Constructor with exercises list passed in.
    public DataTools (String userId, String exerciseKey, ArrayList<Exercise> exercises){
        mExerciseKey = exerciseKey;
        mUserId = userId;
        mExercises = exercises;
        mUserWorkoutExercisesRef = FirebaseDatabase.getInstance().getReference().child("user-workout-exercises").child(mUserId);
    }

    public DataTools (String userId, String exerciseKey, ArrayList<Exercise> exercises, ArrayList<String> workoutKeys,  ArrayList<String> workoutDates){
        mExerciseKey = exerciseKey;
        mUserId = userId;
        mExercises = exercises;
        mWorkoutKeys = workoutKeys;
        mExerciseDates = workoutDates;
        mUserWorkoutExercisesRef = FirebaseDatabase.getInstance().getReference().child("user-workout-exercises").child(mUserId);
    }


    // Methods for loading data from Firebase

    //Load all exercises and workout keys
    public void loadExercises() {
        Log.i(TAG, "loadExercises()");
        mUserWorkoutExercisesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange");
                for (DataSnapshot workout : dataSnapshot.getChildren()) {

                    for (DataSnapshot exercise : workout.getChildren()) {

                        if (exercise.getKey().equals(mExerciseKey)) {

                            mWorkoutKeys.add(workout.getKey());
                            Log.i(TAG, "Workout key: " + workout.getKey());

                            Exercise e = exercise.getValue(Exercise.class);
                            mExercises.add(e);
                        }
                    }

                }

                Log.i(TAG, "Exercises loaded. Number of exercises : " + mExercises.size());

                mListener.onExercisesLoadComplete();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void loadWorkoutKeys() {
        Log.i(TAG, "loadWorkoutKeys()");
        mUserWorkoutExercisesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange");
                for (DataSnapshot workout : dataSnapshot.getChildren()) {

                    for (DataSnapshot exercise : workout.getChildren()) {
                        if (exercise.getKey().equals(mExerciseKey)) {
                            mWorkoutKeys.add(workout.getKey());
                            Log.i(TAG, "Workout key: " + workout.getKey());
                        }
                    }
                }
                mListener.onWorkoutKeysLoadComplete();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void loadWorkoutDates(final List<String> workoutKeys) {

        mUserWorkoutExercisesRef.getRoot().child("user-workouts").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot workout : dataSnapshot.getChildren()) {
                    if (mWorkoutKeys.contains(workout.getKey())) {
                        Workout w = workout.getValue(Workout.class);
                        mExerciseDates.add(w.getClientTimeStamp());
                        Log.i(TAG, "Added date to exercise dates list: " + w.getClientTimeStamp());
                    }
                }
                Log.i(TAG, "Finished loading workout dates");

                mListener.onWorkoutDatesLoadComplete();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void loadRecord(){

        mUserWorkoutExercisesRef.getRoot().child("user-records").child(mUserId).child(mExerciseKey).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "Get record: " + dataSnapshot.getValue());
                mExerciseRecord = dataSnapshot.getValue(Record.class);
                mListener.onExerciseRecordLoadComplete();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Listener for load completion
    public interface OnDataLoadCompleteListener{
        public void onExercisesLoadComplete();
        public void onWorkoutDatesLoadComplete();
        public void onWorkoutKeysLoadComplete();
        public void onExerciseRecordLoadComplete();
    }

    public void setOnDataLoadCompleteListener(OnDataLoadCompleteListener listener){
        this.mListener = listener;
    }

    // Getters

    public ArrayList<Exercise> getExercises() {
        return mExercises;
    }

    public ArrayList<String> getExerciseDates() { return mExerciseDates; }

    public ArrayList<String> getWorkoutKeys() {
        return mWorkoutKeys;
    }

    public Record getExerciseRecord() { return mExerciseRecord; }

    public String getExerciseKey() {
        return mExerciseKey;
    }

    public String getRequestorWorkoutKey() {
        return mRequestorWorkoutKey;
    }

    // Setters

    public void setExerciseRecord(Record exerciseRecord) {
        mExerciseRecord = exerciseRecord;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        mExercises = exercises;
    }

    public void setExerciseDates(ArrayList<String> exerciseDates) {
        mExerciseDates = exerciseDates;
    }

    public void setWorkoutKeys(ArrayList<String> workoutKeys) {
        mWorkoutKeys = workoutKeys;
    }

    public void setRequestorWorkoutKey(String requestorWorkoutKey) {
        mRequestorWorkoutKey = requestorWorkoutKey;
    }

    // Maps
    public void createWeightsRepsMaps(){

        for (Exercise exercise: mExercises){

            if (exercise.getSets() == 0) {
                Log.i(TAG, "No sets recorded for this exercise");
            } else {
                List<Double> weightsList = (ArrayList) exercise.getWeight();
                List<Integer> repsList = (ArrayList) exercise.getReps();

                mWeightsMap.put(exercise, weightsList);
                mRepsMap.put(exercise, repsList);
            }
        }

    }

    // Methods for calculations

    public int totalPrescribedSets(){
        int totalPrescribedSets = 0;
        for(Exercise exercise : mExercises){
            List<Integer> prescribedReps = exercise.getPrescribedReps();
            if (prescribedReps != null) {
                int prescribedSets = exercise.getPrescribedReps().size();
                totalPrescribedSets = totalPrescribedSets + prescribedSets;
            }
        }
        return totalPrescribedSets;
    }

    public int totalSets(){
        int totalSets = 0;
        for(Exercise exercise : mExercises){
            int sets = exercise.getSets();
            totalSets = totalSets + sets;
        }
        return totalSets;
    }

    public int totalReps(){
        int totalReps = 0;
        for(Exercise exercise : mExercises) {
            if (exercise.getSets() == 0) {
                Log.i(TAG, "No sets recorded for this exercise");
            } else {
                List<Integer> repsList = exercise.getReps();
                Log.i(TAG, "repsList = " + repsList.size());

                for (int rep : repsList) {
                    totalReps = totalReps + rep;
                }
            }
        }

        return totalReps;
    }

    public double totalVolume(){

        double totalVolume = 0;

        for (Exercise exercise: mExercises){

            if (exercise.getSets() == 0) {
                Log.i(TAG, "No sets recorded for this exercise");
            } else {
                List<Double> weightsList = exercise.getWeight();
                List<Integer> repsList = exercise.getReps();
                List<Double> volumeList = new ArrayList<>();
                for (int i = 0; i < weightsList.size(); i++){
                    double weight = weightsList.get(i);
                    int reps = repsList.get(i);
                    double setVolume = weight * reps;
                    volumeList.add(setVolume);
                    Log.i(TAG, "Volume for this set = " + setVolume);
                    totalVolume = totalVolume + setVolume;
                }
                mVolumeMap.put(exercise, volumeList);
            }
        }
        return totalVolume;
    }

    public String getMuscleGroupsForWorkout(){
        List<String> muscleGroupsList = new ArrayList<>();
        for (Exercise e : mExercises){
            if (e.getMuscleGroup().equals("full body")){
                muscleGroupsList.clear();
                muscleGroupsList.add("full body");
                break;
            }
            if (!muscleGroupsList.contains(e.getMuscleGroup())){
                muscleGroupsList.add(e.getMuscleGroup());
            }
        }
        String muscleGroupsString = new String();
        for (int i = 0; i < muscleGroupsList.size(); i++){
            if (i == 0){
                muscleGroupsString = muscleGroupsList.get(i);
            } else {
                muscleGroupsString += "\n" + muscleGroupsList.get(i);
            }
        }

        return muscleGroupsString;
    }

    public int getEstimatedWorkoutDurationSeconds(){
        int workoutDuration = 300; // 5 minute warm up as standard
        for (Exercise e : mExercises){
            if (e.getPrescribedReps() == null){
                // reps not prescribed for exercise, add default value of 5 minutes for this exercise
                workoutDuration+=300;
            } else {

                // estimate duration for set
                List<Integer> prescribedReps = e.getPrescribedReps();
                int exerciseDuration = 0;
                for (int rep : prescribedReps){
                    int setDuration = rep * 5;
                    workoutDuration += setDuration;
                }

                // add in rest intervals
                int totalRestTime = 0;
                if (e.getRestInterval() > 0){
                    totalRestTime = e.getRestInterval() * (prescribedReps.size() - 1);

                } else {
                    totalRestTime = 60 * (prescribedReps.size() - 1);
                }
                workoutDuration += totalRestTime;
            }
        }

        // add in rest between exercises
        int restBetweenExercises = 120 * (mExercises.size() - 1);
        workoutDuration += restBetweenExercises;

        return workoutDuration;
    }


    //TODO: bug - does not return most reps for heaviest lift if less reps lifted on same weight in same session
    public Map<String, Object> heaviestWeightLifted(){
        Map<String, Object> heaviestWeightMap = new HashMap<>();
        double heaviestWeight = 0;
        int numReps = 0;
        int index = 0;
        String workoutKey;
        String workoutDate;

        for (int j = 0; j < mExercises.size(); j++){
            Log.i(TAG, "Exercise" + mExercises.get(j).getWeight());
            Exercise e = mExercises.get(j);
            List<Double> weightList = e.getWeight();
            List<Integer> repsList = e.getReps();
            if (weightList != null) {
                for (int i = 0; i < weightList.size(); i++){
                    double highestVol = 0;
                    if (weightList.get(i) >= heaviestWeight){
                        heaviestWeight = weightList.get(i);
                        double volume = heaviestWeight * repsList.get(i);
                        if (volume > highestVol){
                            numReps = repsList.get(i);
                        }
                        index = j;
                    }
                }
            }
        }

        workoutKey = mWorkoutKeys.get(index);
        workoutDate = mExerciseDates.get(index);

        heaviestWeightMap.put(KEY_WEIGHT, heaviestWeight);
        heaviestWeightMap.put(KEY_REPS, numReps);
        heaviestWeightMap.put(KEY_DATE, workoutDate);
        heaviestWeightMap.put(KEY_WORKOUT_KEY, workoutKey);

        Log.i(TAG, "Heaviest weight : " + heaviestWeight + " lifted for " + numReps + " reps" + " Exercise index: " + index + " on: " + workoutDate + " workoutKey: " + workoutKey);

        return heaviestWeightMap;
    }

    public Map<String, Object> mostReps(){
        Map<String, Object> mostRepsMap = new HashMap<>();
        double weight = 0;
        int mostReps = 0;
        int index = 0;
        String workoutKey;
        String workoutDate;

        for (int j = 0; j < mExercises.size(); j++){

            Exercise e = mExercises.get(j);
            List<Double> weightList = e.getWeight();
            List<Integer> repsList = e.getReps();
            if (weightList != null) {
                for (int i = 0; i < repsList.size(); i++){
                    double highestVol = 0;
                    if (repsList.get(i) >= mostReps){
                        mostReps = repsList.get(i);
                        double volume = weightList.get(i) * mostReps;
                        if (volume > highestVol){
                            weight = weightList.get(i);
                        }
                        index = j;
                    }
                }
            }
        }

        workoutKey = mWorkoutKeys.get(index);
        workoutDate = mExerciseDates.get(index);

        mostRepsMap.put(KEY_WEIGHT, weight);
        mostRepsMap.put(KEY_REPS, mostReps);
        mostRepsMap.put(KEY_DATE, workoutDate);
        mostRepsMap.put(KEY_WORKOUT_KEY, workoutKey);

        Log.i(TAG, "Weight : " + weight + " lifted for " + mostReps + " reps" + " Exercise index: " + index + " on: " + workoutDate + " workoutKey: " + workoutKey);

        return mostRepsMap;
    }

    public Map<String, Object> mostVolume(){
        Map<String, Object> mostVolumeMap = new HashMap<>();
        double mostVol = 0.0;
        double weight = 0.0;
        int reps = 0;
        int index = 0;
        String workoutKey;
        String workoutDate;

        for (int i = 0; i < mExercises.size(); i++){
            Exercise e = mExercises.get(i);
            List<Double> weightList = e.getWeight();
            List<Integer> repsList = e.getReps();
            if (weightList != null) {
                for (int j = 0; j < weightList.size(); j++) {
                    double volume = weightList.get(j) * repsList.get(j);
                    if (volume > mostVol) {
                        mostVol = volume;
                        weight = weightList.get(j);
                        reps = repsList.get(j);
                        index = i;
                    }
                }
            }

        }

        workoutKey = mWorkoutKeys.get(index);
        workoutDate = mExerciseDates.get(index);

        mostVolumeMap.put(KEY_VOLUME, mostVol);
        mostVolumeMap.put(KEY_WEIGHT, weight);
        mostVolumeMap.put(KEY_REPS, reps);
        mostVolumeMap.put(KEY_DATE, workoutDate);
        mostVolumeMap.put(KEY_WORKOUT_KEY, workoutKey);

        Log.i(TAG, "Maximum volume in single set: " + mostVol + " set on: " + workoutDate + " workoutKey: " +workoutKey);

        return mostVolumeMap;
    }


    public double estimatedMax(double weightLifted, int reps, int repMax) {
        double oneRepMaxEstimate = (weightLifted / (1.0278 - (0.0278 * reps)));
        double estimatedMax;
        int repMaxRequired = repMax - 1;

        double[] estimatedReps = new double[]{1, 0.95, 0.90, 0.88, 0.86, 0.83, 0.80, 0.78, 0.76, 0.75, 0.72, 0.70};

        estimatedMax = oneRepMaxEstimate * estimatedReps[repMaxRequired];

        return estimatedMax;

    }

    // Create DataTools for specified date range - can then analyse statistics for date range using normal data tools methods.
    public DataTools getDataToolsForDateRange(DataTools dataTools, int dateRange){

        DataTools oldDataTools = dataTools;
        DataTools newDataTools = new DataTools(Utils.getUid(), oldDataTools.getExerciseKey());

        DateTime dateFrom;
        DateTime dateTo;

        DateTime now = new DateTime();

        switch(dateRange){

            case TODAY:
                dateFrom = now.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                dateTo = now.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                newDataTools = setNewDataTools(dateFrom, dateTo, oldDataTools);
                break;
            case THIS_WEEK:
                dateFrom = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                dateTo = now.withDayOfWeek(DateTimeConstants.SUNDAY).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                newDataTools = setNewDataTools(dateFrom, dateTo, oldDataTools);
                break;
            case THIS_MONTH:
                dateFrom = now.withDayOfMonth(1).withTimeAtStartOfDay();
                dateTo = dateFrom.plusMonths(1).minusDays(1).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                Log.i(TAG, "dateFrom: " + dateFrom.toString() + " dateTo: " + dateTo.toString());
                newDataTools = setNewDataTools(dateFrom, dateTo, oldDataTools);
                break;
            case LAST_28_DAYS:
                dateFrom = now.minusDays(28).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                dateTo = now.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                newDataTools = setNewDataTools(dateFrom, dateTo, oldDataTools);
                break;
            case LAST_6_MONTHS:
                dateFrom = now.minusMonths(6).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                dateTo = now.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                newDataTools = setNewDataTools(dateFrom, dateTo, oldDataTools);
                break;
            case THIS_YEAR:
                dateFrom = new DateTime().dayOfYear().withMinimumValue().withTimeAtStartOfDay();
                dateTo = now.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                newDataTools = setNewDataTools(dateFrom, dateTo, oldDataTools);
                break;
            case ALL_TIME:
                break;
        }

        return newDataTools;
    }



    private DataTools setNewDataTools(DateTime dateFrom, DateTime dateTo, DataTools dataTools){

        DataTools oldDataTools = dataTools;
        DataTools newDataTools = new DataTools(Utils.getUid(), oldDataTools.getExerciseKey());

        List<Exercise> exercisesOld = oldDataTools.getExercises();
        List<String> datesOld = oldDataTools.getExerciseDates();
        List<String> workoutKeysOld = oldDataTools.getWorkoutKeys();

        List<Exercise> exercisesNew = new ArrayList<>();
        List<String> datesNew = new ArrayList<>();
        List<String> workoutKeysNew = new ArrayList<>();


        Log.i(TAG, "Date from: " + dateFrom + " Date to: " + dateTo);

        Interval interval = new Interval(dateFrom, dateTo);

        for (int i = 0; i < datesOld.size(); i++) {
            DateTime date = DateTime.parse(datesOld.get(i), DateTimeFormat.forPattern("yyyy-MM-dd, HH:mm:ss"));
            Log.i(TAG, "dates of this exercise: " + date.toString());
            if (interval.contains(date)) {
                Log.i(TAG, "This workout was performed on " + date.toString() + " workoutKey: " + workoutKeysOld.get(i));
                datesNew.add(datesOld.get(i));
                newDataTools.setExerciseDates((ArrayList) datesNew);


                exercisesNew.add(exercisesOld.get(i));
                newDataTools.setExercises((ArrayList) exercisesNew);


                workoutKeysNew.add(workoutKeysOld.get(i));
                newDataTools.setWorkoutKeys((ArrayList) workoutKeysNew);
            }
        }


        if (datesNew.size() == 0){
            Log.i(TAG, "No workouts found, return unchanged datatools object");
            newDataTools = oldDataTools;
        }

        Log.i(TAG, "datesNew: " + newDataTools.getExerciseDates());
        for (Exercise e : newDataTools.getExercises()) {
            Log.i(TAG, "exercisesNew: " + e.getReps());
        }
        Log.i(TAG, "workoutKeysNew: " + newDataTools.getWorkoutKeys());

        return newDataTools;

    }

    public DataTools getDataToolsForSingleSession(String workoutKey){

        DataTools newDataTools = new DataTools(Utils.getUid(), getExerciseKey());

        List<Exercise> exercisesOld = getExercises();
        List<String> datesOld = getExerciseDates();
        List<String> workoutKeysOld = getWorkoutKeys();

        List<Exercise> exercisesNew = new ArrayList<>();
        List<String> datesNew = new ArrayList<>();
        List<String> workoutKeysNew = new ArrayList<>();

        for (int i = 0; i < workoutKeysOld.size(); i++) {
            if (workoutKeysOld.get(i).contains(workoutKey)) {
                Log.i(TAG, "Workout found for this date");
                datesNew.add(datesOld.get(i));
                newDataTools.setExerciseDates((ArrayList) datesNew);

                exercisesNew.add(exercisesOld.get(i));
                newDataTools.setExercises((ArrayList) exercisesNew);

                workoutKeysNew.add(workoutKeysOld.get(i));
                newDataTools.setWorkoutKeys((ArrayList) workoutKeysNew);
            }
        }

        Log.i(TAG, "datesNew: " + newDataTools.getExerciseDates());
        for (Exercise e : newDataTools.getExercises()) {
            Log.i(TAG, "exercisesNew: " + e.getReps());
        }
        Log.i(TAG, "workoutKeysNew: " + newDataTools.getWorkoutKeys());

        return newDataTools;
    }


    private Interval getInterval(int dateRange) {

        DateTime now = new DateTime();
        DateTime dateFrom;
        DateTime dateTo;

        switch (dateRange) {
            case TODAY:
                dateFrom = now.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                dateTo = now.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                return new Interval(dateFrom, dateTo);

            case THIS_WEEK:
                dateFrom = now.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                dateTo = now.withDayOfWeek(DateTimeConstants.SUNDAY).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                return new Interval(dateFrom, dateTo);

            case THIS_MONTH:
                dateFrom = now.withDayOfMonth(1).withTimeAtStartOfDay();
                dateTo = dateFrom.plusMonths(1).minusDays(1).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                return new Interval(dateFrom, dateTo);

            case LAST_28_DAYS:
                dateFrom = now.minusDays(28).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                dateTo = now.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                return new Interval(dateFrom, dateTo);

            case LAST_6_MONTHS:
                dateFrom = now.minusMonths(6).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                dateTo = now.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                return new Interval(dateFrom, dateTo);

            case THIS_YEAR:
                dateFrom = new DateTime().dayOfYear().withMinimumValue().withTimeAtStartOfDay();
                dateTo = now.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                return new Interval(dateFrom, dateTo);

            case ALL_TIME:
                dateFrom = getFirstWorkoutDate();
                dateTo = now.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                return new Interval(dateFrom, dateTo);
        }

        return null;
    }

    public DateTime getFirstWorkoutDate(){
        Collections.sort(mExerciseDates);
        DateTime date = Utils.getDateTimeFromString(mExerciseDates.get(0), Utils.DATE_FORMAT_FULL);
        Log.i(TAG, "First workout date: " + date.toString());
        return date;
    }

    // Record analysis
    public Map<String, Object> getRepMax(int numReps){

        Map<String, Object> repMaxMap = new HashMap<>();

        double maxWeight = 0.0;
        String workoutDate = new String();
        String workoutKey = new String();
        int index = 0;

        for (int j = 0; j < mExercises.size(); j++){
            Exercise e = mExercises.get(j);
            if (e.getReps() != null) {
                for (int i = 0; i < e.getReps().size(); i++) {
                    if (e.getReps().get(i) == numReps) {
                        double currentWeight = e.getWeight().get(i);
                        Log.i(TAG, "Reps found matching rep-max, and weight lifted: " + currentWeight);
                        if (currentWeight > maxWeight) {
                            maxWeight = currentWeight;
                            index = j;
                            workoutDate = mExerciseDates.get(index);
                            workoutKey = mWorkoutKeys.get(index);
                        }
                    }
                }
            }
        }

        repMaxMap.put(KEY_WEIGHT, maxWeight);
        repMaxMap.put(KEY_DATE, workoutDate);
        repMaxMap.put(KEY_WORKOUT_KEY, workoutKey);

        Log.i(TAG, numReps + " rep-max found for this date range: " + maxWeight + " At index: " + index + " Date set: " + workoutDate + " workoutKey: " + workoutKey);

        return repMaxMap;

    }


    /****** METHODS FOR RECORDS ******/

    //checks if weight lifted is a record, if so, updates the database with the new record
    public boolean isRecord(double weight, String reps, String workoutKey){
        boolean recordSet = false;
        String key = reps + " rep-max";
        Map<String, List<Double>> records = mExerciseRecord.getRecords();

        // Set record date to same date as workout and set time of record to current time of adding
        // This is to ensure that if a record is back dated on an EARLIER date, it is not recorded with the current date

        String workoutDate = mExerciseDates.get(mWorkoutKeys.indexOf(workoutKey));
        LocalTime timeNow = new LocalTime();
        String format = "yyyy-MM-dd, HH:mm:ss";
        DateTime recordDate = DateTime.parse((workoutDate), DateTimeFormat.forPattern(format)).withHourOfDay(timeNow.getHourOfDay()).withMinuteOfHour(timeNow.getMinuteOfHour()).withSecondOfMinute(timeNow.getSecondOfMinute());
        String date = recordDate.toString(format);

        if (records == null){
            Log.i(TAG, "sets map not yet created");
        } else {
            if (records.containsKey(key)) {
                int index = records.get(key).size() - 1;
                double oldWeight = records.get(key).get(index);
                Log.i(TAG, "This number of reps has been recorded before, and the weight lifted was: " + records.get(key).get(index));
                if(weight > oldWeight){
                    Log.i(TAG, "New " + reps + " rep-max set");

                   /* removePreviousRecordFromSameWorkout(key, workoutKey);*/
                    //update record
                    mExerciseRecord.getRecords().get(key).add(weight);

                    //update record date



                    mExerciseRecord.getDate().get(key).add(date);

                    //update workout key
                    mExerciseRecord.getWorkoutKey().get(key).add(workoutKey);

                    recordSet = true;
                } else {
                    Log.i(TAG, "No record set");
                }
            } else {
                Log.i(TAG, "This number of reps has never been done before, add to record. New " + reps + " rep-max set");
                List<Double> weightList = new ArrayList<>();
                weightList.add(weight);

                List<String> dateList = new ArrayList<>();
                dateList.add(date);

                List<String> workoutKeyList = new ArrayList<>();
                workoutKeyList.add(workoutKey);

                mExerciseRecord.getRecords().put(key, weightList);
                mExerciseRecord.getDate().put(key, dateList);
                mExerciseRecord.getWorkoutKey().put(key, workoutKeyList);

                recordSet = true;
            }
        }
        return recordSet;
    }

   /* private void removePreviousRecordFromSameWorkout(String key, String workoutKey) {
        List<String> workoutKeys = mExerciseRecord.getWorkoutKey().get(key);
        List<Double> records = mExerciseRecord.getRecords().get(key);
        List<String> workoutDate = mExerciseRecord.getDate().get(key);

        if (workoutKeys.contains(workoutKey)){
            Log.i(TAG, "A record for this rep range has already been recorded in this workout.");
            ArrayList<Integer> indexList = new ArrayList<>();
            for (int i = 0; i < workoutKeys.size(); i++) {
                if (workoutKey.equals(workoutKeys.get(i))) {
                    indexList.add(i);
                }
                Log.i(TAG, "indexList = " + indexList);
            }
            Collections.sort(indexList, Collections.<Integer>reverseOrder());
            for (int j : indexList){
                workoutKeys.remove(j);
                records.remove(j);
                workoutDate.remove(j);
            }

            mExerciseRecord.getWorkoutKey().remove(key);
            mExerciseRecord.getDate().remove(key);
            mExerciseRecord.getRecords().remove(key);

            mExerciseRecord.getWorkoutKey().put(key, workoutKeys);
            mExerciseRecord.getRecords().put(key, records);
            mExerciseRecord.getDate().put(key, workoutDate);

        }
    }*/

    /****** END METHODS FOR RECORDS ******/

}
