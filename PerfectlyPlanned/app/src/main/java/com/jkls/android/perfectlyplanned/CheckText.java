package com.jkls.android.perfectlyplanned;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Address;

/**
 * Created by eande on 5/29/2017.
 */

public class CheckText extends AsyncTask {
    String username1;
    String password1;
    String currDateTime1;
    private Context mContext;
    public String ACTION_1;
    String title;
    String date;
    String loc;
    String time;
    String from;
    public static final String PREFS_NAME = "MyTimeFile";
    private static final String TAG = "CheckText";

    public CheckText(Context context, String user_name, String pass_word, String currDateTime){
        mContext = context;
        username1 = user_name;
        password1 = pass_word;
        currDateTime1 = currDateTime;
    }

    public  void displayNotification(Context context, int ID) {
        int NOTIFICATION_ID = ID;

        ACTION_1 = Integer.toString(ID);
        Intent action1Intent = new Intent(context, NotificationActionService.class).setAction(ACTION_1);
        action1Intent.putExtra("title", title);
        action1Intent.putExtra("date", date);
        action1Intent.putExtra("loc", loc);
        action1Intent.putExtra("time", time);

        PendingIntent action1PendingIntent = PendingIntent.getService(context, 0, action1Intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo_big)
                .setContentTitle("Perfect Planner")
                .setAutoCancel(true)
                .setContentText(title + "\n" + "from: " + from)
                .setContentIntent(action1PendingIntent)
                .setLights(Color.RED, 3000, 3000);
        //.addAction(new NotificationCompat.Action(R.drawable.logo_small, "Action 1", action1PendingIntent));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        System.out.println("we made it");
    }

    public void check() {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
        try{
            //Retrieving the last date and time the app was run
            SharedPreferences lastDate = mContext.getSharedPreferences(PREFS_NAME, 0);
            //The second value for getString is just a random default value
            String lastDateTime = lastDate.getString("currdatetime", "Mon May 01 08:00:00 EDT 2010");
            Date lastdate = format.parse(lastDateTime);

            //This is creating a list of all the texts and their information
            ContentResolver contentResolver = mContext.getContentResolver();
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor c = contentResolver.query(uri, null, null, null, null);
            int count = 1;

            // Read the sms data and checks for events starting with the most recent
            if(c.moveToFirst()) {

                for (int i = 0; i < /*c.getCount()*/50; i++) {
                    SMSData sms = new SMSData();
                    sms.setBody(c.getString(c.getColumnIndexOrThrow("body")).toString());
                    sms.setNumber(c.getString(c.getColumnIndexOrThrow("address")).toString());
                    System.out.println("count: " + i + " number: " + sms.getNumber() + " body: " + sms.getBody());

                    String date1 =  c.getString(c.getColumnIndex("date"));
                    Long timeStamp = Long.parseLong(date1);
                    Date timestamp = new Date(timeStamp);
                    String currentDate = timestamp.toString();
                    //This takes the date as a string and formats it to match the last date stored locally
                    Date currDate = format.parse(currentDate);
                    System.out.println(currDate);

                    if(currDate.before(lastdate)) {
                        break;
                    }

                    if (sms.getBody().contains("Event:")) {
                        String body = sms.getBody();
                        from = sms.getNumber();
                        title = "";
                        loc = "";
                        date = "";
                        time = "";
                        String[] splited = body.split("\\s+");
                        int p = 2;
                        if (splited.length >= 2) {
                            title = splited[1];
                            //Iterate for spaces in between Title
                            while (!splited[p].equals("Date")) {
                                title = title + " " + splited[p];
                                p++;
                            }
                        }
                        if (splited.length >= 4) {
                            p++; //Index after the word "Date"
                            date = splited[p];
                        }
                        if (splited.length >= 6) {
                            p = p + 2; //Skip previous date and the word "Location"
                            loc = splited[p];
                            p++;
                            while (!splited[p].equals("Time")) {
                                loc = loc + " " + splited[p];
                                p++;
                            }
                        }
                        if (splited.length >= 8) {
                            p++;
                            time = splited[p] + ":" + splited[p + 1];
                        }
                        displayNotification(mContext, count);
                        count++;
                    }
                    c.moveToNext();
                }
            }

            //Saving the current time for next comparison because we have reached an email that has already been checked
            /*SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString("currdatetime", currDateTime1);
            editor.commit();*/

            c.close();
        }catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "error");
        }
    }
    public class SMSData extends IntentService {
        // Number from witch the sms was send
        private String number;
        // SMS text body
        private String body;

        //The following two methods are just to make the manifest happy
        public SMSData(){super("NotificationActionService");}
        @Override
        protected void onHandleIntent(Intent intent) {}

        public String getNumber() {return number;}
        public void setNumber(String number) {this.number = number;}
        public String getBody() {return body;}
        public void setBody(String body) {this.body = body;}
    }

    @Override
    protected Object doInBackground(Object[] params) {
        check();
        return null;
    }
}