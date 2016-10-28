package com.zonesciences.pyrros.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.WorkoutsAdapter;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.viewholder.WorkoutViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter on 18/10/2016.
 */
public abstract class WorkoutsListFragment extends Fragment {

    private static final String TAG = "WorkoutsListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Workout, WorkoutViewHolder> mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mManager;

    private Map<String, List<Exercise>> mWorkoutExercisesMap = new HashMap<>();
    public WorkoutsListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate() called");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user-workout-exercises").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "Getting list of exercises for workouts" + dataSnapshot.hasChildren());
                for (DataSnapshot workout : dataSnapshot.getChildren()) {
                    String s = workout.getKey();
                    Log.i(TAG, "Workouts loaded: " + s);
                    List<Exercise> exercises = new ArrayList<Exercise>();
                    for (DataSnapshot exerciseKey : workout.getChildren()){
                        Exercise exercise = exerciseKey.getValue(Exercise.class);
                        Log.i(TAG, "Exercise added to list: " + exercise.getName());
                        exercises.add(exercise);
                    }
                    mWorkoutExercisesMap.put(s, exercises);
                    Log.i(TAG, "WorkoutExercisesMap updated: " + mWorkoutExercisesMap.size());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_workouts_list, container, false);

        // [START create_database_reference]

        // [END create_database_reference]

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.workouts_list);
        mRecyclerView.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout, show latest workouts on top
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mManager);

        //Set up FirebaseRecyclerAdapter with the Query
        //This is an abstract method that passes the database instance in and returns a query specific
        //to the subclass that extends it. The method is overridden in the subclass (which is why it
        //is declared abstract) with the specific query that should be utilised to display the relevant
        //list of workouts.

        Query workoutsQuery = getQuery(mDatabase);

        mAdapter = new WorkoutsAdapter(Workout.class, R.layout.item_workout, WorkoutViewHolder.class, workoutsQuery, mDatabase, getUid());
        mRecyclerView.setAdapter(mAdapter);
    }

    // [START users_subscribed_transcation]
    private void onUsersClicked(DatabaseReference workoutRef){
        workoutRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Workout w = mutableData.getValue(Workout.class);

                if (w == null) {
                    return Transaction.success(mutableData);
                }

                if (w.users.containsKey(getUid())){
                    // Unstar the workout and remove self from subscribed users
                    w.userCount = w.userCount - 1;
                    w.users.remove(getUid());
                } else {
                    // Star the workout and add self to subscribed users list
                    w.userCount = w.userCount + 1;
                    w.users.put(getUid(), true);
                }

                //set value and report transaction success
                mutableData.setValue(w);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                //Transaction completed
                Log.d(TAG, "workoutTransaction:onComplete:" + databaseError);

            }
        });
    }
    // [END users_susbscribed_transaction]

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    //Method to generate query. Override in subclass with specific query required.
    public abstract Query getQuery(DatabaseReference database);
}

