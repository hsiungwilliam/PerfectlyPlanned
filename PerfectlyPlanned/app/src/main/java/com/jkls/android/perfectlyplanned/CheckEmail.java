package com.jkls.android.perfectlyplanned;

import android.app.Activity;
import android.app.IntentService;
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
import java.util.Calendar;
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

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthException;
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

import static android.content.ContentValues.TAG;

/**
 * Created by Jon-kyle on 4/10/2017.
 */

//Test Change
public class CheckEmail extends AsyncTask{
    String username1;
    String password1;
    Intent intent;
    private Context mContext;
    String title;
    String date;
    String loc;
    Address from;
    int notificationId = 0;
    public CheckEmail(Context context, String user_name, String pass_word){
        mContext = context;
        username1 = user_name;
        password1 = pass_word;


    }



    public class NotificationUtils {
       public String ACTION_1;

        public  void displayNotification(Context context) {
            final int NOTIFICATION_ID = 1;
          //  System.out.println("I made it againa");
             ACTION_1 = "action_1";
            Intent action1Intent = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_1);
            action1Intent.putExtra("title", title);
            action1Intent.putExtra("date", date);
            action1Intent.putExtra("loc", loc);


            PendingIntent action1PendingIntent = PendingIntent.getService(context, 0,
                    action1Intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.logo_big)
            .setContentTitle("Perfect Planner")
            .setContentText(title + "\n" + "from: " + from)
            .setContentIntent(action1PendingIntent)
            .setLights(Color.RED, 3000, 3000);
                            //.addAction(new NotificationCompat.Action(R.drawable.logo_small,
                                //   "Action 1", action1PendingIntent));

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




    public  void check(String host, String storeType, String user,
                             String password)
    {

        try {

            //create properties field
            Properties properties = new Properties();
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
            Session emailSession = Session.getDefaultInstance(properties);

            //create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("imaps");

         store.connect(host, user, password);

            //create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            System.out.println("messages.length---" + messages.length);

            for (int i = 0, n = messages.length; i < n; i++) {
                Message message = messages[i];

                    System.out.println("---------------------------------");
                    System.out.println("Email Number " + (i + 1));
                    System.out.println("Subject: " + message.getSubject());
                    System.out.println("From: " + message.getFrom()[0]);

                if(message.getSubject().contains("Event")) {
                    String body = message.getSubject();
                    from = message.getFrom()[0];
                   title = "";
                   loc = "";
                    date = "";
              // System.out.print("Text: ");
                String[] splited = body.split("\\s+");
                if(splited.length>=2)
                    title = splited[1];
                if(splited.length>=4)
                    date = splited[3];
                if(splited.length>=6)
                    loc = splited[5];
                NotificationUtils news = new NotificationUtils();
                    news.displayNotification(mContext);
                    Toast.makeText(mContext, "Scanning finished",
                            Toast.LENGTH_SHORT).show();
                // addevent(title, date, loc, from);
                    //Toast.makeText(CheckEmail.this, "Failed Registration: ", Toast.LENGTH_SHORT).show();

                }
            }

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

    public  void main(String[] args) {

        String host = "imaps.gmail.com";// change accordingly
        String mailStoreType = "imaps";
        String username = "jkls2713@gmail.com";// change accordingly
        String password = "sticks27";// change accordingly

        check(host, mailStoreType, username, password);

    }

    @Override
    protected Object doInBackground(Object[] params) {
        String host = "";
        if(username1.contains("gmail"))
        host = "imap.gmail.com";// change accordingly
        if(username1.contains("hotmail")){
            host = "imap.mail.outlook.com";
            System.out.println("set it homie");
        }
        if(username1.contains("yahoo")){
            host = "imap.mail.yahoo.com";
            System.out.println("set it homie");
        }

        String mailStoreType = "imaps";
        String username = username1;// change accordingly
        String password = password1;// change accordingly
         check(host, mailStoreType, username, password);

        return null;
    }


}
