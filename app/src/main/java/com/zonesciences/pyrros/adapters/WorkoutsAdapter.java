package com.zonesciences.pyrros.adapters;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.MainActivity;
import com.zonesciences.pyrros.R;

import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.viewholder.WorkoutViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter on 26/10/2016.
 */
public class WorkoutsAdapter extends FirebaseRecyclerAdapter<Workout, WorkoutViewHolder> {


    private static final String TAG = "WorkoutsAdapter";

    private static final String WORKOUT_EXERCISES = "Workout Exercises";
    private static final String WORKOUT_ID = "Workout ID";

    DatabaseReference mDatabaseReference;
    Context mContext;
    String mUid;
    int mNumExercises;
    Map<String, List<Exercise>> mWorkoutExercisesMap;
    List<Exercise> mExercises;
    List<Integer> mTrackedIds;
    ArrayList<String> mExerciseKeys = new ArrayList<>();

    public WorkoutsAdapter(Class<Workout> modelClass, int modelLayout, Class<WorkoutViewHolder> viewHolderClass, Query ref, DatabaseReference databaseReference, String uid, Map<String, List<Exercise>> workoutExercisesMap, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        Log.i(TAG, "WorkoutsAdapter constructor called");
        mDatabaseReference = databaseReference;
        mUid = uid;
        mWorkoutExercisesMap = workoutExercisesMap;
        mContext = context;
    }

    @Override
    public WorkoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        Log.i(TAG, "onCreateViewHolder() called");
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        WorkoutViewHolder holder = new WorkoutViewHolder(view);
        return holder;
    }

    protected void populateViewHolder(final WorkoutViewHolder viewHolder, final Workout workout, final int position) {
        Log.i(TAG, "populateViewHolder() called");

        //This gets the unique key for the workout associated with the item in the list.
        final DatabaseReference workoutRef  = getRef(position);

        if (!mWorkoutExercisesMap.containsKey(workoutRef.getKey())){ //the stupid listener is set up and will notify adapter that data has changed when a new exercise is added when an exercise is added from the NewWorkoutActivity
            Log.i(TAG, "No workout key exists");
        } else {
            mExercises = mWorkoutExercisesMap.get(workoutRef.getKey());

            Log.i(TAG, "mExercises before sort: " + mExercises);
            Collections.sort(mExercises);
            for (Exercise e : mExercises){
                Log.i(TAG, "Name of exercise: " + e.getName() + " Order: " + e.getOrder());
            }
            mNumExercises = mExercises.size();
            Log.i(TAG, "mWorkoutExercises Map contains exercises: " + mNumExercises);

            LinearLayout exercisesContainer = (LinearLayout) viewHolder.itemView.findViewById(R.id.workout_exercises_container);
            exercisesContainer.removeAllViews();

            for (int i = 0; i < mNumExercises; i++) {
                View view = LayoutInflater.from(viewHolder.itemView.getContext()).inflate(R.layout.item_workout_exercises, null);
                TextView exerciseText = (TextView) view.findViewById(R.id.workout_exercise_name);
                exerciseText.setText(mExercises.get(i).getName());
                exercisesContainer.addView(view);
            }
        }

        //Set click listener for the whole workout view
        final String workoutKey = workoutRef.getKey();

        //TODO: FIX: this is duplicating exercises every time the workout is clicked
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Launch WorkoutActivity

                List<Exercise> exercisesToLoad = mWorkoutExercisesMap.get(workoutRef.getKey());
                for (Exercise exercise : exercisesToLoad){
                    String s = exercise.getName();
                    mExerciseKeys.add(s);
                }

                Log.i(TAG, "mExericseKeys: " + mExercises);

                Bundle extras = new Bundle();
                Log.i(TAG, "Exercises to pass to new activity " + mExerciseKeys);
                extras.putSerializable(WORKOUT_EXERCISES, mExerciseKeys);
                extras.putString(WORKOUT_ID, workoutKey);
                Intent i = new Intent (mContext, WorkoutActivity.class);
                i.putExtras(extras);
                mContext.startActivity(i);
                mExerciseKeys.clear();
            }
        });

        //Add set number of text views to the view holder

        //Determine if the current user has subscribed to this workout and set UI accordingly
        if(workout.users.containsKey(mUid)){
            viewHolder.usersImageView.setImageResource(R.drawable.ic_toggle_star_24);
        } else {
            viewHolder.usersImageView.setImageResource(R.drawable.ic_toggle_star_outline_24);
        }

        String workoutExercisesReference = mDatabaseReference.child("workout-exercises").child(workoutKey).getKey();



        //Bind Workout to ViewHolder, setting OnClickListener for the users button
        viewHolder.bindToWorkout(workout, workoutExercisesReference, new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //Need to write to both places the workout is stored
                DatabaseReference globalWorkoutRef = mDatabaseReference.child("workouts").child(workoutRef.getKey());
                DatabaseReference userPostRef = mDatabaseReference.child("user-workouts").child(workout.uid).child(workoutRef.getKey());

                //Run two transactions
                onUsersClicked(globalWorkoutRef);
                onUsersClicked(userPostRef);

            }
        });
    }

    @Override
    public void onViewRecycled(WorkoutViewHolder viewHolder){
        Log.i(TAG, "onViewRecycled called");
    }

    private void onUsersClicked(DatabaseReference workoutRef){
        Log.i(TAG, "onUsersClicked called");
        workoutRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Workout w = mutableData.getValue(Workout.class);

                if (w == null) {
                    return Transaction.success(mutableData);
                }

                if (w.users.containsKey(mUid)){
                    // Unstar the workout and remove self from subscribed users
                    w.userCount = w.userCount - 1;
                    w.users.remove(mUid);
                } else {
                    // Star the workout and add self to subscribed users list
                    w.userCount = w.userCount + 1;
                    w.users.put(mUid, true);
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

}
