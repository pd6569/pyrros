package com.zonesciences.pyrros.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.zonesciences.pyrros.fragment.DashboardFragment;
import com.zonesciences.pyrros.fragment.WorkoutsCalendarFragment;
import com.zonesciences.pyrros.fragment.WorkoutsListFragment;

public class HomeScreenPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "HomeScreenPagerAdapter";
    private final FragmentManager mFragmentManager;

    Fragment[] fragments = new Fragment[]{
            WorkoutsListFragment.newInstance(),
            new DashboardFragment()
    };

    String tabTitles[] = new String[]{
            "Workouts",
            "Dashboard"
    };

    //constructor
    public HomeScreenPagerAdapter(FragmentManager fm){
        super(fm);
        mFragmentManager = fm;
    }


    @Override
    public Fragment getItem(int position) {
            return fragments[position];
        }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return tabTitles[position];
    }


}