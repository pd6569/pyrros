package com.zonesciences.pyrros.fragment.EditWorkout;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.fragment.DatePickerFragment;
import com.zonesciences.pyrros.models.Workout;

public class WorkoutPropertiesFragment extends Fragment {

    private static final String ARG_USER_ID = "UserId";
    private static final String ARG_WORKOUT_KEY = "WorkoutKey";

    private DatabaseReference mDatabase;

    private Workout mWorkout;
    private String mUserId;
    private String mWorkoutKey;

    // Views
    TextView mDateText;
    TextView mCreatorText;
    TextView mNameText;
    TextView mNumExercisesText;
    Switch mSharedSwitch;
    TextView mUserCountText;

    //Data
    boolean mIsShared;
    String mDate;

    public static WorkoutPropertiesFragment newInstance(String userId, String workoutKey){
        Bundle args = new Bundle();

        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_WORKOUT_KEY, workoutKey);
        WorkoutPropertiesFragment fragment = new WorkoutPropertiesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public WorkoutPropertiesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mUserId = bundle.getString(ARG_USER_ID);
        mWorkoutKey = bundle.getString(ARG_WORKOUT_KEY);

        mDatabase = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_workout_properties, container, false);

        if (mWorkout == null){
            mDatabase.child("user-workouts").child(mUserId).child(mWorkoutKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mWorkout = dataSnapshot.getValue(Workout.class);
                    setWorkoutDataView(rootView);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            setWorkoutDataView(rootView);
        }

        return rootView;
    }

    public void setWorkoutDataView(final View view){

        mIsShared = mWorkout.getShared();
        mDate = mWorkout.getClientTimeStamp();

        mDateText = (TextView) view.findViewById(R.id.date_textview);
        mDateText.setText(mDate);
        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment fragment = new DatePickerFragment();
                fragment.show(getChildFragmentManager(), "datePicker");
            }
        });

        mCreatorText = (TextView) view.findViewById(R.id.creator_textview);
        mCreatorText.setText(mWorkout.getCreator());

        mNameText = (TextView) view.findViewById(R.id.name_textview);
        mNameText.setText(mWorkout.getName());

        mNumExercisesText = (TextView) view.findViewById(R.id.num_exercises_textview);
        mNumExercisesText.setText(Integer.toString(mWorkout.getNumExercises()));


        mSharedSwitch = (Switch) view.findViewById(R.id.shared_switch);
        mSharedSwitch.setChecked(mIsShared);
        mSharedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mIsShared = true;
                    Snackbar snackbar = Snackbar.make(view, "Workout is shared", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    mIsShared = false;
                    Snackbar snackbar = Snackbar.make(view, "Workout is not shared", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

        mUserCountText = (TextView) view.findViewById(R.id.user_count_textview);
        mUserCountText.setText(Integer.toString(mWorkout.getUserCount()));

    }

}
