package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;

/**
 * Created by peter on 26/12/2016.
 */

public class RoutineExercisesAdapter extends RecyclerView.Adapter<RoutineExercisesAdapter.ViewHolder> {

    private static final String TAG = "RoutineExercisesAdapter";

    Context mContext;
    ArrayList<Exercise> mWorkoutExercises = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView exerciseName;

        public ViewHolder(View itemView) {
            super(itemView);

            exerciseName = (TextView) itemView.findViewById(R.id.exercise_name);
        }
    }

    public RoutineExercisesAdapter (Context context, ArrayList<Exercise> workoutExercises){
        Log.i(TAG, "adapter created");
        this.mContext = context;
        this.mWorkoutExercises = workoutExercises;
    }

    @Override
    public RoutineExercisesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_exercise, parent, false);
        return new RoutineExercisesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RoutineExercisesAdapter.ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder. exercise name: " + mWorkoutExercises);
        holder.exerciseName.setText(mWorkoutExercises.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mWorkoutExercises.size();
    }
}
