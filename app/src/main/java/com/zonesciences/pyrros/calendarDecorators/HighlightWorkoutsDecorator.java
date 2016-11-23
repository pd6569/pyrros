package com.zonesciences.pyrros.calendarDecorators;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Peter on 23/11/2016.
 */
public class HighlightWorkoutsDecorator implements DayViewDecorator {

    private static final String TAG = "HighlightDecorator";

    private final Calendar calendar = Calendar.getInstance();
    private List<Calendar> mWorkoutDates = new ArrayList<>();

    private final Drawable highlightDrawable;
    private static final int color = Color.parseColor("#feefae");

    public HighlightWorkoutsDecorator(List<String> workoutKeys){
        highlightDrawable = new ColorDrawable(color);
        createCalendarDates(workoutKeys);
    }

    private void createCalendarDates(List<String> workoutKeys) {
        for (int i = 0; i < workoutKeys.size(); i++) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            try {
                Date date = sdf.parse(workoutKeys.get(i));
                cal.setTime(date);
                mWorkoutDates.add(cal);
                Log.i(TAG, "Converted date key into calendar object: " + cal.getTime());
            } catch (ParseException e) {
                Log.i(TAG, "Failed to parse date: " + e);
            }
        }
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {

        boolean isWorkoutDay = false;

        for (int i = 0; i < mWorkoutDates.size(); i++){
            if (day.getCalendar().equals(mWorkoutDates.get(i))){
                Log.i(TAG, "Found a match for workout date: " + mWorkoutDates.get(i).getTime() + " calendarDay: " + day.getCalendar().getTime());
                isWorkoutDay = true;
            }
        }

        return isWorkoutDay;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(highlightDrawable);
    }
}
