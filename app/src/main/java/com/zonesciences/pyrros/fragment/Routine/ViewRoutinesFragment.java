package com.zonesciences.pyrros.fragment.Routine;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zonesciences.pyrros.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewRoutinesFragment extends Fragment {

    public static ViewRoutinesFragment newInstance() {

        Bundle args = new Bundle();

        ViewRoutinesFragment fragment = new ViewRoutinesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ViewRoutinesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_routines, container, false);
    }

}
