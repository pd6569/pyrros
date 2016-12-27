package com.zonesciences.pyrros.fragment.CreateRoutine;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.CreateWorkoutActivity;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Routine;
import com.zonesciences.pyrros.models.User;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */


    //TODO: BUG - when launching directly into sort workout fragment, "done" arrow does not show on moving/deleting exercises

public class RoutineDetailsFragment extends Fragment {

    private static final String TAG = "RoutineDetailsFragment";

    // REQUEST CODE
    private static final int REQUEST_CREATE_WORKOUT = 1;

    // View
    AutoCompleteTextView mWorkoutNameField;
    Button mAddWorkoutButton;
    LinearLayout mLinearLayoutWorkoutContainer;

    // Maps
    /*Map<Integer,  String> mWorkoutViewNameMap = new HashMap<>();*/
    Map<Integer, View> mWorkoutViewMap = new HashMap<>();
    Map<Integer, ArrayList<Exercise>> mWorkoutViewIdExercisesMap = new HashMap<>();
    Map<String, ArrayList<Exercise>> mWorkoutKeyExercisesMap = new HashMap<>();
    Map<Integer, Workout> mWorkoutIdWorkoutObjectMap = new HashMap<>();

    // Track workouts to update view and object
    int mWorkoutCardToUpdate;
    String mWorkoutKeyToUpdate;

    // AutoComplete
    ArrayAdapter mAutoCompleteAdapter;
    String[] mWorkoutNameSuggestions;

    // Data tracking
    Routine mRoutine;
    ArrayList<Workout> mWorkouts = new ArrayList<>();
    ArrayList<String> mWorkoutKeys = new ArrayList<>();
    int mNumWorkouts;

    // Listener
    WorkoutChangedListener mWorkoutChangedListener;

    // Firebase and user info
    DatabaseReference mDatabase;
    String mRoutineKey;
    String mUsername;
    String mUid;
    String mClientTimeStamp;

    // Track routine changes in order to write changes to database
    boolean mRoutineChanged = false;

    public RoutineDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        mWorkoutNameSuggestions = getResources().getStringArray(R.array.workout_name_suggestions);
        mAutoCompleteAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, mWorkoutNameSuggestions);

        mUid = Utils.getUid();
        mClientTimeStamp = Utils.getClientTimeStamp(true);
        mRoutine = new Routine(mUid, mClientTimeStamp, true);

        // Get username and update routine object
        mDatabase = Utils.getDatabase().getReference();
        mDatabase.child("users").child(Utils.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mUsername = user.getUsername();
                mRoutine.setCreator(mUsername);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Generate unique routines key
        mRoutineKey = mDatabase.child("routines").push().getKey();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_routine_details, container, false);

        Log.i(TAG, "onCreateView");


        mWorkoutNameField = (AutoCompleteTextView) rootView.findViewById(R.id.autocomplete_field_workout_name);
        mWorkoutNameField.setAdapter(mAutoCompleteAdapter);
        mWorkoutNameField.setThreshold(1);

        mLinearLayoutWorkoutContainer = (LinearLayout) rootView.findViewById(R.id.linear_layout_routine_workout_container);

        mAddWorkoutButton = (Button) rootView.findViewById(R.id.button_routine_add_workout);
        mAddWorkoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                addWorkout();
            }
        });

        return rootView;
    }

    public void addWorkout(){

        mRoutineChanged = true;
        mNumWorkouts++;
        Log.i(TAG, "Workout added: " + mNumWorkouts + " workouts");

        // generate unique id for each workoutView
        final View workoutView = LayoutInflater.from(getContext()).inflate(R.layout.item_routine_workout, null);
        final int workoutViewId = View.generateViewId();
        workoutView.setId(workoutViewId);

        String workoutTitle = mWorkoutNameField.getText().toString();
        TextView title = (TextView) workoutView.findViewById(R.id.routine_workout_item_textview);
        mWorkoutNameField.setText("");
        title.setText(workoutTitle);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Workout clicked. CardView ID: " + workoutView.getId());
            }
        });

        // Create unique workout Id and add workout object
        final Workout workout = new Workout (mUid, mUsername, mClientTimeStamp, workoutTitle, true);
        final String workoutKey = mDatabase.child("routine-workouts").push().getKey();
        mWorkouts.add(workout);
        mWorkoutKeys.add(workoutKey);
        mWorkoutIdWorkoutObjectMap.put(workoutViewId, workout);


        ImageView deleteWorkout = (ImageView) workoutView.findViewById(R.id.routine_workout_delete_imageview);
        deleteWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRoutineChanged = true;
                mNumWorkouts--;
                Log.i(TAG, "Workout deleted: " + mNumWorkouts + " workouts");

                mLinearLayoutWorkoutContainer.removeView(workoutView);
                mWorkoutViewIdExercisesMap.remove(workoutViewId);

                // Notify activity
                mWorkoutChangedListener.onWorkoutRemoved();

                // Remove workout from map and list and remove workout id
                mWorkouts.remove(workout);
                mWorkoutKeys.remove(workoutKey);
                mWorkoutIdWorkoutObjectMap.remove(workoutViewId);
                mWorkoutKeyExercisesMap.remove(workoutKey);
            }
        });

                /*mWorkoutViewNameMap.put(workoutId, workoutTitle);*/

        TextView noExercisesTextView = (TextView) workoutView.findViewById(R.id.no_exercises_textview);
        noExercisesTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view){

                // when returning to this activity, can update correct workoutView card and correct workoutId
                mWorkoutCardToUpdate = workoutViewId;
                mWorkoutKeyToUpdate = workoutKey;

                Intent i = new Intent(getContext(), CreateWorkoutActivity.class);
                i.putExtra(CreateWorkoutActivity.ARG_CREATE_WORKOUT_FOR_ROUTINE, true);
                startActivityForResult(i, REQUEST_CREATE_WORKOUT);
            }
        });

        // add new workout view to container at top of layout
        mLinearLayoutWorkoutContainer.addView(workoutView, 0);

        // track specific workoutView by workoutViewId
        mWorkoutViewMap.put(workoutViewId, workoutView);

        // Notify activity
        mWorkoutChangedListener.onWorkoutAdded();


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CREATE_WORKOUT){
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Result OK");

                final ArrayList<Exercise> workoutExercises = (ArrayList<Exercise>) data.getSerializableExtra(CreateWorkoutActivity.EXTRA_WORKOUT_EXERCISES);
                mRoutineChanged = data.getBooleanExtra(CreateWorkoutActivity.EXTRA_EXERCISES_CHANGED, false);

                final int workoutViewId = mWorkoutCardToUpdate;
                final String workoutKey = mWorkoutKeyToUpdate;

                Log.i(TAG, "workoutKey: " + workoutKey);

                // get view to update and remove all views
                View viewToUpdate = mWorkoutViewMap.get(mWorkoutCardToUpdate);
                LinearLayout exercisesContainer = (LinearLayout) viewToUpdate.findViewById(R.id.linear_layout_routine_workout_exercises_container);
                exercisesContainer.removeAllViews();

                // remove previous exercises from map
                if (mWorkoutViewIdExercisesMap.containsKey(workoutViewId)) mWorkoutViewIdExercisesMap.remove(workoutViewId);
                if (mWorkoutKeyExercisesMap.containsKey(workoutKey)) mWorkoutKeyExercisesMap.remove(workoutKey);

                if (workoutExercises.size() > 0){

                    // add new exercises
                    mWorkoutViewIdExercisesMap.put(workoutViewId, workoutExercises);
                    mWorkoutKeyExercisesMap.put(workoutKey, workoutExercises);

                    // Generate new views from exercise list and add to container
                    for (int i = 0; i < workoutExercises.size(); i++){
                        TextView exercise = new TextView(getContext());
                        exercise.setText(workoutExercises.get(i).getName());
                        exercise.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
                        exercise.setPadding(0, 5, 0, 5);
                        exercisesContainer.addView(exercise);
                    }

                    // clicking on exercise list will open select exercise/order exercise activity
                    exercisesContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mWorkoutCardToUpdate = workoutViewId;
                            mWorkoutKeyToUpdate = workoutKey;

                            Intent i = new Intent(getContext(), CreateWorkoutActivity.class);
                            i.putExtra(CreateWorkoutActivity.ARG_CREATE_WORKOUT_FOR_ROUTINE, true);
                            i.putExtra(CreateWorkoutActivity.EXTRA_WORKOUT_EXERCISES, workoutExercises);
                            startActivityForResult(i, REQUEST_CREATE_WORKOUT);
                        }
                    });

                    viewToUpdate.findViewById(R.id.no_exercises_textview).setVisibility(View.GONE);

                } else {
                    Log.i(TAG, "All exercises have been remove from the workout");
                    viewToUpdate.findViewById(R.id.no_exercises_textview).setVisibility(View.VISIBLE);
                }

            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "Result cancelled");
            }
        }
    }


    // Set listener
    public void setOnWorkoutChangedListener(WorkoutChangedListener listener){
        this.mWorkoutChangedListener = listener;
    }

    @Override
    public void onPause(){
        super.onPause();

        Log.i(TAG, "onPause");

        if (mRoutineChanged) {
            // Update routine local object
            mRoutine.setNumWorkouts(mNumWorkouts);

            Map<String, Boolean> workoutsInRoutine = new HashMap<>();
            for (String workoutId : mWorkoutKeys) {
                workoutsInRoutine.put(workoutId, true);
            }
            mRoutine.setWorkouts(workoutsInRoutine);

            // Clear data before rewriting
            Map<String, Object> clearRoutineData = new HashMap<>();
            if (mNumWorkouts == 0){
                Log.i(TAG, "No exercises, so remove routine for now");
                clearRoutineData.put("/routines/" + mRoutineKey, null);
                clearRoutineData.put("/user-routines/" + mUid + "/" + mRoutineKey, null);
            }
            clearRoutineData.put("/routine-workouts/" + mRoutineKey, null);
            clearRoutineData.put("/routine-workout-exercises/" + mRoutineKey, null);
            clearRoutineData.put("/user-routine-workouts/" + mUid + "/" + mRoutineKey, null);
            clearRoutineData.put("/user-routine-workout-exercises/" + mUid + "/" + mRoutineKey, null);
            mDatabase.updateChildren(clearRoutineData);

            // Write to database
            if (mNumWorkouts > 0) {
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/routines/" + mRoutineKey, mRoutine.toMap());
                childUpdates.put("/user-routines/" + mUid + "/" + mRoutineKey, mRoutine.toMap());

                for (int i = 0; i < mWorkouts.size(); i++) {

                    if (!mWorkoutKeyExercisesMap.isEmpty()) {
                        List<Exercise> exercises = mWorkoutKeyExercisesMap.get(mWorkoutKeys.get(i));
                        if (exercises != null) {
                            mWorkouts.get(i).setNumExercises(exercises.size());
                            for (Exercise e : exercises) {
                                String exerciseKey = e.getName();
                                e.setExerciseId();
                                childUpdates.put("/routine-workout-exercises/" + mRoutineKey + "/" + mWorkoutKeys.get(i) + "/" + exerciseKey, e.toMap());
                                childUpdates.put("/user-routine-workout-exercises/" + mUid + "/" + mRoutineKey + "/" + mWorkoutKeys.get(i) + "/" + exerciseKey, e.toMap());
                            }
                        }
                    }

                    childUpdates.put("/routine-workouts/" + mRoutineKey + "/" + mWorkoutKeys.get(i), mWorkouts.get(i).toMap());
                    childUpdates.put("/user-routine-workouts/" + mUid + "/" + mRoutineKey + "/" + mWorkoutKeys.get(i), mWorkouts.get(i).toMap());

                }
                mDatabase.updateChildren(childUpdates);
            }

            mRoutineChanged = false;
        }

    }

    public void deleteRoutine(){
        Map<String, Object> deleteRoutine = new HashMap<>();
        deleteRoutine.put("/routines/" + mRoutineKey, null);
        deleteRoutine.put("/user-routines/" + mUid + "/" + mRoutineKey, null);
        mDatabase.updateChildren(deleteRoutine);
    }

    // Getters and setters

    public Routine getRoutine() {
        return mRoutine;
    }

    public void setRoutine(Routine routine) {
        this.mRoutine = routine;
    }

    public void setRoutineChanged(boolean routineChanged) {
        this.mRoutineChanged = routineChanged;
    }

    public int getNumWorkouts() {
        return mNumWorkouts;
    }

    public String getRoutineKey() {
        return mRoutineKey;
    }
}
