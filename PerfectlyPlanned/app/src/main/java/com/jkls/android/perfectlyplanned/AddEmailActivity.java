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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eande on 6/2/2017.
 */

public class AddEmailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddEmailActivity";
    public static final String PREFS_NAME4 = "MyCountFile";
    private DatabaseReference mRef;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;

    private Button mDoneButton;
    private Button mExitButton;
    private EditText mEmailField;
    private EditText mPasswordField;

    private Context mContext;
    String username1;
    String password1;
    String currDateTime1;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        username1 = getIntent().getStringExtra("username");
        password1 = getIntent().getStringExtra("password");
        currDateTime1 = getIntent().getStringExtra("datetime");
        mContext = getBaseContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_email);
        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        // Views
        mExitButton = (Button) findViewById(R.id.button_exit);
        mDoneButton = (Button) findViewById(R.id.button_done);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Click listeners
        mExitButton.setOnClickListener(this);
        mDoneButton.setOnClickListener(this);

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

        //This part here is getting the count value from the database
        getCount();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //When the user clicks on the exit button, they return to the accounts page with no saved information
    public void showExitButton(){
        Log.d(TAG, "exit add email page");
        Intent in = new Intent(AddEmailActivity.this, UserAccountsActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    public void getCount(){
        String username = usernameFromEmail(username1);
        DatabaseReference myRef1 = mRef.child("users/" + username);

        myRef1.child("/count").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                count = Integer.parseInt(dataSnapshot.getValue().toString());
                System.out.println("count inside of getCount in add email is: " + count);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    //When a user clicks done, all info is saved into the database and they return to accounts with the new info displayed
    public void showDoneButton(){
        Log.d(TAG, "added another email to list");
        if (!validateForm()) {
            return;
        }

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        String username = usernameFromEmail(username1);

        //This part is adding the new account into the database
        String str = "/account" + Integer.toString(count);
        PostAccount post = new PostAccount(email, password);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("users/" + username + "/accounts" + str, postValues);
        mRef.updateChildren(childUpdates);

        //this part is updating the count value in the database
        updateCount();

        System.out.println("Email: " + email + " Password: " + password + " Count: " + count + " inside add email activity");

        Intent in = new Intent(AddEmailActivity.this, UserAccountsActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    @IgnoreExtraProperties
    public class PostAccount {
        public String email;
        public String password;
        public PostAccount() {// Default constructor required for calls to DataSnapshot.getValue(Post.class)
        }
        public PostAccount(String email, String password) {
            this.email = email;
            this.password = password;
        }
        @Exclude
        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("email", email);
            result.put("password", password);
            return result;
        }
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {return email;}
    }

    public void updateCount(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String username = usernameFromEmail(username1);
        DatabaseReference myRef = database.child("users/" + username);
        count++;
        myRef.child("count").setValue(count);

        String value = Integer.toString(count);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME4, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().commit();
        editor.putString("count", value);
        editor.commit();
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

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_exit) {
            showExitButton();
        } else if (i == R.id.button_done) {
            showDoneButton();
        }
    }
}
