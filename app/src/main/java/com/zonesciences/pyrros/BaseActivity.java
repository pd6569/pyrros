package com.zonesciences.pyrros;

import android.app.ProgressDialog;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class BaseActivity extends AppCompatActivity {

    String mUnitSystem;

    @VisibleForTesting
    public ProgressDialog mProgressDialog;


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    public String getUid(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getUnitSystem() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("pref_unit", null);
    }

}
