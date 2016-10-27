package com.zonesciences.pyrros.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;

import org.w3c.dom.Text;

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
    public LinearLayout exercisesContainerLinearLayout;

    public List<Exercise> mExercises;
    public List<String> mWorkoutsInflated;

    public WorkoutViewHolder(View itemView) {
        super(itemView);

        titleTextView = (TextView) itemView.findViewById(R.id.workout_title);
        creatorTextView = (TextView) itemView.findViewById(R.id.workout_creator);
        usersImageView = (ImageView) itemView.findViewById(R.id.workout_users);
        numUsersTextView = (TextView) itemView.findViewById(R.id.workout_num_users);
        exercisesContainerLinearLayout = (LinearLayout) itemView.findViewById(R.id.linear_layout_exercises_container);

    }

    public void bindToWorkout(Workout workout, final DatabaseReference workoutExercisesReference, View.OnClickListener usersClickListener){
        Log.i(TAG, "bindToWorkout called");
        mExercises = new ArrayList<>();

        titleTextView.setText(workout.name);
        creatorTextView.setText(workout.creator);
        numUsersTextView.setText(String.valueOf(workout.userCount));

        workoutExercisesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Exercise exercise = dataSnapshot.getValue(Exercise.class);
                mExercises.add(exercise);

                Log.i(TAG, "mExercises called within eventlistener = " + mExercises.size());

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
        });

        Log.i(TAG, "mExercise called within bindWorkout = " + mExercises.size());

        usersImageView.setOnClickListener(usersClickListener);
    }
}
