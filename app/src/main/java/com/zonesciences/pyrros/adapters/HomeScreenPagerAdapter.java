package com.zonesciences.pyrros.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.zonesciences.pyrros.fragment.DashboardFragment;
import com.zonesciences.pyrros.fragment.WorkoutsCalendarFragment;
import com.zonesciences.pyrros.fragment.WorkoutsFragment;

public class HomeScreenPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "HomeScreenPagerAdapter";
    private final FragmentManager mFragmentManager;
    private Fragment mFragmentAtPos0;

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
        if (position == 0){
            if (mFragmentAtPos0 == null){
                mFragmentAtPos0 = WorkoutsFragment.newInstance(new WorkoutsFragmentListener() {
                    @Override
                    public void onSwitchWorkoutsView() {
                        Log.i(TAG, "onSwitchWorkoutsView called");
                        mFragmentManager.beginTransaction().remove(mFragmentAtPos0).commit();
                        mFragmentAtPos0 = new WorkoutsCalendarFragment();
                        notifyDataSetChanged();
                    }
                });
            }
            return mFragmentAtPos0;
        }

        return new DashboardFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return tabTitles[position];
    }

    @Override
    public int getItemPosition(Object object)
    {
        if (object instanceof WorkoutsFragment && mFragmentAtPos0 instanceof WorkoutsCalendarFragment) {
            return POSITION_NONE;
        }

        return POSITION_UNCHANGED;
    }

    public interface WorkoutsFragmentListener {
        void onSwitchWorkoutsView();
    }

}