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
 * Created by Peter on 30/11/2016.
 */
public class ExercisesFilterAdapter extends RecyclerView.Adapter<ExercisesFilterAdapter.ExercisesFilterViewHolder> {

    Context mContext;

    List<Exercise> mExercises;


    public class ExercisesFilterViewHolder extends RecyclerView.ViewHolder {

        TextView mExerciseName;

        public ExercisesFilterViewHolder(View itemView) {
            super(itemView);
            mExerciseName = (TextView) itemView.findViewById(R.id.exercise_filter_name);
        }
    }

    public ExercisesFilterAdapter(Context context, List<Exercise> exercises){
        this.mContext = context;
        this.mExercises = exercises;
    }

    @Override
    public ExercisesFilterAdapter.ExercisesFilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_exercise_filter, parent, false);
        return new ExercisesFilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExercisesFilterAdapter.ExercisesFilterViewHolder holder, int position) {
        holder.mExerciseName.setText(mExercises.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mExercises.size();
    }





}
