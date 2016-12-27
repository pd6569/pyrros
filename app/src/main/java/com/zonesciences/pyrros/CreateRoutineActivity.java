package com.zonesciences.pyrros;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.zonesciences.pyrros.ItemTouchHelper.OnDragListener;
import com.zonesciences.pyrros.fragment.CreateRoutine.RoutineDetailsFragment;
import com.zonesciences.pyrros.fragment.CreateRoutine.WorkoutChangedListener;

public class CreateRoutineActivity extends BaseActivity {

    private static final String TAG = "CreateRoutineAcctivity";

    RoutineDetailsFragment mRoutineDetailsFragment;

    // View
    TextView mRoutineNameTextView;
    EditText mRoutineNameEditText;

    // Menu items / toolbar
    Toolbar mToolbar;
    MenuItem mEditRoutineMenuItem;
    MenuItem mSaveRoutineMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_routine);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_create_routine);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        mRoutineDetailsFragment = new RoutineDetailsFragment();
        mRoutineDetailsFragment.setOnWorkoutChangedListener(new WorkoutChangedListener() {
            @Override
            public void onWorkoutAdded() {
                Log.i(TAG, "Workout added");
            }

            @Override
            public void onWorkoutRemoved() {
                Log.i(TAG, "Workout removed");
            }

            @Override
            public void onWorkoutChanged() {
                Log.i(TAG, "Workout changed");
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.routine_fragment_container, mRoutineDetailsFragment);
        ft.commit();

        mRoutineNameTextView = (TextView) findViewById(R.id.toolbar_title_create_routine);
        mRoutineNameEditText = (EditText) findViewById(R.id.toolbar_title_edit_text);

        mRoutineNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editRoutineName();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_routine, menu);
        mEditRoutineMenuItem = menu.findItem(R.id.action_edit_routine);
        mSaveRoutineMenuItem = menu.findItem(R.id.action_save_routine);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_edit_routine:
                Log.i(TAG, "Edit workout");
                editRoutineName();
                return true;

            case R.id.action_save_routine:
                Log.i(TAG, "Save routine");
                saveRoutine();
            default:
                break;
        }
        return false;
    }

    public void editRoutineName(){
        mRoutineNameTextView.setVisibility(View.GONE);
        mRoutineNameEditText.setVisibility(View.VISIBLE);
        mRoutineNameEditText.requestFocus();
        mEditRoutineMenuItem.setVisible(false);
        mSaveRoutineMenuItem.setVisible(true);
    }

    public void saveRoutine(){
        String routineName = mRoutineNameEditText.getText().toString();
        mRoutineNameTextView.setText(routineName);
        mRoutineNameTextView.setVisibility(View.VISIBLE);
        mRoutineNameEditText.setVisibility(View.GONE);
        mEditRoutineMenuItem.setVisible(true);
        mSaveRoutineMenuItem.setVisible(false);

        // update routine object
        mRoutineDetailsFragment.getRoutine().setName(routineName);
    }
}
