package com.zonesciences.pyrros.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class WorkoutsCalendarFragment extends Fragment {

    private static final String TAG = "WorkoutsCalendar";

    private static final String ARG_WORKOUT_EXERCISES_MAP = "WorkoutExercisesMap";

    CalendarView mCalendarView;

    // Data
    Map<String, List<Exercise>> mWorkoutExercisesMap;

    // Listener
    private OnSwitchToListViewListener mListViewListener;

    public WorkoutsCalendarFragment() {
        // Required empty public constructor
    }

    public static WorkoutsCalendarFragment newInstance(OnSwitchToListViewListener listener, Map<String, List<Exercise>> workoutExercisesMap) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_WORKOUT_EXERCISES_MAP, (Serializable) workoutExercisesMap);
        WorkoutsCalendarFragment fragment = new WorkoutsCalendarFragment();
        fragment.setArguments(args);
        fragment.setOnSwitchToListViewListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mWorkoutExercisesMap = (Map<String, List<Exercise>>) bundle.getSerializable(ARG_WORKOUT_EXERCISES_MAP);

        Log.i(TAG, "mWorkoutExerciseMap has been received, size: " + mWorkoutExercisesMap.size());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_workouts_calendar, container, false);

        mCalendarView = (CalendarView) rootView.findViewById(R.id.calendar_view);
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int day) {
                Log.i(TAG, "Date changed");

            }
        });


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_workouts_calendar_view, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        switch(i){
            case R.id.action_list_view:
                mListViewListener.displayListView();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public interface OnSwitchToListViewListener {
        void displayListView();
    }

    public void setOnSwitchToListViewListener (OnSwitchToListViewListener listener){
        this.mListViewListener = listener;
    }
}
