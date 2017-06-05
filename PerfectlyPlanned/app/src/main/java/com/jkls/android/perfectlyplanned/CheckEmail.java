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
import com.sun.mail.util.MailSSLSocketFactory;

import javax.activation.DataHandler;
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
            Properties properties;
            //new Properties();
            properties = System.getProperties();

            /*properties.setProperty("mail.imap.ssl.enable", "true");
            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", "995");
            properties.put("mail.imap.starttls.enable", "true");*/

            properties.setProperty("mail.store.protocol", "imaps");
            //Set host address
            //properties.setProperty("mail.imaps.host", imaps.gmail.com);
            //Set specified port
            //properties.setProperty("mail.imaps.port", "993");
            //Using SSL
            //properties.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            //properties.setProperty("mail.imaps.socketFactory.fallback", "false");

            //  properties.put( "mail.pop3.auth", "true" );
            Session emailSession = Session.getDefaultInstance(properties, null);


            System.out.println("getStore()");
            //create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("imaps");

            System.out.println("Attempting to Connect to Pop server");
            System.out.println("Host: " + host);
            System.out.println("User: " + user);
            System.out.println("Pass: " + password);
            store.connect(host, user, password);

            System.out.println("Connection Successful");

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

                    //Body
                    DataHandler dataHandler2 = message.getDataHandler();
                    MimeMultipart mimeMultipart2 = (MimeMultipart) dataHandler2.getContent();
                    System.out.println("Pulling apart the multipart");
                    for (int k = 0; k < mimeMultipart2.getCount(); k++) {
                        BodyPart bodyPart = mimeMultipart2.getBodyPart(k);
                        System.out.println("Bodypart type: " + bodyPart.getContentType());
                        if(bodyPart.getContentType().contains("ALTERNATIVE")){
                            System.out.println("Reading body text");
                            System.out.println(bodyPart.getContent().toString());
                        }
                    }

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
                        //This Toast creates "java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()"
                        //Toast.makeText(mContext, "Scanning finished", Toast.LENGTH_SHORT).show();

                        //addevent(title, date, loc, from);
                        //Toast.makeText(CheckEmail.this, "Failed Registration: ", Toast.LENGTH_SHORT).show();

                    }//End of reading the subject line
                    else if(message.getSubject().contains("Making an event for myself")){
                        System.out.println("Checking body");
                        //Get message type
                        //System.out.println("Object Type is :" + message.getContentType());
                        //System.out.println("Content " + message.get
                        DataHandler dataHandler = message.getDataHandler();
                        MimeMultipart mimeMultipart = (MimeMultipart) dataHandler.getContent();
                        System.out.println("Pulling apart the multipart");
                        for (int k = 0; k < mimeMultipart.getCount(); k++) {
                            BodyPart bodyPart = mimeMultipart.getBodyPart(k);
                            //System.out.println("Bodypart type: " + bodyPart.getContentType());
                            if(bodyPart.getContentType().contains("TEXT/PLAIN; charset=UTF-8")){
                                //System.out.println("Reading body text");
                                System.out.println(bodyPart.getContent().toString());
                            }
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
            Toast.makeText(mContext, "Email type not currently supported",
                    Toast.LENGTH_SHORT).show();
        } catch (MessagingException e) {
            e.printStackTrace();
            Log.d(TAG,  "hey2");
            Toast.makeText(mContext, "Access denied check credentials",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "hey3");
        }
    }

    public  void main(String[] args, String currDateTime) {
        String host = "imaps.gmail.com";
        String mailStoreType = "imaps";
        String username = args[1];
        String password = args[2];
        Log.d(TAG, username);
        Log.d(TAG, password);

        /*
        This was the original code:
        String host = "imaps.gmail.com";// change accordingly
        String mailStoreType = "imaps";
        String username = "jkls2713@gmail.com";// change accordingly
        String password = "sticks27";// change accordingly
        */

        check(host, mailStoreType, username, password);
    }

    @Override
    protected Object doInBackground(Object[] params) {
        String host = "";
        if(username1.contains("gmail"))
            host = "imap.gmail.com";// change accordingly
        if(username1.contains("hotmail"))
            host = "imap.mail.outlook.com";
        if(username1.contains("yahoo"))
            host = "imap.mail.yahoo.com";
        if(username1.contains("outlook")) {
            System.out.println("Host is outlook");
            host = "imap.outlook.com";
        }

        System.out.println("Username is " + username1);

        String mailStoreType = "imaps";
        String username = username1;// change accordingly
        String password = password1;// change accordingly
        check(host, mailStoreType, username, password);

        return null;
    }
}
