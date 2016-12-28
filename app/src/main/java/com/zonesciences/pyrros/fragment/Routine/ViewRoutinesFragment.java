package com.zonesciences.pyrros.fragment.Routine;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Routine;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewRoutinesFragment extends Fragment {

    private static final String TAG = "ViewRoutinesFragment";

    // Firebase
    DatabaseReference mDatabase;
    String mUid;

    // Routine objects
    List<Routine> mRoutines = new ArrayList<>();

    public static ViewRoutinesFragment newInstance() {

        Bundle args = new Bundle();

        ViewRoutinesFragment fragment = new ViewRoutinesFragment();
        fragment.setArguments(args);
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

    private void createWorkouts(String routineKey, String workoutKey, final Routine routine, final boolean lastWorkout){
        mDatabase.child("user-routine-workouts").child(mUid).child(routineKey).child(workoutKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Workout w = dataSnapshot.getValue(Workout.class);
                routine.addWorkoutToList(w);

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_routines, container, false);

        return rootView;
    }

}
