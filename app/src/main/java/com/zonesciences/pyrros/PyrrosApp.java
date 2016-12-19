package com.zonesciences.pyrros;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;

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

    //TODO: Change this to use ActivityLifeCycleCallbacks instead
    private BaseActivity mCurrentActivity = null;

    public BaseActivity getCurrentActivity(){
        return mCurrentActivity;
    }

    public void setCurrentActivity(BaseActivity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
}
