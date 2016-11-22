package com.zonesciences.pyrros.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Peter on 11/11/2016.
 */
public class Utils {

    public static final String TAG = "Utils";

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

    public static String getClientTimeStamp(boolean includeTime){
        SimpleDateFormat df;
        if (includeTime) {
            df = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
        } else {
            df = new SimpleDateFormat("EEE, dd MMM");
        }
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    public static String getUid(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
