package com.zonesciences.pyrros.fragment.Routine;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.RoutinesAdapter;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Routine;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */

    //TODO: IF THERE ARE NO ROUTINES, THEN LOADING NEVER COMPLETES
    //TODO: MAKE SURE ROUTINES ONLY SHOW IN COMMUNITY FILTER WHEN THEY ARE ACTUALLY COMPLETED! NOT WITH 0 WORKOUTS

public class ViewRoutinesFragment extends Fragment {

    private static final String TAG = "ViewRoutinesFragment";

    // Filter constants
    public static final String FILTER_PYROS = "PyrosRoutines";
    public static final String FILTER_USER_ROUTINES = "UserRoutines";
    public static final String FILTER_FAVOURITE_ROUTINES = "FavouriteRoutines";
    public static final String FILTER_FRIENDS_ROUTINES = "FriendsRoutines";
    public static final String FILTER_COMMUNITY_ROUTINES = "CommunityRoutines";

    // Firebase
    DatabaseReference mDatabase;
    String mUid;

    // Routines
    List<Routine> mRoutines = new ArrayList<>();

    // RecyclerView
    RecyclerView mRecycler;
    RoutinesAdapter mAdapter;
    LinearLayoutManager mLinearLayoutManager;
    TextView mNewRoutinesNotificationText;

    // Listeners
    RoutineLoadListener mLoadListener;
    RoutineSelectedListener mRoutineSelectedListener;

    // Loading
    int mNumLoads;

    //Filter
    String mCurrentFilter;

    // Firebase Listener tracking
    boolean mRoutinesChildListenerIsSet = false;
    int mNumRoutinesAdded;
    int mNumRoutinesFirstLoad;

    public static ViewRoutinesFragment newInstance(RoutineLoadListener routineLoadListener, RoutineSelectedListener routineSelectedListener) {

        Bundle args = new Bundle();
        ViewRoutinesFragment fragment = new ViewRoutinesFragment();
        fragment.setArguments(args);
        fragment.setRoutineLoadListener(routineLoadListener);
        fragment.setRoutineSelectedListener(routineSelectedListener);
        return fragment;
    }

    public ViewRoutinesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        mDatabase = Utils.getDatabase().getReference();
        mUid = Utils.getUid();

        mLoadListener.onLoadStart();
        loadRoutines(FILTER_USER_ROUTINES);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_routines, container, false);
        Log.i(TAG, "onCreateView");

        mNewRoutinesNotificationText = (TextView) rootView.findViewById(R.id.new_routines_published_textview);
        mNewRoutinesNotificationText.setVisibility(View.INVISIBLE);
        mNewRoutinesNotificationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecycler.smoothScrollToPosition(mRoutines.size()-1);
            }
        });
        mRecycler = (RecyclerView) rootView.findViewById(R.id.recycler_view_routines);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mLinearLayoutManager);
        mRecycler.setHasFixedSize(true);
        if (mAdapter != null){
            mRecycler.setAdapter(mAdapter);
        }

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    /**
     * Load routines from database
     */

    public void loadRoutines(final String routinesFilter){

        Query routinesQuery = mDatabase.child("user-routines").child(mUid);

        switch (routinesFilter) {
            case FILTER_USER_ROUTINES:
                routinesQuery = mDatabase.child("user-routines").child(mUid);
                mCurrentFilter = FILTER_USER_ROUTINES;
                break;

            case FILTER_COMMUNITY_ROUTINES:
                routinesQuery = mDatabase.child("routines").orderByKey().limitToLast(50);
                mCurrentFilter = FILTER_COMMUNITY_ROUTINES;
                break;
        }


        routinesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Track last routine in order to stop showing loading dialog
                int numRoutines = (int) dataSnapshot.getChildrenCount();
                int currentRoutine = 0;
                boolean lastRoutine = false;

                for (DataSnapshot routine : dataSnapshot.getChildren()) {
                    currentRoutine++;
                    Log.i(TAG, "Num routines: " + numRoutines + " current routine: " + currentRoutine);
                    String routineKey = routine.getKey();
                    Routine r = routine.getValue(Routine.class);
                    r.setRoutineKey(routineKey);
                    mRoutines.add(r);

                    if (currentRoutine == numRoutines) lastRoutine = true;

                    createWorkouts(routineKey, r, lastRoutine, routinesFilter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void createWorkouts(final String routineKey, final Routine routine, final boolean lastRoutine, final String routinesFilter) {
        Query workouts = mDatabase.child("user-routine-workouts").child(mUid).child(routineKey);

        switch (routinesFilter){
            case FILTER_USER_ROUTINES:
                workouts = mDatabase.child("user-routine-workouts").child(mUid).child(routineKey);
                break;
            case FILTER_COMMUNITY_ROUTINES:
                workouts =  mDatabase.child("routine-workouts").child(routineKey);
                break;
        }

        workouts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // track workouts with exercises - once final exercises from last workout in last routine loaded, can stop showing loading dialog

                int numWorkoutsWithExercises = 0;
                int currentWorkoutWithExercises = 0;
                boolean lastWorkout = false;

                // Calculate number of workouts with exercises
                for (DataSnapshot workout : dataSnapshot.getChildren()){
                    if (workout.getValue(Workout.class).getNumExercises() > 0) {
                        numWorkoutsWithExercises++;
                        Log.i(TAG, "numWorkoutsWithExercises: " + workout.getValue(Workout.class).getName() + " - "  + numWorkoutsWithExercises);
                    }
                }


                for (DataSnapshot workout : dataSnapshot.getChildren()){

                    Workout w = workout.getValue(Workout.class);
                    routine.addWorkoutToList(w);
                    Log.i(TAG, "Added workout to list: " + w.getName());
                    if (w.getNumExercises() > 0) {
                        currentWorkoutWithExercises++;
                        if (currentWorkoutWithExercises == numWorkoutsWithExercises) {
                            Log.i(TAG, "Last workout with exercises. " + w.getName() + " number exercises: " + w.getNumExercises());
                            lastWorkout = true;
                        }
                        addExercisesToWorkout(w, w.getWorkoutKey(), routineKey, lastRoutine, lastWorkout, routinesFilter);
                    }

                }

                // ensure that loading completes if the final routine contains no workouts with exercises!

                if (numWorkoutsWithExercises == 0){
                    Log.i(TAG, "Routine contains empty workouts. Check if last routine and finish loading. Routine: " + routine.getName() + " key: " + routineKey);
                    if (lastRoutine){
                        finishLoadingRoutines();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void addExercisesToWorkout(final Workout workout, String workoutKey, String routineKey, final boolean lastRoutine, final boolean lastWorkout, String routinesFilter) {
        Query exercises = mDatabase.child("user-routine-workout-exercises").child(mUid).child(routineKey).child(workoutKey);

        switch (routinesFilter){
            case FILTER_USER_ROUTINES:
                exercises = mDatabase.child("user-routine-workout-exercises").child(mUid).child(routineKey).child(workoutKey);
                break;
            case FILTER_COMMUNITY_ROUTINES:
                exercises =  mDatabase.child("routine-workout-exercises").child(routineKey).child(workoutKey);
                break;
        }

        exercises.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int numExercises = (int) dataSnapshot.getChildrenCount();
                int currentExercise = 0;

                Log.i(TAG, "Loading exercises for workout: " + workout.getName() + " datasnapshot children: " + dataSnapshot.getChildrenCount());
                List<Exercise> exercisesList = new ArrayList<Exercise>();
                for (DataSnapshot exercise : dataSnapshot.getChildren()){
                    currentExercise++;

                    Exercise e = exercise.getValue(Exercise.class);
                    Log.i(TAG, "Current Exercise: " + currentExercise + " Add exercise: " + e.getName());
                    exercisesList.add(e);

                    // hide loading dialog if all exercises have been loaded
                    if (currentExercise == numExercises && lastRoutine == true && lastWorkout == true){
                        Log.i(TAG, "All exercises added to workouts. Routine Loading complete");

                        finishLoadingRoutines();
                    }
                }
                Collections.sort(exercisesList);
                workout.setExercises(exercisesList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void finishLoadingRoutines(){
        mNumLoads++;

        mLoadListener.onLoadComplete(mRoutines);

        // Check if it is the first load of routines
        if (mNumLoads == 1) {
            mNumRoutinesFirstLoad = mRoutines.size();

            // sort workout order
            for (int i = 0; i < mRoutines.size(); i++) {
                Log.i(TAG, "Sort workouts in routine: " + mRoutines.get(i).getName() + " Num workouts: " + mRoutines.get(i).getNumWorkouts());
                if (mRoutines.get(i).getWorkoutsList() != null) {
                    Collections.sort(mRoutines.get(i).getWorkoutsList());
                }
            }
            mLinearLayoutManager.scrollToPosition(mRoutines.size() - 1);

            mAdapter = new RoutinesAdapter(getContext(), mRoutines, new RoutineSelectedListener() {
                @Override
                public void onRoutineSelected(Routine routine) {
/*                Log.i(TAG, "Routine selected: " + routine.getName() + " num workouts: " + routine.getWorkoutsList().size());*/
                    mRoutineSelectedListener.onRoutineSelected(routine);
                }
            });

            mRecycler.setAdapter(mAdapter);
        } else {
            if (mAdapter != null) {
                mAdapter.notifyItemInserted(mRoutines.size() - 1);
                notifyNewRoutinesAdded();
            }
        }

        // set listener for real time database changes for community filter
        if (mCurrentFilter.equals(FILTER_COMMUNITY_ROUTINES) && mRoutinesChildListenerIsSet == false) {
            Log.i(TAG, "Loading static information finished, now add listener for dynamic routine additions");
            mDatabase.child("routines").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    Log.i(TAG, "child added: " + dataSnapshot.getKey());
                    if (!mCurrentFilter.equals(FILTER_USER_ROUTINES)) {
                        mNumRoutinesAdded++;
                        // Do not add routines that have already been added by the single value event listener on first load
                        if (mNumRoutinesAdded > mRoutines.size()) {
                            Routine routine = dataSnapshot.getValue(Routine.class);
                            mRoutines.add(routine);
                            createWorkouts(dataSnapshot.getKey(), routine, true, FILTER_COMMUNITY_ROUTINES);
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.i(TAG, "child changed: " + dataSnapshot.getKey());
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "child removed: " + dataSnapshot.getKey());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Log.i(TAG, "child moved: " + dataSnapshot.getKey());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i(TAG, "onCancelled");
                }
            });
            mRoutinesChildListenerIsSet = true;
        }

    }

    private void notifyNewRoutinesAdded(){
        // Fade in
        mNewRoutinesNotificationText.setAlpha(0.0f);
        mNewRoutinesNotificationText.setVisibility(View.VISIBLE);
        mNewRoutinesNotificationText.animate()
                .setDuration(500)
                .alpha(1.0f);

        //Fade out
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setStartOffset(5000);
        alphaAnimation.setDuration(500);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mNewRoutinesNotificationText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mNewRoutinesNotificationText.setAnimation(alphaAnimation);

    }

    // Load listener
    public void setRoutineLoadListener (RoutineLoadListener listener){
        this.mLoadListener = listener;
    }

    public void setRoutineSelectedListener (RoutineSelectedListener listener){
        this.mRoutineSelectedListener = listener;
    }

    // Getters and setters


    public List<Routine> getRoutines() {
        return mRoutines;
    }

    public void setRoutines(List<Routine> routines) {
        mRoutines = routines;
    }

    public void setNumLoads(int numLoads) {
        mNumLoads = numLoads;
    }
}
