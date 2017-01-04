package com.zonesciences.pyrros.fragment.CreateWorkout;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.adapters.AddSetExerciseOptionsAdapter;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseOptionsFragment extends Fragment {

    private static final String TAG = "ExerciseOptionsFragment";

    // Arguments
    private static final String ARG_EXERCISE = "ExerciseToEdit";

    // Data
    Exercise mExercise;
    List<Integer> mSets;
    int mNumReps;

    // View
    EditText mNumRepsField;
    Button mIncreaseReps;
    Button mDecreaseReps;
    Button mAddSet;
    LinearLayout mRepTempoLinearLayout;
    LinearLayout mRestIntervalLinearLayout;

    // Recycler
    RecyclerView mRecyclerView;
    AddSetExerciseOptionsAdapter mAdapter;

    public static ExerciseOptionsFragment newInstance(Exercise exercise){
        ExerciseOptionsFragment fragment = new ExerciseOptionsFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_EXERCISE, exercise);
        fragment.setArguments(args);

        return fragment;
    }

    public ExerciseOptionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        mExercise = getArguments().getParcelable(ARG_EXERCISE);
        mSets = mExercise.getPrescribedReps();
        Log.i(TAG, "Exercise: " + mExercise.getName() + " Prescribed reps: " + mSets);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_exercise_options, container, false);

        // Recycler for adding sets
        mRecyclerView = (RecyclerView)  view.findViewById(R.id.dialog_exercise_options_add_sets_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        if (mSets == null){
            mSets = new ArrayList<Integer>();
        }

        mAdapter = new AddSetExerciseOptionsAdapter(getContext(), mSets);
        mRecyclerView.setAdapter(mAdapter);

        // Rep Tempo
        mRepTempoLinearLayout = (LinearLayout) view.findViewById(R.id.exercise_options_rep_tempo_setting);
        mRepTempoLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepTempoDialog();
            }
        });

        // Rest
        mRestIntervalLinearLayout = (LinearLayout) view.findViewById(R.id.exercise_options_rep_tempo_setting);
        mRestIntervalLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRestIntervalDialog();
            }
        });

        mNumRepsField = (EditText) view.findViewById(R.id.exercise_options_num_reps_edit_text);
        mDecreaseReps = (Button) view.findViewById(R.id.exercise_options_decrease_reps_button);
        mDecreaseReps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNumRepsField.getText().toString().isEmpty()) {
                    mNumReps = Integer.parseInt(mNumRepsField.getText().toString());
                } else {
                    mNumReps = 0;
                }
                if (mNumReps > 0){
                    mNumReps--;
                }
                mNumRepsField.setText("" + mNumReps);
            }
        });
        mIncreaseReps = (Button) view.findViewById(R.id.exercise_options_increase_reps_button);
        mIncreaseReps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNumRepsField.getText().toString().isEmpty()) {
                    mNumReps = Integer.parseInt(mNumRepsField.getText().toString());
                } else {
                    mNumReps = 0;
                }
                mNumReps++;
                mNumRepsField.setText("" + mNumReps);
            }
        });

        mAddSet = (Button) view.findViewById(R.id.exercise_options_add_set);
        mAddSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSets == null){
                    mSets = new ArrayList<Integer>();
                }
                mSets.add(Integer.parseInt(mNumRepsField.getText().toString()));
                mExercise.setPrescribedReps(mSets);
                mAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    public void showRepTempoDialog(){

    }

    public void showRestIntervalDialog(){

    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_workout_changes_complete){
            Log.i(TAG, "Workout changes complete");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}


/*
                    // Sets
                    mSets = mWorkoutExercises.get(getAdapterPosition()).getPrescribedReps();

                    if (mSets != null) {
                        Log.i(TAG, "mSets" + mSets.size());
                    }

                    Log.i(TAG, "Open dialog options");
                    LayoutInflater inflater = LayoutInflater.from(mActivity);
                    View dialogView = inflater.inflate(R.layout.fragment_exercise_options, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setView(dialogView);

                    // Find views
                    TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_exercise_options_exercise_title_textview);
                    dialogTitle.setText(mWorkoutExercises.get(getAdapterPosition()).getName());
                    ImageView closeDialog = (ImageView) dialogView.findViewById(R.id.dialog_exercise_options_close_imageview);

                    //RecyclerView
                    RecyclerView recyclerView = (RecyclerView)  dialogView.findViewById(R.id.dialog_exercise_options_add_sets_recycler);
                    recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
                    recyclerView.setHasFixedSize(true);
                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL);
                    recyclerView.addItemDecoration(dividerItemDecoration);
                    if (mSets == null){
                        mSets = new ArrayList<Integer>();
                    }
                    final AddSetExerciseOptionsAdapter adapter = new AddSetExerciseOptionsAdapter(mActivity, mSets);
                    recyclerView.setAdapter(adapter);


                    final EditText numRepsField = (EditText) dialogView.findViewById(R.id.exercise_options_num_reps_edit_text);
                    Button decreaseReps = (Button) dialogView.findViewById(R.id.exercise_options_decrease_reps_button);
                    decreaseReps.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!numRepsField.getText().toString().isEmpty()) {
                                mNumReps = Integer.parseInt(numRepsField.getText().toString());
                            } else {
                                mNumReps = 0;
                            }
                            if (mNumReps > 0){
                                mNumReps--;
                            }
                            numRepsField.setText("" + mNumReps);
                        }
                    });
                    Button increaseReps = (Button) dialogView.findViewById(R.id.exercise_options_increase_reps_button);
                    increaseReps.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!numRepsField.getText().toString().isEmpty()) {
                                mNumReps = Integer.parseInt(numRepsField.getText().toString());
                            } else {
                                mNumReps = 0;
                            }
                            mNumReps++;
                            numRepsField.setText("" + mNumReps);
                        }
                    });

                    Button addSet = (Button) dialogView.findViewById(R.id.exercise_options_add_set);
                    addSet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mSets == null){
                                mSets = new ArrayList<Integer>();
                            }
                            mSets.add(Integer.parseInt(numRepsField.getText().toString()));
                            adapter.notifyDataSetChanged();
                        }
                    });

                    builder
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialogBox, int id){
                                    Log.i(TAG, "OK");
                                    if (!adapter.getSets().isEmpty()){
                                        mWorkoutExercises.get(getAdapterPosition()).setPrescribedReps(adapter.getSets());

                                    }
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    Log.i(TAG, "onDismiss");
                                    if (!adapter.getSets().isEmpty()){
                                        mWorkoutExercises.get(getAdapterPosition()).setPrescribedReps(adapter.getSets());
                                    }
                                    mSets = null;
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialogBox, int id){
                                    dialogBox.cancel();
                                    mSets = null;
                                }
                            });
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    closeDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });*/
