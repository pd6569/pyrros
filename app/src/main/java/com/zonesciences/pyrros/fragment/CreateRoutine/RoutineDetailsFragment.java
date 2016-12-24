package com.zonesciences.pyrros.fragment.CreateRoutine;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zonesciences.pyrros.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoutineDetailsFragment extends Fragment {

    private static final String TAG = "RoutineDetailsFragment";

    Button mAddDayButton;
    EditText mWorkoutNameField;
    LinearLayout mLinearLayoutWorkoutContainer;


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
                title.setText(mWorkoutNameField.getText().toString());

                RecyclerView recyclerView = new RecyclerView(getContext(), null);
                int id = View.generateViewId();
                Log.i(TAG, "recycler id: " + id);
                recyclerView.setId(id);
                recyclerView.setHasFixedSize(true);
                mLinearLayoutWorkoutContainer.addView(recyclerView);

                mLinearLayoutWorkoutContainer.addView(workoutView);
            }
        });

        return rootView;
    }

}
