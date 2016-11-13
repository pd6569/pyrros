package com.zonesciences.pyrros.fragment;


import android.os.Bundle;
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
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.ExerciseHistoryAdapter;
import com.zonesciences.pyrros.models.Exercise;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ExerciseHistoryFragment extends Fragment {

    private static final String TAG = "ExerciseHistoryFragment";

    private static final String ARG_EXERCISE_KEY = "ExerciseKey";
    private static final String ARG_USER_ID = "UserId";
    private static final String ARG_EXERCISE_HISTORY_DATES = "ExerciseHistoryDates";
    private static final String ARG_EXERCISE_HISTORY = "ExerciseHistory";


    DatabaseReference mUserWorkoutExercisesRef;

    String mExerciseKey;
    String mUserId;

    //Data
    List<Exercise> mExercises;
    List<String> mExerciseDates;

    //RecyclerView
    RecyclerView mExerciseHistoryRecycler;
    ExerciseHistoryAdapter mAdapter;
    LinearLayoutManager mLinearLayoutManager;

    //Sort order
    boolean newestFirst = true;

    public static ExerciseHistoryFragment newInstance(String exerciseKey, String userId, List<String> exerciseDates, List<Exercise> exercises) {
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_KEY, exerciseKey);
        args.putString(ARG_USER_ID, userId);
        args.putStringArrayList(ARG_EXERCISE_HISTORY_DATES, (ArrayList<String>) exerciseDates);
        args.putSerializable(ARG_EXERCISE_HISTORY, (Serializable) exercises);
        ExerciseHistoryFragment fragment = new ExerciseHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ExerciseHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        Bundle bundle = getArguments();
        mExerciseKey = bundle.getString(ARG_EXERCISE_KEY);
        mUserId = bundle.getString(ARG_USER_ID);
        mExerciseDates = (List) bundle.getSerializable(ARG_EXERCISE_HISTORY_DATES);
        mExercises = (List) bundle.getSerializable(ARG_EXERCISE_HISTORY);

        //TODO: METHODS TO SORT DATA INTO REVERSE ORDER

        mAdapter = new ExerciseHistoryAdapter(getContext(), mExerciseDates, mExercises);

        Log.i(TAG, "Exercise history obtained. " + mExercises.size());
        Log.i(TAG, "Exercise dates obtained. " + mExerciseDates.size());


        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");

        View rootView =  inflater.inflate(R.layout.fragment_exercise_history, container, false);

        TextView tv = (TextView) rootView.findViewById(R.id.textview_exercise_history);
        tv.setText("Exercise history for: " + mExerciseKey);

        mExerciseHistoryRecycler = (RecyclerView) rootView.findViewById(R.id.recycler_exercise_history);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mExerciseHistoryRecycler.setLayoutManager(mLinearLayoutManager);
        mExerciseHistoryRecycler.setHasFixedSize(true);
        mExerciseHistoryRecycler.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_exercise_history, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        switch(i){
            case R.id.action_reverse_order:
                /*newestFirst = !newestFirst;
                Log.i(TAG, "Reverse the order");
                Collections.reverse(mExerciseDates);
                Collections.reverse(mExercises);
                mAdapter.notifyDataSetChanged();

                if (newestFirst) {
                    item.setTitle(R.string.menu_oldest_first);
                } else {
                    item.setTitle(R.string.menu_newest_first);
                }*/


                break;
        }

        return super.onOptionsItemSelected(item);
    }


}

