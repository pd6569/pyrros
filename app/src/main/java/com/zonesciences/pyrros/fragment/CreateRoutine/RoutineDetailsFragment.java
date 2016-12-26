package com.zonesciences.pyrros.fragment.CreateRoutine;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zonesciences.pyrros.CreateRoutineActivity;
import com.zonesciences.pyrros.CreateWorkoutActivity;
import com.zonesciences.pyrros.ItemTouchHelper.OnDragListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.RoutineExercisesAdapter;
import com.zonesciences.pyrros.adapters.SortWorkoutAdapter;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoutineDetailsFragment extends Fragment {

    private static final String TAG = "RoutineDetailsFragment";

    // REQUEST CODE
    private static final int REQUEST_CREATE_WORKOUT = 1;

    Button mAddDayButton;
    EditText mWorkoutNameField;
    LinearLayout mLinearLayoutWorkoutContainer;
    TextView mNoExercisesTextView;

    // Recycler view
    RecyclerView mRecyclerView;
    RoutineExercisesAdapter mAdapter;


    // Data
    ArrayList<Exercise> mWorkoutExercises;

    // Maps
    Map<String, Integer> mLayoutMap = new HashMap<>();

    public RoutineDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_routine_details, container, false);

        mLinearLayoutWorkoutContainer = (LinearLayout) rootView.findViewById(R.id.linear_layout_routine_workout_container);
        mWorkoutNameField = (EditText) rootView.findViewById(R.id.edit_text_routine_name);


        mAddDayButton = (Button) rootView.findViewById(R.id.button_routine_add_day);
        mAddDayButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                View workoutView = LayoutInflater.from(getContext()).inflate(R.layout.cardview_routine_day, null);


                TextView title = (TextView) workoutView.findViewById(R.id.routine_workout_item_textview);
                String workoutTitle = mWorkoutNameField.getText().toString();
                title.setText(workoutTitle);

                if (mWorkoutExercises == null) {
                    mWorkoutExercises = new ArrayList<Exercise>();
                }

                /*mRecyclerView = (RecyclerView) workoutView.findViewById(R.id.recycler_routine_exercises);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setHasFixedSize(true);
                mAdapter = new RoutineExercisesAdapter(getContext(), mWorkoutExercises);
                mRecyclerView.setAdapter(mAdapter);*/


                mLinearLayoutWorkoutContainer.addView(workoutView);

                mNoExercisesTextView = (TextView) workoutView.findViewById(R.id.no_exercises_textview);
                mNoExercisesTextView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view){
                        Log.i(TAG, "No exercises, open exercise selection. Text clicked: " + view.getId());
                        Intent i = new Intent(getContext(), CreateWorkoutActivity.class);
                        i.putExtra(CreateWorkoutActivity.ARG_CREATE_WORKOUT_FOR_ROUTINE, true);
                        startActivityForResult(i, REQUEST_CREATE_WORKOUT);
                    }
                });
            }
        });



        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CREATE_WORKOUT){
            if (resultCode == RESULT_OK) {
                ArrayList<Exercise> workoutExercises = new ArrayList<>();
                workoutExercises = (ArrayList<Exercise>) data.getSerializableExtra(CreateWorkoutActivity.EXTRA_WORKOUT_EXERCISES);
                if (workoutExercises.size() > 0){
                    mWorkoutExercises = workoutExercises;
                    /*mAdapter = new RoutineExercisesAdapter(getContext(), mWorkoutExercises);
                    mRecyclerView.setAdapter(mAdapter);*/


                    Log.i(TAG, "mWorkoutExercises: " + mWorkoutExercises.size());
                }

                Log.i(TAG, "Request from create workout activity received successfully. Data received: " + workoutExercises.size());
            }
        }
    }




}
