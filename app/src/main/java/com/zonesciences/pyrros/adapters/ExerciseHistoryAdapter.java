package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;

import java.util.List;

/**
 * Created by Peter on 10/11/2016.
 */
public class ExerciseHistoryAdapter extends RecyclerView.Adapter<ExerciseHistoryAdapter.ViewHolder> {

    Context mContext;

    //Exercise data
    List<String> mWorkoutDates;
    List<Exercise> mExercises;

    public ExerciseHistoryAdapter(Context context, List<String> workoutDate, List<Exercise> exercises){
        this.mContext = context;
        this.mWorkoutDates = workoutDate;
        this.mExercises = exercises;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_exercise_history, parent, false);
        return new ExerciseHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mExerciseDate.setText(mWorkoutDates.get(position));
    }

    @Override
    public int getItemCount() {
        return mExercises.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mExerciseDate;

        public ViewHolder(View itemView) {
            super(itemView);
            mExerciseDate = (TextView) itemView.findViewById(R.id.exercise_history_title_date);
        }
    }
}
