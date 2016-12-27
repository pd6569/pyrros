package com.zonesciences.pyrros.fragment.CreateRoutine;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zonesciences.pyrros.CreateWorkoutActivity;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */

    //TODO: BUG - when one exercise remaining, cannot remove when launching CreateWorkoutActivity
    //TODO: BUG - when launching directly into sort workout fragment, "done" arrow does not show on moving/deleting exercises

public class RoutineDetailsFragment extends Fragment {

    private static final String TAG = "RoutineDetailsFragment";

    // REQUEST CODE
    private static final int REQUEST_CREATE_WORKOUT = 1;

    // View
    AutoCompleteTextView mWorkoutNameField;
    Button mAddWorkoutButton;
    LinearLayout mLinearLayoutWorkoutContainer;

    // Maps
    /*Map<Integer,  String> mWorkoutViewNameMap = new HashMap<>();*/
    Map<Integer, View> mWorkoutViewMap = new HashMap<>();
    Map<Integer, ArrayList<Exercise>> mWorkoutExercisesMap = new HashMap<>();

    // Track views to update by id
    int mWorkoutCardToUpdate;

    // Data
    ArrayAdapter mAutoCompleteAdapter;
    String[] mWorkoutNameSuggestions;

    // Listener
    WorkoutChangedListener mWorkoutChangedListener;

    public RoutineDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        mWorkoutNameSuggestions = getResources().getStringArray(R.array.workout_name_suggestions);
        mAutoCompleteAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, mWorkoutNameSuggestions);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_routine_details, container, false);

        Log.i(TAG, "onCreateView");


        mWorkoutNameField = (AutoCompleteTextView) rootView.findViewById(R.id.autocomplete_field_workout_name);
        mWorkoutNameField.setAdapter(mAutoCompleteAdapter);
        mWorkoutNameField.setThreshold(1);

        mLinearLayoutWorkoutContainer = (LinearLayout) rootView.findViewById(R.id.linear_layout_routine_workout_container);

        mAddWorkoutButton = (Button) rootView.findViewById(R.id.button_routine_add_workout);
        mAddWorkoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                addWorkout();
            }
        });

        return rootView;
    }

    public void addWorkout(){

        // generate unique id for each workoutView
        final View workoutView = LayoutInflater.from(getContext()).inflate(R.layout.item_routine_workout, null);
        final int workoutViewId = View.generateViewId();
        workoutView.setId(workoutViewId);

        String workoutTitle = mWorkoutNameField.getText().toString();
        TextView title = (TextView) workoutView.findViewById(R.id.routine_workout_item_textview);
        mWorkoutNameField.setText("");
        title.setText(workoutTitle);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Workout clicked. CardView ID: " + workoutView.getId());
            }
        });

        ImageView deleteWorkout = (ImageView) workoutView.findViewById(R.id.routine_workout_delete_imageview);
        deleteWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Delete workout card id: " + workoutViewId);
                mLinearLayoutWorkoutContainer.removeView(workoutView);
                mWorkoutExercisesMap.remove(workoutViewId);

                // Notify activity
                mWorkoutChangedListener.onWorkoutRemoved();
            }
        });

                /*mWorkoutViewNameMap.put(workoutId, workoutTitle);*/

        TextView noExercisesTextView = (TextView) workoutView.findViewById(R.id.no_exercises_textview);
        noExercisesTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view){

                // when returning to this activity, can update correct workoutView card
                mWorkoutCardToUpdate = workoutViewId;
                Intent i = new Intent(getContext(), CreateWorkoutActivity.class);
                i.putExtra(CreateWorkoutActivity.ARG_CREATE_WORKOUT_FOR_ROUTINE, true);
                startActivityForResult(i, REQUEST_CREATE_WORKOUT);
            }
        });

        // add new workout view to container at top of layout
        mLinearLayoutWorkoutContainer.addView(workoutView, 0);

        // track specific workoutView by workoutViewId
        mWorkoutViewMap.put(workoutViewId, workoutView);

        // Notify activity
        mWorkoutChangedListener.onWorkoutAdded();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CREATE_WORKOUT){
            if (resultCode == RESULT_OK) {
                final ArrayList<Exercise> workoutExercises = (ArrayList<Exercise>) data.getSerializableExtra(CreateWorkoutActivity.EXTRA_WORKOUT_EXERCISES);
                if (workoutExercises.size() > 0){
                    final int cardId = mWorkoutCardToUpdate;
                    if (mWorkoutExercisesMap.containsKey(cardId)){
                        mWorkoutExercisesMap.remove(cardId);
                    }
                    mWorkoutExercisesMap.put(cardId, workoutExercises);
                    View viewToUpdate = mWorkoutViewMap.get(mWorkoutCardToUpdate);
                    LinearLayout exercisesContainer = (LinearLayout) viewToUpdate.findViewById(R.id.linear_layout_routine_workout_exercises_container);
                    exercisesContainer.removeAllViews();
                    for (int i = 0; i < workoutExercises.size(); i++){
                        TextView exercise = new TextView(getContext());
                        exercise.setText(workoutExercises.get(i).getName());
                        exercisesContainer.addView(exercise);
                    }

                    exercisesContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i(TAG, "card layout clicked. card id: " + cardId);
                            mWorkoutCardToUpdate = cardId;
                            Intent i = new Intent(getContext(), CreateWorkoutActivity.class);
                            i.putExtra(CreateWorkoutActivity.ARG_CREATE_WORKOUT_FOR_ROUTINE, true);
                            i.putExtra(CreateWorkoutActivity.EXTRA_WORKOUT_EXERCISES, workoutExercises);
                            startActivityForResult(i, REQUEST_CREATE_WORKOUT);
                            for (Exercise e : workoutExercises){
                                Log.i(TAG, "Passing exercises back to create workoutactivity " + e.getName());
                            }
                        }
                    });

                    viewToUpdate.findViewById(R.id.no_exercises_textview).setVisibility(View.GONE);

                }

                Log.i(TAG, "Request from create workout activity received successfully. Data received: " + workoutExercises.size());
            }
        }
    }

    // Set listener
    public void setOnWorkoutChangedListener(WorkoutChangedListener listener){
        this.mWorkoutChangedListener = listener;
    }


}
