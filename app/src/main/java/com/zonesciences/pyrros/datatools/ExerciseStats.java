package com.zonesciences.pyrros.datatools;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zonesciences.pyrros.models.Exercise;

import java.util.ArrayList;


/**
 * Created by Peter on 12/11/2016.
 */
public class ExerciseStats extends DataTools {


    public ExerciseStats(String userId, String exerciseKey) {
        super(userId, exerciseKey);
    }

    public ExerciseStats(String userId, String exerciseKey, ArrayList<Exercise> exercises) {
        super(userId, exerciseKey, exercises);
    }


}
