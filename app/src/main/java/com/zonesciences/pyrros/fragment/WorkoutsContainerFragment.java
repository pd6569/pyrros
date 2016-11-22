package com.zonesciences.pyrros.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zonesciences.pyrros.R;


public class WorkoutsContainerFragment extends Fragment {

    private static final String TAG = "WorkoutsContainer";

    private FragmentManager mFragmentManager;

    public WorkoutsContainerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        mFragmentManager = getChildFragmentManager();
        if (mFragmentManager.getFragments() != null) {
            for (Fragment frag : mFragmentManager.getFragments()) {
                mFragmentManager.beginTransaction().remove(frag).commit();
            }
        }
        mFragmentManager.beginTransaction().add(R.id.workouts_list_calendar_container, WorkoutsListFragment.newInstance(new OnViewSwitchedListener() {
            @Override
            public void switchView() {
                mFragmentManager.beginTransaction().replace(R.id.workouts_list_calendar_container, WorkoutsCalendarFragment.newInstance(new OnViewSwitchedListener() {
                    @Override
                    public void switchView() {
                        mFragmentManager.popBackStack();
                    }
                })).addToBackStack(null).commit();
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


    public interface OnViewSwitchedListener{
        void switchView();
    }

}
