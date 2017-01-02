package com.zonesciences.pyrros.Dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zonesciences.pyrros.models.Exercise;

import java.util.List;

/**
 * Created by peter on 02/01/2017.
 */

public class ExerciseOptionsDialogFragment extends DialogFragment {

    private static final String ARG_EXERCISES = "ExerciseToEdit";

    static ExerciseOptionsDialogFragment newInstance(Exercise exercise){
        ExerciseOptionsDialogFragment fragment = new ExerciseOptionsDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_EXERCISES, exercise);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }
}
