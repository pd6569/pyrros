package com.zonesciences.pyrros.fragment.CreateWorkout;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zonesciences.pyrros.R;

public class SortWorkoutFragment extends Fragment {

    // View
    RecyclerView mRecyclerView;


    public SortWorkoutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sort_workout, container, false);

        return rootView;
    }


}
