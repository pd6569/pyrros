package com.zonesciences.pyrros.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zonesciences.pyrros.R;


public class WorkoutsContainerFragment extends Fragment {


    public WorkoutsContainerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_workouts_container, container, false);

        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction().add(R.id.workouts_list_calendar_container, WorkoutsListFragment.newInstance()).commit();

        return rootView;
    }

}
