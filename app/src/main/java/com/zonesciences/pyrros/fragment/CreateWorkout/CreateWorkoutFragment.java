package com.zonesciences.pyrros.fragment.CreateWorkout;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
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
import com.zonesciences.pyrros.models.Record;
import com.zonesciences.pyrros.models.User;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
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

    public static CreateWorkoutFragment newInstance(String userId, String username){
        Bundle bundle = new Bundle();
        bundle.putString(ARG_USER_ID, userId);
        bundle.putString(ARG_USER_NAME, username);
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

    // Database, workout and user details
    DatabaseReference mDatabase;
    String mWorkoutKey;
    String mUserId;
    String mUsername;

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

    // Exercise listener
    ExercisesListener mExercisesListener;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mContext = getActivity();

        mBodyPartsArray = getResources().getStringArray(R.array.bodyparts);
        mEquipmentArray = getResources().getStringArray(R.array.equipment);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserId = bundle.getString(ARG_USER_ID);
        mUsername = bundle.getString(ARG_USER_NAME);

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
                for (DataSnapshot exercise : dataSnapshot.getChildren()){
                    Exercise e = exercise.getValue(Exercise.class);
                    mAllExercises.add(e);
                }
                mFilteredExercises.addAll(mAllExercises);
                mAdapter = new ExercisesFilterAdapter(mContext, (ArrayList) mFilteredExercises);
                mAdapter.setExercisesListener(new ExercisesListener() {
                    @Override
                    public void onExerciseAdded(Exercise exercise) {
                        if (!mStartWorkoutAction.isVisible()){
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
                    }

                    @Override
                    public void onExercisesChanged(ArrayList<Exercise> exerciseList) {
                        mWorkoutExercises = (ArrayList) exerciseList;
                        mExercisesListener.onExercisesChanged((ArrayList) mWorkoutExercises);
                        Log.i(TAG, "Exercises changed: " + mWorkoutExercises.size());
                    }
                });
                mExercisesFilterRecycler.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mBodypartSpinner = (Spinner) rootView.findViewById(R.id.spinner_bodypart_filter);
        ArrayAdapter<CharSequence> bodypartAdapter = ArrayAdapter.createFromResource(getContext(), R.array.bodyparts, R.layout.simple_spinner_item);
        bodypartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBodypartSpinner.setAdapter(bodypartAdapter);
        mBodypartSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mFilterHistory.clear();
                mCurrentBodyPartFilterIndex = pos;
                Toast.makeText(getActivity(), "Filter selected: " + mBodyPartsArray[pos], Toast.LENGTH_SHORT).show();
                if (pos == 0) {
                    Log.i(TAG, "Filter: " + mBodyPartsArray[pos].toLowerCase());
                    mFilteredExercises.clear();
                    mFilteredExercises.addAll(mAllExercises);

                } else {
                    Log.i(TAG, "Filter: " + mBodyPartsArray[pos].toLowerCase() + " mAllExercises size: " + mAllExercises.size());

                    List<Exercise> list = getExercisesForFilter(pos);

                    Log.i(TAG, "mFilteredExercises SIZE: " + mFilteredExercises.size() + " list size: " + list.size() + " mAllExercises: " + mAllExercises.size());

                    mFilteredExercises.clear();
                    mFilteredExercises.addAll(list);
                }

                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mEquipmentSpinner = (Spinner) rootView.findViewById(R.id.spinner_equipment_filter);
        ArrayAdapter<CharSequence> equipmentAdapter = ArrayAdapter.createFromResource(getContext(), R.array.equipment, R.layout.simple_spinner_item);
        equipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEquipmentSpinner.setAdapter(equipmentAdapter);
        mEquipmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Toast.makeText(getActivity(), "Filter selected: " + mEquipmentArray[pos], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_create_workout, menu);
        super.onCreateOptionsMenu(menu, inflater);

        mStartWorkoutAction = menu.findItem(R.id.action_start_workout);
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
                mFilterHistory.add(getExercisesForFilter(mCurrentBodyPartFilterIndex));
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

    public List<Exercise> getExercisesForFilter(int index){
        List<Exercise> filteredList = new ArrayList<>();
        for (Exercise e : mAllExercises) {
            if (e.getMuscleGroup().toLowerCase().equals(mBodyPartsArray[index].toLowerCase())) {
                Log.i(TAG, "Found exercises for " + mBodyPartsArray[index] + " Exercise: " + e.getName() + " Is selected: "+ e.isSelected());
                filteredList.add(e);
            }
        }
        return filteredList;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_start_workout:

                mWorkoutKey = mDatabase.child("workouts").push().getKey();

                ArrayList<Exercise> exercisesToLoad = (ArrayList) mAdapter.getWorkoutExercises();
                final ArrayList<String> exerciseKeysList = new ArrayList<>();


                Workout newWorkout = new Workout(mUserId, mUsername, Utils.getClientTimeStamp(true), "", true);
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

                mDatabase.child("records").addListenerForSingleValueEvent(new ValueEventListener() {
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
                });


                Bundle extras = new Bundle();
                Log.i(TAG, "Exercises to pass to new activity " + exerciseKeysList);
                extras.putSerializable(WORKOUT_EXERCISES, exerciseKeysList);
                extras.putString(WORKOUT_ID, mWorkoutKey);
                extras.putSerializable(WORKOUT_EXERCISE_OBJECTS, exercisesToLoad);
                Intent i = new Intent (getActivity(), WorkoutActivity.class);
                i.putExtras(extras);
                startActivity(i);
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


    public void setExercisesListener(ExercisesListener listener){
        this.mExercisesListener = listener;
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

}
