package com.zonesciences.pyrros;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Peter on 19/10/2016.
 */
public class PyrrosApp extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        /* Enable disk persistence */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
