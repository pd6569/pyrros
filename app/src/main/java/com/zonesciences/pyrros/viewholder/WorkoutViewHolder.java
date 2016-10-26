package com.zonesciences.pyrros.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 19/10/2016.
 */
public class WorkoutViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "WorkoutViewHolder";
    public TextView titleTextView;
    public TextView creatorTextView;

    public ImageView usersImageView;
    public TextView numUsersTextView;
    public TextView bodyTextView;

    public List<Exercise> mExercises;

    public WorkoutViewHolder(View itemView) {
        super(itemView);

        titleTextView = (TextView) itemView.findViewById(R.id.workout_title);
        creatorTextView = (TextView) itemView.findViewById(R.id.workout_creator);
        usersImageView = (ImageView) itemView.findViewById(R.id.workout_users);
        numUsersTextView = (TextView) itemView.findViewById(R.id.workout_num_users);
        bodyTextView = (TextView) itemView.findViewById(R.id.workout_content);
    }

    public void bindToWorkout(Workout workout, final DatabaseReference workoutExercisesReference, View.OnClickListener usersClickListener){
        Log.i(TAG, "bindToWorkout called");
        mExercises = new ArrayList<>();

        titleTextView.setText(workout.name);
        creatorTextView.setText(workout.creator);
        numUsersTextView.setText(String.valueOf(workout.userCount));

        workoutExercisesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "does workout have child exercises : " + dataSnapshot.getRef());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*workoutExercisesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Exercise exercise = dataSnapshot.getValue(Exercise.class);
                mExercises.add(exercise);
                Log.i(TAG, "Exercises for workout added. Exercise name: " + exercise.getName() + " Belongs to workout " + workoutExercisesReference.getKey());
                Log.i(TAG, "mExercises contains " + mExercises.size() + " exercises");
                bodyTextView.setText(exercise.getName());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        usersImageView.setOnClickListener(usersClickListener);
    }
}
