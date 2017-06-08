package com.jkls.android.perfectlyplanned;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Created by eande on 6/6/2017.
 */

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SignUpActivity";

    private Button mContinueButton;
    private Button mExitButton;
    private RadioButton mUserRButton;
    private RadioButton mVenderRButton;
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
        count = getIntent().getIntExtra("count", 0);
        mContext = getBaseContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Views
        mContinueButton = (Button) findViewById(R.id.button_continue);
        mExitButton = (Button) findViewById(R.id.button_exit);
        mUserRButton = (RadioButton) findViewById(R.id.button_user);
        mVenderRButton = (RadioButton) findViewById(R.id.button_vender);

        // Click listeners
        mContinueButton.setOnClickListener(this);
        mExitButton.setOnClickListener(this);
    }

    public void showUserButtonHandler(View v){
        mVenderRButton.setChecked(false);
        mUserRButton.setChecked(true);
    }

    public void showVenderButtonHandler(View v){
        mUserRButton.setChecked(false);
        mVenderRButton.setChecked(true);
    }

    public void continueButton(){
        if(mUserRButton.isChecked()) {
            Log.d(TAG, "Going to user sign up");
            Intent in = new Intent(SignUpActivity.this, UserSignUpActivity.class);
            in.putExtra("username", username1);
            in.putExtra("password", password1);
            in.putExtra("datetime", currDateTime1);
            in.putExtra("count", 0);
            mContext.startActivity(in);
        } else if(mVenderRButton.isChecked()){
            Log.d(TAG, "Going to vender sign up");
            Intent in = new Intent(SignUpActivity.this, VenderSignUpActivity.class);
            in.putExtra("username", username1);
            in.putExtra("password", password1);
            in.putExtra("datetime", currDateTime1);
            in.putExtra("count", 0);
            mContext.startActivity(in);
        }else{
            Log.d(TAG, "Did not pick any choices");
            Toast.makeText(mContext, "You need to pick one choice", Toast.LENGTH_SHORT).show();
            return;
        }
        finish();
    }

    public void exitButton(){
        Intent in = new Intent(SignUpActivity.this, SignInActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_continue) {
            continueButton();
        } else if (i == R.id.button_exit) {
            exitButton();
        }
    }
}
