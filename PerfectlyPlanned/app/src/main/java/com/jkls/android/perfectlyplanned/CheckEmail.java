package com.jkls.android.perfectlyplanned;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import android.os.Handler;

import android.content.Intent;
import android.os.Looper;
import android.provider.CalendarContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;

import javax.mail.Address;
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

public class CheckEmail extends AsyncTask{
    String username1;
    String password1;
    String currDateTime1;
    String currDateTime2;
    Intent intent;
    private Context mContext;
    private DatabaseReference mRef;
    String title;
    String date;
    String loc;
    String time;
    Address from;
    int count;
    Handler mHandler;
    int notificationId = 0;
    public static final String PREFS_NAME = "MyTimeFile";
    public static final String PREFS_NAME1 = "MyBListFile";
    public static final String PREFS_NAME2 = "MyWListFile";
    public static final String PREFS_NAME3 = "MyBlackListFile";
    public static final String PREFS_NAME4 = "MyWhiteListFile";

    public CheckEmail(Context context, String user_name, String pass_word, String currDateTime){
        mContext = context;
        username1 = user_name;
        password1 = pass_word;
        currDateTime1 = currDateTime;
        mRef = FirebaseDatabase.getInstance().getReference();
        mHandler = new Handler();
    }

    public class NotificationUtils {
        public String ACTION_1;

        public  void displayNotification(Context context, int ID, String sender) {
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

            int result = checkLists(sender);
            //0 means not on any lists so a notification is created
            //1 means on the white list so the event goes straight to the calendar
            //2 means on the black list so nothing happens
            if(result == 0) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            } else if(result == 1) {
                System.out.println("This event found in the inbox is from a sender on the white list");
                addEventToCalendar();
            } else if(result == 2){
                System.out.println("This event found in the inbox is from a sender on the black list");
            }
        }

        //Checks both the back list and the white list for emails and compares those to the sender
        public int checkLists(String sender){
            //This is getting the black and white list counts
            SharedPreferences temp1 = mContext.getSharedPreferences(PREFS_NAME3, 0);
            String BListCount = temp1.getString("count", "0");
            int bListCount = Integer.parseInt(BListCount);
            SharedPreferences temp2 = mContext.getSharedPreferences(PREFS_NAME4, 0);
            String WListCount = temp2.getString("count", "0");
            int wListCount = Integer.parseInt(WListCount);

            //This is getting the black and white list emails and comparing them to sender
            SharedPreferences temp3 = mContext.getSharedPreferences(PREFS_NAME1, 0);
            for(int i = 0;i<bListCount; i++){
                String email = "email" + Integer.toString(i);
                String BListEmails = temp3.getString(email, "none");
                System.out.println("BList email: " + BListEmails);
                if(sender.equals(BListEmails))
                    return 2;
            }
            SharedPreferences temp4 = mContext.getSharedPreferences(PREFS_NAME2, 0);
            for(int i = 0;i<bListCount; i++){
                String email = "email" + Integer.toString(i);
                String WListEmails = temp4.getString(email, "none");
                System.out.println("WList email: " + WListEmails);
                if(sender.equals(WListEmails))
                    return 1;
            }
            return 0;
        }

        public void addEventToCalendar(){
            System.out.println("inside of add event to calendar");
            try {
                //This is splitting up the time into hours and minutes
                String[] splited = time.split(":");
                int hour = Integer.parseInt(splited[0]);
                int minute = Integer.parseInt(splited[1]);

                //This is creating the calendar instance
                TimeZone tz = TimeZone.getDefault();
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                String[] parts = date.split("/");
                int year = Integer.parseInt(parts[2]);
                int month = Integer.parseInt(parts[0]);
                int day = Integer.parseInt(parts[1]);

                //This is getting the date and time for the begin time
                long startTime = 0;
                long endTime = 0;
                Calendar beginTime = Calendar.getInstance();
                Calendar EndTime = Calendar.getInstance();
                if (splited[2].equals("PM"))
                    hour = hour+12;
                beginTime.set(year, month-1, day, hour, minute);
                startTime = beginTime.getTimeInMillis();
                EndTime.set(year, month-1, day, hour+1, minute);
                endTime = EndTime.getTimeInMillis();
                //For some reason, you can see from the print statement below that the month is printing out
                //correctly but when it is placing the event in the calendar it is a month off, so that's why
                //the begin time and the end time are month-1. I don't know why it is doing this.
                System.out.println(year + " " + month + " " + day + " " + hour + " " + minute);

                //This is placing the values inside the calendar
                ContentResolver cr = mContext.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, startTime);
                values.put(CalendarContract.Events.DTEND, endTime);
                values.put(CalendarContract.Events.TITLE, title);
                values.put(CalendarContract.Events.CALENDAR_ID, 1);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, tz.getID());
                values.put(CalendarContract.Events.EVENT_LOCATION, loc);
                notificationId++;
                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                //The returned Uri contains the content-retriever URI for the newly-inserted event, including its id
                long eventID = Long.parseLong(uri.getLastPathSegment());

            } catch (SecurityException e) {
            } catch (Exception e) {}
        }
    }


    //This does not do anything, it was part of the original code
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

    public void getDateTime(){
        SharedPreferences getTime = mContext.getSharedPreferences(PREFS_NAME, 0);
        currDateTime2 = getTime.getString("time", "Sun Jan 01 08:00:00 EDT 2017");
    }

    //This checks all emails in the inbox for events
    public  void check(String host, String storeType, String user, String password) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
        try {
            //Retrieving the last date and time the app was run
            getDateTime();
            Date lastdate = format.parse(currDateTime2);

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

                //This ensure the app is only looking at emails that have been received since the last check
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

                        //This is getting the whole "from" from the email and splitting it by the spaces
                        String[] str = message.getFrom()[0].toString().split("\\s+");
                        //This is grabbing the actual email address from the "from" string
                        String temp = str[2];
                        StringBuilder sb = new StringBuilder(temp);
                        //This is deleting the < character
                        sb.deleteCharAt(0);
                        int size = sb.length();
                        //this is deleting the > character
                        sb.deleteCharAt(size-1);
                        NotificationUtils news = new NotificationUtils();
                        news.displayNotification(mContext, count, sb.toString());
                        count++;
                    }
                }
            }
            //close the store and folder objects
            emailFolder.close(false);
            store.close();
            System.out.println("done with checking emails");

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

    //When the check has completed, a toast will appear letting the user know
    protected void onHandleIntent() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "Email Check Complete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected Object doInBackground(Object[] params) {
        String host = "";
        if(username1.contains("gmail"))
            host = "imap.gmail.com";// change accordingly
        if(username1.contains("outlook"))
            host = "imap.outlook.com";
        if(username1.contains("yahoo"))
            host = "imap.mail.yahoo.com";

        String mailStoreType = "imaps";
        String email = username1;// change accordingly
        String password = password1;// change accordingly
        check(host, mailStoreType, email, password);
        onHandleIntent();
        return null;
    }
}
