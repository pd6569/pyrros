package com.zonesciences.pyrros.ActionMode;

import android.content.Context;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.SetsAdapter;
import com.zonesciences.pyrros.adapters.SortWorkoutAdapter;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;

/**
 * Created by Peter on 05/12/2016.
 */
public class ActionModeCallback implements ActionMode.Callback {

    private static final String TAG = "ActionModeCallback";
    private Context mContext;
    private SortWorkoutAdapter mSortWorkoutAdapter;
    private SetsAdapter mSetsAdapter;
    private ArrayList<Exercise> mWorkoutExercises;
    boolean mIsSortWorkout;

    // Exercises changed listener
    onFinishedActionMode mActionModeFinishedListener;

    public ActionModeCallback(){

    }

    public ActionModeCallback(Context context, SortWorkoutAdapter sortWorkoutAdapter, ArrayList<Exercise> workoutExercise){
        this.mContext = context;
        this.mSortWorkoutAdapter = sortWorkoutAdapter;
        this.mWorkoutExercises = workoutExercise;
        mIsSortWorkout = true;
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
                if (mIsSortWorkout) {
                    mSortWorkoutAdapter.deleteSelectedItems();
                } else {
                    mSetsAdapter.deleteSelectedItems();
                }
                mActionModeFinishedListener.onActionModeFinished();
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Log.i(TAG, "onDestroyActionMode");
        if (mIsSortWorkout) {
            mSortWorkoutAdapter.clearSelectedItems();
        } else {
            mSetsAdapter.clearSelectedItems();
        }
        mActionModeFinishedListener.onActionModeFinished();
    }

    public interface onFinishedActionMode {
        void onActionModeFinished();
    }

    public void setOnFinishedActionModeListener(onFinishedActionMode listener){
        this.mActionModeFinishedListener = listener;
    }

    public void setSetsAdapter(SetsAdapter setsAdapter) {
        mSetsAdapter = setsAdapter;
    }
}
