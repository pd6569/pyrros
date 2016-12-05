package com.zonesciences.pyrros.ActionMode;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.zonesciences.pyrros.CreateWorkoutActivity;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.SortWorkoutAdapter;
import com.zonesciences.pyrros.fragment.CreateWorkout.CreateWorkoutFragment;
import com.zonesciences.pyrros.fragment.CreateWorkout.SortWorkoutFragment;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;

/**
 * Created by Peter on 05/12/2016.
 */
public class ActionModeCallback implements ActionMode.Callback {

    private static final String TAG = "ActionModeCallback";
    private Context mContext;
    private SortWorkoutAdapter mSortWorkoutAdapter;
    private ArrayList<Exercise> mWorkoutExercises;

    // Exercises changed listener
    OnExercisesChangedListener mExercisesChangedListener;

    public ActionModeCallback(Context context, SortWorkoutAdapter sortWorkoutAdapter, ArrayList<Exercise> workoutExercise){
        this.mContext = context;
        this.mSortWorkoutAdapter = sortWorkoutAdapter;
        this.mWorkoutExercises = workoutExercise;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_cab, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_delete:
                Log.i(TAG, "Delete the exercise. get selected exercises: " + mSortWorkoutAdapter.getSelectedExerciseIds());
                mSortWorkoutAdapter.deleteSelectedExercises();
                mExercisesChangedListener.onExercisesDeleted();
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    public interface OnExercisesChangedListener {
        void onExercisesDeleted();
    }

    public void setOnExercisesChangedListener (OnExercisesChangedListener listener){
        this.mExercisesChangedListener = listener;
    }
}
