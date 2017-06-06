package com.jkls.android.perfectlyplanned;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by eande on 6/6/2017.
 */

public class VenderSignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "VenderSignUpActivity";

    private Button mExitButton;
    private Context mContext;
    String username1;
    String password1;
    String currDateTime1;
    String email1;
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

        // Views
        mExitButton = (Button) findViewById(R.id.button_exit);

        // Click listeners
        mExitButton.setOnClickListener(this);
    }

    public void exitButton(){
        Intent in = new Intent(VenderSignUpActivity.this, SignInActivity.class);
        in.putExtra("username", email1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_exit) {
            exitButton();
        }
    }
}
