package com.zonesciences.pyrros.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.zonesciences.pyrros.R;

public class WorkoutsCalendarFragment extends Fragment {

    CalendarView mCalendarView;

    public WorkoutsCalendarFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_workouts_calendar, container, false);

        mCalendarView = (CalendarView) rootView.findViewById(R.id.calendar_view);

        return rootView;
    }

}
