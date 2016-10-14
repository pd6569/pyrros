package com.zonesciences.pyrros;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private TextView welcomeTextView;
    private Button logoutButton;

    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialise Firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            loadLoginView();
        } else{
            mUserId = mFirebaseUser.getUid();
            Log.d("FUCK YOU", "LOGGED IN SUCCESSFULLY RUNNING MAIN ACTIVITY");
            welcomeTextView = (TextView) findViewById(R.id.welcome_user);
            welcomeTextView.setText("Welcome " + mUserId);
        }

        logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseAuth.signOut();
                loadLoginView();
            }
        });

    }


    public void loadLoginView(){
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
