package com.zonesciences.pyrros.fragment.Workouts;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;

import java.util.List;
import java.util.Map;


public class WorkoutsContainerFragment extends Fragment {

    private static final String TAG = "WorkoutsContainer";

    private FragmentManager mFragmentManager;

    private Map<String, List<Exercise>> mWorkoutExercisesMap;

    public WorkoutsContainerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        //remove all fragments, prevent duplication
        mFragmentManager = getChildFragmentManager();
        if (mFragmentManager.getFragments() != null) {
            for (Fragment frag : mFragmentManager.getFragments()) {
                mFragmentManager.beginTransaction().remove(frag).commit();
            }
        }

        //  workout exercise map is set when calendar view is clicked from list view, and this map is passed to calendar fragment
        mFragmentManager.beginTransaction().add(R.id.workouts_list_calendar_container, WorkoutsListFragment.newInstance(new WorkoutsListFragment.OnSwitchToCalendarViewListener() {
            @Override
            public void displayCalendarView(Map<String, List<Exercise>> workoutExercisesMap) {
                mWorkoutExercisesMap = workoutExercisesMap;
                mFragmentManager.beginTransaction().replace(R.id.workouts_list_calendar_container, WorkoutsCalendarFragment.newInstance(new WorkoutsCalendarFragment.OnSwitchToListViewListener() {
                    @Override
                    public void displayListView() {
                        mFragmentManager.popBackStack();
                    }
                }, mWorkoutExercisesMap)).addToBackStack(null).commit();
            }
        })).commit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_workouts_container, container, false);


        return rootView;
    }




}
