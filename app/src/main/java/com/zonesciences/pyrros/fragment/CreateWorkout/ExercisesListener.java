package com.zonesciences.pyrros.fragment.CreateWorkout;

import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;

/**
 * Created by Peter on 05/12/2016.
 */
public interface ExercisesListener {

    public void onExerciseAdded (Exercise exercise);
    public void onExercisesEmpty ();
    public void onExerciseRemoved (Exercise exercise);
    public void onExercisesChanged (ArrayList<Exercise> exerciseList);

}
