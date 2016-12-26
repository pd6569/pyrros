package com.zonesciences.pyrros;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.zonesciences.pyrros.ItemTouchHelper.OnDragListener;
import com.zonesciences.pyrros.fragment.CreateRoutine.RoutineDetailsFragment;

public class CreateRoutineActivity extends BaseActivity {

    private static final String TAG = "CreateRoutineAcctivity";

    RoutineDetailsFragment mRoutineDetailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_routine);

        mRoutineDetailsFragment = new RoutineDetailsFragment();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.routine_fragment_container, mRoutineDetailsFragment);
        ft.commit();

    }


}
