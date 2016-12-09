package com.zonesciences.pyrros.fragment.CreateWorkout;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zonesciences.pyrros.ActionMode.ActionModeCallback;
import com.zonesciences.pyrros.ActionMode.RecyclerClickListener;
import com.zonesciences.pyrros.ActionMode.RecyclerTouchListener;
import com.zonesciences.pyrros.CreateWorkoutActivity;
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperCallback;
import com.zonesciences.pyrros.ItemTouchHelper.OnDragListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.ExercisesFilterAdapter;
import com.zonesciences.pyrros.adapters.SortWorkoutAdapter;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class SortWorkoutFragment extends Fragment implements OnDragListener {

    private static final String TAG = "SortWorkoutFrag";

    private static final String ARG_EXERCISES = "WorkoutExerciseList";

    // RecyclerView components and views
    RecyclerView mRecyclerView;
    SortWorkoutAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    Toolbar mToolbar;

    // Context
    Context mContext;

    // Data
    ArrayList<Exercise> mWorkoutExercises = new ArrayList<>();
    boolean mExercisesAdded;

    // Touch Helper
    ItemTouchHelper mItemTouchHelper;
    ItemTouchHelper.Callback mItemTouchHelperCallback;
    boolean isBeingDragged;

    // Action Mode
    ActionMode mActionMode;

    // Exercise Listener
    ExercisesListener mExercisesListener;

    // Menu
    private MenuItem mStartWorkoutAction;

    public static SortWorkoutFragment newInstance(){
        SortWorkoutFragment fragment = new SortWorkoutFragment();
        return fragment;
    }


    public SortWorkoutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        mContext = getContext();

        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_sort_workout, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_sort_workout);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new SortWorkoutAdapter(getActivity(), mWorkoutExercises, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (mActionMode != null){
                    // select with single click if action mode is active
                    onExerciseSelected(position);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                if (!isBeingDragged) {
                    mAdapter.setAllowReordering(false);
                    mAdapter.notifyDataSetChanged();
                    onExerciseSelected(position);
                }
            }
        }));

        return rootView;
    }

    private void onExerciseSelected(int position){
        mAdapter.toggleSelection(position);
        boolean hasSelectedExercises = mAdapter.getSelectedCount() > 0;

        if (hasSelectedExercises && mActionMode == null){
            // there are some selected items, start the action mode
            Log.i(TAG, "Start action mode");
            ActionModeCallback actionModeCallback = new ActionModeCallback(getActivity(), mAdapter, mWorkoutExercises);
            actionModeCallback.setOnFinishedActionModeListener(new ActionModeCallback.onFinishedActionMode() {
                @Override
                public void onActionModeFinished() {
                    if (mActionMode != null){
                        mActionMode.finish();
                        setActionModeNull();
                    }
                }
            });
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
        }
        else if (!hasSelectedExercises && mActionMode != null){
            // no selected items, finish action mode
            Log.i(TAG, "End action mode");
            mActionMode.finish();
            setActionModeNull();
            mAdapter.setAllowReordering(true);
            mAdapter.notifyDataSetChanged();
        }
        if (mActionMode != null){
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(mAdapter.getSelectedCount()) + " selected");

        }
    }

    //set action mode null after use
    public void setActionModeNull(){
        if (mActionMode != null){
            mActionMode = null;
        }
    }

    // delete selected exercises


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG, "onStart");


    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        if (isVisibleToUser) {

            if (!mExercisesAdded) {
                if (!mWorkoutExercises.isEmpty()) {
                    mExercisesAdded = true;
                    mAdapter = new SortWorkoutAdapter(getActivity(), mWorkoutExercises, this);
                    mAdapter.setExercisesListener(new ExercisesListener() {
                        @Override
                        public void onExerciseAdded(Exercise exercise) {

                        }

                        @Override
                        public void onExercisesEmpty() {
                            Log.i(TAG, "Exercises empty");
                            mStartWorkoutAction.setVisible(false);
                        }

                        @Override
                        public void onExerciseRemoved(Exercise exercise) {

                        }

                        @Override
                        public void onExercisesChanged(ArrayList<Exercise> exerciseList) {
                            Log.i(TAG, "Exercises changed in sort workout adapter, fragment notified. Now notify host activity");
                            mWorkoutExercises = exerciseList;
                            mExercisesListener.onExercisesChanged(mWorkoutExercises);
                        }
                    });
                    mRecyclerView.setAdapter(mAdapter);
                    mItemTouchHelperCallback = new ItemTouchHelperCallback(mAdapter);
                    mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
                    mItemTouchHelper.attachToRecyclerView(mRecyclerView);
                }
            }
        }
    }


    public List<Exercise> getWorkoutExercises() {
        return mWorkoutExercises;
    }

    public void setWorkoutExercises(List<Exercise> workoutExercises) {
        mWorkoutExercises = (ArrayList) workoutExercises;
    }

    public SortWorkoutAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onStartDrag (RecyclerView.ViewHolder viewHolder){
        mItemTouchHelper.startDrag(viewHolder);
        isBeingDragged = true;
        Log.i(TAG, "Item is being dragged, do not allow selection");
    }

    @Override
    public void onStopDrag(){
        isBeingDragged = false;
        Log.i(TAG, "Item has finished being dragged, allow selection");
    }

    /**
     * Set exercise change listener
     */

    public void setExercisesListener(ExercisesListener listener){
        this.mExercisesListener = listener;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_create_workout, menu);
        super.onCreateOptionsMenu(menu, inflater);

        mStartWorkoutAction = menu.findItem(R.id.action_start_workout);
        if (mWorkoutExercises.size() > 0){
            mStartWorkoutAction.setVisible(true);
        }

        MenuItem search = menu.findItem(R.id.action_search);
        search.setVisible(false);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_start_workout:

                CreateWorkoutFragment frag = (CreateWorkoutFragment) ((CreateWorkoutActivity) getActivity()).getFragment(0);
                frag.startWorkout();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }

    }
}
