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
import com.zonesciences.pyrros.utils.Utils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter on 19/10/2016.
 */
public class WorkoutViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "WorkoutViewHolder";
    public TextView titleTextView;
    public TextView creatorTextView;

    public ImageView usersImageView;
    public TextView numUsersTextView;

    public WorkoutViewHolder(View itemView) {
        super(itemView);

        titleTextView = (TextView) itemView.findViewById(R.id.workout_title);
        creatorTextView = (TextView) itemView.findViewById(R.id.workout_creator);
        usersImageView = (ImageView) itemView.findViewById(R.id.workout_users);
        numUsersTextView = (TextView) itemView.findViewById(R.id.workout_num_users);


    }

    public void bindToWorkout(Workout workout, String workoutExercisesReference,View.OnClickListener usersClickListener){
        Log.i(TAG, "bindToWorkout called");

        if (workout.getName().isEmpty()) {
            titleTextView.setText(Utils.formatDate(workout.getClientTimeStamp(), 0));
        } else {
                titleTextView.setText(Utils.formatDate(workout.getClientTimeStamp(), 0) + " - " + workout.getName());
            }
        creatorTextView.setText(workout.creator);
        numUsersTextView.setText(String.valueOf(workout.userCount));
        usersImageView.setOnClickListener(usersClickListener);
    }

}
