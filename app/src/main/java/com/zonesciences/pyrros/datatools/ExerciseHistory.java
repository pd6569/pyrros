package com.zonesciences.pyrros.datatools;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.fragment.ExerciseFragment;
import com.zonesciences.pyrros.models.Exercise;
import com.zonesciences.pyrros.models.Workout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter on 08/11/2016.
 */

public class ExerciseHistory extends DataTools{

    //Uses methods from base class DataTools to generate history using helper methods

    private static final String TAG = "ExerciseHistory.class";

    public ExerciseHistory(String userId, String exerciseKey) {
        super(userId, exerciseKey);
    }

    public ExerciseHistory(String userId, String exerciseKey, ArrayList<Exercise> exercises) {
        super(userId, exerciseKey, exercises);
    }

}
