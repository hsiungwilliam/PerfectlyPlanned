package com.jkls.android.perfectlyplanned;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by eande on 5/25/2017.
 */

public class InitializationActivity extends AsyncTask{
    private static final String TAG = "InitializationActivity";
    private DatabaseReference mRef;
    String signoff;
    static String userName;
    static String username2;
    static String password2;
    static String currDateTime2;
    String emailOptions;
    String emailFrequency;
    Boolean settingsChange1;
    Boolean accessVar;
    static int count;
    AlarmManager alarm = null;
    PendingIntent sender;
    static private Context mContext;
    public static final String PREFS_NAME = "MyEmailFrequencyFile";
    public static final String PREFS_NAME1 = "MyCheckOptionsFile";
    public static final String PREFS_NAME2 = "MyExitFile";
    public static final String PREFS_NAME3 = "MyCountFile";
    public static final String PREFS_NAME4 = "MyTimeFile";

    public InitializationActivity(Context context, String user_name, String pass_word, String currDateTime, Boolean settingsChange){
        mContext = context;
        username2 = user_name;
        password2 = pass_word;
        currDateTime2 = currDateTime;
        settingsChange1 = settingsChange;
        mRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected Object doInBackground(Object[] params) {
        System.out.println("made it into the background");
        //This will get the initial values stored in the app/database
        getDBInfo();
        return null;
    }

    //Starts checking the emails/texts on demand
    public void onDemand() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Intent intent3 = new Intent(mContext, goToCheckEmailText.class).putExtra("emailOptions", emailOptions);
        sender = PendingIntent.getBroadcast(mContext, 0, intent3, 0);
        // Schedule the alarm to go off one time
        alarm = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    //Starts checking the emails/texts every 30 minutes
    public void registerAlarmMin(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        try {
            Intent intent1 = new Intent(context, goToCheckEmailText.class).putExtra("emailOptions", emailOptions);
            sender = PendingIntent.getBroadcast(context, 0, intent1, 0);
            // Schedule the alarm
            alarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1800000, sender);//30min interval
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {return email;}
    }

    //When an alarm is on, this is where it goes to check the emails
    public static class goToCheckEmailText extends BroadcastReceiver {
        public goToCheckEmailText() {}
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "StartEmailCheck");

            final String emailOptions = intent.getExtras().getString("emailOptions");
            if (emailOptions.equals("Text"))
                new CheckText(context, username2, password2, currDateTime2).execute("");
            else if(emailOptions.equals("Email") || emailOptions.equals("Both")) {
                //This will get the values from the database for the username and password
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                DatabaseReference myRef = database.child("users/" + userName);

                myRef.child("/accounts").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        System.out.println("count value: " + count);
                        for (int i = 0; i < count; i++) {
                            String strAcc = "/account" + Integer.toString(i);
                            String e = dataSnapshot.child(strAcc + "/email").getValue().toString();
                            String p = dataSnapshot.child(strAcc + "/password").getValue().toString();
                            System.out.println("checking email: " + e + " and password: " + p);
                            new CheckEmail(mContext, e, p, currDateTime2).execute("");
                        }
                        if(emailOptions.equals("Both"))
                            new CheckText(mContext, username2, password2, currDateTime2).execute("");
                        Toast.makeText(mContext, "Checking Completed", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
            //updateCurrentDateTime();
        }

        public void updateCurrentDateTime(){
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            String currentDateTime = new Date().toString();
            DatabaseReference myRef = database.child("users/" + userName);
            myRef.child("currentDateTime").setValue(currentDateTime);

            SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME4, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString("time", currentDateTime);
            editor.commit();
        }
    }

    //Starts checking the emails/texts once a day
    public void registerAlarmDay(Context context) {
        //This gets the current time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Set the alarm to start at approximately 8:00 AM
        /*Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(System.currentTimeMillis());
        calendar1.set(Calendar.HOUR_OF_DAY, 8);*/

        try {
            Intent intent2 = new Intent(context, goToCheckEmailText.class).putExtra("emailOptions", emailOptions);
            sender = PendingIntent.getBroadcast(context, 0, intent2, 0);
            // Schedule the alarm
            alarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            //This will do one alarm to go off when updated
            //alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
            //24 hr interval starting at 8:00 AM
            //alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 90000, sender);//3 minute interval used for testing

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void signOff(){
        //To end the other alarms going off in the background, need to create an alarm with the same name and cancel it
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Intent intent3 = new Intent(mContext, goToCheckEmailText.class);
        sender = PendingIntent.getBroadcast(mContext, 0, intent3, 0);
        alarm = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        alarm.cancel(sender);
    }

    public void getDBInfo(){
        System.out.println("inside get DB info");
        String username = usernameFromEmail(username2);
        DatabaseReference myRef1 = mRef.child("users/" + username);

        /*myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("inside the add listener");
                emailFrequency = dataSnapshot.child("/freq").getValue().toString();
                emailOptions = dataSnapshot.child("/opt").getValue().toString();
                count = Integer.parseInt(dataSnapshot.child("/email").getValue().toString());
                currDateTime2 = dataSnapshot.child("/currentDateTime").getValue().toString();
                accessVar = Boolean.parseBoolean(dataSnapshot.child("/accessVar").getValue().toString());
                System.out.println(emailFrequency + " " + emailOptions + " " + count + " " + currDateTime2 + " " + accessVar);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });*/


        Query topPostsQuery = myRef1;
        final DatabaseReference allUsersRef = myRef1;

        topPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(final DataSnapshot topPostsSnapshot) {
                allUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot allUsersSnapshot) {
                        System.out.println("inside the add listener");
                        emailFrequency = allUsersSnapshot.child("/freq").getValue().toString();
                        emailOptions = allUsersSnapshot.child("/opt").getValue().toString();
                        count = Integer.parseInt(allUsersSnapshot.child("/count").getValue().toString());
                        currDateTime2 = allUsersSnapshot.child("/currentDateTime").getValue().toString();
                        accessVar = Boolean.parseBoolean(allUsersSnapshot.child("/accessVar").getValue().toString());
                        System.out.println(emailFrequency + " " + emailOptions + " " + count + " " + currDateTime2 + " " + accessVar);
                        main();
                    }
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("There was an error: 1");
                    }
                });
            }
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("There was an error: 2");
            }
        });
    }

    public void updateAccessVariable(){
        System.out.println("inside update access variable");
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String username = usernameFromEmail(username2);
        DatabaseReference myRef = database.child("users/" + username);
        myRef.child("/accessVar").setValue(true);
    }

    public void updateGlobalVar(){
        //this will update the email frequency
        SharedPreferences saveFrequency = mContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = saveFrequency.edit();
        editor.putString("checkFrequency", emailFrequency);
        editor.commit();

        //this will update the email options
        SharedPreferences saveOptions = mContext.getSharedPreferences(PREFS_NAME1, 0);
        SharedPreferences.Editor editor1 = saveOptions.edit();
        editor1.putString("checkOptions", emailOptions);
        editor1.commit();

        //this will update the count value
        String value = Integer.toString(count);
        SharedPreferences saveCount = mContext.getSharedPreferences(PREFS_NAME3, 0);
        SharedPreferences.Editor editor2 = saveCount.edit();
        editor2.putString("count", value);
        editor2.commit();

        //this will update the time
        SharedPreferences saveTime = mContext.getSharedPreferences(PREFS_NAME4, 0);
        SharedPreferences.Editor editor3 = saveTime.edit();
        editor3.putString("time", currDateTime2);
        editor3.commit();
    }

    public void main() {
        //This updates the access variable
        updateAccessVariable();
        //This will get the initialize the information for the rest of the app to use
        updateGlobalVar();

        SharedPreferences signoffCheck = mContext.getSharedPreferences(PREFS_NAME2, 0);
        signoff = signoffCheck.getString("checkSignoff", "False");
        userName = usernameFromEmail(username2);
        System.out.println(emailFrequency + " " + emailOptions + " " + count + " " + currDateTime2 + " " + accessVar);

        //This jumps to the corresponding method to perform the needed actions
        if(signoff.equals("True") && !emailFrequency.equals("Demand"))
            signOff();
        else {
            if (emailFrequency.equals("Min"))
                registerAlarmMin(mContext);
            else if (emailFrequency.equals("Day"))
                registerAlarmDay(mContext);
            else if (emailFrequency.equals("Demand"))
                onDemand();
        }
    }
}
