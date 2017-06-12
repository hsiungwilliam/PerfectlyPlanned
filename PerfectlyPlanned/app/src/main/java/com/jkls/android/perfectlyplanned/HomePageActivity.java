package com.jkls.android.perfectlyplanned;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

/**
 * Created by eande on 5/26/2017.
 */

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "HomePageActivity";
    public static final String PREFS_NAME = "MyExitFile";
    public static final String PREFS_NAME2 = "MyCountFile";
    public static final String PREFS_NAME1 = "MyCheckOptionsFile";
    public static final String PREFS_NAME3 = "MyEmailFrequencyFile";
    public static final String PREFS_NAME4 = "MyTimeFile";
    public static final String PREFS_NAME5 = "MyBlackListFile";
    public static final String PREFS_NAME6 = "MyWhiteListFile";
    public static final String PREFS_NAME7 = "MyTempBListFile";
    public static final String PREFS_NAME8 = "MyTempWListFile";
    public static final String PREFS_NAME9 = "MyBListFile";
    public static final String PREFS_NAME10 = "MyWListFile";

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
    String emailOptions;
    int count;

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

        //This will pop up a window asking if its ok if the app accesses your text messages
        ActivityCompat.requestPermissions(HomePageActivity.this, new String[]{android.Manifest.permission.READ_SMS}, 1);
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

    public void getCount(){
        SharedPreferences emailCount = getSharedPreferences(PREFS_NAME2, 0);
        String value = emailCount.getString("count", "0");
        count = Integer.parseInt(value);
    }

    public void getEmailOptions(){
        SharedPreferences emailCheckOptions = getSharedPreferences(PREFS_NAME1, 0);
        emailOptions = emailCheckOptions.getString("checkOptions", "Both");
    }

    public void updateCurrentDateTime(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String currentDateTime = new Date().toString();
        DatabaseReference myRef = database.child("users/" + username1);
        myRef.child("currentDateTime").setValue(currentDateTime);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME4, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().commit();
        editor.putString("time", currentDateTime);
        editor.commit();
    }

    //Handles when the check button is clicked
    public void showCheckButton(){
        Log.d(TAG, "check");
        getCount();
        getEmailOptions();
        System.out.println("inside of check button, count: " + count + " email options: " + emailOptions);

        if (emailOptions.equals("Text"))
            new CheckText(mContext, username1, password1, currDateTime1).execute("");
        else if(emailOptions.equals("Email") || emailOptions.equals("Both")) {
            //This will get the values from the database for the username and password
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            DatabaseReference myRef = database.child("users/" + username1);
            myRef.child("/accounts").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("count value: " + count);
                    for (int i = 0; i < count; i++) {
                        String temp = "/account" + Integer.toString(i);
                        String e = dataSnapshot.child(temp + "/email").getValue().toString();
                        String p = dataSnapshot.child(temp + "/password").getValue().toString();
                        System.out.println("checking email: " + e + " and password: " + p);
                        new CheckEmail(mContext, e, p, currDateTime1).execute("");
                    }
                    if(emailOptions.equals("Both"))
                        new CheckText(mContext, username1, password1, currDateTime1).execute("");
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        //updateCurrentDateTime();
    }

    //Handles when the accounts button is clicked
    public void showAccountsButton(){
        Log.d(TAG, "accounts");
        Intent in = new Intent(HomePageActivity.this, UserAccountsActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
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
        try {
            Log.d(TAG, "signoff");
            updateAccessVariable();
            //Im not sure if this is the best way but this lets the system know the user has signed off and turns off any alarms
            SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString("checkSignoff", "True");
            editor.commit();
            new InitializationActivity(mContext, username1, password1, currDateTime1, false).execute("");
            editor.clear().commit();
            editor.putString("onExit", "Signoff");
            editor.putString("checkSignoff", "False");
            editor.commit();

            //This is to clear all the saved values inside the app
            SharedPreferences clearCheckOptions = mContext.getSharedPreferences(PREFS_NAME1, 0);
            SharedPreferences.Editor editor1 = clearCheckOptions.edit();
            editor1.clear().commit();
            SharedPreferences clearCount = mContext.getSharedPreferences(PREFS_NAME2, 0);
            SharedPreferences.Editor editor2 = clearCount.edit();
            editor2.clear().commit();
            SharedPreferences clearFrequency = mContext.getSharedPreferences(PREFS_NAME3, 0);
            SharedPreferences.Editor editor3 = clearFrequency.edit();
            editor3.clear().commit();
            SharedPreferences clearTime = mContext.getSharedPreferences(PREFS_NAME4, 0);
            SharedPreferences.Editor editor4 = clearTime.edit();
            editor4.clear().commit();
            SharedPreferences clearBlackListCount = mContext.getSharedPreferences(PREFS_NAME5, 0);
            SharedPreferences.Editor editor5 = clearBlackListCount.edit();
            editor5.clear().commit();
            SharedPreferences clearWhiteListCount = mContext.getSharedPreferences(PREFS_NAME6, 0);
            SharedPreferences.Editor editor6 = clearWhiteListCount.edit();
            editor6.clear().commit();
            SharedPreferences clearBlackListPlaceHolder = mContext.getSharedPreferences(PREFS_NAME7, 0);
            SharedPreferences.Editor editor7 = clearBlackListPlaceHolder.edit();
            editor7.clear().commit();
            SharedPreferences clearWhiteListPlaceHolder = mContext.getSharedPreferences(PREFS_NAME8, 0);
            SharedPreferences.Editor editor8 = clearWhiteListPlaceHolder.edit();
            editor8.clear().commit();
            SharedPreferences clearBlackList = mContext.getSharedPreferences(PREFS_NAME9, 0);
            SharedPreferences.Editor editor9 = clearBlackList.edit();
            editor9.clear().commit();
            SharedPreferences clearWhiteList = mContext.getSharedPreferences(PREFS_NAME10, 0);
            SharedPreferences.Editor editor10 = clearWhiteList.edit();
            editor10.clear().commit();

            finish();
            System.exit(0);
        }catch (Exception e) {
            Log.d(TAG, "Problem signing off");
        }
    }

    //This variable was used to purposely update the database to be able to access it, but there is not need for it anymore
    public void updateAccessVariable(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef = database.child("users/" + username1);
        myRef.child("accessVar").setValue(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            // If request is cancelled, the result arrays are empty.
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the contacts-related task you need to do.
                } else {
                    // permission denied. Disable the functionality that depends on this permission.
                    Toast.makeText(HomePageActivity.this, "Permission denied to read your Texts", Toast.LENGTH_SHORT).show();
                }
                break;
            // other 'case' lines to check for other permissions this app might request
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_check) {
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
