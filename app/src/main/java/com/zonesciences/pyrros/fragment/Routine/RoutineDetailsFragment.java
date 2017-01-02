package com.zonesciences.pyrros.fragment.Routine;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import com.zonesciences.pyrros.ItemTouchHelper.ItemTouchHelperCallback;
import com.zonesciences.pyrros.ItemTouchHelper.OnDragListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.RoutineWorkoutsAdapter;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Routine;
import com.zonesciences.pyrros.models.User;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */

    //TODO: BUG - when launching directly into sort workout fragment, "done" arrow does not show on moving/deleting exercises
    //TODO: Write workoutKey and routineKey into objects when creating anywhere in app
    //TODO: fix workoutChanged issue (createworkout activity always says that workout has changed)

public class RoutineDetailsFragment extends Fragment implements OnDragListener {

    private static final String TAG = "RoutineDetailsFragment";

    // REQUEST CODE
    private static final int REQUEST_CREATE_WORKOUT = 1;

    // View
    AutoCompleteTextView mWorkoutNameField;
    Button mAddWorkoutButton;
    LinearLayout mLinearLayoutWorkoutContainer;

    // Recycler
    RecyclerView mRecycler;
    RoutineWorkoutsAdapter mAdapter;

    // Item touch helper
    // Touch Helper
    ItemTouchHelper mItemTouchHelper;
    ItemTouchHelper.Callback mItemTouchHelperCallback;

    // Maps
    Map<Integer, View> mWorkoutViewMap = new HashMap<>();
    Map<Integer, ArrayList<Exercise>> mWorkoutViewIdExercisesMap = new HashMap<>();
    Map<String, ArrayList<Exercise>> mWorkoutKeyExercisesMap = new HashMap<>();
    Map<Integer, Workout> mWorkoutIdWorkoutObjectMap = new HashMap<>();

    // Track workouts to update view and object
    int mWorkoutCardToUpdate;
    String mWorkoutKeyToUpdate;
    int mWorkoutPositionToUpdate;

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

    public static RoutineDetailsFragment newInstance() {
        Bundle args = new Bundle();
        RoutineDetailsFragment fragment = new RoutineDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

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

        // Generate unique routines key if new routine
        if (mRoutine == null) {
            mRoutine = new Routine(mUid, mClientTimeStamp, true);
            mRoutineKey = mDatabase.child("routines").push().getKey();
        } else {
            mRoutineKey = mRoutine.getRoutineKey();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_routine_details, container, false);

        Log.i(TAG, "onCreateView");


        mWorkoutNameField = (AutoCompleteTextView) rootView.findViewById(R.id.autocomplete_field_workout_name);
        mWorkoutNameField.setAdapter(mAutoCompleteAdapter);
        mWorkoutNameField.setThreshold(1);

        /*mLinearLayoutWorkoutContainer = (LinearLayout) rootView.findViewById(R.id.linear_layout_routine_workout_container);*/

        mAddWorkoutButton = (Button) rootView.findViewById(R.id.button_routine_add_workout);
        mAddWorkoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                addWorkout();
            }
        });

        mRecycler = (RecyclerView) rootView.findViewById(R.id.recycler_routine_workouts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecycler.setLayoutManager(layoutManager);




        if (mRoutine.getWorkoutsList() != null) {
            Log.i(TAG, "Has workouts, set adapter");
            mAdapter = new RoutineWorkoutsAdapter(getActivity(), mRoutine.getWorkoutsList(), addExerciseListener, workoutChangedListener, this);
            mRecycler.setAdapter(mAdapter);

            mItemTouchHelperCallback = new ItemTouchHelperCallback(mAdapter, true, false);
            mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
            mItemTouchHelper.attachToRecyclerView(mRecycler);
        }

        return rootView;
    }

    public void addWorkout(){

        mRoutineChanged = true;

        String workoutTitle = mWorkoutNameField.getText().toString();
        Workout workout = new Workout (mUid, mUsername, mClientTimeStamp, workoutTitle, true);
        String workoutKey = mDatabase.child("user-routines").child(mRoutineKey).push().getKey();
        workout.setWorkoutKey(workoutKey);
        mRoutine.addWorkoutToList(workout);

        if (mAdapter == null){
            mAdapter = new RoutineWorkoutsAdapter(getActivity(), mRoutine.getWorkoutsList(), addExerciseListener, workoutChangedListener, this);
            mRecycler.setAdapter(mAdapter);

            mItemTouchHelperCallback = new ItemTouchHelperCallback(mAdapter, true, false);
            mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
            mItemTouchHelper.attachToRecyclerView(mRecycler);
        }
        mAdapter.notifyItemInserted(0);
        mRecycler.smoothScrollToPosition(0);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CREATE_WORKOUT) {
            if (resultCode == RESULT_OK) {

                Log.i(TAG, "Result OK");

                final ArrayList<Exercise> workoutExercises = (ArrayList<Exercise>) data.getSerializableExtra(CreateWorkoutActivity.EXTRA_WORKOUT_EXERCISES);

                for (Exercise e : workoutExercises){
                    Log.i(TAG, "Exercise: " + e.getName() + " Prescribed Reps : " + e.getPrescribedReps());
                }

                mRoutineChanged = data.getBooleanExtra(CreateWorkoutActivity.EXTRA_EXERCISES_CHANGED, false);
                Log.i(TAG, "routineChanged: " + mRoutineChanged);

                if (mRoutineChanged) {
                    mRoutine.getWorkoutsList().get(mWorkoutPositionToUpdate).setExercises(workoutExercises);
                    mAdapter.notifyDataSetChanged();
                }

            } else {
                Log.i(TAG, "All exercises have been remove from the workout");
            }

        } else if (resultCode == RESULT_CANCELED) {
            Log.i(TAG, "Result cancelled");
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
            int numWorkouts = mAdapter.getWorkouts().size();
            List<Workout> workouts = mRoutine.getWorkoutsList();
            for (Workout workout : workouts){
                Log.i(TAG, "Workout : " + workout.getName() + " Order: " + workout.getWorkoutOrder());
            }

            mRoutine.setNumWorkouts(numWorkouts);

            Map<String, Boolean> workoutsInRoutine = new HashMap<>();
            for (Workout workout : workouts) {
                String workoutKey = workout.getWorkoutKey();
                workoutsInRoutine.put(workoutKey, true);
            }
            mRoutine.setWorkouts(workoutsInRoutine);

            // Clear data before rewriting
            Map<String, Object> clearRoutineData = new HashMap<>();
            if (numWorkouts == 0){
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
            if (numWorkouts > 0) {
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/routines/" + mRoutineKey, mRoutine.toMap());
                childUpdates.put("/user-routines/" + mUid + "/" + mRoutineKey, mRoutine.toMap());

                for (int i = 0; i < workouts.size(); i++) {

                    List<Exercise> exercises = workouts.get(i).getExercises();
                    if (exercises != null) {
                        workouts.get(i).setNumExercises(exercises.size());
                        for (Exercise e : exercises) {
                            String exerciseKey = e.getName();
                            e.setExerciseId();
                            childUpdates.put("/routine-workout-exercises/" + mRoutineKey + "/" + workouts.get(i).getWorkoutKey() + "/" + exerciseKey, e.toMap());
                            childUpdates.put("/user-routine-workout-exercises/" + mUid + "/" + mRoutineKey + "/" + workouts.get(i).getWorkoutKey() + "/" + exerciseKey, e.toMap());
                        }
                    }


                    childUpdates.put("/routine-workouts/" + mRoutineKey + "/" + workouts.get(i).getWorkoutKey(), workouts.get(i).toMap());
                    childUpdates.put("/user-routine-workouts/" + mUid + "/" + mRoutineKey + "/" + workouts.get(i).getWorkoutKey(), workouts.get(i).toMap());

                }
                mDatabase.updateChildren(childUpdates);
            }

            mRoutineChanged = false;
        }

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

    // Listeners

    RoutineWorkoutsAdapter.AddExerciseListener addExerciseListener = new RoutineWorkoutsAdapter.AddExerciseListener() {
        @Override
        public void onAddFirstExercises(int workoutPositionToUpdate) {
            Log.i(TAG, "First exercises added");
            mWorkoutPositionToUpdate = workoutPositionToUpdate;
            Intent i = new Intent(getContext(), CreateWorkoutActivity.class);
            i.putExtra(CreateWorkoutActivity.ARG_CREATE_WORKOUT_FOR_ROUTINE, true);
            i.putExtra(CreateWorkoutActivity.EXTRA_WORKOUT_TITLE, mAdapter.getWorkouts().get(workoutPositionToUpdate).getName());
            startActivityForResult(i, REQUEST_CREATE_WORKOUT);
        }

        @Override
        public void onChangeExistingExercises(int workoutPositionToUpdate) {
            Log.i(TAG, "Edit existing exercises");
            mWorkoutPositionToUpdate = workoutPositionToUpdate;

            ArrayList<Exercise> workoutExercises = (ArrayList) mRoutine.getWorkoutsList().get(mWorkoutPositionToUpdate).getExercises();

            for (Exercise e : workoutExercises){
                Log.i(TAG, "Exercise: " + e.getName() + " Prescribed Reps : " + e.getPrescribedReps());
            }

            Collections.sort(workoutExercises);

            Intent i = new Intent(getContext(), CreateWorkoutActivity.class);
            i.putExtra(CreateWorkoutActivity.ARG_CREATE_WORKOUT_FOR_ROUTINE, true);
            i.putExtra(CreateWorkoutActivity.EXTRA_WORKOUT_EXERCISES, workoutExercises);
            i.putExtra(CreateWorkoutActivity.EXTRA_WORKOUT_TITLE, mAdapter.getWorkouts().get(workoutPositionToUpdate).getName());
            startActivityForResult(i, REQUEST_CREATE_WORKOUT);
        }
    };

    WorkoutChangedListener workoutChangedListener = new WorkoutChangedListener() {
        @Override
        public void onWorkoutAdded() {
            Log.i(TAG, "Workout Added");
            mRoutineChanged = true;
        }

        @Override
        public void onWorkoutRemoved() {
            Log.i(TAG, "Workout Removed");
            mRoutineChanged = true;
        }

        @Override
        public void onWorkoutChanged() {
            Log.i(TAG, "Workout Changed");
            mRoutineChanged = true;
        }
    };

    // Drag Listener
    @Override
    public void onStartDrag (RecyclerView.ViewHolder viewHolder){
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onStopDrag(){

    }
}
