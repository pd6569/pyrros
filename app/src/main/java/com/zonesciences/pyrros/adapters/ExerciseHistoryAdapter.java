package com.zonesciences.pyrros.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.zonesciences.pyrros.models.Exercise;

import java.util.List;

/**
 * Created by Peter on 10/11/2016.
 */
public class ExerciseHistoryAdapter extends RecyclerView.Adapter<ExerciseHistoryAdapter.ViewHolder> {

    //Execise data
    List<String> mWorkoutDates;
    List<Exercise> mExercises;

    public ExerciseHistoryAdapter(List<String> workoutDate, List<Exercise> exercises){
        this.mWorkoutDates = workoutDate;
        this.mExercises = exercises;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
