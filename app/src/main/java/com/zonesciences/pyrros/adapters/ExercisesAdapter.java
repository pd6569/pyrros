package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;

import java.util.List;

/**
 * Created by Peter on 24/10/2016.
 */
public class ExercisesAdapter extends RecyclerView.Adapter<ExercisesAdapter.ViewHolder> {

    private List<String> mExercises;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView exerciseName;

        public ViewHolder(View itemView) {
            super(itemView);

            exerciseName = (TextView) itemView.findViewById(R.id.exercise_name);
        }
    }

    // Provide suitable constructor
    public ExercisesAdapter(List<String> exercises) {
        mExercises = exercises;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ExercisesAdapter.ViewHolder holder, int position) {
        holder.exerciseName.setText(mExercises.get(position));
    }



    @Override
    public int getItemCount() {
        return mExercises.size();
    }
}
