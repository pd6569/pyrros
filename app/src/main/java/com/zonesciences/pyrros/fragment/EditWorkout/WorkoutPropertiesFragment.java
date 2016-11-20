package com.zonesciences.pyrros.fragment.EditWorkout;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zonesciences.pyrros.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkoutPropertiesFragment extends Fragment {


    public WorkoutPropertiesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workout_properties, container, false);
    }

}
