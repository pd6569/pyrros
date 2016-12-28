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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.BaseActivity;
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
public class ViewRoutinesFragment extends Fragment {

    private static final String TAG = "ViewRoutinesFragment";

    // Firebase
    DatabaseReference mDatabase;
    String mUid;

    // Routines
    List<Routine> mRoutines = new ArrayList<>();

    // RecyclerView
    RecyclerView mRecycler;


    // Listeners
    RoutineLoadListener mLoadListener;
    RoutineSelectedListener mRoutineSelectedListener;

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
        loadRoutines();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_routines, container, false);
        Log.i(TAG, "onCreateView");

        mRecycler = (RecyclerView) rootView.findViewById(R.id.recycler_view_routines);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    private void loadRoutines(){
        mDatabase.child("user-routines").child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int numRoutines = (int) dataSnapshot.getChildrenCount();
                int currentRoutine = 0;

                for (DataSnapshot routine : dataSnapshot.getChildren()){
                    currentRoutine++;
                    Log.i(TAG, "Num routines: " + numRoutines + " current routine: " + currentRoutine);
                    String routineKey = routine.getKey();
                    Routine r = routine.getValue(Routine.class);
                    r.setRoutineKey(routineKey);
                    mRoutines.add(r);

                    int numWorkouts = r.getWorkouts().size();
                    int currentWorkout = 0;
                    boolean lastWorkout = false;

                    for (String workoutKey : r.getWorkouts().keySet()){
                        currentWorkout++;
                        if (currentRoutine == numRoutines && currentWorkout == numWorkouts) {
                            lastWorkout = true;
                        }
                        Log.i(TAG, "numWorkouts: " + numWorkouts + " currentWorkout: " + currentWorkout + " last workout: " + lastWorkout);
                        Log.i(TAG, "Routine: " + routineKey + " workoutKey: " + workoutKey);
                        createWorkouts(routineKey, workoutKey, r, lastWorkout);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createWorkouts(final String routineKey, final String workoutKey, final Routine routine, final boolean lastWorkout){
        mDatabase.child("user-routine-workouts").child(mUid).child(routineKey).child(workoutKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Workout w = dataSnapshot.getValue(Workout.class);
                routine.addWorkoutToList(w);
                addExercisesToWorkout(w, workoutKey, routineKey, lastWorkout);

                if (lastWorkout) {
                    Log.i(TAG, "Finished creating routines");
                    for (Routine routine : mRoutines) {
                        Log.i(TAG, "routine name: " + routine.getName() + "numWorkouts: " + routine.getNumWorkouts() + "workoutList: " + routine.getWorkoutsList().size());
                        for (int i = 0; i < routine.getWorkoutsList().size(); i++){
                            Log.i(TAG, "Workout name: " + routine.getWorkoutsList().get(i).getName());
                        }
                    };

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addExercisesToWorkout(final Workout workout, String workoutKey, String routineKey, final boolean lastWorkout) {
        mDatabase.child("user-routine-workout-exercises").child(mUid).child(routineKey).child(workoutKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int numExercises = (int) dataSnapshot.getChildrenCount();
                int currentExercise = 0;

                List<Exercise> exercisesList = new ArrayList<Exercise>();
                for (DataSnapshot exercise : dataSnapshot.getChildren()){
                    currentExercise++;

                    Exercise e = exercise.getValue(Exercise.class);
                    exercisesList.add(e);

                    if (currentExercise == numExercises && lastWorkout == true){
                        Log.i(TAG, "All exercises added to workouts. Loading complete");

                        mLoadListener.onLoadComplete(mRoutines);
                        RoutinesAdapter adapter = new RoutinesAdapter(getContext(), mRoutines, new RoutineSelectedListener() {
                            @Override
                            public void onRoutineSelected(Routine routine) {
                                Log.i(TAG, "Routine selected: " + routine.getName() + " num workouts: " + routine.getWorkoutsList().size());
                                mRoutineSelectedListener.onRoutineSelected(routine);
                            }
                        });

                        mRecycler.setAdapter(adapter);
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

    // Load listener
    public void setRoutineLoadListener (RoutineLoadListener listener){
        this.mLoadListener = listener;
    }

    public void setRoutineSelectedListener (RoutineSelectedListener listener){
        this.mRoutineSelectedListener = listener;
    }

}
