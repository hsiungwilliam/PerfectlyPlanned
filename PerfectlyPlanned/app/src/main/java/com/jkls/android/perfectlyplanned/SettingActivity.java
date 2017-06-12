package com.jkls.android.perfectlyplanned;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by eande on 5/23/2017.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SettingActivity";
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;

    private EditText mBlackField;
    private EditText mWhiteField;
    private Button mUpdateButton;
    private Button mExitButton;
    private Button mDeleteButton;
    private Button mDeleteWhiteList;
    private Button mDeleteBlackList;
    private RadioButton mEmailRButton;
    private RadioButton mTextRButton;
    private RadioButton mBothRButton;
    private RadioButton mMinRButton;
    private RadioButton mDayRButton;
    private RadioButton mDemandRButton;
    private ListView Blist;
    private ListView Wlist;

    private Context mContext;
    public static final String PREFS_NAME = "MyEmailFrequencyFile";
    public static final String PREFS_NAME1 = "MyCheckOptionsFile";
    public static final String PREFS_NAME2 = "MyBlackListFile";
    public static final String PREFS_NAME3 = "MyWhiteListFile";
    public static final String PREFS_NAME4 = "MyTempBListFile";
    public static final String PREFS_NAME5 = "MyTempWListFile";
    public static final String PREFS_NAME6 = "MyBListFile";
    public static final String PREFS_NAME7 = "MyWListFile";
    String emailOptions;
    String emailFrequency;
    String username1;
    String password1;
    String currDateTime1;
    ArrayAdapter<String> adapter1;
    ArrayAdapter<String> adapter2;
    ArrayList<String> listItems1 = new ArrayList<String>();
    ArrayList<String> listItems2 = new ArrayList<String>();
    int Bcount;
    int Wcount;

    public void SettingActivity(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        username1 = getIntent().getStringExtra("username");
        password1 = getIntent().getStringExtra("password");
        currDateTime1 = getIntent().getStringExtra("datetime");
        mContext = getBaseContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();

        // Views
        mWhiteField = (EditText) findViewById(R.id.editText);
        mBlackField = (EditText) findViewById(R.id.editText2);
        mUpdateButton = (Button) findViewById(R.id.button_update);
        mExitButton = (Button) findViewById(R.id.button_exit);
        mDeleteButton = (Button) findViewById(R.id.button_delete);
        mDeleteBlackList = (Button) findViewById(R.id.button_deleteBlackList);
        mDeleteWhiteList = (Button) findViewById(R.id.button_deleteWhiteList);
        mEmailRButton = (RadioButton) findViewById(R.id.button_email);
        mTextRButton = (RadioButton) findViewById(R.id.button_text);
        mBothRButton = (RadioButton) findViewById(R.id.button_both);
        mMinRButton = (RadioButton) findViewById(R.id.button_min);
        mDayRButton = (RadioButton) findViewById(R.id.button_day);
        mDemandRButton = (RadioButton) findViewById(R.id.button_demand);
        Blist = (ListView) findViewById(R.id.list_black);
        Wlist = (ListView) findViewById(R.id.list_white);

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
        mDeleteButton.setOnClickListener(this);
        mDeleteBlackList.setOnClickListener(this);
        mDeleteWhiteList.setOnClickListener(this);
        //the onclick listener gets the value inside the list that has been clicked on
        Blist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String emailstr = ((TextView) view).getText().toString();
                String positionstr = Blist.getItemAtPosition(position).toString();
                SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME4, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear().commit();
                editor.putString("email", emailstr);
                editor.putString("position", positionstr);
                editor.commit();
            }
        });
        //the ontouch  listener makes sure the list inside the scrollview scrolls correctly
        Blist.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (view.getId() == R.id.list_black) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction()&MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
        Wlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String emailstr = ((TextView) view).getText().toString();
                String positionstr = Wlist.getItemAtPosition(position).toString();
                SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME5, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear().commit();
                editor.putString("email", emailstr);
                editor.putString("position", positionstr);
                editor.commit();
            }
        });
        Wlist.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (view.getId() == R.id.list_white) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction()&MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

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

        getBlackListCount();
        getWhiteListCount();

        displayBlackList();
        displayWhiteList();
    }

    public void displayBlackList(){
        DatabaseReference myRef = mRef.child("users/" + username1);
        myRef.child("/BlackList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(int i = 0; i<Bcount; i++){
                    String temp = "/email" + Integer.toString(i);
                    System.out.println(temp);
                    String e = dataSnapshot.child(temp + "/email").getValue().toString();
                    System.out.println(e);
                    addToBList(e);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void addToBList(String e){
        adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems1);
        Blist.setAdapter(adapter1);
        // this line adds the data of your EditText and puts in your array
        listItems1.add(e);
        // next thing you have to do is check if your adapter has changed
        adapter1.notifyDataSetChanged();
    }

    public void displayWhiteList(){
        DatabaseReference myRef = mRef.child("users/" + username1);
        myRef.child("/WhiteList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(int i = 0; i<Wcount; i++){
                    String temp = "/email" + Integer.toString(i);
                    System.out.println(temp);
                    String e = dataSnapshot.child(temp + "/email").getValue().toString();
                    System.out.println(e);
                    addToWList(e);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void addToWList(String e){
        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems2);
        Wlist.setAdapter(adapter2);
        // this line adds the data of your EditText and puts in your array
        listItems2.add(e);
        // next thing you have to do is check if your adapter has changed
        adapter2.notifyDataSetChanged();
    }

    //This updates everything that has been changed on this page in the database
    public void update() {
        Log.d(TAG, "update");

        Boolean value = false;
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
            value = true;
            System.out.println("updated to min");
        } else if (mDayRButton.isChecked() && !emailFrequency.equals("Day")) {
            SharedPreferences settings1 = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor1 = settings1.edit();
            editor1.clear().commit();
            editor1.putString("checkFrequency", "Day");
            editor1.commit();
            updateOpt("Day");
            value = true;
            System.out.println("updated to day");
        } else if (mDemandRButton.isChecked() && !emailFrequency.equals("Demand")) {
            SharedPreferences settings1 = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor1 = settings1.edit();
            editor1.clear().commit();
            editor1.putString("checkFrequency", "Demand");
            editor1.commit();
            updateOpt("Demand");
            value = true;
            System.out.println("updated to on demand");
        }
        //This will update the settings
        if(value == true)
            new InitializationActivity(mContext, username1, password1, currDateTime1, true).execute("");

        //If there is any new information in either the black or white fields then it gets added here
        String strTemp = mWhiteField.getText().toString();
        if(!strTemp.equals("")) {
            Log.d(TAG, "added another email to the white list");
            CharSequence ch1 = mWhiteField.getText().toString();
            if(!isEmailValid(ch1)){
                mWhiteField.setError("Valid Email Required");
                return;
            }

            String temp = mWhiteField.getText().toString();
            //This part is adding the new account into the database
            String str = "/email" + Integer.toString(Wcount);
            HashMap<String, Object> result = new HashMap<>();
            result.put("email", temp);
            Map<String, Object> postValues = result;

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("users/" + username1 + "/WhiteList" + str, postValues);
            mRef.updateChildren(childUpdates);
            //this part is updating the count value in the database
            updateWCount();

            String num = Integer.toString(Wcount-1);
            String email = "email" + num;
            SharedPreferences settings = getSharedPreferences(PREFS_NAME7, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString(email, strTemp);
            editor.commit();
        }
        String Tempstr1 = mBlackField.getText().toString();
        if(!Tempstr1.equals("")) {
            Log.d(TAG, "added another email to the black list");
            CharSequence ch = mBlackField.getText().toString();
            if(!isEmailValid(ch)){
                mBlackField.setError("Valid Email Required");
                return;
            }

            String temp = mBlackField.getText().toString();
            //This part is adding the new account into the database
            String str = "/email" + Integer.toString(Bcount);
            HashMap<String, Object> result = new HashMap<>();
            result.put("email", temp);
            Map<String, Object> postValues = result;

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("users/" + username1 + "/BlackList" + str, postValues);
            mRef.updateChildren(childUpdates);
            //this part is updating the count value in the database
            updateBCount();

            String num = Integer.toString(Bcount-1);
            String email = "email" + num;
            SharedPreferences settings = getSharedPreferences(PREFS_NAME6, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString(email, Tempstr1);
            editor.commit();
        }

        Toast.makeText(SettingActivity.this, "Update Complete", Toast.LENGTH_SHORT).show();

        //Once everything has been updated then it is returned to the settings page
        Intent in = new Intent(SettingActivity.this, SettingActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    //This just exits the page, saves nothing, and goes back to the home screen
    private void exit() {
        Log.d(TAG, "exit");
        finish();

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

    public void updateFreq(String value){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef = database.child("users/" + username1);
        myRef.child("freq").setValue(value);
    }

    public void updateOpt(String value){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef = database.child("users/" + username1);
        myRef.child("opt").setValue(value);
    }

    //This will delete the users account if the user selects yes from the pop up window
    public void delete(){
        Log.d(TAG, "Deleting an email account");
        open(null);
    }

    public void open(View view){
        System.out.println("inside of open");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to delete this account?");
        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                System.out.println("you clicked yes, the account is being deleted");
                //This is deleting the account from the Authentication
                final FirebaseUser currentUser = mAuth.getCurrentUser();
                AuthCredential credential = EmailAuthProvider.getCredential(username1, password1);
                currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User account deleted.");
                                }
                            }
                        });
                    }
                });

                //This is deleting the account from the database
                DatabaseReference myRef = mRef.child("users/" + username1);
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().removeValue();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                //This returns the user to the sign in page
                Intent in = new Intent(SettingActivity.this, SignInActivity.class);
                in.putExtra("username", username1);
                in.putExtra("password", password1);
                in.putExtra("datetime", currDateTime1);
                mContext.startActivity(in);
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("you clicked no, the account is not being deleted");
                return;
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void getWhiteListCount(){
        DatabaseReference myRef1 = mRef.child("users/" + username1);
        myRef1.child("/whiteListCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Wcount = Integer.parseInt(dataSnapshot.getValue().toString());
                System.out.println("count inside of get white list count is: " + Wcount);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void getBlackListCount(){
        DatabaseReference myRef1 = mRef.child("users/" + username1);
        myRef1.child("/blackListCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Bcount = Integer.parseInt(dataSnapshot.getValue().toString());
                System.out.println("count inside of black list count is: " + Bcount);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void updateBCount(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef = database.child("users/" + username1);
        Bcount++;
        myRef.child("/blackListCount").setValue(Bcount);

        String value = Integer.toString(Bcount);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME2, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().commit();
        editor.putString("count", value);
        editor.commit();
    }

    public void updateWCount(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef = database.child("users/" + username1);
        Wcount++;
        myRef.child("/whiteListCount").setValue(Wcount);

        String value = Integer.toString(Wcount);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME3, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().commit();
        editor.putString("count", value);
        editor.commit();
    }

    public void deleteBlackList(){
        //This is getting the value and index of the selected item
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME4, 0);
        String emailstr = settings.getString("email", "none");
        String positionstr = settings.getString("posistion", "0");
        System.out.println(emailstr + " " + positionstr);
        int pos = Integer.parseInt(positionstr);
        //This is deleting that item from the list
        Blist.setAdapter(adapter1);
        listItems1.remove(pos);
        adapter1.notifyDataSetChanged();

        //This is deleting that item from the database
        String str = "/email" + Integer.toString(Bcount-1);
        DatabaseReference myRef = mRef.child("users/" + username1 + "/BlackList");
        myRef.child(str).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //dataSnapshot.child("/email").getRef().removeValue();
                dataSnapshot.getRef().removeValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //This is updating the count value inside the database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef0 = database.child("users/" + username1);
        int num = Bcount - 1;
        myRef0.child("/blackListCount").setValue(num);

        Intent in = new Intent(SettingActivity.this, SettingActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    public void deleteWhiteList(){
        //This is getting the value and index of the selected item
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME5, 0);
        String emailstr = settings.getString("email", "none");
        String positionstr = settings.getString("posistion", "0");
        System.out.println(emailstr + " " + positionstr);
        int pos = Integer.parseInt(positionstr);
        //This is deleting that item from the list
        Wlist.setAdapter(adapter2);
        listItems2.remove(pos);
        adapter2.notifyDataSetChanged();

        //This is deleting that item from the database
        String str = "/email" + Integer.toString(Bcount-1);
        DatabaseReference myRef = mRef.child("users/" + username1 + "/WhiteList");
        myRef.child(str).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //dataSnapshot.child("/email").getRef().removeValue();
                dataSnapshot.getRef().removeValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //This is updating the count value inside the database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef0 = database.child("users/" + username1);
        int num = Wcount - 1;
        myRef0.child("/whiteListCount").setValue(num);

        Intent in = new Intent(SettingActivity.this, SettingActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_update) {
            update();
        } else if (i == R.id.button_exit) {
            exit();
        } else if (i == R.id.button_delete){
            delete();
        } else if (i == R.id.button_deleteWhiteList){
            deleteWhiteList();
        } else if (i == R.id.button_deleteBlackList){
            deleteBlackList();
        }
    }
}
