package com.jkls.android.perfectlyplanned;

import android.app.IntentService;
import android.content.Intent;
import android.provider.CalendarContract;

import java.util.Calendar;

import javax.mail.Address;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class NotificationActionService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FOO = "com.jkls.android.perfectlyplanned.action.FOO";
    public static final String ACTION_BAZ = "com.jkls.android.perfectlyplanned.action.BAZ";
    int notificationId= 0;
    // TODO: Rename parameters
    public static final String EXTRA_PARAM1 = "com.jkls.android.perfectlyplanned.extra.PARAM1";
    public static final String EXTRA_PARAM2 = "com.jkls.android.perfectlyplanned.extra.PARAM2";

    public NotificationActionService() {
        super("NotificationActionService");
    }
    String ACTION_1= "action_1";
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            String date = intent.getStringExtra("date");
                    String loc = intent.getStringExtra("loc");;
            String title = intent.getStringExtra("title");

            // addevent(title, date, loc, from);
            //  DebugUtils.log("Received notification action: " + action);
            if (ACTION_1.equals(action)) {
                // TODO: handle action 1.
                System.out.println("heyp0");
                addevent(title, date, loc);
                // If you want to cancel the notification: NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
            }else{
                System.out.println("heyp");
            }
        }
    }
    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void addevent(String title, String date, String loc ){

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

        calendarIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationId++;
        this.startActivity(calendarIntent);
        //   ((Activity)mContext).finish();

        /*var event = CalendarApp.getDefaultCalendar().createAllDayEvent('Apollo 11 Landing',
                new Date('July 20, 1969'));
        Logger.log('Event ID: ' + event.getId());*/

    }

}
