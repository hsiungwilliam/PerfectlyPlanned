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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
 * Created by eande on 5/31/2017.
 */

public class UserSignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "UserSignUpActivity";
    public static final String PREFS_NAME = "MyEmailFrequencyFile";
    public static final String PREFS_NAME1 = "MyCheckOptionsFile";
    public static final String PREFS_NAME2 = "MySignupEmailFile";
    public static final String PREFS_NAME3 = "MySignupPasswordFile";
    public static final String PREFS_NAME4 = "MyCountFile";
    private DatabaseReference mRef;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;

    private Button mAddButton;
    private Button mSignupButton;
    private Button mExitButton;
    private EditText mEmailField;
    private EditText mPasswordField;
    private RadioButton mEmailRButton;
    private RadioButton mTextRButton;
    private RadioButton mBothRButton;
    private RadioButton mMinRButton;
    private RadioButton mDayRButton;
    private RadioButton mDemandRButton;

    private Context mContext;
    String username1;
    String email1;
    String password1;
    String currDateTime1;
    String emailOptions;
    String emailFrequency;
    String freq;
    String opt;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        username1 = getIntent().getStringExtra("username");
        email1 = getIntent().getStringExtra("email");
        password1 = getIntent().getStringExtra("password");
        currDateTime1 = getIntent().getStringExtra("datetime");
        count = getIntent().getIntExtra("count", 0);
        mContext = getBaseContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_user);
        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        // Views
        mAddButton = (Button) findViewById(R.id.button_add);
        mExitButton = (Button) findViewById(R.id.button_exit);
        mSignupButton = (Button) findViewById(R.id.button_signup);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        mEmailRButton = (RadioButton) findViewById(R.id.button_email);
        mTextRButton = (RadioButton) findViewById(R.id.button_text);
        mBothRButton = (RadioButton) findViewById(R.id.button_both);
        mMinRButton = (RadioButton) findViewById(R.id.button_min);
        mDayRButton = (RadioButton) findViewById(R.id.button_day);
        mDemandRButton = (RadioButton) findViewById(R.id.button_demand);

        // Click listeners
        mAddButton.setOnClickListener(this);
        mExitButton.setOnClickListener(this);
        mSignupButton.setOnClickListener(this);

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

        //This ensure the keyboard will not pop up when the page does, only when you click on editText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //This is just the initializing of the shared preferences for both the email options and the check frequency
        SharedPreferences emailCheckFrequency = getSharedPreferences(PREFS_NAME, 0);
        emailFrequency = emailCheckFrequency.getString("checkFrequency", "OnDemand");
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

    //This handles when the add another email account has been clicked. It will save the current information and refresh the page
    public void showAddButton(){
        Log.d(TAG, "Adding another email account");
        if (!validateForm()) {
            return;
        }
        //This saves the radio buttons
        update();
        //This section is saving the username and password into shared preferences so they can be saved when the user selects sigh up
        String temp = mEmailField.getText().toString();
        String temp1 = mPasswordField.getText().toString();
        String temp2 = "email" + Integer.toString(count);
        String temp3 = "password" + Integer.toString(count);
        SharedPreferences emails = mContext.getSharedPreferences(PREFS_NAME2, 0);
        SharedPreferences.Editor editor = emails.edit();
        editor.putString(temp2, temp);
        editor.commit();
        SharedPreferences passwords = mContext.getSharedPreferences(PREFS_NAME3, 0);
        SharedPreferences.Editor editor1 = passwords.edit();
        editor1.putString(temp3, temp1);
        editor1.commit();
        count++;

        //This section is just refreshing the user sign up page
        Intent in = new Intent(UserSignUpActivity.this, UserSignUpActivity.class);
        in.putExtra("username", username1);
        in.putExtra("email", email1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        in.putExtra("count", count);
        mContext.startActivity(in);
        finish();
    }

    //This handles the exit button, no information will be saved and the user will be brought back to the sign in page
    public void showExitButton(){
        Log.d(TAG, "exit from signup page");
        //This will clear any saved emails
        SharedPreferences emails = mContext.getSharedPreferences(PREFS_NAME2, 0);
        SharedPreferences.Editor editor = emails.edit();
        editor.clear().commit();
        //This will clear any saved passwords
        SharedPreferences passwords = mContext.getSharedPreferences(PREFS_NAME3, 0);
        SharedPreferences.Editor editor1 = passwords.edit();
        editor1.clear().commit();

        //
        //This will remove the username and userId from the database since it will not be used
        //

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query userQuery = ref.child("users").equalTo(username1);
        userQuery.getRef().removeValue();

        Intent in = new Intent(UserSignUpActivity.this, SignInActivity.class);
        in.putExtra("username", email1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    //This handles the signup button, the information will be saved in the database and the user wil be brought to the home page
    public void showSignupButton(){
        Log.d(TAG, "user signup");
        if (!validateForm()) {
            return;
        }
        //This is gathering the information collected from the email and password text boxes and saving it
        String temp = mEmailField.getText().toString();
        String temp1 = mPasswordField.getText().toString();
        String temp2 = "email" + Integer.toString(count);
        String temp3 = "password" + Integer.toString(count);
        SharedPreferences emails = mContext.getSharedPreferences(PREFS_NAME2, 0);
        SharedPreferences.Editor editor = emails.edit();
        editor.putString(temp2, temp);
        editor.commit();
        SharedPreferences passwords = mContext.getSharedPreferences(PREFS_NAME3, 0);
        SharedPreferences.Editor editor1 = passwords.edit();
        editor1.putString(temp3, temp1);
        editor1.commit();
        count++;
        //This saves the information collected from the radio buttons
        update();
        //This will save all information into the database
        updateSettings();
        //This will clear any saved emails
        SharedPreferences emails1 = mContext.getSharedPreferences(PREFS_NAME2, 0);
        SharedPreferences.Editor editor2 = emails1.edit();
        editor2.clear().commit();
        //This will clear any saved passwords
        SharedPreferences passwords1 = mContext.getSharedPreferences(PREFS_NAME3, 0);
        SharedPreferences.Editor editor3 = passwords1.edit();
        editor3.clear().commit();

        Intent in = new Intent(UserSignUpActivity.this, HomePageActivity.class);
        in.putExtra("username", email1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        Toast.makeText(UserSignUpActivity.this, "Sign Up Completed", Toast.LENGTH_SHORT).show();
        finish();
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {mEmailField.setError(null);}

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {mPasswordField.setError(null);}

        CharSequence ch = mEmailField.getText().toString();
        if(!isEmailValid(ch)){
            mEmailField.setError("Valid Email Required");
            result = false;
        } else {mEmailField.setError(null);}
        return result;
    }

    public void update() {
        //If the settings for frequency of checking has changed then the information needs to be updated
        if (mEmailRButton.isChecked()) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME1, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString("checkOptions", "Email");
            editor.commit();
            opt = "Email";
            System.out.println("updated to Email only");
        } else if (mTextRButton.isChecked()) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME1, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString("checkOptions", "Text");
            editor.commit();
            opt = "Text";
            System.out.println("updated to Text only");
        } else if (mBothRButton.isChecked()) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME1, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString("checkOptions", "Both");
            editor.commit();
            opt = "Both";
            System.out.println("updated to both email and text");
        }

        //If the settings for ways to check for events have changed then the information needs to be updated
        if (mMinRButton.isChecked()) {
            SharedPreferences settings1 = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor1 = settings1.edit();
            editor1.clear().commit();
            editor1.putString("checkFrequency", "Min");
            editor1.commit();
            freq = "Min";
            System.out.println("updated to min");
        } else if (mDayRButton.isChecked()) {
            SharedPreferences settings1 = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor1 = settings1.edit();
            editor1.clear().commit();
            editor1.putString("checkFrequency", "Day");
            editor1.commit();
            freq = "Day";
            System.out.println("updated to day");
        } else if (mDemandRButton.isChecked()) {
            SharedPreferences settings1 = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor1 = settings1.edit();
            editor1.clear().commit();
            editor1.putString("checkFrequency", "Demand");
            editor1.commit();
            freq = "Demand";
            System.out.println("updated to on demand");
        }
    }

    private void updateSettings(){
        String currentDateTime = new Date().toString();
        Post post = new Post(username1, email1, currentDateTime, freq, opt, count);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("users/" + username1, postValues);
        mRef.updateChildren(childUpdates);

        String value = Integer.toString(count);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME4, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().commit();
        editor.putString("count", value);
        editor.commit();

        addEmailsToDB();
    }

    private void addEmailsToDB(){
        for(int i = 0; i<count; i++) {
            String str = "/account" + Integer.toString(i);
            PostAccount post = new PostAccount(i);
            Map<String, Object> postValues = post.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("users/" + username1 + "/accounts" + str, postValues);
            mRef.updateChildren(childUpdates);
        }
    }

    @IgnoreExtraProperties
    public class Post {
        public String uid;
        public String username;
        public String currentDateTime;
        public String freq;
        public String opt;
        public int count;

        public Post() {// Default constructor required for calls to DataSnapshot.getValue(Post.class)
        }
        public Post(String uid, String username, String currentDateTime, String freq, String opt, int count) {
            this.uid = uid;
            this.username = username;
            this.currentDateTime = currentDateTime;
            this.freq = freq;
            this.opt = opt;
            this.count = count;
        }
        @Exclude
        public Map<String, Object> toMap() {
            System.out.println("inside map");
            HashMap<String, Object> result = new HashMap<>();
            result.put("uid", uid);
            result.put("username", username);
            result.put("currentDateTime", currentDateTime);
            result.put("freq", freq);
            result.put("opt", opt);
            result.put("count", count);
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
            System.out.println("adding email: " + getUsername + " and password: " + getPassword + " to database");

            result.put("email", getUsername);
            result.put("password", getPassword);
            return result;
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_add) {
            showAddButton();
        } else if (i == R.id.button_exit) {
            showExitButton();
        } else if (i == R.id.button_signup) {
            showSignupButton();
        }
    }
}
