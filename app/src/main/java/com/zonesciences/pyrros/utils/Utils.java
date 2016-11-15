package com.zonesciences.pyrros.utils;

import android.util.Log;

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

    public static String formatDate(String date) {
        String oldDate = date;
        String oldDateFormat = "yyyy-MM-dd, HH:mm:ss";

        String newDate = new String();
        String newDateFormat = "EEE, dd MMM";

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
}
