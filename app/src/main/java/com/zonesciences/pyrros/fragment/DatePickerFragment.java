package com.zonesciences.pyrros.fragment;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.zonesciences.pyrros.R;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment  {

    int mYear;
    int mMonth;
    int mDay;

    DatePickerDialog.OnDateSetListener mOnDateSetListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        return new DatePickerDialog(getActivity(), mOnDateSetListener , mYear, mMonth, mDay);
    }

    public void setDate(int year, int month, int day){
        mYear = year;
        mMonth = month;
        mDay = day;
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener dateSetListener){
        this.mOnDateSetListener = dateSetListener;
    }

}
