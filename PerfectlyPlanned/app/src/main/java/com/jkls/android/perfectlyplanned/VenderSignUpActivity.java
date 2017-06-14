package com.jkls.android.perfectlyplanned;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import android.app.Activity;

import android.content.SharedPreferences;
import android.net.Uri;

import android.provider.Settings;
import android.support.annotation.NonNull;

import android.text.TextUtils;
import android.util.Log;

import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eande on 6/6/2017.
 */

public class VenderSignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "VenderSignUpActivity";
    private DatabaseReference mRef;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private Button mExitButton;
    private Button mContinueSignUp;
    private Context mContext;

    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPhoneField;

    String username1;
    String password1;
    String currDateTime1;
    String email1;
    String phone1;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        username1 = getIntent().getStringExtra("username");
        email1 = getIntent().getStringExtra("email");
        password1 = getIntent().getStringExtra("password");
        currDateTime1 = getIntent().getStringExtra("datetime");
        count = getIntent().getIntExtra("count", 0);
        mContext = getBaseContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_vender);
        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        // Views
        mExitButton = (Button) findViewById(R.id.button_exit);
        mContinueSignUp = (Button) findViewById(R.id.button_sign_up);
        mNameField = (EditText) findViewById(R.id.input_name);
        mEmailField = (EditText) findViewById(R.id.input_email);
        mPhoneField = (EditText) findViewById(R.id.input_phone_number);

        // Click listeners
        mContinueSignUp.setOnClickListener(this);
        mExitButton.setOnClickListener(this);

        //This allows the keyboard to disappear when clicking elsewhere on the screen
        mNameField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    hideKeyboard(v);
            }
        });
        mEmailField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    hideKeyboard(v);
            }
        });
        mPhoneField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    hideKeyboard(v);
            }
        });

        //This ensure the keyboard will not pop up when the page does, only when you click on editText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void exitButton(){
        Intent in = new Intent(VenderSignUpActivity.this, SignInActivity.class);
        in.putExtra("username", email1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    public void signUpButton(){
        updateSettings();
        Log.d(TAG, "Signing up new vent");
        Intent in = new Intent(VenderSignUpActivity.this, SignInActivity.class);
        in.putExtra("username", email1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    //this places all the saved information into the database
    private void updateSettings(){
        String currentDateTime = new Date().toString();
        Post post = new Post(username1, currDateTime1, email1, phone1);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("venders/" + username1, postValues);
        mRef.updateChildren(childUpdates);
    }

    @IgnoreExtraProperties
    public class Post {
        public String username;
        public String currentDateTime;
        public String phoneNumber;
        public String email;
        public int count;

        public Post() {// Default constructor required for calls to DataSnapshot.getValue(Post.class)
        }
        public Post(String username, String currentDateTime, String phoneNumber, String email) {
            this.username = username;
            this.currentDateTime = currentDateTime;
            this.phoneNumber = phoneNumber;
            this.email = email;
        }
        @Exclude
        public Map<String, Object> toMap() {
            System.out.println("inside map");
            HashMap<String, Object> result = new HashMap<>();
            result.put("username", username);
            result.put("currentDateTime", currentDateTime);
            result.put("phoneNumber", phoneNumber);
            result.put("email", email);
            return result;
        }
    }

    @IgnoreExtraProperties
    public class PostAccount {
        int num;
        public static final String PREFS_NAME2 = "MySignupEmailFile";
        public static final String PREFS_NAME3 = "MySignupPasswordFile";

        public PostAccount() {// Default constructor required for calls to DataSnapshot.getValue(Post.class)
        }
        public PostAccount(int num) {
            this.num = num;
        }
        @Exclude
        public Map<String, Object> toMap() {
            System.out.println("inside map part 2, i = " + num);
            HashMap<String, Object> result = new HashMap<>();

            String temp2 = "email" + Integer.toString(num);
            String temp3 = "password" + Integer.toString(num);

            SharedPreferences checkUsername = getSharedPreferences(PREFS_NAME2, 0);
            String getUsername = checkUsername.getString(temp2, "empty@gmail.com");
            SharedPreferences checkPassword = getSharedPreferences(PREFS_NAME3, 0);
            String getPassword = checkPassword.getString(temp3, "1234");
            //System.out.println("adding email: " + getUsername + " and password: " + getPassword + " to database");

            result.put("email", getUsername);
            result.put("password", getPassword);
            return result;
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_exit) {
            exitButton();
        } else if (i == R.id.button_signup){
            signUpButton();
        }
    }
}
