package com.zonesciences.pyrros.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.timessquare.CalendarPickerView;
import com.zonesciences.pyrros.R;

import java.util.Calendar;
import java.util.Date;

public class WorkoutsCalendarFragment extends Fragment {

    private static final String TAG = "WorkoutsCalendar";

    CalendarView mCalendarView;
    private WorkoutsContainerFragment.OnViewSwitchedListener mOnViewSwitchedListener;

    public WorkoutsCalendarFragment() {
        // Required empty public constructor
    }

    public static WorkoutsCalendarFragment newInstance(WorkoutsContainerFragment.OnViewSwitchedListener listener) {

        Bundle args = new Bundle();
        WorkoutsCalendarFragment fragment = new WorkoutsCalendarFragment();
        fragment.setArguments(args);
        fragment.setOnViewSwitchedListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate() called");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_workouts_calendar, container, false);

        Log.i(TAG, "WorkoutsCalendarFragment loaded");
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        CalendarPickerView calendar = (CalendarPickerView) rootView.findViewById(R.id.calendar_view);
        Date today = new Date();
        calendar.init(today, nextYear.getTime())
                .withSelectedDate(today);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_workouts, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem calendarView = (MenuItem) menu.findItem(R.id.action_calendar_view);
        calendarView.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        switch(i){
            case R.id.action_list_view:
                mOnViewSwitchedListener.switchView();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setOnViewSwitchedListener (WorkoutsContainerFragment.OnViewSwitchedListener listener){
        this.mOnViewSwitchedListener = listener;
    }
}
