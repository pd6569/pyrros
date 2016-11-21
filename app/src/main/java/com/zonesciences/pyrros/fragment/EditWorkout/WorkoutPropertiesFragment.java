package com.zonesciences.pyrros.fragment.EditWorkout;


import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.zonesciences.pyrros.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WorkoutPropertiesFragment extends Fragment {

    private static final String TAG = "WorkoutProperties";

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

    boolean mPropertiesChanged;

    //Data
    boolean mIsShared;
    String mDate;
    String mWorkoutName;

    //Data for user undo
    String mPreviousDate;
    String mPreviousWorkoutName;

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
        mWorkoutName = mWorkout.getName();

        mDateText = (TextView) view.findViewById(R.id.date_textview);
        mDateText.setText(Utils.formatDate(mDate, 1));
        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.setDate(Utils.convertToCalendarObj(mDate).get(Calendar.YEAR), Utils.convertToCalendarObj(mDate).get(Calendar.MONTH), Utils.convertToCalendarObj(mDate).get(Calendar.DAY_OF_MONTH));
                fragment.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        mPropertiesChanged = true;
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month, day);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
                        mPreviousDate = mDate;
                        mDate = sdf.format(cal.getTime());
                        mWorkout.setClientTimeStamp(mDate);
                        Log.i(TAG, "New date: " + mDate);
                        mDateText.setText(Utils.formatDate(mDate, 1));
                        Snackbar snackbar = Snackbar.make(view, "Workout date changed", Snackbar.LENGTH_LONG)
                                .setAction(R.string.action_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v){
                                        mDate = mPreviousDate;
                                        mWorkout.setClientTimeStamp(mDate);
                                        mDateText.setText(Utils.formatDate(mDate, 1));
                                    }
                                });
                        snackbar.show();
                    }
                });
                fragment.show(getChildFragmentManager(), "datePicker");
            }
        });

        mCreatorText = (TextView) view.findViewById(R.id.creator_textview);
        mCreatorText.setText(mWorkout.getCreator());

        mNameText = (TextView) view.findViewById(R.id.name_textview);
        mNameText.setText(mWorkoutName);
        mNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialogView = inflater.inflate(R.layout.dialog_user_input, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(dialogView);

                final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_textview);
                dialogTitle.setVisibility(View.GONE);
                final EditText userInputEditText = (EditText) dialogView.findViewById(R.id.user_input_edit_text);
                userInputEditText.setHint("Enter workout name");
                builder
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialogBox, int id){
                                mPropertiesChanged = true;
                                mPreviousWorkoutName = mWorkoutName;
                                mWorkoutName = userInputEditText.getText().toString();
                                mWorkout.setName(mWorkoutName);
                                mNameText.setText(mWorkoutName);

                                Snackbar snackbar = Snackbar.make(view, "Workout name changed", Snackbar.LENGTH_LONG)
                                        .setAction(R.string.action_undo, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v){
                                                mWorkoutName = mPreviousWorkoutName;
                                                mWorkout.setName(mWorkoutName);
                                                mNameText.setText(mWorkoutName);
                                            }
                                        });
                                snackbar.show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialogBox, int id){
                                dialogBox.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        mNumExercisesText = (TextView) view.findViewById(R.id.num_exercises_textview);
        mNumExercisesText.setText(Integer.toString(mWorkout.getNumExercises()));


        mSharedSwitch = (Switch) view.findViewById(R.id.shared_switch);
        mSharedSwitch.setChecked(mIsShared);
        mSharedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mPropertiesChanged = true;
                if (isChecked) {
                    mIsShared = true;
                    Snackbar snackbar = Snackbar.make(view, "Workout is shared", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    mIsShared = false;
                    Snackbar snackbar = Snackbar.make(view, "Workout is not shared", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                mWorkout.setShared(mIsShared);
            }
        });

        mUserCountText = (TextView) view.findViewById(R.id.user_count_textview);
        mUserCountText.setText(Integer.toString(mWorkout.getUserCount()));

    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause(). Update properties");
        if (mPropertiesChanged) {
            if (mWorkout != null) {
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/user-workouts/" + mUserId + "/" + mWorkoutKey, mWorkout);
                childUpdates.put("/workouts/" + mWorkoutKey, mWorkout);
                mDatabase.updateChildren(childUpdates);
            }
        }
    }

}
