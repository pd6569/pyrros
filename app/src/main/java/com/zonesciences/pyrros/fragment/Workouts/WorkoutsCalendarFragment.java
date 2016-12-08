package com.zonesciences.pyrros.fragment.Workouts;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.zonesciences.pyrros.CreateWorkoutActivity;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.calendarDecorators.HighlightWorkoutsDecorator;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;
import com.zonesciences.pyrros.utils.Utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutsCalendarFragment extends Fragment {

    private static final String TAG = "WorkoutsCalendar";

    private static final String ARG_WORKOUT_EXERCISES_MAP = "WorkoutExercisesMap";
    private static final String WORKOUT_EXERCISES = "Workout Exercises";
    private static final String WORKOUT_ID = "Workout ID";
    private static final String WORKOUT_EXERCISES_OBJECTS = "WorkoutExerciseObjects";
    private static final String ARG_WORKOUT_DATE = "WorkoutDate";

    private DatabaseReference mDatabase;

    MaterialCalendarView mMaterialCalendarView;

    // Bottomsheet View
    private View mBottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private RelativeLayout mTitleContainer;
    private TextView mTitle;
    private ImageView mLaunchWorkoutImage;

    // Alert dialog for multiple workouts on same day
    AlertDialog mAlertDialog;


    // Data
    Map<String, List<Exercise>> mWorkoutExercisesMap;
    Map<String, Map<String, String>> mWorkoutDatesMap = new HashMap<>();
    List<Exercise> mExercises = new ArrayList<>();

    // Units
    private String mUnit;
    private double mConversionMultiple;

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

        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_unit", null).equals("metric")){
            mUnit = " kgs";
            mConversionMultiple = 1.0;
        } else {
            mUnit = " lbs";
            mConversionMultiple = 2.20462;
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_workouts_calendar, container, false);

        mMaterialCalendarView = (MaterialCalendarView) rootView.findViewById(R.id.material_calendar_view);
        mMaterialCalendarView.setSelectedDate(Calendar.getInstance().getTime());

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

                List workoutKeys = new ArrayList(mWorkoutDatesMap.keySet());
                mMaterialCalendarView.addDecorator(new HighlightWorkoutsDecorator(getContext(), workoutKeys));


                Log.i(TAG, "WorkoutDatesMap created, size: " + mWorkoutDatesMap.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet_calendar);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setPeekHeight(0);

        mTitleContainer = (RelativeLayout) rootView.findViewById(R.id.bottom_sheet_calendar_title_container);
        mTitleContainer.setVisibility(View.VISIBLE);

        mTitle = (TextView) rootView.findViewById(R.id.bottom_sheet_calendar_title);
        mLaunchWorkoutImage = (ImageView) rootView.findViewById(R.id.bottom_sheet_calendar_go_to_workout);

        mMaterialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay calendarDay, boolean selected) {
                final Calendar cal = calendarDay.getCalendar();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String date = sdf.format(cal.getTime());
                Log.i (TAG, "Date formatted: " + date);

                //Get exercises times map (maps time to workout ID - important if more than one workout performed on a single day)
                Map<String,String> timeMap = mWorkoutDatesMap.get(date);

                if (timeMap != null){
                    List<String> workoutTimes = new ArrayList<String>(timeMap.keySet());
                    Log.i(TAG, workoutTimes.size() + " workouts found for this date");
                    if (workoutTimes.size() > 1){
                        List<CharSequence> timeKeys = new ArrayList<>();
                        List<String> workoutKeys = new ArrayList<String>();

                        for (String time : workoutTimes){
                            Log.i(TAG, "Workout at: " + time);
                            timeKeys.add(time);
                            String workoutKey = timeMap.get(time);
                            workoutKeys.add(workoutKey);

                        }
                        CharSequence[] timeOptions = timeKeys.toArray(new CharSequence[timeKeys.size()]);

                        createAlertDialog(timeOptions, workoutKeys, date, rootView);

                    } else {

                        final String workoutKey = timeMap.get(workoutTimes.get(0));

                        createBottomSheet(workoutKey, date, "", rootView);

                    }
                } else {
                    Log.i(TAG, "No workout found for this date");
                    Snackbar snackbar = Snackbar.make(rootView, "No workouts on this date", Snackbar.LENGTH_SHORT)
                            .setAction(R.string.action_create_workout, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String date = Utils.convertCalendarDateToString(cal, Utils.DATE_FORMAT_FULL);
                                    Intent i = new Intent(getActivity(), CreateWorkoutActivity.class);
                                    i.putExtra(ARG_WORKOUT_DATE, date);
                                    getActivity().startActivity(i);
                                }
                            });
                    snackbar.show();
                }

            }
        });



        return rootView;
    }

    private void createAlertDialog(final CharSequence[] timeOptions, final List<String> workoutKeys, final String date, final View rootView) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Multiple workouts found, please choose");
        builder.setSingleChoiceItems(timeOptions, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int item) {
                String key = workoutKeys.get(item);
                createBottomSheet(key, date, (String) timeOptions[item], rootView);
                mAlertDialog.dismiss();
            }
        });

        mAlertDialog = builder.create();
        mAlertDialog.show();

    }

    private void createBottomSheet(final String workoutKey, final String date, String time, final View rootView){

        String workoutTime;
        if (time.isEmpty()){
            workoutTime = "";
        } else {
            workoutTime = " at " + time;
        }
        mExercises = mWorkoutExercisesMap.get(workoutKey);

        Collections.sort(mExercises);

        int numExercises = mExercises.size();

        mTitle.setText(Utils.formatDate(date, "yyyy-MM-dd", 1) + workoutTime);

        mLaunchWorkoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<CharSequence> exerciseKeys = new ArrayList<>();
                for (Exercise e : mExercises) {
                    exerciseKeys.add(e.getName());
                }

                Bundle extras = new Bundle();
                extras.putSerializable(WORKOUT_EXERCISES, (ArrayList) exerciseKeys);
                extras.putString(WORKOUT_ID, workoutKey);
                extras.putSerializable(WORKOUT_EXERCISES_OBJECTS, (ArrayList) mExercises);
                Intent i = new Intent (getContext(), WorkoutActivity.class);
                i.putExtras(extras);
                startActivity(i);
            }
        });

        LinearLayout exercisesContainer = (LinearLayout) rootView.findViewById(R.id.workout_exercises_container);
        exercisesContainer.removeAllViews();

        for (int i = 0; i < numExercises; i++) {

            Exercise currentExercise = mExercises.get(i);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_workout_exercises, null);
            TextView exerciseText = (TextView) view.findViewById(R.id.workout_exercise_name);
            LinearLayout setsContainer = (LinearLayout) view.findViewById(R.id.workout_sets_container);
            exerciseText.setText(currentExercise.getName());

            if (currentExercise.getSets() == 0){
                TextView noSets = (TextView) view.findViewById(R.id.workout_no_sets);
                noSets.setVisibility(View.VISIBLE);
            }

            for (int j = 0; j < currentExercise.getSets(); j++){

                Log.i(TAG, "GETTING SETS FOR: currentExercise = " + currentExercise.getName());
                View setsView = LayoutInflater.from(getContext()).inflate(R.layout.item_sets, null);
                TextView setNumber = (TextView) setsView.findViewById(R.id.textview_set_number);
                TextView setWeight = (TextView) setsView.findViewById(R.id.textview_set_weight);
                TextView setReps = (TextView) setsView.findViewById(R.id.textview_set_reps);

                setNumber.setVisibility(View.GONE);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f);
                double weight = currentExercise.getWeight().get(j) * mConversionMultiple;
                String s = Utils.formatWeight(weight);
                setWeight.setText(s + mUnit);
                setWeight.setLayoutParams(params);
                setReps.setText("" + currentExercise.getReps().get(j) + " reps");
                setReps.setLayoutParams(params);

                setsContainer.addView(setsView);

            }

            exercisesContainer.addView(view);
        }

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

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
