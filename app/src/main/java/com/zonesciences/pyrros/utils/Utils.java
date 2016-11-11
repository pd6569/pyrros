package com.zonesciences.pyrros.utils;

import android.util.Log;

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
}
