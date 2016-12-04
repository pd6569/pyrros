package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;


import java.util.ArrayList;


/**
 * Created by Peter on 03/12/2016.
 */
public class SortWorkoutAdapter extends RecyclerView.Adapter<SortWorkoutAdapter.SortWorkoutViewHolder> {

    Context mContext;
    ArrayList<Exercise> mWorkoutExercises = new ArrayList<>();

    public class SortWorkoutViewHolder extends RecyclerView.ViewHolder {

        TextView exerciseName;

        public SortWorkoutViewHolder(View itemView) {
            super(itemView);
            exerciseName = (TextView) itemView.findViewById(R.id.sort_workout_exercise_name);
        }
    }

    public SortWorkoutAdapter (Context context, ArrayList<Exercise> workoutExercises){
        System.out.println("sort workout adapter called");
        this.mContext = context;
        this.mWorkoutExercises = workoutExercises;
    }

    @Override
    public SortWorkoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_sort_workout, parent, false);
        return new SortWorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SortWorkoutViewHolder holder, int position) {
        holder.exerciseName.setText(mWorkoutExercises.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mWorkoutExercises.size();
    }




}
