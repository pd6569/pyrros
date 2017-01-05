package com.zonesciences.pyrros.utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.R;
import com.zonesciences.pyrros.WorkoutActivity;
import com.zonesciences.pyrros.fragment.CreateWorkout.CreateWorkoutFragment;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.User;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter on 11/11/2016.
 */
public class Utils {

    public static final String TAG = "Utils";

    // Units
    public static final String UNIT_METRIC = " kgs";
    public static final String UNIT_IMPERIAL = " lbs";

    // Date formats
    public static final String DATE_FORMAT_FULL = "yyyy-MM-dd, HH:mm:ss";
    public static final String DATE_FORMAT_1 = "EEE, dd MMM";
    public static final String DATE_FORMAT_2 = "EEE dd MMM, yyyy";
    public static final String DATE_FORMAT_DATE_ONLY = "yyyy-MM-dd";
    public static final String DATE_FORMAT_TIME_ONLY = "HH:mm";

    // Time
    public static final String MINUTES = "MinutesDisplay";
    public static final String SECONDS = "SecondsDisplay";

    public static String formatWeight(double weight){
        String s;
        if (weight == Math.floor(weight)){
            s = String.format("%.0f", weight);
        } else {
            s = String.format("%1.2f", weight);
            if (s.charAt(s.length()-1) == '0'){
                s = s.substring(0, s.length()-1);
                if (s.charAt(s.length()-1) == '0') {
                    s = s.substring(0, s.length() - 2);
                }
            }
        }
        return s;
    }

    public static String formatDate(String date, String oldFormat, int newFormat) {
        String oldDate = date;
        String oldDateFormat = oldFormat;
        String newDate = new String();
        String newDateFormat = new String();

        if (newFormat == 0) {
            newDateFormat = "EEE, dd MMM";
        } else if (newFormat == 1){
            newDateFormat = "EEE dd MMM, yyyy";
        } else if (newFormat == 2) {
            newDateFormat = "yyyy-MM-dd";
        } else if (newFormat == 3) {
            newDateFormat = "HH:mm";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(oldDateFormat);

        try {
            Date d = sdf.parse(oldDate);
            sdf.applyPattern(newDateFormat);
            newDate = sdf.format(d);
        } catch (ParseException e) {
            Log.i(TAG, "Failed to parse date: " + e);
        }
        return newDate;
    }

    public static Calendar convertToCalendarObj (String dateToConvert) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        try {
            Date date = sdf.parse(dateToConvert);
            cal.setTime(date);
        } catch (ParseException e) {
            Log.i(TAG, "Failed to parse date: " + e);
        }

        return cal;
    }

    public static Calendar convertDateStringToCalendar (String dateToConvert, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        try {
            Date date = sdf.parse(dateToConvert);
            cal.setTime(date);
        } catch (ParseException e) {
            Log.i(TAG, "Failed to parse date: " + e);
        }

        return cal;
    }

    public static String convertCalendarDateToString (Calendar calendar, String formatRequired){
        SimpleDateFormat sdf = new SimpleDateFormat(formatRequired);
        return sdf.format(calendar.getTime());
    }

    public static DateTime getDateTimeFromString(String date, String dateFormat){
        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat);
        DateTime dateTime = formatter.parseDateTime(date);
        return dateTime;
    }

    public static String getClientTimeStamp(boolean includeTime){
        /*SimpleDateFormat df;
        if (includeTime) {
            df = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
        } else {
            df = new SimpleDateFormat("EEE, dd MMM");
        }
        String date = df.format(Calendar.getInstance().getTime());*/

        String format;
        if (includeTime) {
            format = "yyyy-MM-dd, HH:mm:ss";
        } else {
            format = "EEE, dd MMM";
        }

        DateTime now = new DateTime();
        return now.toString(format);
    }


    public static String getUid(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    public static FirebaseDatabase getDatabase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        return database;
    }

    // Method for formatting countdown display
    public static Map<String, String> timeToDisplay (long timeRemainingMillis){

        Map<String, String> timeToDisplay = new HashMap<>();

        int timeRemainingSecs = (int) (timeRemainingMillis / 1000);

        int secondsRemaining;

        double minutesRemaining = timeRemainingSecs / 60;

        if (minutesRemaining >= 1){
            secondsRemaining = (timeRemainingSecs - ((int) minutesRemaining * 60));
        } else {
            secondsRemaining = timeRemainingSecs;
        }

        String minsToDisplay = new String();
        String secsToDisplay = new String();

        if (minutesRemaining < 10){
            minsToDisplay = 0 + Integer.toString((int) minutesRemaining);
        } else {
            minsToDisplay = Integer.toString((int) minutesRemaining);
        }
        if (secondsRemaining < 10){
            secsToDisplay = 0 + Integer.toString(secondsRemaining);
        } else {
            secsToDisplay = Integer.toString(secondsRemaining);
        }

        timeToDisplay.put(MINUTES, minsToDisplay);
        timeToDisplay.put(SECONDS, secsToDisplay);

        return timeToDisplay;

    }


    /// Format display of sets information.

    /**
     * Returns String which displays number of sets and reps for an exercise
     * @param sets List of reps for the sets to be formatted into string
     * @return String with formatted sets and reps
     */
    public static String generateSetsInfoString (List<Integer> sets) {

        int numSets = sets.size();
        String setsInfo;
        boolean sameReps = false;
        int repsLow = Collections.min(sets);
        int repsHigh = Collections.max(sets);
        if (repsLow == repsHigh) sameReps = true;
        if (sameReps) {
            setsInfo = numSets + " sets of " + repsHigh + " reps";
        } else {
            setsInfo = numSets + " sets of " + repsLow + "-" + repsHigh + " reps";
        }

        return setsInfo;
    }
}
