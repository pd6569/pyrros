package com.zonesciences.pyrros;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zonesciences.pyrros.fragment.CreateWorkout.CreateWorkoutFragment;
import com.zonesciences.pyrros.fragment.DashboardFragment;
import com.zonesciences.pyrros.fragment.Workouts.WorkoutViewListener;
import com.zonesciences.pyrros.fragment.Workouts.WorkoutsContainerFragment;
import com.zonesciences.pyrros.models.User;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference mDatabase;

    private HomeScreenPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private String mUserId;
    private User mUser;

    private Context mContext;

    private String mExercisesFile = "exercises";

    private boolean mAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialise Firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mContext = getApplicationContext();

        if (mFirebaseUser == null) {
            loadLoginView();
        } else{
            mUserId = mFirebaseUser.getUid();
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

            mViewPager = (ViewPager) findViewById(R.id.viewpager_homescreen);

            mPagerAdapter = new HomeScreenPagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mPagerAdapter);


            //Set locations to keep in sync
            mDatabase.child("user-exercises").child(mUserId).keepSynced(true);
            mDatabase.child("user-workout-exercises").child(mUserId).keepSynced(true);
            mDatabase.child("user-workouts").child(mUserId).keepSynced(true);
            mDatabase.child("user-records").child(mUserId).keepSynced(true);


            //Give the TabLayout for region selection the ViewPager
            TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs_homescreen);
            tabLayout.setupWithViewPager(mViewPager, false);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
            getSupportActionBar().setTitle("Pyros Trainer");

            //Button launches NewWorkoutActivity
            findViewById(R.id.fab_new_workout).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    /*startActivity(new Intent(MainActivity.this, NewWorkoutActivity.class));*/
                    startActivity(new Intent(MainActivity.this, CreateWorkoutActivity.class));
                }
            });
        }

    }


    public void loadLoginView(){
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    public void signOut(){
        mFirebaseAuth.signOut();
        loadLoginView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (i == R.id.action_purge_database) {
            purgeDatabase();
        } else if ( i == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (i == R.id.action_sync_exercises){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://pyrros-e0668.appspot.com/");
            final long ONE_MEGABYTE = 1024 * 1024;
            storageRef.child("Exercises.csv").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Log.i(TAG, "Exercise list downloaded successfully");
                    try {
                        FileOutputStream fos = openFileOutput(mExercisesFile, MODE_PRIVATE);
                        fos.write(bytes);
                        fos.close();
                        Toast.makeText(mContext, "Exercises saved to internal storage", Toast.LENGTH_SHORT).show();
                        processExercises();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle errors
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    public void processExercises(){
        try {
            List<String[]> exercises = new ArrayList<>();
            FileInputStream fin = openFileInput(mExercisesFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            String line;
            while ((line = reader.readLine()) != null){
                String[] RowData = line.split(",");
                exercises.add(RowData);
                String exerciseName = RowData[0];
                String muscleGroup = RowData[1];
                Log.i (TAG, "Exercise Name: " + exerciseName + " Muscle group: " + muscleGroup);
            }
            fin.close();
            createExercises(exercises);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createExercises(List<String[]> exerciseData){
        Map<String, Object> childUpdates = new HashMap<>();
        for (int i = 0; i < exerciseData.size(); i++){
            String exerciseKey = exerciseData.get(i)[0];
            childUpdates.put("/exercises/" + exerciseKey + "/name/", exerciseData.get(i)[0]);
            childUpdates.put("/exercises/" + exerciseKey + "/muscleGroup/", exerciseData.get(i)[1]);
        }
        mDatabase.updateChildren(childUpdates);
    }

    public void purgeDatabase(){
        mDatabase.child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getAccountType().equals("admin")){
                    Toast.makeText(getApplicationContext(), "Deleting the database, Motherfucker", Toast.LENGTH_SHORT).show();
                    Map<String, Object> purgeDatabase = new HashMap<>();
                    purgeDatabase.put("/timestamps/", null);
                    purgeDatabase.put("/user-exercises/", null);
                    purgeDatabase.put("/user-workouts/", null);
                    purgeDatabase.put("/workout-exercises/", null);
                    purgeDatabase.put("/user-workout-exercises/", null);
                    purgeDatabase.put("/workouts/", null);
                    purgeDatabase.put("/user-records/", null);
                    purgeDatabase.put("/records/", null);
                    mDatabase.updateChildren(purgeDatabase);
                } else {
                    Toast.makeText(getApplicationContext(), "You are not an admin, motherfucker", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class HomeScreenPagerAdapter extends FragmentPagerAdapter {

        Fragment[] fragments = new Fragment[]{
                new WorkoutsContainerFragment(),
                new DashboardFragment()
        };

        String tabTitles[] = new String[]{
                "Workouts",
                "Dashboard"
        };

        //constructor
        public HomeScreenPagerAdapter(FragmentManager fm){
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new Fragment();
            if (position == 0){
                WorkoutsContainerFragment workoutsContainerFragment = new WorkoutsContainerFragment();
                workoutsContainerFragment.setWorkoutViewListener(new WorkoutViewListener() {
                    @Override
                    public void calendarViewActive() {
                        Log.i(TAG, "CalendarView Active");
                    }

                    @Override
                    public void listViewActive() {
                        Log.i(TAG, "ListView Active");
                    }
                });
                fragment = workoutsContainerFragment;

            } else if (position == 1){
                fragment = new DashboardFragment();
            }

            return fragment;
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



}
