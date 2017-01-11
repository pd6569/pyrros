package com.zonesciences.pyrros.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zonesciences.pyrros.CreateWorkoutActivity;
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperAdapter;
import com.zonesciences.pyrros.ItemTouchHelper.OnDragListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.RoutineActivity;
import com.zonesciences.pyrros.datatools.DataTools;
import com.zonesciences.pyrros.fragment.Routine.WorkoutChangedListener;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by peter on 28/12/2016.
 */

public class RoutineWorkoutsAdapter extends RecyclerView.Adapter<RoutineWorkoutsAdapter.ViewHolder> implements ItemTouchHelperAdapter{

    private static final String TAG = "RoutineWorkoutsAdapter";

    Context mActivity;
    List<Workout> mWorkouts;

    // Listeners
    AddExerciseListener mAddExerciseListener;
    WorkoutChangedListener mWorkoutChangedListener;
    OnDragListener mDragListener;

    // Allow editing of workout
    boolean mAllowEditing;

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView workoutTitleTextView;
        ImageView deleteWorkoutImageView;
        ImageView reorderHandle;
        TextView noExercisesTextView;
        LinearLayout exercisesContainerLinearLayout;

        // Workout Overview
        LinearLayout workoutOverviewContainer;
        TextView numExercises;
        TextView totalSetsTextView;
        TextView muscleGroupsTextView;
        TextView workoutDurationTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            workoutTitleTextView = (TextView) itemView.findViewById(R.id.routine_workout_item_textview);
            deleteWorkoutImageView = (ImageView) itemView.findViewById(R.id.routine_workout_delete_imageview);
            if (!mAllowEditing){
                deleteWorkoutImageView.setVisibility(View.INVISIBLE);
            }
            deleteWorkoutImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWorkouts.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    mWorkoutChangedListener.onWorkoutRemoved();
                }
            });
            reorderHandle = (ImageView) itemView.findViewById(R.id.routine_workout_reorder_workouts);
            if (!mAllowEditing){
                reorderHandle.setVisibility(View.INVISIBLE);
            }
            noExercisesTextView = (TextView) itemView.findViewById(R.id.no_exercises_textview);
            if (mAllowEditing) {
                noExercisesTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAddExerciseListener.onAddFirstExercises(getAdapterPosition());
                    }
                });
            }

            exercisesContainerLinearLayout = (LinearLayout) itemView.findViewById(R.id.linear_layout_routine_workout_exercises_container);
            if (mAllowEditing) {
                exercisesContainerLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "Clicked workout: " + mWorkouts.get(getAdapterPosition()).getName() + " Position: " + getAdapterPosition());
                        mAddExerciseListener.onChangeExistingExercises(getAdapterPosition());
                    }
                });
            }

            workoutOverviewContainer = (LinearLayout) itemView.findViewById(R.id.linear_layout_routine_workout_overview_container);
            numExercises = (TextView) itemView.findViewById(R.id.routine_workout_num_exercises_textview);
            totalSetsTextView = (TextView) itemView.findViewById(R.id.routine_workout_total_sets_textview);
            muscleGroupsTextView = (TextView) itemView.findViewById(R.id.routine_workout_muscle_groups_textview);
            workoutDurationTextView = (TextView) itemView.findViewById(R.id.routine_workout_workout_duration_textview);
        }
    }

    public RoutineWorkoutsAdapter(Activity activity, List<Workout> workouts, AddExerciseListener addExerciseListener, WorkoutChangedListener workoutChangedListener, OnDragListener dragListener){
        this.mActivity = activity;
        this.mWorkouts = workouts;
        this.mAddExerciseListener = addExerciseListener;
        this.mWorkoutChangedListener = workoutChangedListener;
        this.mDragListener = dragListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.item_routine_workout, parent, false);
        return new RoutineWorkoutsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.workoutTitleTextView.setText(mWorkouts.get(position).getName());
        holder.exercisesContainerLinearLayout.removeAllViews();
        holder.reorderHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDragListener.onStartDrag(holder);
                return true;
            }
        });

        List<Exercise> exercises = mWorkouts.get(position).getExercises();

        if (exercises != null) {
            if (exercises.isEmpty()) {
                holder.noExercisesTextView.setVisibility(View.VISIBLE);
            } else {
                holder.noExercisesTextView.setVisibility(View.GONE);
            }
            for (Exercise e : exercises){
                LayoutInflater inflater = LayoutInflater.from(mActivity);
                View exerciseView = inflater.inflate(R.layout.item_routine_exercise_details, null);
                TextView exerciseNameText = (TextView) exerciseView.findViewById(R.id.routine_exercise_details_exercise_name);
                TextView setsText = (TextView) exerciseView.findViewById(R.id.routine_exercise_details_sets);

                exerciseNameText.setText(e.getName());
                List<Integer> sets = e.getPrescribedReps();
                if (sets != null){
                    setsText.setText(Utils.generateSetsInfoString(sets));
                } else {
                    setsText.setVisibility(View.GONE);
                }

                holder.exercisesContainerLinearLayout.addView(exerciseView);
            }
        } else {
            holder.noExercisesTextView.setVisibility(View.VISIBLE);
        }

        // Set workout overview layout
        holder.workoutOverviewContainer.setVisibility(View.GONE);
        if (exercises != null){
            holder.workoutOverviewContainer.setVisibility(View.VISIBLE);
            DataTools dataTools = new DataTools((ArrayList) mWorkouts.get(position).getExercises());
            int totalSets = dataTools.totalPrescribedSets();
            String muscleGroups = dataTools.getMuscleGroupsForWorkout();
            int workoutDurationMinutes = dataTools.getEstimatedWorkoutDurationSeconds() / 60;

            holder.numExercises.setText(Integer.toString(exercises.size()));
            holder.totalSetsTextView.setText(Integer.toString(totalSets));
            holder.muscleGroupsTextView.setText(muscleGroups);
            holder.workoutDurationTextView.setText(Integer.toString(workoutDurationMinutes) + " mins");
        }
    }

    @Override
    public int getItemCount() {
        return mWorkouts.size();
    }

    // Getters and setters

    public List<Workout> getWorkouts() {
        return mWorkouts;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.mWorkouts = workouts;
    }

    public void setAllowEditing(boolean allowEditing) {
        mAllowEditing = allowEditing;
    }

    // Listener
    public interface AddExerciseListener {
        void onAddFirstExercises(int workoutPositionToUpdate);
        void onChangeExistingExercises(int workoutPositionToUpdate);
    }

    // START ITEM TOUCH HELPER METHODS

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition){
            for (int i = fromPosition; i < toPosition; i++){
                Collections.swap(mWorkouts, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--){
                Collections.swap(mWorkouts, i, i - 1);
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
        setWorkoutOrder();
        mWorkoutChangedListener.onWorkoutChanged();
    }

    // END ITEM TOUCH HELPER METHODS

    public void setWorkoutOrder(){
        for (int i = 0; i < mWorkouts.size(); i++){
            mWorkouts.get(i).setWorkoutOrder(i);
            for (Workout workout : mWorkouts){
                Log.i(TAG, "Workout : " + workout.getName() + " Order: " + workout.getWorkoutOrder());
            }
        }
    }
}
