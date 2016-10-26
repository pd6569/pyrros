package com.zonesciences.pyrros.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Workout;

/**
 * Created by Peter on 19/10/2016.
 */
public class WorkoutViewHolder extends RecyclerView.ViewHolder {

    public TextView titleTextView;
    public TextView creatorTextView;

    public ImageView usersImageView;
    public TextView numUsersTextView;
    public TextView bodyTextView;

    public WorkoutViewHolder(View itemView) {
        super(itemView);

        titleTextView = (TextView) itemView.findViewById(R.id.workout_title);
        creatorTextView = (TextView) itemView.findViewById(R.id.workout_creator);
        usersImageView = (ImageView) itemView.findViewById(R.id.workout_users);
        numUsersTextView = (TextView) itemView.findViewById(R.id.workout_num_users);
        bodyTextView = (TextView) itemView.findViewById(R.id.workout_content);
    }

    public void bindToWorkout(Workout workout, View.OnClickListener usersClickListener){
        titleTextView.setText(workout.name);
        creatorTextView.setText(workout.creator);
        numUsersTextView.setText(String.valueOf(workout.userCount));
        bodyTextView.setText("List of exercises to display");
        usersImageView.setOnClickListener(usersClickListener);
    }
}
