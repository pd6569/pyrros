package com.zonesciences.pyrros.adapters;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntegerRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.MainActivity;
import com.zonesciences.pyrros.R;

import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Record;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;
import com.zonesciences.pyrros.viewholder.WorkoutViewHolder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Peter on 26/10/2016.
 */
public class WorkoutsAdapter extends FirebaseRecyclerAdapter<Workout, WorkoutViewHolder> {


    private static final String TAG = "WorkoutsAdapter";

    private static final String WORKOUT_EXERCISES = "Workout Exercises";
    private static final String WORKOUT_ID = "Workout ID";
    private static final String WORKOUT_EXERCISES_OBJECTS = "WorkoutExerciseObjects";

    DatabaseReference mDatabaseReference;
    Context mContext;
    String mUid;
    int mNumExercises;
    Map<String, List<Exercise>> mWorkoutExercisesMap;
    List<Exercise> mExercises;
    ArrayList<String> mExerciseKeys = new ArrayList<>();

    String mUnit;
    double mConversionMultiple;

    // Restore database
    int movesCompleted;

    public WorkoutsAdapter(Class<Workout> modelClass, int modelLayout, Class<WorkoutViewHolder> viewHolderClass, Query ref, DatabaseReference databaseReference, String uid, Map<String, List<Exercise>> workoutExercisesMap, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        Log.i(TAG, "WorkoutsAdapter constructor called");
        mDatabaseReference = databaseReference;
        mUid = uid;
        mWorkoutExercisesMap = workoutExercisesMap;
        mContext = context;
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getString("pref_unit", null).equals("metric")){
            mUnit = " kgs";
            mConversionMultiple = 1.0;
        } else {
            mUnit = " lbs";
            mConversionMultiple = 2.20462;
        }
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

            Collections.sort(mExercises);

            mNumExercises = mExercises.size();
            Log.i(TAG, "mWorkoutExercises Map contains exercises: " + mNumExercises);

            LinearLayout exercisesContainer = (LinearLayout) viewHolder.itemView.findViewById(R.id.workout_exercises_container);
            exercisesContainer.removeAllViews();

            for (int i = 0; i < mNumExercises; i++) {

                Exercise currentExercise = mExercises.get(i);
                View view = LayoutInflater.from(viewHolder.itemView.getContext()).inflate(R.layout.item_workout_exercises, null);
                TextView exerciseText = (TextView) view.findViewById(R.id.workout_exercise_name);
                LinearLayout setsContainer = (LinearLayout) view.findViewById(R.id.workout_sets_container);
                exerciseText.setText(currentExercise.getName());

                if (currentExercise.getSets() == 0){
                    TextView noSets = (TextView) view.findViewById(R.id.workout_no_sets);
                    noSets.setVisibility(View.VISIBLE);
                }

                for (int j = 0; j < currentExercise.getSets(); j++){

                    Log.i(TAG, "GETTING SETS FOR: currentExercise = " + currentExercise.getName());
                    View setsView = LayoutInflater.from(viewHolder.itemView.getContext()).inflate(R.layout.item_sets, null);
                    LinearLayout setsLayout = (LinearLayout) setsView.findViewById(R.id.linear_layout_sets);
                    TextView setNumber = (TextView) setsView.findViewById(R.id.textview_set_number);
                    TextView setWeight = (TextView) setsView.findViewById(R.id.textview_set_weight);
                    TextView setReps = (TextView) setsView.findViewById(R.id.textview_set_reps);

                    setsLayout.setFocusable(false);
                    setsLayout.setClickable(false);
                    setNumber.setVisibility(View.GONE);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f);
                    double weight = currentExercise.getWeight().get(j) * mConversionMultiple;
                    String s = Utils.formatWeight(weight);
                    setWeight.setText(s + mUnit);
                    setWeight.setLayoutParams(params);
                    setReps.setText("" + currentExercise.getReps().get(j) + " reps");
                    setReps.setLayoutParams(params);

                    setsContainer.addView(setsView);

                }

                exercisesContainer.addView(view);
            }
        }

        //Set click listener for the whole workout view
        final String workoutKey = workoutRef.getKey();

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Launch WorkoutActivity

                ArrayList<Exercise> exercisesToLoad = (ArrayList) mWorkoutExercisesMap.get(workoutRef.getKey());
                for (Exercise exercise : exercisesToLoad){
                    String s = exercise.getName();
                    mExerciseKeys.add(s);
                }

                Log.i(TAG, "mExericseKeys: " + mExercises);

                Bundle extras = new Bundle();
                Log.i(TAG, "Exercises to pass to new activity " + mExerciseKeys);
                extras.putSerializable(WORKOUT_EXERCISES, mExerciseKeys);
                extras.putString(WORKOUT_ID, workoutKey);
                extras.putSerializable(WORKOUT_EXERCISES_OBJECTS, exercisesToLoad);
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
        }, new View.OnClickListener(){
            @Override
            public void onClick(final View view){
                PopupMenu menu = new PopupMenu(mContext, view, Gravity.RIGHT);
                menu.getMenuInflater().inflate(R.menu.menu_popup_workout_list, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()){
                            case R.id.menu_popup_workout_delete:
                                Log.i(TAG, "Workout to delete: " + workoutKey + " workout date: " + workout.getClientTimeStamp());
                                Map<String, Object> childUpdates = new HashMap<String, Object>();
                                childUpdates.put("/workouts/" + workoutKey, null);
                                childUpdates.put("/user-workouts/" + mUid + "/" + workoutKey, null);
                                childUpdates.put("/workout-exercises/" + workoutKey, null);
                                childUpdates.put("/user-workout-exercises/" + mUid + "/" + workoutKey, null);

                                // Copy records before deleting (only need to copy user data - can restore to both locations if required)
                                moveFirebaseRecord(mDatabaseReference.child("user-workouts").child(mUid).child(workoutKey), mDatabaseReference.child("deleted").child("user-workouts").child(mUid).child(workoutKey), false);
                                moveFirebaseRecord(mDatabaseReference.child("user-workout-exercises").child(mUid).child(workoutKey), mDatabaseReference.child("deleted").child("user-workout-exercises").child(mUid).child(workoutKey), false);

                                mDatabaseReference.updateChildren(childUpdates);
                                notifyItemRemoved(position);

                                Snackbar snackbar = Snackbar.make(view, R.string.workout_deleted, Snackbar.LENGTH_LONG).setAction(R.string.action_undo, new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){

                                        // Restore data to both user and normal locations
                                        moveFirebaseRecord(mDatabaseReference.child("deleted").child("user-workouts").child(mUid).child(workoutKey), mDatabaseReference.child("user-workouts").child(mUid).child(workoutKey), true);
                                        moveFirebaseRecord(mDatabaseReference.child("deleted").child("user-workout-exercises").child(mUid).child(workoutKey), mDatabaseReference.child("user-workout-exercises").child(mUid).child(workoutKey), true);
                                        moveFirebaseRecord(mDatabaseReference.child("deleted").child("user-workouts").child(mUid).child(workoutKey), mDatabaseReference.child("workouts").child(workoutKey), true);
                                        moveFirebaseRecord(mDatabaseReference.child("deleted").child("user-workout-exercises").child(mUid).child(workoutKey), mDatabaseReference.child("workout-exercises").child(workoutKey), true);

                                        // Remove the data from "deleted" node
                                        Map<String, Object> childUpdates = new HashMap<String, Object>();
                                        childUpdates.put("/deleted/user-workout-exercises/" + mUid + "/" + workoutKey, null);
                                        childUpdates.put("/deleted/user-workouts/" + mUid + "/" + workoutKey, null);
                                        mDatabaseReference.updateChildren(childUpdates);

                                        Snackbar snackbar = Snackbar.make(view, R.string.workout_restored, Snackbar.LENGTH_SHORT);
                                        View sbView = snackbar.getView();
                                        sbView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.snackbarPositive));
                                        snackbar.show();
                                    }
                                });
                                snackbar.show();
                                return true;
                            case R.id.menu_popup_workout_do_workout:

                                String newWorkoutKey = mDatabaseReference.child("workouts").push().getKey();
                                String userName = workout.getCreator();
                                String workoutName = workout.getName();

                                List<Exercise> exercises = mWorkoutExercisesMap.get(workoutKey);
                                List<Exercise> newExercises = new ArrayList<Exercise>();
                                final List<String> exerciseKeysList = new ArrayList<String>();

                                Workout newWorkout = new Workout(mUid, userName, Utils.getClientTimeStamp(true), workoutName, true);
                                newWorkout.setNumExercises(exercises.size());

                                // Write to database
                                Map<String, Object> childUpdatesCopyWorkout = new HashMap<>();
                                childUpdatesCopyWorkout.put("/workouts/" + newWorkoutKey, newWorkout);
                                childUpdatesCopyWorkout.put("/user-workouts/" + mUid + "/" + newWorkoutKey, newWorkout);
                                childUpdatesCopyWorkout.put("/timestamps/workouts/" + newWorkoutKey + "/created/", ServerValue.TIMESTAMP);
                                for (Exercise exercise : newExercises){
                                    String exerciseKey = exercise.getName();
                                    exerciseKeysList.add(exerciseKey);

                                }
                                for (Exercise exerciseToCopy : exercises){
                                    Log.i(TAG, "Exercise to load: " + exerciseToCopy.getName());
                                    exerciseKeysList.add(exerciseToCopy.getName());
                                    Exercise newExercise = new Exercise(exerciseToCopy, mUid);
                                    newExercise.setOrder(exerciseToCopy.getOrder());
                                    newExercise.setExerciseId(UUID.randomUUID().toString());
                                    newExercises.add(newExercise);

                                    // write to database
                                    childUpdatesCopyWorkout.put("/workout-exercises/" + newWorkoutKey + "/" + exerciseToCopy.getName(), newExercise.toMap());
                                    childUpdatesCopyWorkout.put("/user-workout-exercises/" + mUid + "/" + newWorkoutKey + "/" + exerciseToCopy.getName(), newExercise.toMap());
                                }

                                mDatabaseReference.updateChildren(childUpdatesCopyWorkout);

                                /*mDatabaseReference.child("records").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (String exerciseKey : exerciseKeysList){
                                            if (!dataSnapshot.hasChild(exerciseKey)){
                                                Record record = new Record(exerciseKey, mUid);
                                                mDatabaseReference.child("records").child(exerciseKey).child(mUid).setValue(record);
                                                mDatabaseReference.child("user-records").child(mUid).child(exerciseKey).setValue(record);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });*/

                                Bundle extras = new Bundle();
                                Log.i(TAG, "Exercises to pass to new activity " + exerciseKeysList);
                                extras.putSerializable(WORKOUT_EXERCISES, (ArrayList) exerciseKeysList);
                                extras.putString(WORKOUT_ID, newWorkoutKey);
                                extras.putSerializable(WORKOUT_EXERCISES_OBJECTS, (ArrayList) newExercises);
                                Intent i = new Intent (mContext, WorkoutActivity.class);
                                i.putExtras(extras);
                                mContext.startActivity(i);
                                return true;

                            case R.id.menu_popup_workout_add_to_routine:
                                break;
                        }
                        return true;
                    }
                });

                menu.show();
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

    public void moveFirebaseRecord(DatabaseReference fromPath, final DatabaseReference toPath, final boolean notifyDataSetChanged)
    {
        fromPath.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                toPath.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener()
                {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference)
                    {
                        if (firebaseError != null)
                        {
                            Log.i(TAG,"Copy failed");
                        }
                        else
                        {
                            Log.i(TAG, "Success");
                            if (notifyDataSetChanged){
                                movesCompleted++;
                                Log.i(TAG, "Moves completed: " + movesCompleted);
                                if (movesCompleted == 4){
                                    notifyDataSetChanged();
                                }
                            }

                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "Copy Failed");
            }

        });
    }

}
