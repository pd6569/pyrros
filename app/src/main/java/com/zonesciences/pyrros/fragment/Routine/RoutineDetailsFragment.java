package com.zonesciences.pyrros.fragment.Routine;


import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
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
import static android.view.View.GONE;

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
    CardView mAddWorkoutCardView;
    LinearLayout mAddWorkoutLayout;
    AutoCompleteTextView mWorkoutNameField;
    Button mAddWorkoutButton;

    // Views for create new workout
    CardView mRoutinePropetiesCardView;
    TextInputLayout mRoutineNameLayout;
    TextInputLayout mRoutineDescriptionLayout;
    EditText mRoutineNameField;
    EditText mRoutineDescriptionField;
    Spinner mRoutineGoalsSpinner;
    Spinner mRoutineLevelSpinner;
    TextView mSaveRoutinePropertiesText;
    SwitchCompat mSharedSwitch;

    // Spinner adapter
    ArrayAdapter mGoalsAdapter;
    ArrayAdapter mLevelAdapter;

    // Floating action button
    FloatingActionButton mFabAddWorkout;

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
    RoutineCreatedListener mRoutineCreatedListener;

    // Firebase and user info
    DatabaseReference mDatabase;
    String mRoutineKey;
    String mUsername;
    String mUid;
    String mClientTimeStamp;

    // Track routine changes in order to write changes to database
    boolean mRoutineChanged = false;

    // View to show on load
    boolean mIsNewRoutine = true;
    boolean mShowAddWorkoutPanel = false;

    // Routine properties
    boolean mGoalPropertySet;
    boolean mLevelPropertySet;

    // Editable
    boolean mAllowEditing;


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

        // Check if user has priveleges to change routine
        if (mRoutine != null){
            Log.i(TAG, "Routine is not null");
            Log.i(TAG, "mUid: " + mUid + " Routine user id: " + mRoutine.getUid());
            if (mUid.equals(mRoutine.getUid())){
                // User created this routine, so allow editing
                mAllowEditing = true;
                Log.i(TAG, "User: " + mUid + " matches creator of routine: " + mRoutine.getUid() + " set allow editing: " + mAllowEditing);
            }
        }

        // Generate unique routines key if new routine (routine can be set from viewroutines fragment via setter)
        if (mRoutine == null) {
            mAllowEditing = true;
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

        mFabAddWorkout = (FloatingActionButton) rootView.findViewById(R.id.fab_add_workout);
        if (!mAllowEditing){
            mFabAddWorkout.setVisibility(GONE);
            mFabAddWorkout.setOnClickListener(null);
        }
        mAddWorkoutCardView = (CardView) rootView.findViewById(R.id.add_workout_cardview);
        if (!mAllowEditing){
            mAddWorkoutCardView.setVisibility(GONE);
        }
        mAddWorkoutLayout = (LinearLayout) rootView.findViewById(R.id.layout_add_workout);
        mWorkoutNameField = (AutoCompleteTextView) rootView.findViewById(R.id.autocomplete_field_workout_name);
        mAddWorkoutButton = (Button) rootView.findViewById(R.id.button_routine_add_workout);

        // Create new workout
        mRoutinePropetiesCardView = (CardView) rootView.findViewById(R.id.routine_properties_cardview);
        mRoutineNameLayout = (TextInputLayout) rootView.findViewById(R.id.input_layout_routine_name);
        mRoutineDescriptionLayout = (TextInputLayout) rootView.findViewById(R.id.input_layout_routine_description);
        mRoutineNameField = (EditText) rootView.findViewById(R.id.routine_name_edit_text);
        if (mRoutine.getName() != null){
            String name = mRoutine.getName();
            Log.i(TAG, "Set routine name: " + mRoutine.getName());
            mRoutineNameField.setText(name);
        }
        mRoutineDescriptionField = (EditText) rootView.findViewById(R.id.routine_description_edit_text);
        if (mRoutine.getDescription() != null){
            String description = mRoutine.getDescription();
            Log.i(TAG, "Set routine desc: " + description);
            mRoutineDescriptionField.setText(description);
        }

        mRoutineGoalsSpinner = (Spinner) rootView.findViewById(R.id.routine_goal_spinner);
        mRoutineGoalsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mRoutine.setGoal(parent.getItemAtPosition(pos).toString());
                mGoalPropertySet = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mRoutineLevelSpinner = (Spinner) rootView.findViewById(R.id.routine_level_spinner);
        mRoutineLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mRoutine.setLevel(parent.getItemAtPosition(pos).toString());
                mLevelPropertySet = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mSaveRoutinePropertiesText = (TextView) rootView.findViewById(R.id.routine_properties_ok_textview);
        mSaveRoutinePropertiesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveRoutineProperties();
            }
        });

        mGoalsAdapter = ArrayAdapter.createFromResource(getContext(), R.array.routine_properties_goal, R.layout.simple_spinner_item);
        mLevelAdapter = ArrayAdapter.createFromResource(getContext(), R.array.routine_properties_level, R.layout.simple_spinner_item);

        mRoutineGoalsSpinner.setAdapter(mGoalsAdapter);
        if (mRoutine.getGoal() != null){
            String goal = mRoutine.getGoal();
            String[] goals = getResources().getStringArray(R.array.routine_properties_goal);
            for (int i = 0 ; i < goals.length; i++){
                if (goals[i].equals(goal)){
                    mRoutineGoalsSpinner.setSelection(i);
                }
            }
        }

        mRoutineLevelSpinner.setAdapter(mLevelAdapter);
        if (mRoutine.getLevel() != null){
            String level = mRoutine.getGoal();
            String[] levels = getResources().getStringArray(R.array.routine_properties_level);
            for (int i = 0 ; i < levels.length; i++){
                if (levels[i].equals(level)){
                    mRoutineLevelSpinner.setSelection(i);
                }
            }
        }

        mSharedSwitch = (SwitchCompat) rootView.findViewById(R.id.routine_shared_switch);
        mSharedSwitch.setChecked(mRoutine.getShared());

        mSharedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.i(TAG, "Shared switch changed: " + isChecked);
                mRoutine.setShared(isChecked);
            }
        });

        if (mIsNewRoutine){
            Log.i(TAG, "New routine being created");
            mRoutinePropetiesCardView.setVisibility(View.VISIBLE);
            mAddWorkoutCardView.setVisibility(GONE);
            mFabAddWorkout.setVisibility(GONE);
        } else {
            mRoutinePropetiesCardView.setVisibility(GONE);
        }

        if (mShowAddWorkoutPanel == false && mIsNewRoutine == false){
            mFabAddWorkout.setVisibility(View.VISIBLE);
            mAddWorkoutCardView.setVisibility(GONE);
        }

        mFabAddWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideFabShowAddWorkout();
            }
        });


        mWorkoutNameField.setAdapter(mAutoCompleteAdapter);
        mWorkoutNameField.setThreshold(1);

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
            mAdapter.setAllowEditing(mAllowEditing);
            mRecycler.setAdapter(mAdapter);

            mItemTouchHelperCallback = new ItemTouchHelperCallback(mAdapter, true, false);
            mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
            mItemTouchHelper.attachToRecyclerView(mRecycler);
        }

        return rootView;
    }

    private void saveRoutineProperties(){
        if (mRoutineNameField.getText().toString().trim().isEmpty()){
            mRoutineNameLayout.setError(getString(R.string.error_routine_name));
            mRoutineNameField.requestFocus();
            return;
        }
        mRoutineCreatedListener.onRoutineCreated(mRoutineNameField.getText().toString());
        mRoutine.setName(mRoutineNameField.getText().toString());

        if (!mRoutineDescriptionField.getText().toString().trim().isEmpty()){
            mRoutine.setDescription(mRoutineDescriptionField.getText().toString().trim());
        }

        if (!mGoalPropertySet){
            mRoutine.setGoal("General");
        }

        if (!mLevelPropertySet){
            mRoutine.setLevel("Any");
        }

        mRoutinePropetiesCardView.animate()
                .translationX(mRoutinePropetiesCardView.getWidth())
                .alpha(0.0f)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mRoutinePropetiesCardView.setVisibility(View.GONE);
                        hideFabShowAddWorkout();
                        showWorkoutRecycler();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    public void addWorkout(){

        showWorkoutRecycler();

        if (mWorkoutNameField.getText().toString().equals("")){
            Snackbar sb = Snackbar.make(mWorkoutNameField, "Enter name for workout", Snackbar.LENGTH_SHORT);
            sb.show();
            return;
        }

        mRoutineChanged = true;

        String workoutTitle = mWorkoutNameField.getText().toString();
        mWorkoutNameField.setText("");

        Workout workout = new Workout (mUid, mUsername, mClientTimeStamp, workoutTitle, true);
        String workoutKey = mDatabase.child("user-routines").child(mRoutineKey).push().getKey();
        workout.setWorkoutKey(workoutKey);
        mRoutine.addWorkoutToList(workout);

        if (mAdapter == null){
            mAdapter = new RoutineWorkoutsAdapter(getActivity(), mRoutine.getWorkoutsList(), addExerciseListener, workoutChangedListener, this);
            mAdapter.setAllowEditing(mAllowEditing);
            mRecycler.setAdapter(mAdapter);

            mItemTouchHelperCallback = new ItemTouchHelperCallback(mAdapter, true, false);
            mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
            mItemTouchHelper.attachToRecyclerView(mRecycler);
        }
        mAdapter.notifyItemInserted(0);
        mRecycler.smoothScrollToPosition(0);

        showFabHideAddWorkout();
    }


    public void hideFabShowAddWorkout(){
        mAddWorkoutCardView.setVisibility(View.VISIBLE);
        mAddWorkoutCardView.setAlpha(0.0f);
        mWorkoutNameField.requestFocus();
        mAddWorkoutCardView.animate()
                .translationY(0)
                .alpha(1.0f)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mFabAddWorkout.animate().alpha(0.0f);
                        mFabAddWorkout.setVisibility(GONE);

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    public void showFabHideAddWorkout(){
        Log.i(TAG,"showFabHideAddWorkout");
        mAddWorkoutCardView.animate()
                .translationY(-mAddWorkoutCardView.getHeight())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mAddWorkoutCardView.setVisibility(GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
        mFabAddWorkout.animate()
                .alpha(1.0f)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mFabAddWorkout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    public void showRoutineProperties(){
        mRoutinePropetiesCardView.setAlpha(0.0f);
        mRoutinePropetiesCardView.animate()
                .alpha(1.0f)
                .translationX(0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mRoutinePropetiesCardView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    public void hideAll(){
        if (mAddWorkoutCardView.getVisibility() == View.VISIBLE) {
            hideAddWorkoutPanel();
        }
        if (mFabAddWorkout.getVisibility() == View.VISIBLE){
            hideFab();
        }
        if (mRecycler.getVisibility() == View.VISIBLE){
            hideWorkoutRecycler();
        }
    }

    private void hideAddWorkoutPanel(){
        mAddWorkoutCardView.animate()
                .translationY(-mAddWorkoutCardView.getHeight())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mAddWorkoutCardView.setVisibility(GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    private void hideFab(){
        mFabAddWorkout.animate()
                .alpha(0.0f)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mFabAddWorkout.setVisibility(GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    private void hideWorkoutRecycler(){
        mRecycler.animate()
                .alpha(0.0f)
                .translationX(-mRecycler.getWidth())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mRecycler.setVisibility(GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    private void showWorkoutRecycler(){
        if (mRecycler.getVisibility() != View.VISIBLE){
            Log.i(TAG, "Show recycler");
            mRecycler.animate()
                    .alpha(1.0f)
                    .translationX(0)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            mRecycler.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
        }
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mRoutineCreatedListener = (RoutineCreatedListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement RoutineCreatedListener");
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        Log.i(TAG, "onPause. Write routine to database");

        // Update routine local object


        List<Workout> workouts = mRoutine.getWorkoutsList();
        int numWorkouts = 0;
        if (workouts != null) {
            numWorkouts = workouts.size();

            Map<String, Boolean> workoutsInRoutine = new HashMap<>();
            for (Workout workout : workouts) {
                String workoutKey = workout.getWorkoutKey();
                workoutsInRoutine.put(workoutKey, true);
            }
            mRoutine.setWorkouts(workoutsInRoutine);
        }

        mRoutine.setNumWorkouts(numWorkouts);

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

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
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

    public void setShowAddWorkoutPanel(boolean showAddWorkoutPanel) {
        mShowAddWorkoutPanel = showAddWorkoutPanel;
    }

    public void setNewRoutine(boolean newRoutine) {
        mIsNewRoutine = newRoutine;
    }

    // Set listener
    public void setOnWorkoutChangedListener(WorkoutChangedListener listener){
        this.mWorkoutChangedListener = listener;
    }

    // Other methods

    public void showAddWorkoutView(){
        Log.i(TAG, "Show add workout view");
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

    public interface RoutineCreatedListener {
        void onRoutineCreated(String routineName);
    }

    // Drag Listener
    @Override
    public void onStartDrag (RecyclerView.ViewHolder viewHolder){
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onStopDrag(){

    }

}
