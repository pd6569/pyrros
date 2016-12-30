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
import com.zonesciences.pyrros.fragment.Routine.WorkoutChangedListener;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;

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

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView workoutTitleTextView;
        ImageView deleteWorkoutImageView;
        ImageView reorderHandle;
        TextView noExercisesTextView;
        LinearLayout exercisesContainerLinearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            workoutTitleTextView = (TextView) itemView.findViewById(R.id.routine_workout_item_textview);
            deleteWorkoutImageView = (ImageView) itemView.findViewById(R.id.routine_workout_delete_imageview);
            deleteWorkoutImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWorkouts.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    mWorkoutChangedListener.onWorkoutRemoved();
                }
            });
            reorderHandle = (ImageView) itemView.findViewById(R.id.routine_workout_reorder_workouts);
            noExercisesTextView = (TextView) itemView.findViewById(R.id.no_exercises_textview);
            noExercisesTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAddExerciseListener.onAddFirstExercises(getAdapterPosition());
                }
            });

            exercisesContainerLinearLayout = (LinearLayout) itemView.findViewById(R.id.linear_layout_routine_workout_exercises_container);
            exercisesContainerLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Clicked workout: " + mWorkouts.get(getAdapterPosition()).getName() + " Position: " + getAdapterPosition());
                    mAddExerciseListener.onChangeExistingExercises(getAdapterPosition());
                }
            });
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
                TextView exerciseName = new TextView(mActivity);
                exerciseName.setText(e.getName());
                holder.exercisesContainerLinearLayout.addView(exerciseName);
            }
        } else {
            holder.noExercisesTextView.setVisibility(View.VISIBLE);
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

    }

    // END ITEM TOUCH HELPER METHODS
}