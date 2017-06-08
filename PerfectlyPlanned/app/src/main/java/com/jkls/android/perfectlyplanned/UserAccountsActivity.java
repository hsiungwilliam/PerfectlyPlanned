package com.jkls.android.perfectlyplanned;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by eande on 6/2/2017.
 */

public class UserAccountsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "UserAccountsActivity";
    public static final String PREFS_NAME1 = "MyEmailFile";
    private DatabaseReference mRef;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;

    private Button mAddButton;
    private Button mDeleteButton;
    private Button mExitButton;
    private ListView list;

    private Context mContext;
    String username1;
    String password1;
    String currDateTime1;
    Boolean value;
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    String emailstr;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        username1 = getIntent().getStringExtra("username");
        password1 = getIntent().getStringExtra("password");
        currDateTime1 = getIntent().getStringExtra("datetime");
        mContext = getBaseContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_accounts);
        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        // Views
        mAddButton = (Button) findViewById(R.id.button_add);
        mExitButton = (Button) findViewById(R.id.button_exit);
        mDeleteButton = (Button) findViewById(R.id.button_delete);
        list = (ListView) findViewById(R.id.listview_email);

        // Click listeners
        mAddButton.setOnClickListener(this);
        mExitButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String emailstr = ((TextView) view).getText().toString();
                String positionstr = list.getItemAtPosition(position).toString();
                SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME1, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear().commit();
                editor.putString("email", emailstr);
                editor.putString("position", positionstr);
                editor.commit();
            }
        });

        //We need to get the count value
        getCount();
        //Go to display the values in the list view
        displayList();
    }
    public void getCount(){
        String username = usernameFromEmail(username1);
        DatabaseReference myRef1 = mRef.child("users/" + username);

        myRef1.child("/count").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                count = Integer.parseInt(dataSnapshot.getValue().toString());
                System.out.println("count inside of getCount is: " + count);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void displayList(){
        String username = usernameFromEmail(username1);
        DatabaseReference myRef = mRef.child("users/" + username);

        myRef.child("/accounts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(int i = 0; i<count; i++){
                    String temp = "account" + Integer.toString(i);
                    System.out.println(temp);
                    String e = dataSnapshot.child(temp + "/email").getValue().toString();
                    System.out.println(e);
                    addToList(e);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void addToList(String e){
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        list.setAdapter(adapter);
        // this line adds the data of your EditText and puts in your array
        listItems.add(e);
        // next thing you have to do is check if your adapter has changed
        adapter.notifyDataSetChanged();
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {return email;}
    }

    //When the add button gets clicked, another page pops up for you to add another email account
    public void showAddButton(){
        Log.d(TAG, "Adding another email account");

        Intent in = new Intent(UserAccountsActivity.this, AddEmailActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    //When the exit button gets clicked, it takes the user back to the home screen
    public void showExitButton(){
        Log.d(TAG, "Exiting accounts page");
        Intent in = new Intent(UserAccountsActivity.this, HomePageActivity.class);
        in.putExtra("username", username1);
        in.putExtra("password", password1);
        in.putExtra("datetime", currDateTime1);
        mContext.startActivity(in);
        finish();
    }

    //When the delete button gets clicked, which ever account was selected will be deleted from the database
    public void showDeleteButton(){
        Log.d(TAG, "Deleting an email account");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        open(null);
    }

    public void open(View view){
        System.out.println("inside of open");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to delete this account?");
        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                value = true;
                System.out.println("you clicked yes, value: " + value);
                //This is getting the value and index of the selected item
                SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME1, 0);
                emailstr = settings.getString("email", "none");
                String positionstr = settings.getString("posistion", "0");
                System.out.println(emailstr + " " + positionstr);
                int pos = Integer.parseInt(positionstr);
                //This is deleting that item from the list
                list.setAdapter(adapter);
                listItems.remove(pos);
                adapter.notifyDataSetChanged();

                //This is deleting that item from the database
                String str = "/account" + Integer.toString(count-1);
                String username = usernameFromEmail(username1);
                DatabaseReference myRef = mRef.child("users/" + username + "/accounts");
                myRef.child(str).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.child("/email").getRef().removeValue();
                        dataSnapshot.child("/password").getRef().removeValue();
                        dataSnapshot.getRef().removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                //This is updating the count value inside the database
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                String usernamestr = usernameFromEmail(username1);
                DatabaseReference myRef0 = database.child("users/" + usernamestr);
                int num = count - 1;
                myRef0.child("count").setValue(num);

                Intent in = new Intent(UserAccountsActivity.this, UserAccountsActivity.class);
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
                value = false;
                System.out.println("you clicked no, value: " + value);
                return;
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_add) {
            showAddButton();
        } else if (i == R.id.button_exit) {
            showExitButton();
        } else if (i == R.id.button_delete) {
            showDeleteButton();
        }
    }
}
