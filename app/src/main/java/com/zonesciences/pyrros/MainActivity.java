package com.zonesciences.pyrros;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.fragment.DashboardFragment;
import com.zonesciences.pyrros.fragment.TrainerFragment;
import com.zonesciences.pyrros.fragment.WorkoutsFragment;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.User;
import com.zonesciences.pyrros.models.Workout;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference mDatabase;

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private String mUserId;
    private User mUser;

    private boolean mAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialise Firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser == null) {
            loadLoginView();
        } else{
            mUserId = mFirebaseUser.getUid();

            mViewPager = (ViewPager) findViewById(R.id.viewpager_homescreen);

            mPagerAdapter = new FragmentHomescreenPagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mPagerAdapter);

            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

            //Set locations to keep in sync
            mDatabase.child("user-exercises").child(mUserId).keepSynced(true);
            mDatabase.child("user-workout-exercises").child(mUserId).keepSynced(true);
            mDatabase.child("user-workouts").child(mUserId).keepSynced(true);

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
                    startActivity(new Intent(MainActivity.this, NewWorkoutActivity.class));
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
        }

        return super.onOptionsItemSelected(item);
    }




    class FragmentHomescreenPagerAdapter extends FragmentPagerAdapter {

        private final Fragment[] mFragments = new Fragment[] {
                new WorkoutsFragment(),
                new DashboardFragment(),
        };

        String tabTitles[] = new String[]{
                "Workouts",
                "Dashboard"
        };

        //constructor
        public FragmentHomescreenPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return tabTitles[position];
        }
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

}
