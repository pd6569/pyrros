package com.zonesciences.pyrros.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zonesciences.pyrros.CreateWorkoutActivity;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.RoutineActivity;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;

import java.util.List;

/**
 * Created by peter on 28/12/2016.
 */

public class RoutineWorkoutsAdapter extends RecyclerView.Adapter<RoutineWorkoutsAdapter.ViewHolder> {

    private static final String TAG = "RoutineWorkoutsAdapter";

    Context mActivity;
    List<Workout> mWorkouts;

    // Listener
    AddExerciseListener mAddExerciseListener;

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView workoutTitleTextView;
        ImageView deleteWorkoutImageView;
        TextView noExercisesTextView;
        LinearLayout exercisesContainerLinearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            workoutTitleTextView = (TextView) itemView.findViewById(R.id.routine_workout_item_textview);
            deleteWorkoutImageView = (ImageView) itemView.findViewById(R.id.routine_workout_delete_imageview);
            noExercisesTextView = (TextView) itemView.findViewById(R.id.no_exercises_textview);
            noExercisesTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAddExerciseListener.onAddFirstExercises();
                }
            });

            exercisesContainerLinearLayout = (LinearLayout) itemView.findViewById(R.id.linear_layout_routine_workout_exercises_container);
            exercisesContainerLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Clicked workout: " + mWorkouts.get(getAdapterPosition()).getName() + " Position: " + getAdapterPosition());
                    mAddExerciseListener.onChangeExistingExercises();
                }
            });
        }
    }

    public RoutineWorkoutsAdapter(Activity activity, List<Workout> workouts, AddExerciseListener listener){
        this.mActivity = activity;
        this.mWorkouts = workouts;
        this.mAddExerciseListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.item_routine_workout, parent, false);
        return new RoutineWorkoutsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.workoutTitleTextView.setText(mWorkouts.get(position).getName());
        holder.exercisesContainerLinearLayout.removeAllViews();
        List<Exercise> exercises = mWorkouts.get(position).getExercises();
        if (exercises != null) {
            holder.noExercisesTextView.setVisibility(View.GONE);
            for (Exercise e : exercises){
                TextView exerciseName = new TextView(mActivity);
                exerciseName.setText(e.getName());
                holder.exercisesContainerLinearLayout.addView(exerciseName);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mWorkouts.size();
    }

    // Listener
    public interface AddExerciseListener {
        void onAddFirstExercises();
        void onChangeExistingExercises();
    }
}
