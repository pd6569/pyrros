package com.zonesciences.pyrros.fragment;


import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutsCalendarFragment extends Fragment {

    private static final String TAG = "WorkoutsCalendar";

    private static final String ARG_WORKOUT_EXERCISES_MAP = "WorkoutExercisesMap";

    private DatabaseReference mDatabase;

    CalendarView mCalendarView;

    // Bottomsheet View
    private View mBottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private TextView mTitle;


    // Data
    Map<String, List<Exercise>> mWorkoutExercisesMap;
    Map<String, Map<String, String>> mWorkoutDatesMap = new HashMap<>();

    // Listener
    private OnSwitchToListViewListener mListViewListener;

    public WorkoutsCalendarFragment() {
        // Required empty public constructor
    }

    public static WorkoutsCalendarFragment newInstance(OnSwitchToListViewListener listener, Map<String, List<Exercise>> workoutExercisesMap) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_WORKOUT_EXERCISES_MAP, (Serializable) workoutExercisesMap);
        WorkoutsCalendarFragment fragment = new WorkoutsCalendarFragment();
        fragment.setArguments(args);
        fragment.setOnSwitchToListViewListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mWorkoutExercisesMap = (Map<String, List<Exercise>>) bundle.getSerializable(ARG_WORKOUT_EXERCISES_MAP);

        Log.i(TAG, "mWorkoutExerciseMap has been received, size: " + mWorkoutExercisesMap.size());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user-workouts").child(Utils.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot workout : dataSnapshot.getChildren()) {
                    Workout w = workout.getValue(Workout.class);
                    String dateKey = Utils.formatDate(w.getClientTimeStamp(), "yyyy-MM-dd, HH:mm:ss", 2);
                    String timeKey = Utils.formatDate(w.getClientTimeStamp(), "yyyy-MM-dd, HH:mm:ss", 3);

                    if (mWorkoutDatesMap.containsKey(dateKey)) {
                        mWorkoutDatesMap.get(dateKey).put(timeKey, workout.getKey());
                    } else {
                        Map<String, String> timeMap = new HashMap<>();
                        timeMap.put(timeKey, workout.getKey());
                        mWorkoutDatesMap.put(dateKey, timeMap);
                    }
                }
                Log.i(TAG, "WorkoutDatesMap created, size: " + mWorkoutDatesMap.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_workouts_calendar, container, false);

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet_calendar);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setPeekHeight(0);

        mTitle = (TextView) rootView.findViewById(R.id.bottom_sheet_calendar_title);

        mCalendarView = (CalendarView) rootView.findViewById(R.id.calendar_view);
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int day) {
                Log.i(TAG, "Date changed, format: " + year + "-" + month + "-" + day);
                //Convert selected date to same format as key for map
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String date = sdf.format(cal.getTime());
                Log.i (TAG, "Date formatted: " + date);

                Map<String,String> map = mWorkoutDatesMap.get(date);

                if (map != null){
                    List<String> workoutTimes = new ArrayList<String>(map.keySet());
                    Log.i(TAG, workoutTimes.size() + " workouts found for this date");
                    if (workoutTimes.size() > 1){
                        for (String time : workoutTimes){
                            Log.i(TAG, "Workout at: " + time);
                        }
                    } else {
                        Log.i(TAG, "Only 1 workout performed on this day: " + workoutTimes.get(0));
                        mTitle.setText(Utils.formatDate(date, "yyyy-MM-dd", 1));
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                } else {
                    Log.i(TAG, "No workout found for this date");
                    Snackbar snackbar = Snackbar.make(rootView, "No workouts on this date", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
        });

        ;


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_workouts_calendar_view, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        switch(i){
            case R.id.action_list_view:
                mListViewListener.displayListView();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public interface OnSwitchToListViewListener {
        void displayListView();
    }

    public void setOnSwitchToListViewListener (OnSwitchToListViewListener listener){
        this.mListViewListener = listener;
    }

}
