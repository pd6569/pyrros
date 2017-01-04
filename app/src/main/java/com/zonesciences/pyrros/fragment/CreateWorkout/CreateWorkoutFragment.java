package com.zonesciences.pyrros.fragment.CreateWorkout;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.CreateWorkoutActivity;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.adapters.ExercisesFilterAdapter;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateWorkoutFragment extends Fragment implements SearchView.OnQueryTextListener {

    private static final String ARG_USER_ID = "UserId";
    private static final String ARG_USER_NAME = "WorkoutId";

    public CreateWorkoutFragment() {
        // Required empty public constructor
    }

    public static CreateWorkoutFragment newInstance(String userId){
        Bundle bundle = new Bundle();
        bundle.putString(ARG_USER_ID, userId);
        CreateWorkoutFragment fragment = new CreateWorkoutFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private static final String TAG = "CreateWorkoutFrag";

    private static final String WORKOUT_EXERCISE_OBJECTS = "WorkoutExerciseObjects";
    private static final String WORKOUT_EXERCISES = "Workout Exercises";
    private static final String WORKOUT_ID = "Workout ID";

    Context mContext;

    // mAllExercises contains all exercise objects. IT IS THE SOURCE OF THE EXERCISE OBJECTS, EVERYTHING ELSE SIMPLY REFERENCES THESE EXERCISES MEMORY LOCATIONS
    // Therefore changes to the exercises via filters, in the adapters are all done on the same original exercises generated and stored in AllExercises
    // Changes made in sortworkout adapter also hold reference to these exercises, and therefore when order is updated or exericses unselected, this is made
    // on the same exercises. No new exercise objects are instantiated, the references are simply passed around.

    // Data
    List<Exercise> mAllExercises = new ArrayList<>();
    List<Exercise> mFilteredExercises = new ArrayList<>(); // references the exercise objects contained in all exercises - NOT NEW OBJECTS IN MEMORY
    List<List<Exercise>> mFilterHistory = new ArrayList<>();
    List<Exercise> mWorkoutExercises = new ArrayList<>();
    List<Exercise> mPreselectedExercises = new ArrayList<>(); // if create workout is launched from routine activity need to ensure any selected exercises are marked as selected

    // Database, workout and user details
    DatabaseReference mDatabase;
    String mWorkoutKey;
    String mUserId;
    String mUsername;
    String mWorkoutDate;

    // View
    Spinner mBodypartSpinner;
    Spinner mEquipmentSpinner;
    RecyclerView mExercisesFilterRecycler;
    LinearLayoutManager mLayoutManager;
    DividerItemDecoration mDivider;

    // Adapter
    ExercisesFilterAdapter mAdapter;

    // Menu
    MenuItem mStartWorkoutAction;


    String[] mBodyPartsArray;
    String[] mEquipmentArray;

    // Search Filter
    int mPreviousSearchStringLength;

    // Spinner Filter
    int mCurrentBodyPartFilterIndex;
    int mCurrentEquipmentFilterIndex;

    // Exercise listener
    ExercisesListener mExercisesListener;

    // Where is fragment being displayed
    boolean mInEditWorkout;
    boolean mCreateWorkoutForRoutine;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mContext = getActivity();

        mBodyPartsArray = getResources().getStringArray(R.array.bodyparts);
        mEquipmentArray = getResources().getStringArray(R.array.equipment);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserId = bundle.getString(ARG_USER_ID);

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_create_workout, container, false);

        mExercisesFilterRecycler = (RecyclerView) rootView.findViewById(R.id.recycler_exercises_filter);
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mExercisesFilterRecycler.setHasFixedSize(true);
        mExercisesFilterRecycler.setLayoutManager(mLayoutManager);
        mDivider = new DividerItemDecoration(mExercisesFilterRecycler.getContext(), mLayoutManager.getOrientation());
        mExercisesFilterRecycler.addItemDecoration(mDivider);


        mDatabase.child("exercises").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // mAllExercises may be set externally before fragment is loaded e.g. from edit workout
                if (mAllExercises.isEmpty()) {
                    for (DataSnapshot exercise : dataSnapshot.getChildren()) {
                        Exercise e = exercise.getValue(Exercise.class);
                        mAllExercises.add(e);
                    }
                }

                // make sure that any preselected exercises are marked as checked and ordered.
                if (mCreateWorkoutForRoutine){
                    if (mPreselectedExercises != null && !mPreselectedExercises.isEmpty()) {
                        setOrderAndSelection(mPreselectedExercises);
                    }
                }

                mFilteredExercises.addAll(mAllExercises);
                mAdapter = new ExercisesFilterAdapter(mContext, (ArrayList) mFilteredExercises, mInEditWorkout);
                mAdapter.setExercisesListener(new ExercisesListener() {
                    @Override
                    public void onExerciseAdded(Exercise exercise) {
                        mExercisesListener.onExerciseAdded(exercise);
                        if (!mStartWorkoutAction.isVisible() && !mInEditWorkout){
                            mStartWorkoutAction.setVisible(true);
                        }
                    }

                    @Override
                    public void onExercisesEmpty() {
                        Log.i(TAG, "Exercises Empty");
                        mStartWorkoutAction.setVisible(false);
                    }


                    @Override
                    public void onExerciseRemoved(Exercise exercise) {
                        mExercisesListener.onExerciseRemoved(exercise);
                        if (!mStartWorkoutAction.isVisible() && !mInEditWorkout){
                            mStartWorkoutAction.setVisible(true);
                        }
                    }

                    @Override
                    public void onExercisesChanged(ArrayList<Exercise> exerciseList) {
                        mWorkoutExercises = (ArrayList) exerciseList;
                        mExercisesListener.onExercisesChanged((ArrayList) mWorkoutExercises);
                        Log.i(TAG, "Exercises changed: " + mWorkoutExercises.size());
                    }

                    @Override
                    public void onExerciseSelected(Exercise exercise) {

                    }
                });

                // if exercises are preselected from RoutineActivity, then ensure that these are checked and that sort workout is notified
                if (!mWorkoutExercises.isEmpty()){
                    mAdapter.setWorkoutExercises((ArrayList<Exercise>) mWorkoutExercises);
                    mExercisesListener.onExercisesChanged((ArrayList<Exercise>) mWorkoutExercises);
                }
                mExercisesFilterRecycler.setAdapter(mAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        String defaultTextBodyPart = "Body part";
        String defaultTextEquipment = "Equipment";

        mBodypartSpinner = (Spinner) rootView.findViewById(R.id.spinner_bodypart_filter);
        mBodypartSpinner.setAdapter(new FilterSpinnerAdapter(mContext, R.layout.spinner_row, mBodyPartsArray, defaultTextBodyPart));
        mBodypartSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mFilterHistory.clear();
                mCurrentBodyPartFilterIndex = pos;
                Toast.makeText(getActivity(), "Filter selected: " + mBodyPartsArray[pos], Toast.LENGTH_SHORT).show();

                List<Exercise> list = getExercisesForBodypartFilter(pos);

                mFilteredExercises.clear();
                mFilteredExercises.addAll(list);

                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mEquipmentSpinner = (Spinner) rootView.findViewById(R.id.spinner_equipment_filter);
        mEquipmentSpinner.setAdapter(new FilterSpinnerAdapter(mContext, R.layout.spinner_row, mEquipmentArray, defaultTextEquipment));
        mEquipmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mFilterHistory.clear();
                mCurrentEquipmentFilterIndex = pos;

                Toast.makeText(getActivity(), "Filter selected: " + mEquipmentArray[pos], Toast.LENGTH_SHORT).show();

                List<Exercise> list = getExercisesForEquipmentFilter(pos);

                mFilteredExercises.clear();
                mFilteredExercises.addAll(list);


                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        return rootView;
    }

    // if exercises are preselected from Routine Activity, then make sure that these are "checked"
    private void setOrderAndSelection (List<Exercise> selectedExercises){
        for (Exercise e : mAllExercises){
            for (Exercise selectedExercise : selectedExercises){
                if (e.getName().equals(selectedExercise.getName())){
                    e.setSelected(true);
                    e.setOrder(selectedExercise.getOrder());
                    e.setReps(selectedExercise.getReps());
                    e.setPrescribedReps(selectedExercise.getPrescribedReps());
                    e.setRepTempo(selectedExercise.getRepTempo());
                    mWorkoutExercises.add(e);
                    Collections.sort(mWorkoutExercises);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_create_workout, menu);
        super.onCreateOptionsMenu(menu, inflater);

        mStartWorkoutAction = menu.findItem(R.id.action_start_workout);

        // change "start" text to tick/done icon if creating/editing workout for a routine
        if (mCreateWorkoutForRoutine){
            mStartWorkoutAction.setIcon(R.drawable.ic_done_white_24dp);
        }


        if (mWorkoutExercises.size() > 0){
            mStartWorkoutAction.setVisible(true);
        }

        if (mInEditWorkout) mStartWorkoutAction.setVisible(false);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        boolean addHistory = true;
        int length = newText.length();
        List<Exercise> exercisesToSearch = new ArrayList<>();

        if (length == 0){
            mFilterHistory.clear();
        }

        if(mFilterHistory.isEmpty()){
            if (mCurrentBodyPartFilterIndex == 0) {
                mFilterHistory.add(mAllExercises);
            } else {
                mFilterHistory.add(getExercisesForFilter(true, mCurrentBodyPartFilterIndex));
            }
            mAdapter.notifyDataSetChanged();
        }

        Log.i(TAG, "onQueryTextChange. newText Length: " + length + " previous Text Length: " + mPreviousSearchStringLength);
        if (length < mPreviousSearchStringLength) {
            addHistory = false;
        }

        exercisesToSearch = mFilterHistory.get(mFilterHistory.size()-1);

        newText = newText.toLowerCase();
        ArrayList<Exercise> newList = new ArrayList<>();
        for (Exercise exercise : exercisesToSearch){
            String name = exercise.getName().toLowerCase();
            if(name.contains(newText)){
                newList.add(exercise);
            }
        }

        if (addHistory) {
            mFilterHistory.add(newList);
        } else {
            if(mFilterHistory.size()>1){
                mFilterHistory.remove(mFilterHistory.size()-1);
            }
        }

        setFilter(newList);
        mPreviousSearchStringLength = newText.length();
        return true;
    }

    public void setFilter(ArrayList<Exercise> newList){

        mFilteredExercises.clear();
        mFilteredExercises.addAll(newList);

        Log.i(TAG, "mFilter history added: " + mFilterHistory.size());

        mAdapter.notifyDataSetChanged();
    }

    public List<Exercise> getExercisesForFilter(boolean bodypart, int index){
        List<Exercise> filteredList = new ArrayList<>();

        if (bodypart) {
            for (Exercise e : mAllExercises) {
                if (e.getMuscleGroup().toLowerCase().equals(mBodyPartsArray[index].toLowerCase())) {
                    Log.i(TAG, "Found exercises for " + mBodyPartsArray[index] + " Exercise: " + e.getName() + " Equipment: " + e.getEquipment() + " Is selected: " + e.isSelected());
                    filteredList.add(e);
                }
            }
        } else {
            for (Exercise e : mAllExercises) {
                if (e.getEquipment().toLowerCase().equals(mEquipmentArray[index].toLowerCase())) {
                    Log.i(TAG, "Found exercises for " + mEquipmentArray[index] + " Exercise: " + e.getName() + " Equipment: " + e.getEquipment() + " Is selected: " + e.isSelected());
                    filteredList.add(e);
                }
            }
        }
        return filteredList;
    }


    public List<Exercise> getExercisesForBodypartFilter(int index){

        List<Exercise> filteredList = new ArrayList<>();

        if (mCurrentEquipmentFilterIndex == 0){

            for (Exercise e : mAllExercises) {

                if (index != 0) {

                    if (e.getMuscleGroup().toLowerCase().equals(mBodyPartsArray[index].toLowerCase())) {
                        Log.i(TAG, "Found exercises for " + mBodyPartsArray[index] + " Exercise: " + e.getName() + " Is selected: " + e.isSelected());
                        filteredList.add(e);
                    }
                } else {
                    // Filter "all" selected for both bodypart and equipment
                    return mAllExercises;
                }
            }
        } else {
            Log.i(TAG, "equipment filter: " + mEquipmentArray[mCurrentEquipmentFilterIndex]);
            List<Exercise> equipmentFilter = getExercisesForFilter(false, mCurrentEquipmentFilterIndex);
            if (index != 0) {
                for (Exercise e : equipmentFilter) {

                    if (e.getMuscleGroup().toLowerCase().equals(mBodyPartsArray[index].toLowerCase())) {
                        Log.i(TAG, "Found exercises for " + mBodyPartsArray[index] + " Exercise: " + e.getName() + " Is selected: " + e.isSelected());
                        filteredList.add(e);
                    }
                }
            } else {
                // Return all exercises for just the equipment filter
                return equipmentFilter;
            }
        }

        return filteredList;
    }


    public List<Exercise> getExercisesForEquipmentFilter(int index){

        List<Exercise> filteredList = new ArrayList<>();

        if (mCurrentBodyPartFilterIndex == 0){

            for (Exercise e : mAllExercises) {

                if (index != 0) {

                    if (e.getEquipment().toLowerCase().equals(mEquipmentArray[index].toLowerCase())) {
                        Log.i(TAG, "Found exercises for " + mEquipmentArray[index] + " Exercise: " + e.getName() + " Is selected: " + e.isSelected());
                        filteredList.add(e);
                    }
                } else {
                    // Filter "all" selected for both bodypart and equipment
                    return mAllExercises;
                }
            }
        } else {
            Log.i(TAG, "bodypart filter: " + mBodyPartsArray[mCurrentBodyPartFilterIndex]);
            List<Exercise> bodyPartFilter = getExercisesForFilter(true, mCurrentBodyPartFilterIndex);
            if (index != 0) {
                for (Exercise e : bodyPartFilter) {

                    if (e.getEquipment().toLowerCase().equals(mEquipmentArray[index].toLowerCase())) {
                        Log.i(TAG, "Found exercises for " + mEquipmentArray[index] + " Exercise: " + e.getName() + " Is selected: " + e.isSelected());
                        filteredList.add(e);
                    }
                }
            } else {
                // Return all exercises for just the bodypart filter
                return bodyPartFilter;
            }
        }

        return filteredList;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_start_workout:

                startWorkout();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }

    }

    public List<Exercise> getWorkoutExercises (){
        return mAdapter.getWorkoutExercises();
    }

    public ExercisesFilterAdapter getAdapter() {
        return mAdapter;
    }

    public void setInEditWorkout(boolean inEditWorkout) {
        mInEditWorkout = inEditWorkout;
    }

    public void setCreateWorkoutForRoutine (boolean createWorkoutForRoutine) {
        mCreateWorkoutForRoutine = createWorkoutForRoutine;
    }

    public void setExercisesListener(ExercisesListener listener){
        this.mExercisesListener = listener;
    }

    public void startWorkout(){

        // if activity is being started from RoutineActivity then DO NOT START A WORKOUT!

        if (mCreateWorkoutForRoutine) {
            createWorkoutForRoutine();
            return;
        }

        mWorkoutKey = mDatabase.child("workouts").push().getKey();
        mWorkoutDate = ((CreateWorkoutActivity) getActivity()).getWorkoutDate();
        ArrayList<Exercise> exercisesToLoad = (ArrayList) mAdapter.getWorkoutExercises();
        final ArrayList<String> exerciseKeysList = new ArrayList<>();

        String workoutDate;
        if (mWorkoutDate == null){
            workoutDate = Utils.getClientTimeStamp(true);
        } else {
            workoutDate = mWorkoutDate;
        }
        Workout newWorkout = new Workout(mUserId, mUsername, workoutDate, "", true);
        newWorkout.setNumExercises(exercisesToLoad.size());

        // Write to database
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/workouts/" + mWorkoutKey, newWorkout);
        childUpdates.put("/user-workouts/" + mUserId + "/" + mWorkoutKey, newWorkout);
        childUpdates.put("/timestamps/workouts/" + mWorkoutKey + "/created/", ServerValue.TIMESTAMP);
        for (Exercise exercise : exercisesToLoad){
            exercise.setExerciseId(UUID.randomUUID().toString());
            String exerciseKey = exercise.getName();
            exerciseKeysList.add(exerciseKey);
            childUpdates.put("/workout-exercises/" + mWorkoutKey + "/" + exerciseKey, exercise.toMap());
            childUpdates.put("/user-workout-exercises/" + mUserId + "/" + mWorkoutKey + "/" + exerciseKey, exercise.toMap());
        }
        mDatabase.updateChildren(childUpdates);

        /*mDatabase.child("records").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (String exerciseKey : exerciseKeysList){
                    if (!dataSnapshot.hasChild(exerciseKey)){
                        Record record = new Record(exerciseKey, mUserId);
                        mDatabase.child("records").child(exerciseKey).child(mUserId).setValue(record);
                        mDatabase.child("user-records").child(mUserId).child(exerciseKey).setValue(record);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


        Bundle extras = new Bundle();
        Log.i(TAG, "Exercises to pass to new activity " + exerciseKeysList);
        extras.putSerializable(WORKOUT_EXERCISES, exerciseKeysList);
        extras.putString(WORKOUT_ID, mWorkoutKey);
        extras.putSerializable(WORKOUT_EXERCISE_OBJECTS, exercisesToLoad);
        Intent i = new Intent (getActivity(), WorkoutActivity.class);
        i.putExtras(extras);
        startActivity(i);
    }

    private void createWorkoutForRoutine(){
        Log.i(TAG, "create workout for routine, do not start a workout, return to routine activity");
        getActivity().onBackPressed();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG, "onStop");
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public List<Exercise> getAllExercises() {
        return mAllExercises;
    }

    public void setAllExercises(List<Exercise> allExercises) {
        mAllExercises = allExercises;
    }

    public void clearAllExercises(){
        mAllExercises.clear();
    }

    public void setPreselectedExercises(ArrayList<Exercise> preselectedExercises) {
        this.mPreselectedExercises = preselectedExercises;
    }

    public class FilterSpinnerAdapter extends ArrayAdapter<String>{

        Context context;
        String[] objects;
        String firstElement;
        boolean isFirstTime;

        public FilterSpinnerAdapter(Context context, int resource, String[] objects, String defaultText) {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
            this.isFirstTime = true;
            setDefaultText(defaultText);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if(isFirstTime) {
                objects[0] = firstElement;
                isFirstTime = false;
            }
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            notifyDataSetChanged();
            return getCustomView(position, convertView, parent);
        }

        public void setDefaultText(String defaultText) {
            this.firstElement = objects[0];
            objects[0] = defaultText;
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.spinner_row, parent, false);
            TextView label = (TextView) row.findViewById(R.id.spinner_text);
            label.setText(objects[position]);

            return row;
        }

    }


}
