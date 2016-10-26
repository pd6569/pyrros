package com.zonesciences.pyrros.adapters;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 24/10/2016.
 */
public class ExercisesAdapter extends RecyclerView.Adapter<ExercisesAdapter.ExerciseViewHolder> {

    public static final String TAG = "ExerciseAdapter";


    private Context mContext;
    private DatabaseReference mWorkoutExerciseReference;
    private DatabaseReference mDatabaseRoot;
    private ChildEventListener mChildEventListener;

    private List<String> mExerciseKeys = new ArrayList<>();
    private List<Exercise> mExercises = new ArrayList<>();

    private String mWorkoutKey;


    public class ExerciseViewHolder extends RecyclerView.ViewHolder {

        public TextView exerciseName;

        public ExerciseViewHolder(View itemView) {
            super(itemView);

            exerciseName = (TextView) itemView.findViewById(R.id.exercise_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "onClick");
                    Snackbar snackbar = Snackbar.make(view, "Removing exercise: " + mExercises.get(getAdapterPosition()).getName(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    mWorkoutExerciseReference.child(mExerciseKeys.get(getAdapterPosition())).removeValue();
                }
            });
        }

    }

    // Provide suitable constructor
    public ExercisesAdapter(final Context context, DatabaseReference workoutExercisesRef, DatabaseReference databaseRoot, String workoutKey) {
        mContext = context;
        mWorkoutExerciseReference = workoutExercisesRef;
        mDatabaseRoot = databaseRoot;
        mWorkoutKey = workoutKey;

        // Create child event listener
        // [START child_event_listener_recycler]
        ChildEventListener childEventListener = new ChildEventListener(){
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName){
                Log.d(TAG, "onChildAdded: " + dataSnapshot.getKey());

                // A new exercise has been added, add it to the displayed list
                Exercise exercise = dataSnapshot.getValue(Exercise.class);

                // [START_EXCLUDE]
                // Update RecyclerView
                mExerciseKeys.add(dataSnapshot.getKey());
                mExercises.add(exercise);
                notifyItemInserted(mExercises.size() - 1);
                // [END_EXCLUDE]
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged :" + dataSnapshot.getKey());

                // An exercise has changed, use the key to determine if we are displaying this
                // exercise and if so display the changed exercise.

                Exercise newExercise = dataSnapshot.getValue(Exercise.class);
                String exerciseKey = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int exerciseIndex = mExerciseKeys.indexOf(exerciseKey);
                if (exerciseIndex > -1) {
                    // Replace with the new data
                    mExercises.set(exerciseIndex, newExercise);

                    // Update the RecyclerView
                    notifyItemChanged(exerciseIndex);
                } else {
                    Log.v(TAG, "onChildChanged:unknown_child" + exerciseKey);
                }
                // [END_EXCLUDE]

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved: " + dataSnapshot.getKey());

                // An exercise has changed, use the key to determine if we are displaying this
                // exercise and if so remove it.

                String exerciseKey = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int exerciseIndex = mExerciseKeys.indexOf(exerciseKey);
                if (exerciseIndex > -1) {
                    // Remove data from the list
                    mExerciseKeys.remove(exerciseIndex);
                    mExercises.remove(exerciseIndex);

                    //Update the RecyclerView
                    notifyItemRemoved(exerciseIndex);
                } else {
                    Log.v(TAG, "onChildRemoved:unknown_child: " + exerciseKey);
                }
                //[END_EXCLUDE]
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved: " + dataSnapshot.getKey());

                // An exercise has changed position, use the key to dtermine if we are displaying
                // this comment and if so move it.
                Exercise movedExercise = dataSnapshot.getValue(Exercise.class);
                String exerciseKey = dataSnapshot.getKey();

                // ...

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load mExercises", Toast.LENGTH_SHORT).show();
            }
        };

        mWorkoutExerciseReference.addChildEventListener(childEventListener);
        // [END child_event_listener_recycler]

        //Store reference to listener so it can be removed on app stop
        mChildEventListener = childEventListener;
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseViewHolder holder, int position) {
        Exercise exercise = mExercises.get(position);
        holder.exerciseName.setText(exercise.getName());
    }


    @Override
    public int getItemCount() {
        return mExercises.size();
    }

}
