package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperAdapter;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Peter on 03/12/2016.
 */
public class SortWorkoutAdapter extends RecyclerView.Adapter<SortWorkoutAdapter.SortWorkoutViewHolder> implements ItemTouchHelperAdapter {

    Context mContext;
    ArrayList<Exercise> mWorkoutExercises = new ArrayList<>();

    boolean mMoved;

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


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        System.out.println("Attemping to moving exercise. mWorkoutExercises size: " + mWorkoutExercises.size() + " from position: " + fromPosition + " toPosition: " + toPosition);
        if (fromPosition < toPosition){
            for (int i = fromPosition; i < toPosition; i++){
                Collections.swap(mWorkoutExercises, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--){
                Collections.swap(mWorkoutExercises, i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @Override
    public void onMoveCompleted() {
    }


}
