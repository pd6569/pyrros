package com.zonesciences.pyrros;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private TextView welcomeTextView;

    private ViewPager mViewPager;

    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialise Firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            loadLoginView();
        } else{
            mUserId = mFirebaseUser.getUid();

            mViewPager = (ViewPager) findViewById(R.id.viewpager_homescreen);

            FragmentPagerAdapter pagerAdapter = new FragmentHomescreenPagerAdapter(getSupportFragmentManager(), MainActivity.this);
            mViewPager.setAdapter(pagerAdapter);

            //Give the TabLayout for region selection the ViewPager
            TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs_homescreen);
            tabLayout.setupWithViewPager(mViewPager, false);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
            getSupportActionBar().setTitle("Pyros Trainer");
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

    class FragmentHomescreenPagerAdapter extends FragmentPagerAdapter {
        String tabTitles[] = new String[]{"Trainer", "Workout", "Food Log", "Analytics", "Calendar", "Leaderboard"};
        Context context;

        //constructor
        public FragmentHomescreenPagerAdapter(FragmentManager fm, Context context){
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("MainActivity", "Position: " + position);
            return HomeScreenPagerFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return tabTitles[position];
        }
    }


}
