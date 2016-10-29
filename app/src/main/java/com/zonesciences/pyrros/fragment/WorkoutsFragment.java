package com.zonesciences.pyrros.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.zonesciences.pyrros.R;


public class WorkoutsFragment extends WorkoutsListFragment {

    public WorkoutsFragment() {}


    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_workouts_query]
        // Last 100 workouts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query userWorkoutsQuery = databaseReference.child("workouts")
                .limitToFirst(100);
        // [END recent_posts_query]

        return userWorkoutsQuery;
    }
}
