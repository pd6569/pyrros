package com.zonesciences.pyrros;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.zonesciences.pyrros.models.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "LoginActivity";

    private EditText mEmailField;
    private EditText mPasswordField;


    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    //Listener which implemented onAuthStateChanged
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Views
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);




        //Create new listener. Check whether user is signed in or out and act accordingly
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }

        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void createAccount(String email, String password){
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.

                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            mEmailField.setText("");
                            mPasswordField.setText("");
                            hideProgressDialog();
                        } else {
                            Log.d(TAG, "createUserWithEmail:onComplete" + task.isSuccessful());
                            hideProgressDialog();
                            Toast.makeText(LoginActivity.this, R.string.account_created, Toast.LENGTH_SHORT).show();
                            onAuthSuccess(task.getResult().getUser());

                        }

                    }


                });
    }



    public void signIn(String email, String password){

        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                // if sign in fails, display a message to the user. If sign in succeeds the auth state listener will be notified and logic to hanld the signed in user can be handled in the listener.
                if (!task.isSuccessful()){
                    Log.w(TAG, "signInWithEmail:failed", task.getException());

                    hideProgressDialog();
                    Toast.makeText(LoginActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                    mEmailField.setText("");
                    mPasswordField.setText("");

                } else {
                    Log.d(TAG, "signInWithEmail :onComplete:" + task.isSuccessful());
                    hideProgressDialog();
                    Toast.makeText(LoginActivity.this, R.string.auth_successful, Toast.LENGTH_SHORT).show();
                    onAuthSuccess(task.getResult().getUser());
                }

            }
        });
    }


    private void onAuthSuccess (FirebaseUser user){
        String username = usernameFromEmail(user.getEmail());

        //Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        // Go to MainActivity
        loadApp();
        finish();

    }


    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    //Write user to database
    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + userId, user);
        childUpdates.put("/timestamps/users/" + userId + "/created", ServerValue.TIMESTAMP);

        mDatabase.updateChildren(childUpdates);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }



    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } /*else if (i == R.id.sign_out_button) {
            signOut();
        }*/
    }

    public void loadApp(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

}
