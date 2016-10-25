package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 24/10/2016.
 */
public class ExercisesAdapter extends RecyclerView.Adapter<ExercisesAdapter.ViewHolder> {

    public static final String TAG = "ExerciseAdapter";

    private List<String> mExercises = new ArrayList<>();

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
