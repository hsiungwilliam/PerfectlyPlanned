package com.jkls.android.perfectlyplanned;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.DebugUtils;
import android.util.Log;
import android.widget.Toast;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sun.mail.util.MailSSLSocketFactory;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.*;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import static android.R.attr.format;
import static android.content.ContentValues.TAG;

/**
 * Created by Jon-kyle on 4/10/2017.
 */

public class CheckEmail extends AsyncTask{
    String username1;
    String password1;
    String currDateTime1;
    Intent intent;
    private Context mContext;
    String title;
    String date;
    String loc;
    String time;
    Address from;
    int count;
    int notificationId = 0;
    public static final String PREFS_NAME = "MyTimeFile";

    public CheckEmail(Context context, String user_name, String pass_word, String currDateTime){
        mContext = context;
        username1 = user_name;
        password1 = pass_word;
        currDateTime1 = currDateTime;
    }

    public class NotificationUtils {
        public String ACTION_1;

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

        /*public class NotificationActionService extends IntentService {
           // System.out.println("I made it againa");
            public NotificationActionService() {
                //System.out.println("I made it againa");
                super("NotificationActionService");
            }

            @Override
            protected void onHandleIntent(Intent intent) {
                //System.out.println("I made it againa");
                String action = intent.getAction();
               // addevent(title, date, loc, from);
                //  DebugUtils.log("Received notification action: " + action);
                if (ACTION_1.equals(action)) {
                    // TODO: handle action 1.

                    // If you want to cancel the notification: NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
                }

            }
        }*/
    }


    public void addevent(String title, String date, String loc, Address from  ){

        /* NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.logo_big);
        mBuilder.setContentTitle("Perfect Planner");
        mBuilder.setContentText(title + "\n" + "from: " + from);
        mBuilder.setLights(Color.RED, 3000, 3000);
        mBuilder.setSound(Uri.parse("uri://sadfasdfasdf.mp3"));
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        System.out.println("I made it");
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(notificationId,mBuilder.build());*/
        System.out.println("I made it againa");
        Calendar cal = Calendar.getInstance();
        Intent calendarIntent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
        calendarIntent.setType("vnd.android.cursor.item/event");
        calendarIntent.putExtra("beginTime", cal.getTimeInMillis());
        calendarIntent.putExtra(CalendarContract.Events.DTSTART, date);
        //calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());
        calendarIntent.putExtra(CalendarContract.Events.TITLE, title);
        calendarIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, loc);
        intent = calendarIntent;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationId++;
        mContext.startActivity(intent);
        //   ((Activity)mContext).finish();

        /*var event = CalendarApp.getDefaultCalendar().createAllDayEvent('Apollo 11 Landing',
                new Date('July 20, 1969'));
        Logger.log('Event ID: ' + event.getId());*/

    }


    public  void check(String host, String storeType, String user, String password) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");

        try {
            //Retrieving the last date and time the app was run
            SharedPreferences lastDate = mContext.getSharedPreferences(PREFS_NAME, 0);
            //The second value for getString is just a random default value
            String lastDateTime = lastDate.getString("currdatetime", "Mon May 01 08:00:00 EDT 2010");
            Date lastdate = format.parse(lastDateTime);

            //create properties field
            //Next line was original code
            //Properties properties = new Properties();

            Properties properties;
            properties = System.getProperties();

            /*properties.setProperty("mail.imap.ssl.enable", "true");
            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", "995");
            properties.put("mail.imap.starttls.enable", "true");*/

            properties.setProperty("mail.store.protocol", "imaps");
            //Set host address
            //properties.setProperty("mail.imaps.host", imaps.gmail.com);
            //Set specified port
            properties.setProperty("mail.imaps.port", "993");
            //Using SSL
            properties.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.setProperty("mail.imaps.socketFactory.fallback", "false");

            //  properties.put( "mail.pop3.auth", "true" );
            Session emailSession = Session.getDefaultInstance(properties, null);

            //create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("imaps");

            System.out.println("right before connect: " + host + " email: " + user + " Password: " + password);
            store.connect(host, user, password);

            //create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            System.out.println("messages.length---" + messages.length);
            int count = 1;

            for (int n = messages.length-1; n >= 0; n--) {
                Message message = messages[n];

                //This gets the date received and converts it to a string
                String currentDate = message.getReceivedDate().toString();
                //This takes the date as a string and formats it to match the last date
                Date currDate = format.parse(currentDate);

                if(currDate.before(lastdate)) {
                    break;
                }
                else{
                    System.out.println("---------------------------------");
                    System.out.println("Email Number " + (messages.length-n));
                    if (message.getSubject() == null)
                        System.out.println("Subject: (no subject)");
                    else
                        System.out.println("Subject: " + message.getSubject());
                    System.out.println("From: " + message.getFrom()[0]);

                    if (message.getSubject() == null);
                    else if(message.getSubject().contains("Event")) {
                        String body = message.getSubject();
                        from = message.getFrom()[0];
                        title = "";
                        loc = "";
                        date = "";
                        time = "";
                        // System.out.print("Text: ");
                        String[] splited = body.split("\\s+");
                        int p = 2;
                        if(splited.length>=2) {
                            title = splited[1];
                            //Iterate for spaces in between Title
                            while (!splited[p].equals("Date")){
                                title = title + " " + splited[p];
                                p++;
                            }
                        }
                        if(splited.length>=4) {
                            p++; //Index after the word "Date"
                            date = splited[p];
                        }
                        if(splited.length>=6){
                            p = p + 2; //Skip previous date and the word "Location"
                            loc = splited[p];
                            p++;
                            while (!splited[p].equals("Time")){
                                loc = loc + " " + splited[p];
                                p++;
                            }
                        }
                        if(splited.length>=8) {
                            p++;
                            time = splited[p] + ":" + splited[p+1];
                        }

                        NotificationUtils news = new NotificationUtils();
                        news.displayNotification(mContext, count);
                        count++;
                    }
                    //This else if checks for invitations sent by outlook
                    else if(message.getSubject().contains("invited")){
                        String subject = message.getSubject();
                        from = message.getFrom()[0];
                        title = "";
                        loc = "";
                        date = "";
                        time = "";
                        String[] splited = subject.split("\\s+");
                        int p = 0;
                        int size = splited.length;
                        //This is searching for the word invited so it can find the title of the event
                        while (p<size){
                            if(splited[p].equals("invited")) {
                                p=p+3;
                                title = splited[p];
                                p++;
                                while (p<size) {
                                    title = title + " " + splited[p];
                                    p++;
                                }
                                break;
                            }
                            p++;
                        }
                        //This is not getting the content of the body!!!!!
                        String body = message.getContent().toString();
                        String[] splitBody = body.split("\\s+");
                        p=0;
                        size = splitBody.length;
                        for (int i = 0; i < size; i++) {
                            System.out.println(splitBody[i] + " ");
                        }
                        //This is searching the body of the email for the date, time, and location
                        while(p<size){
                            if(splitBody[p].equals("When")){
                                System.out.println("It found when");
                                String temp = splitBody[p+1];
                                p=p+2;
                                while(!splitBody[p].equals("Where")) {
                                    temp = temp + " " + splitBody[p];
                                    p++;
                                }
                                loc = splitBody[p+1];
                                p=p+2;
                                while(!splitBody[p].equals("Who")) {
                                    loc = loc + " " + splitBody[p];
                                    p++;
                                }
                                String[] splitTemp = temp.split("\\s+");
                                date = splitTemp[0];
                                for(int i = 1; i<4; i++)
                                    date = date + " " + splitTemp[i];
                                format = new SimpleDateFormat("MM/dd/yyyy");
                                Date d = format.parse(date);
                                date = d.toString();

                                time = splitTemp[4];
                                format = new SimpleDateFormat("hh:mm:a");
                                Date t = format.parse(time);
                                time = t.toString();
                                System.out.println("Title: " + title + " location: " + loc + " date: " + date + " time: " + time);

                                NotificationUtils news = new NotificationUtils();
                                news.displayNotification(mContext, count);
                                count++;
                                break;
                            }
                            p++;
                        }
                    }
                }
            }

            //Saving the current time for next comparison because we have reached an email that has already been checked
            /*SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear().commit();
            editor.putString("currdatetime", currDateTime1);
            editor.commit();*/

            //close the store and folder objects
            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            Log.d(TAG, "hey1" );
            Toast.makeText(mContext, "Email type not currently supported", Toast.LENGTH_SHORT).show();
        } catch (MessagingException e) {
            e.printStackTrace();
            Log.d(TAG,  "hey2");
            Toast.makeText(mContext, "Access denied check credentials", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "hey3");
        }
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {return email;}
    }

    public void getCount(){
        String username = usernameFromEmail(username1);
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
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

    @Override
    protected Object doInBackground(Object[] params) {
        //This will get the values from the database for the username and password
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String username = usernameFromEmail(username1);
        DatabaseReference myRef = database.child("users/" + username);
        getCount();

        myRef.child("/accounts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("count value: " + count);
                for(int i = 0; i<count; i++){
                    String temp = "account" + Integer.toString(i+1);
                    String temp1 = "accountPW" + Integer.toString(i+1);
                    String e = dataSnapshot.child(temp).getValue().toString();
                    String p = dataSnapshot.child(temp1).getValue().toString();
                    System.out.println("checking email: " + e + " and password: " + p);

                    String host = "";
                    if(e.contains("gmail"))
                        host = "imap.gmail.com";
                    if(e.contains("outlook"))
                        host = "imap.outlook.com";
                    if(e.contains("yahoo"))
                        host = "imap.mail.yahoo.com";
                    String mailStoreType = "imaps";
                    System.out.println(host);
                    check(host, mailStoreType, e, p);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        /*String host = "";
        if(username1.contains("gmail"))
            host = "imap.gmail.com";// change accordingly
        if(username1.contains("hotmail"))
            host = "imap.mail.outlook.com";
        if(username1.contains("yahoo"))
            host = "imap.mail.yahoo.com";

        String mailStoreType = "imaps";
        String email = username1;// change accordingly
        String password = password1;// change accordingly
        check(host, mailStoreType, email, password);*/

        return null;
    }
}
