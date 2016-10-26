package com.zonesciences.pyrros.adapters;

import android.util.Log;
import android.view.View;
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
import com.zonesciences.pyrros.R;

import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.viewholder.WorkoutViewHolder;

import java.util.List;

/**
 * Created by Peter on 26/10/2016.
 */
public class WorkoutsAdapter extends FirebaseRecyclerAdapter<Workout, WorkoutViewHolder> {

    private static final String TAG = "WorkoutsAdapter";

    DatabaseReference mDatabaseReference;
    String mUid;

    public WorkoutsAdapter(Class<Workout> modelClass, int modelLayout, Class<WorkoutViewHolder> viewHolderClass, Query ref, DatabaseReference databaseReference, String uid) {
        super(modelClass, modelLayout, viewHolderClass, ref);

        mDatabaseReference = databaseReference;
        mUid = uid;
    }

    protected void populateViewHolder(final WorkoutViewHolder viewHolder, final Workout workout, final int position) {

        //This gets the unique key for the workout associated with the item in the list.
        final DatabaseReference workoutRef  = getRef(position);

        //Set click listener for the whole workout view
        final String workoutKey = workoutRef.getKey();

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Launch ViewWorkout
                Toast.makeText(view.getContext(), "Launching Workout...", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Value of workoutRef: " + workoutRef.getKey());
            }
        });

        //Determine if the current user has subscribed to this workout and set UI accordingly
        if(workout.users.containsKey(mUid)){
            viewHolder.usersImageView.setImageResource(R.drawable.ic_toggle_star_24);
        } else {
            viewHolder.usersImageView.setImageResource(R.drawable.ic_toggle_star_outline_24);
        }

        DatabaseReference workoutExercisesReference = mDatabaseReference.child("workout-exercises").child(workoutKey);


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

    private void onUsersClicked(DatabaseReference workoutRef){
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
