package com.zonesciences.pyrros.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.WorkoutsAdapter;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.viewholder.WorkoutViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WorkoutsListFragment extends Fragment {

    private static final String TAG = "WorkoutsListFragment";

    private static final String STATE_EXERCISE_MAP = "WorkoutListExerciseMap";

    private DatabaseReference mDatabase;
    private Context mContext;


    private FirebaseRecyclerAdapter<Workout, WorkoutViewHolder> mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mManager;

    private Map<String, List<Exercise>> mWorkoutExercisesMap = new HashMap<>();
    private Query mUserWorkoutsQuery;

    private String mUnits;

    private WorkoutsContainerFragment.OnViewSwitchedListener mOnViewSwitchedListener;

    public WorkoutsListFragment() {}

    public static WorkoutsListFragment newInstance(WorkoutsContainerFragment.OnViewSwitchedListener listener) {

        Bundle args = new Bundle();
        WorkoutsListFragment fragment = new WorkoutsListFragment();
        fragment.setArguments(args);
        fragment.setOnViewSwitchedListener(listener);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate() called");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mContext = getActivity();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUnits = sharedPref.getString("pref_unit", null);
        Log.i(TAG, "Units for this app: " + mUnits);

        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);

        Log.i(TAG, "onCreateView() called");
        View rootView = inflater.inflate(R.layout.fragment_workouts_list, container, false);

        // [START create_database_reference]

        // [END create_database_reference]

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.workouts_list);
        mRecyclerView.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_workouts_list_view, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();

        //Creates hashmap with string workoutKey and value containing list of exercises performed in that workout
        createUserWorkoutsMap();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void createUserWorkoutsMap() {

        mDatabase.child("user-workout-exercises").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //gets each workout and associates it with the correct list of exercise objects
                for (DataSnapshot workout : dataSnapshot.getChildren()) {
                    String s = workout.getKey();
                    List<Exercise> exercises = new ArrayList<Exercise>();
                    for (DataSnapshot exerciseKey : workout.getChildren()){
                        Exercise exercise = exerciseKey.getValue(Exercise.class);
                        Log.i(TAG, "Obtained exercise: " + exercise.getName() + " Sets: " + exercise.getSets());
                        exercises.add(exercise);
                    }
                    mWorkoutExercisesMap.put(s, exercises);
                }


                // Set up Layout Manager, reverse layout, show latest workouts on top
                mManager = new LinearLayoutManager(getActivity());
                mManager.setReverseLayout(true);
                mManager.setStackFromEnd(true);
                mRecyclerView.setLayoutManager(mManager);

                mUserWorkoutsQuery = mDatabase.child("user-workouts").child(getUid()).orderByChild("clientTimeStamp").limitToFirst(1000);


                mAdapter = new WorkoutsAdapter(Workout.class, R.layout.item_workout, WorkoutViewHolder.class, mUserWorkoutsQuery, mDatabase, getUid(), mWorkoutExercisesMap, mContext);
                mRecyclerView.setAdapter(mAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        switch(i){
            case R.id.action_calendar_view:
                mOnViewSwitchedListener.switchView();
                break;
        }

        return super.onOptionsItemSelected(item);
    }



    public void setOnViewSwitchedListener (WorkoutsContainerFragment.OnViewSwitchedListener listener){
        this.mOnViewSwitchedListener = listener;
    }
}
