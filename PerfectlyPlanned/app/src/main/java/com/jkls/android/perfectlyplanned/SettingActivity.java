package com.jkls.android.perfectlyplanned;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.Date;

import static android.content.ContentValues.TAG;

/**
 * Created by eande on 5/23/2017.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SettingActivity";
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private EditText mBlackField;
    private EditText mWhiteField;
    private Button mUpdateButton;
    private Button mExitButton;
    private RadioButton mEmailRButton;
    private RadioButton mTextRButton;
    private RadioButton mBothRButton;
    private RadioButton mMinRButton;
    private RadioButton mDayRButton;
    private RadioButton mDemandRButton;
    private LinearLayout mBlackScrollViewLL;
    private LinearLayout mWhiteScrollViewLL;

    private Context mContext;
    public static final String PREFS_NAME = "MyEmailFrequencyFile";
    public static final String PREFS_NAME1 = "MyCheckOptionsFile";
    String emailOptions;
    String emailFrequency;
    String username1;
    String password1;
    String currDateTime1;

    public void SettingActivity(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        username1 = getIntent().getStringExtra("username");
        password1 = getIntent().getStringExtra("password");
        currDateTime1 = getIntent().getStringExtra("datetime");
        mContext = getBaseContext();
        System.out.println(username1 + "settings activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Views
        mBlackField = (EditText) findViewById(R.id.field_black);
        mWhiteField = (EditText) findViewById(R.id.field_white);
        mUpdateButton = (Button) findViewById(R.id.button_update);
        mExitButton = (Button) findViewById(R.id.button_exit);
        mEmailRButton = (RadioButton) findViewById(R.id.button_email);
        mTextRButton = (RadioButton) findViewById(R.id.button_text);
        mBothRButton = (RadioButton) findViewById(R.id.button_both);
        mMinRButton = (RadioButton) findViewById(R.id.button_min);
        mDayRButton = (RadioButton) findViewById(R.id.button_day);
        mDemandRButton = (RadioButton) findViewById(R.id.button_demand);
        mBlackScrollViewLL = (LinearLayout) findViewById(R.id.ScrollView_Black_LL);
        mWhiteScrollViewLL = (LinearLayout) findViewById(R.id.ScrollView_White_LL);

        //This allows the keyboard to disappear when clicking elsewhere on the screen
        mWhiteField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    hideKeyboard(v);
            }
        });
        mBlackField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    hideKeyboard(v);
            }
        });

        // Click listeners
        mUpdateButton.setOnClickListener(this);
        mExitButton.setOnClickListener(this);

        //This ensure the keyboard will not pop up when the page does, only when you click on editText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //This is just the initializing of the shared preferences for both the email options and the check frequency
        SharedPreferences emailCheckFrequency = getSharedPreferences(PREFS_NAME, 0);
        emailFrequency = emailCheckFrequency.getString("checkFrequency", "Demand");
        SharedPreferences emailCheckOptions = getSharedPreferences(PREFS_NAME1, 0);
        emailOptions = emailCheckOptions.getString("checkOptions", "Both");

        //This will initialize the page with the correct radio buttons clicked
        if(emailFrequency.equals("Min")){
            mMinRButton.setChecked(true);
            mDayRButton.setChecked(false);
            mDemandRButton.setChecked(false);
        } else if(emailFrequency.equals("Day")){
            mMinRButton.setChecked(false);
            mDayRButton.setChecked(true);
            mDemandRButton.setChecked(false);
        } else if(emailFrequency.equals("Demand")){
            mMinRButton.setChecked(false);
            mDayRButton.setChecked(false);
            mDemandRButton.setChecked(true);
        }

        if(emailOptions.equals("Email")){
            mEmailRButton.setChecked(true);
            mTextRButton.setChecked(false);
            mBothRButton.setChecked(false);
        } else if(emailOptions.equals("Text")){
            mEmailRButton.setChecked(false);
            mTextRButton.setChecked(true);
            mBothRButton.setChecked(false);
        } else if(emailOptions.equals("Both")){
            mEmailRButton.setChecked(false);
            mTextRButton.setChecked(false);
            mBothRButton.setChecked(true);
        }
    }

    public void update() {
        Log.d(TAG, "update");

        //If the settings for frequency of checking has changed then the information needs to be updated
        if (mEmailRButton.isChecked() && !emailOptions.equals("Email")) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME1, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString("checkOptions", "Email");
            editor.commit();
            updateFreq("Email");

            System.out.println("updated to Email only");
        } else if (mTextRButton.isChecked() && !emailOptions.equals("Text")) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME1, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString("checkOptions", "Text");
            editor.commit();
            updateFreq("Text");

            System.out.println("updated to Text only");
        } else if (mBothRButton.isChecked() && !emailOptions.equals("Both")) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME1, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString("checkOptions", "Both");
            editor.commit();
            updateFreq("Both");

            System.out.println("updated to both email and text");
        }

        //If the settings for ways to check for events have changed then the information needs to be updated
        if (mMinRButton.isChecked() && !emailFrequency.equals("Min")) {
            SharedPreferences settings1 = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor1 = settings1.edit();
            editor1.clear().commit();
            editor1.putString("checkFrequency", "Min");
            editor1.commit();
            updateOpt("Min");

            System.out.println("updated to min");
        } else if (mDayRButton.isChecked() && !emailFrequency.equals("Day")) {
            SharedPreferences settings1 = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor1 = settings1.edit();
            editor1.clear().commit();
            editor1.putString("checkFrequency", "Day");
            editor1.commit();
            updateOpt("Day");

            System.out.println("updated to day");
        } else if (mDemandRButton.isChecked() && !emailFrequency.equals("Demand")) {
            SharedPreferences settings1 = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor1 = settings1.edit();
            editor1.clear().commit();
            editor1.putString("checkFrequency", "Demand");
            editor1.commit();
            updateOpt("Demand");

            System.out.println("updated to on demand");
        }
        //This will update the settings
        new InitializationActivity(mContext, username1, password1, currDateTime1, true).execute("");

        //If there is any new information in either the black or white fields then it gets added here
        if(!mWhiteField.equals("")) {
            TextView textView = new TextView(this);
            String temp = mWhiteField.toString();
            textView.setText(temp);
            mWhiteScrollViewLL.addView(textView);
        }
        if(!mBlackField.equals("")) {
            TextView textView = new TextView(this);
            String temp = mBlackField.toString();
            textView.setText(temp);
            mBlackScrollViewLL.addView(textView);
        }

        Toast.makeText(SettingActivity.this, "Update Complete", Toast.LENGTH_SHORT).show();

        //Once everything has been updated then it is returned to the settings page
        finish();
        Intent in = new Intent(SettingActivity.this, SettingActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
    }

    private void exit() {
        Log.d(TAG, "exit");
        finish();

        //When exiting it returns you to the home screen
        Intent in = new Intent(SettingActivity.this, HomePageActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //Ensures that when one button is clicked, the others are not
    public void showEmailButtonHandler(View v){
        mEmailRButton.setChecked(true);
        mTextRButton.setChecked(false);
        mBothRButton.setChecked(false);
    }
    public void showTextButtonHandler(View v){
        mEmailRButton.setChecked(false);
        mTextRButton.setChecked(true);
        mBothRButton.setChecked(false);
    }
    public void showBothButtonHandler(View v){
        mEmailRButton.setChecked(false);
        mTextRButton.setChecked(false);
        mBothRButton.setChecked(true);
    }
    public void showMinButtonHandler(View v){
        mMinRButton.setChecked(true);
        mDayRButton.setChecked(false);
        mDemandRButton.setChecked(false);
    }
    public void showDayButtonHandler(View v){
        mMinRButton.setChecked(false);
        mDayRButton.setChecked(true);
        mDemandRButton.setChecked(false);
    }
    public void showDemandButtonHandler(View v){
        mMinRButton.setChecked(false);
        mDayRButton.setChecked(false);
        mDemandRButton.setChecked(true);
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {return email;}
    }

    public void updateFreq(String value){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String username = usernameFromEmail(username1);
        String currentDateTime = new Date().toString();
        DatabaseReference myRef = database.child("users/" + username);
        myRef.child("freq").setValue(value);
    }

    public void updateOpt(String value){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String username = usernameFromEmail(username1);
        String currentDateTime = new Date().toString();
        DatabaseReference myRef = database.child("users/" + username);
        myRef.child("opt").setValue(value);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_update) {
            update();
        } else if (i == R.id.button_exit) {
            exit();
        }
    }
}
