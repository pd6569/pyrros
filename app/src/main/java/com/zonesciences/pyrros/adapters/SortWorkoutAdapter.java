package com.zonesciences.pyrros.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperAdapter;
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperViewHolder;
import com.zonesciences.pyrros.ItemTouchHelper.OnDragListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.fragment.CreateWorkout.ExercisesListener;
import com.zonesciences.pyrros.models.Exercise;


import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by Peter on 03/12/2016.
 */
public class SortWorkoutAdapter extends RecyclerView.Adapter<SortWorkoutAdapter.SortWorkoutViewHolder> implements ItemTouchHelperAdapter {

    private static final String TAG = "SortWorkoutAdapter";
    Activity mActivity;
    ArrayList<Exercise> mWorkoutExercises = new ArrayList<>();
    OnDragListener mDragListener;

    SparseBooleanArray mSelectedExerciseIds;
    boolean allowReordering = true;

    // Listener
    ExercisesListener mExercisesListener;

    public class SortWorkoutViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        TextView exerciseName;
        ImageView reorderHandle;

        public SortWorkoutViewHolder(View itemView) {
            super(itemView);
            exerciseName = (TextView) itemView.findViewById(R.id.sort_workout_exercise_name);
            reorderHandle = (ImageView) itemView.findViewById(R.id.sort_workout_reorder_handle);
        }

        @Override
        public void onSelected() {
            itemView.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.colorAccent));
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.cardBackground));
        }

    }

    public SortWorkoutAdapter (Activity activity, ArrayList<Exercise> workoutExercises, OnDragListener dragListener){
        System.out.println("sort workout adapter called");
        this.mActivity = activity;
        this.mWorkoutExercises = workoutExercises;
        this.mDragListener = dragListener;
        mSelectedExerciseIds = new SparseBooleanArray();
        setExerciseOrder();
    }



    @Override
    public SortWorkoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.item_sort_workout, parent, false);
        return new SortWorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SortWorkoutViewHolder holder, int position) {
        holder.exerciseName.setText(mWorkoutExercises.get(position).getName());
        if (allowReordering) {
            holder.reorderHandle.setVisibility(View.VISIBLE);
            holder.reorderHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                        Log.i(TAG, "Drag started");
                        mDragListener.onStartDrag(holder);

                    }


                    return false;
                }
            });
        } else {
            holder.reorderHandle.setVisibility(View.GONE);
        }

        /** Change background color of the selected items in list view  **/
        holder.itemView.setBackgroundColor(mSelectedExerciseIds.get(position) ? ResourcesCompat.getColor(mActivity.getResources(), R.color.colorAccent, null) : Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return mWorkoutExercises.size();
    }


    // Update exercise order
    private void setExerciseOrder() {
        for (int i = 0; i < mWorkoutExercises.size(); i++){
            mWorkoutExercises.get(i).setOrder(i);
            Log.i(TAG, "Setting exercise order. Exercise: " + mWorkoutExercises.get(i).getName() + " order: " + mWorkoutExercises.get(i).getOrder());
        }
    }


    /**
     * Methods for handling list reordering
     */

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
        setExerciseOrder();
        mExercisesListener.onExercisesChanged(mWorkoutExercises);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @Override
    public void onMoveCompleted() {
        mDragListener.onStopDrag();
    }

    /***
     * Methods for selecting exercises
     */

    // Toggle selection methods
    public void toggleSelection(int position) {
        selectExercise(position, !mSelectedExerciseIds.get(position));
    }

    // Remove selected selections
    public void removeSelection(){
        mSelectedExerciseIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    //Put or delete selected position into SparseBooleanArray
    public void selectExercise(int position, boolean isSelected){
        if (isSelected){
            mSelectedExerciseIds.put(position, isSelected);
        } else {
            mSelectedExerciseIds.delete(position);
        }
        notifyDataSetChanged();
    }

    // Get total selected count
    public int getSelectedCount(){
        return mSelectedExerciseIds.size();
    }

    // Return all selected exercises
    public SparseBooleanArray getSelectedExerciseIds(){
        return mSelectedExerciseIds;
    }

    public void clearSelectedExercises(){
        mSelectedExerciseIds.clear();
        allowReordering = true;
        notifyDataSetChanged();
    }

    public void deleteSelectedExercises(){

        for (int i = (mSelectedExerciseIds.size()-1); i >= 0; i--){
            if (mSelectedExerciseIds.valueAt(i)){
                //if current id is selected remove the item via key
                mWorkoutExercises.remove(mSelectedExerciseIds.keyAt(i));
            }
        }
        setAllowReordering(true);
        setExerciseOrder();
        notifyDataSetChanged();
        mSelectedExerciseIds.clear();

        // Notify fragment
        mExercisesListener.onExercisesChanged(mWorkoutExercises);

        Log.i(TAG, mSelectedExerciseIds.size() + " items deleted" + " Selected now: " + mSelectedExerciseIds);
    }

    public void setAllowReordering(boolean allowReordering) {
        this.allowReordering = allowReordering;
    }


    /**
     * Set exercise change listener
     */

    public void setExercisesListener(ExercisesListener listener){
        this.mExercisesListener = listener;
    }

}
