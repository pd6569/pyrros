package com.zonesciences.pyrros;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.adapters.ExercisesFilterAdapter;
import com.zonesciences.pyrros.fragment.CreateWorkout.CreateWorkoutFragment;
import com.zonesciences.pyrros.fragment.CreateWorkout.ExercisesListener;
import com.zonesciences.pyrros.fragment.CreateWorkout.SortWorkoutFragment;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Record;
import com.zonesciences.pyrros.models.User;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateWorkoutActivity extends BaseActivity {

    private static final String TAG = "CreateWorkoutActivity";

    // Args
    private static final String ARG_WORKOUT_DATE = "WorkoutDate";

    // Context
    Context mContext;

    // Database, workout and user details
    DatabaseReference mDatabase;
    String mWorkoutKey;
    String mUserId;
    String mUsername;
    String mWorkoutDate;

    // Toolbar, tabs and pager
    Toolbar mToolbar;
    TabLayout mTabLayout;
    ViewPager mViewPager;
    CreateWorkoutPagerAdapter mPagerAdapter;

    // Fragment reference
    Map<Integer, Fragment> mFragmentReferenceMap = new HashMap<>();

    // Current workout list
    ArrayList<Exercise> mWorkoutExercises = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        Intent intent = getIntent();

        mContext = getApplicationContext();
        if (intent.hasExtra(ARG_WORKOUT_DATE)) {
            mWorkoutDate = intent.getExtras().getString(ARG_WORKOUT_DATE);
        }

        // Initialise database and get user details
        mDatabase = Utils.getDatabase().getReference();
        mUserId = getUid();
        mDatabase.child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mUsername = user.getUsername();
                if (mFragmentReferenceMap.get(0) != null){
                    CreateWorkoutFragment frag = (CreateWorkoutFragment) mFragmentReferenceMap.get(0);
                    frag.setUsername(mUsername);
                }
                Log.i(TAG, "Username: " + mUsername);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mViewPager = (ViewPager) findViewById(R.id.viewpager_create_workout);
        mPagerAdapter = new CreateWorkoutPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_create_workout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.findViewById(R.id.toolbar_title_create_workout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Title clicked");
            }
        });

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs_create_workout);
        mTabLayout.setupWithViewPager(mViewPager);

    }


    class CreateWorkoutPagerAdapter extends FragmentPagerAdapter {

        String[] tabTitles = new String[]{
                "Select Exercises",
                "My workout"
        };

        Fragment[] fragments = new Fragment[]{
                CreateWorkoutFragment.newInstance(mUserId),
                new SortWorkoutFragment()
        };

        public CreateWorkoutPagerAdapter (FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0){
                CreateWorkoutFragment frag = CreateWorkoutFragment.newInstance(mUserId);
                frag.setExercisesListener(new ExercisesListener() {
                    @Override
                    public void onExerciseAdded(Exercise exercise) {

                    }

                    @Override
                    public void onExercisesEmpty() {

                    }

                    @Override
                    public void onExerciseRemoved(Exercise exercise) {
                    }

                    @Override
                    public void onExercisesChanged(ArrayList<Exercise> exerciseList) {
                        mWorkoutExercises = exerciseList;
                        boolean isFirstExercise;
                        SortWorkoutFragment sortWorkoutFragment = (SortWorkoutFragment) mFragmentReferenceMap.get(1);
                        sortWorkoutFragment.setWorkoutExercises(mWorkoutExercises);
                        sortWorkoutFragment.getAdapter().notifyDataSetChanged();
                        Log.i(TAG, "Exercise changed in adapter, activity notified: " + mWorkoutExercises.size());
                    }
                });
                mFragmentReferenceMap.put(position, frag);
                return frag;
            } else {
                SortWorkoutFragment frag = SortWorkoutFragment.newInstance();
                frag.setExercisesListener(new ExercisesListener() {
                    @Override
                    public void onExerciseAdded(Exercise exercise) {
                        Log.i(TAG, "Exercises added");
                    }

                    @Override
                    public void onExercisesEmpty() {
                        Log.i(TAG, "Exercises empty");
                    }

                    @Override
                    public void onExerciseRemoved(Exercise exercise) {
                        Log.i(TAG, "Exercise removed");
                    }

                    @Override
                    public void onExercisesChanged(ArrayList<Exercise> exerciseList) {
                        mWorkoutExercises = exerciseList;
                        CreateWorkoutFragment createWorkoutFragment = (CreateWorkoutFragment) mFragmentReferenceMap.get(0);
                        createWorkoutFragment.getAdapter().notifyDataSetChanged();

                        Log.i(TAG, "Exercises changed. New workout exercises list size: " + mWorkoutExercises.size());

                    }
                });
                mFragmentReferenceMap.put(position, frag);
                return frag;
            }
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return tabTitles[position];
        }

        public Fragment getFragment (int position){
            return mFragmentReferenceMap.get(position);
        }
    }

    public Fragment getFragment (int position){
        return mFragmentReferenceMap.get(position);
    }

    public String getWorkoutDate() {
        return mWorkoutDate;
    }

    public void setWorkoutDate(String workoutDate) {
        mWorkoutDate = workoutDate;
    }
}
