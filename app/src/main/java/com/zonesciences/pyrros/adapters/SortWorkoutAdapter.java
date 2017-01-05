package com.zonesciences.pyrros.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zonesciences.pyrros.ActionMode.ActionModeAdapterInterface;
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperAdapter;
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperViewHolder;
import com.zonesciences.pyrros.ItemTouchHelper.OnDragListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.fragment.CreateWorkout.ExercisesListener;
import com.zonesciences.pyrros.fragment.CreateWorkout.SortWorkoutFragment;
import com.zonesciences.pyrros.models.Exercise;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by Peter on 03/12/2016.
 */

//TODO: consider creating alert dialog as a standalone fragment

public class SortWorkoutAdapter extends RecyclerView.Adapter<SortWorkoutAdapter.SortWorkoutViewHolder> implements ItemTouchHelperAdapter, ActionModeAdapterInterface {

    private static final String TAG = "SortWorkoutAdapter";
    Activity mActivity;
    ArrayList<Exercise> mWorkoutExercises = new ArrayList<>();
    OnDragListener mDragListener;

    SparseBooleanArray mSelectedExerciseIds;
    boolean allowReordering = true;

    // Exerise options variables
    List<Integer> mSets;
    int mNumReps;

    // Listener
    ExercisesListener mExercisesListener;

    public class SortWorkoutViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        TextView exerciseName;
        ImageView reorderHandle;
        ImageView deleteExercise;

        // Exercise options
        LinearLayout exerciseOptionsLayout;
        LinearLayout setsOptionsLayout;
        LinearLayout tempoOptionsLayout;
        LinearLayout restOptionsLayout;
        TextView setsOptions;
        TextView tempoOptions;
        TextView restOptions;

        public SortWorkoutViewHolder(View itemView) {
            super(itemView);
            exerciseName = (TextView) itemView.findViewById(R.id.sort_workout_exercise_name);
            exerciseName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mExercisesListener.onExerciseSelected(mWorkoutExercises.get(getAdapterPosition()));
                }
            });
            reorderHandle = (ImageView) itemView.findViewById(R.id.sort_workout_reorder_handle);
            deleteExercise = (ImageView) itemView.findViewById(R.id.sort_workout_delete);
            deleteExercise.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view){
                    final int position = getAdapterPosition();
                    if (!mWorkoutExercises.get(position).hasSets()) {
                        removeExercise(position);
                    } else {
                        Snackbar snackbar = Snackbar.make(deleteExercise, R.string.delete_exercise_warning, Snackbar.LENGTH_LONG).setAction(R.string.action_delete, new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                removeExercise(position);

                            }
                        });
                        View sbView = snackbar.getView();
                        sbView.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.snackbarNegative));
                        snackbar.show();
                    }

                }
            });
            exerciseOptionsLayout = (LinearLayout) itemView.findViewById(R.id.sort_workout_exercise_options_layout);
            setsOptionsLayout = (LinearLayout) itemView.findViewById(R.id.sort_workout_exercise_options_num_sets_layout);
            tempoOptionsLayout = (LinearLayout) itemView.findViewById(R.id.sort_workout_exercise_options_tempo_layout);
            restOptionsLayout = (LinearLayout) itemView.findViewById(R.id.sort_workout_exercise_options_rest_layout);

            setsOptions = (TextView) itemView.findViewById(R.id.sort_workout_exercise_options_num_sets_text);
            tempoOptions = (TextView) itemView.findViewById(R.id.sort_workout_exercise_options_tempo_text);
            restOptions = (TextView) itemView.findViewById(R.id.sort_workout_exercise_options_rest_layout_text);
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
    public void onBindViewHolder(final SortWorkoutViewHolder holder, final int position) {
        Log.i(TAG, "onBindViewHolder");
        holder.exerciseName.setText(mWorkoutExercises.get(position).getName());
        // CLEAR EXERCISE OPTIONS
        holder.exerciseOptionsLayout.setVisibility(View.GONE);

        if (mWorkoutExercises.get(position).getPrescribedReps() != null || mWorkoutExercises.get(position).getRepTempo() != null || mWorkoutExercises.get(position).getRestInterval() != 0){
            Log.i(TAG, "Some exercise options set");
            // Some exercises options are set, show exercise options
            holder.exerciseOptionsLayout.setVisibility(View.VISIBLE);

            List<Integer> prescribedReps = mWorkoutExercises.get(position).getPrescribedReps();
            int restInterval =  mWorkoutExercises.get(position).getRestInterval();
            String repTempo = mWorkoutExercises.get(position).getRepTempo();

            if (prescribedReps != null){
                holder.setsOptionsLayout.setVisibility(View.VISIBLE);
                holder.setsOptions.setText(Integer.toString(prescribedReps.size()));
                holder.setsOptions.setVisibility(View.VISIBLE);
            }
            if (restInterval != 0){
                holder.restOptionsLayout.setVisibility(View.VISIBLE);
                holder.restOptions.setText(Integer.toString(restInterval) + " seconds");
                holder.restOptions.setVisibility(View.VISIBLE);
            }
            if (repTempo != null){
                holder.tempoOptionsLayout.setVisibility(View.VISIBLE);
                holder.tempoOptions.setText(repTempo);
                holder.tempoOptions.setVisibility(View.VISIBLE);
            }
        }
        if (allowReordering) {
            holder.reorderHandle.setVisibility(View.VISIBLE);
            holder.deleteExercise.setVisibility(View.VISIBLE);
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
            holder.reorderHandle.setVisibility(View.INVISIBLE);
            holder.deleteExercise.setVisibility(View.INVISIBLE);
        }

        /** Change background color of the selected items in list view  **/
        holder.itemView.setBackgroundColor(mSelectedExerciseIds.get(position) ? ResourcesCompat.getColor(mActivity.getResources(), R.color.colorAccent, null) : Color.WHITE);

    }

    @Override
    public int getItemCount() {
        return mWorkoutExercises.size();
    }


    // Update exercise order
    public void setExerciseOrder() {
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
        System.out.println("Attemping to move exercise. mWorkoutExercises size: " + mWorkoutExercises.size() + " from position: " + fromPosition + " toPosition: " + toPosition);
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
        mDragListener.onStopDrag();
        setExerciseOrder();
        mExercisesListener.onExercisesChanged(mWorkoutExercises);
    }

    /***
     * Remove exercise
     */

    private void removeExercise(final int position){
        mWorkoutExercises.get(position).setSelected(false);
        mWorkoutExercises.remove(position);
        notifyItemRemoved(position);
        setExerciseOrder();
        mExercisesListener.onExercisesChanged(mWorkoutExercises);
        if (mWorkoutExercises.isEmpty()) {
            mExercisesListener.onExercisesEmpty();
        }
    }

    /***
     * Methods for selecting exercises
     */

    // Toggle selection methods
    public void toggleSelection(int position) {
        selectItem(position, !mSelectedExerciseIds.get(position));
    }

    // Remove selected selections
    public void removeSelection(){
        mSelectedExerciseIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    //Put or delete selected position into SparseBooleanArray
    public void selectItem(int position, boolean isSelected){
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
    public SparseBooleanArray getSelectedItemIds(){
        return mSelectedExerciseIds;
    }

    public void clearSelectedItems(){
        mSelectedExerciseIds.clear();
        allowReordering = true;
        notifyDataSetChanged();
    }

    public void deleteSelectedItems(){

        for (int i = (mSelectedExerciseIds.size()-1); i >= 0; i--){
            if (mSelectedExerciseIds.valueAt(i)){
                //if current id is selected set exercise as not selected in the workout and remove the item via key
                mWorkoutExercises.get(mSelectedExerciseIds.keyAt(i)).setSelected(false);
                mWorkoutExercises.remove(mSelectedExerciseIds.keyAt(i));
            }
        }
        setAllowReordering(true);
        setExerciseOrder();
        notifyDataSetChanged();
        mSelectedExerciseIds.clear();

        if(mWorkoutExercises.isEmpty()) {
            mExercisesListener.onExercisesEmpty();
        }

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
