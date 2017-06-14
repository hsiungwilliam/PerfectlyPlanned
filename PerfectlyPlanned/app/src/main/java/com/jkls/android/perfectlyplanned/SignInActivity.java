package com.jkls.android.perfectlyplanned;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;
//import com.google.firebase.quickstart.database.models.User;
/* TODO grab sign in from github
    On success call intent to switch to activity with contacts
    setup firebase database
    write report
 */

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    String user_name;
    String pass_word;
    String currentDateTime;
    private static final String TAG = "SignInActivity";
    public static final String PREFS_NAME = "MyExitFile";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mSignInButton;
    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences onExit = getSharedPreferences(PREFS_NAME, 0);
        String onExitPage = onExit.getString("onExit", "Signoff");
        user_name = onExit.getString("username", "jkls2713@gmail.com");
        pass_word = onExit.getString("password", "sticks27");
        currentDateTime = onExit.getString("currdatetime", "Mon Jan 01 08:00:00 EDT 2000");

        //If the last button clicked was signoff then the app needs to recollect all the information
        if(onExitPage.equals("Signoff")) {
            setContentView(R.layout.activity_sign_in);
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mAuth = FirebaseAuth.getInstance();

            // Views
            mEmailField = (EditText) findViewById(R.id.field_email);
            mPasswordField = (EditText) findViewById(R.id.field_password);
            mSignInButton = (Button) findViewById(R.id.button_sign_in);
            mSignUpButton = (Button) findViewById(R.id.button_sign_up);

            // Click listeners
            mSignInButton.setOnClickListener(this);
            mSignUpButton.setOnClickListener(this);

            //This allows the keyboard to disappear when clicking elsewhere on the screen
            mEmailField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus)
                        hideKeyboard(v);
                }
            });
            mPasswordField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus)
                        hideKeyboard(v);
                }
            });
        } else{
            //If the user did not signoff last, then the app still has all the saved information since the user is still signed in and can go straight to the home page
            new InitializationActivity(getBaseContext(), user_name, pass_word, currentDateTime, false).execute("");

            Context temp = getBaseContext();
            Intent in = new Intent(SignInActivity.this, HomePageActivity.class);
            in.putExtra("username", user_name);
            in.putExtra("password", pass_word);
            in.putExtra("datetime", currentDateTime);
            temp.startActivity(in);
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
          //  onAuthSuccess(mAuth.getCurrentUser());
        }
    }

    private void signIn() {
        Log.d(TAG, "signIn");
        if (!validateForm()) {
            return;
        }
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        user_name = email;
        email = email + "@PerfectlyPlanned.com";
        pass_word = password;
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                if (task.isSuccessful()) {
                    //Gets the current date and time to be used for comparison later
                    currentDateTime = new Date().toString();
                    onAuthSuccess(task.getResult().getUser(), currentDateTime);
                } else {
                    FirebaseAuthException e = (FirebaseAuthException)task.getException();
                    Toast.makeText(SignInActivity.this, "Failed Sign in: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(SignInActivity.this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signUp() {
        Log.d(TAG, "signUp");
        if (!validateForm()) {
            return;
        }
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        user_name = email;
        email = email + "@PerfectlyPlanned.com";
        pass_word = password;
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                if (task.isSuccessful()) {
                    //Gets the current date and time to be used for comparison later
                    String currentDateTime = new Date().toString();
                    onAuthSuccessSignUp(task.getResult().getUser(), currentDateTime);
                } else {
                    FirebaseAuthException e = (FirebaseAuthException)task.getException();
                    Toast.makeText(SignInActivity.this, "Failed Registration: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                   Toast.makeText(SignInActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //This is for users already registered, it taked you to the home page
    private void onAuthSuccess(FirebaseUser user, String currDateTime) {
        // Go to MainActivity
        Intent i = new Intent(SignInActivity.this, CheckEmail.class);
        i.putExtra("email", user.getEmail());

        new InitializationActivity(getBaseContext(), user_name, pass_word, currDateTime, false).execute("");
        //Toast.makeText(SignInActivity.this, "Checking Completed", Toast.LENGTH_SHORT).show();

        Context temp = getBaseContext();
        Intent in = new Intent(SignInActivity.this, HomePageActivity.class);
        in.putExtra("username", user_name);
        in.putExtra("password", pass_word);
        in.putExtra("datetime", currDateTime);
        temp.startActivity(in);
        finish();
    }

    //This is for users that need to sign up
    private void onAuthSuccessSignUp(FirebaseUser user, String currDateTime) {
        //String username = usernameFromEmail(user.getEmail());
        // Go to MainActivity
        Intent i = new Intent(SignInActivity.this, CheckEmail.class);
        i.putExtra("email", user.getEmail());

        Context temp = getBaseContext();
        Intent in = new Intent(SignInActivity.this, SignUpActivity.class);
        //in.putExtra("username", username);
        in.putExtra("username", user_name);
        in.putExtra("password", pass_word);
        in.putExtra("datetime", currDateTime);
        in.putExtra("count", 0);
        temp.startActivity(in);
        finish();
    }

    //this makes sure that the user typed in a valid username and password
    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);
        }
        return result;
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_sign_in) {
            signIn();
        } else if (i == R.id.button_sign_up) {
            signUp();
        }
    }
}
