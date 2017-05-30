package com.jkls.android.perfectlyplanned;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by eande on 5/25/2017.
 */

public class InitializationActivity extends AsyncTask{
    private static final String TAG = "InitializationActivity";
    String username1;
    String password1;
    String currDateTime1;
    static String username2;
    static String password2;
    static String currDateTime2;
    String emailOptions;
    String emailFrequency;
    Boolean settingsChange1;
    AlarmManager alarm = null;
    PendingIntent sender;
    private Context mContext;
    public static final String PREFS_NAME = "MyEmailFrequencyFile";
    public static final String PREFS_NAME1 = "MyCheckOptionsFile";

    public InitializationActivity(Context context, String user_name, String pass_word, String currDateTime, Boolean settingsChange){
        mContext = context;
        username1 = user_name;
        username2 = user_name;
        password1 = pass_word;
        password2 = pass_word;
        currDateTime1 = currDateTime;
        currDateTime2 = currDateTime;
        settingsChange1 = settingsChange;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        System.out.println("made it into the background");
        main();
        return null;
    }

    //Starts checking the emails/texts on demand
    public void onDemand() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Intent intent3 = new Intent(mContext, goToCheckEmail.class).putExtra("emailOptions", emailOptions);
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
            Intent intent1 = new Intent(context, goToCheckEmail.class).putExtra("emailOptions", emailOptions);
            sender = PendingIntent.getBroadcast(context, 0, intent1, 0);
            // Schedule the alarm
            alarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1800000, sender);//30min interval
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //When an alarm is on, this is where it goes to check the emails
    public static class goToCheckEmail extends BroadcastReceiver {
        public goToCheckEmail() {
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "StartEmailCheck");
            String emailOptions = intent.getExtras().getString("emailOptions");
            System.out.println("Email option is: " + emailOptions);
            if(emailOptions.equals("Email"))
                new CheckEmail(context, username2, password2, currDateTime2).execute("");
            else if (emailOptions.equals("Text"))
                new CheckText(context, username2, password2, currDateTime2).execute("");
            else if (emailOptions.equals("Both")) {
                new CheckEmail(context, username2, password2, currDateTime2).execute("");
                new CheckText(context, username2, password2, currDateTime2).execute("");
            }
        }
    }

    //Starts checking the emails/texts once a day
    public void registerAlarmDay(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        try {
            Intent intent2 = new Intent(context, goToCheckEmail.class).putExtra("emailOptions", emailOptions);
            sender = PendingIntent.getBroadcast(context, 0, intent2, 0);
            // Schedule the alarm
            alarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            //alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, calendar.getTimeInMillis(, 86400000, sender2);//24 hr interval
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, (calendar.getTimeInMillis()), 90000, sender);//3 minute interval used for testing

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void cancelAlarm() {
        alarm.cancel(sender);
    }

    public void main() {
        //This will get the initial values stored in the app
        SharedPreferences emailCheckFrequency = mContext.getSharedPreferences(PREFS_NAME, 0);
        emailFrequency = emailCheckFrequency.getString("checkFrequency", "OnDemand");
        System.out.println(emailFrequency);
        SharedPreferences emailCheckOptions = mContext.getSharedPreferences(PREFS_NAME1, 0);
        emailOptions = emailCheckOptions.getString("checkOptions", "Both");
        System.out.println(emailOptions);

        //This jumps to the corresponding method to perform the needed actions
        if(emailFrequency.equals("Min"))
            registerAlarmMin(mContext);
        else if(emailFrequency.equals("Day"))
            registerAlarmDay(mContext);
        else if(emailFrequency.equals("Demand"))
            onDemand();
    }
}
