package com.jkls.android.perfectlyplanned;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by eande on 5/26/2017.
 */

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "HomePageActivity";
    public static final String PREFS_NAME = "MyExitFile";
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private Button mCheckButton;
    private Button mSettingsButton;
    private Button mAccountsButton;
    private Button mExitButton;
    private Button mSignOffButton;

    private Context mContext;
    String username1;
    String password1;
    String currDateTime1;

    public void HomePageActivity(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        username1 = getIntent().getStringExtra("username");
        password1 = getIntent().getStringExtra("password");
        currDateTime1 = getIntent().getStringExtra("datetime");
        mContext = getBaseContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Views
        mCheckButton = (Button) findViewById(R.id.button_check);
        mExitButton = (Button) findViewById(R.id.button_exit);
        mSettingsButton = (Button) findViewById(R.id.button_settings);
        mAccountsButton = (Button) findViewById(R.id.button_accounts);
        mSignOffButton = (Button) findViewById(R.id.button_signoff);

        // Click listeners
        mCheckButton.setOnClickListener(this);
        mExitButton.setOnClickListener(this);
        mSettingsButton.setOnClickListener(this);
        mAccountsButton.setOnClickListener(this);
        mSignOffButton.setOnClickListener(this);
    }

    //Handles when the settings button is clicked
    public void showSettingsButton(){
        Log.d(TAG, "settings");
        System.out.println(username1 + "home page");
        Intent in = new Intent(HomePageActivity.this, SettingActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    //Handles when the check button is clicked
    public void showCheckButton(){
        Log.d(TAG, "check");
        new CheckEmail(mContext, username1, password1, currDateTime1).execute("");
    }

    //Handles when the accounts button is clicked
    public void showAccountsButton(){
        Log.d(TAG, "accounts");
        finish();
    }

    //Handles when the exit button is clicked
    public void showExitButton(){
        Log.d(TAG, "exit");
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().commit();
        editor.putString("onExit", "Exit");
        editor.putString("username", username1);
        editor.putString("password", password1);
        editor.putString("currdatetime", currDateTime1);
        editor.commit();
        finish();
    }

    //Handles when the sign off button is clicked
    public void showSignOffButton(){
        Log.d(TAG, "signoff");
        //Im not sure if this is the best way but this lets the system know the user has signed off
        //
        //Also need to figure out a way to stop any alarms that are turned on here
        //
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().commit();
        editor.putString("onExit", "Signoff");
        editor.commit();
        finish();
        System.exit(0);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_update) {
            showCheckButton();
        } else if (i == R.id.button_exit) {
            showExitButton();
        } else if (i == R.id.button_settings) {
            showSettingsButton();
        } else if (i == R.id.button_accounts) {
            showAccountsButton();
        } else if (i == R.id.button_signoff) {
            showSignOffButton();
        }
    }
}
