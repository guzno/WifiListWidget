package se.magnulund.android.wifilistwidget.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import se.magnulund.android.wifilistwidget.AlarmReceiver;
import se.magnulund.android.wifilistwidget.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: erikeelde
 * Date: 3/22/12
 * Time: 12:28
 * To change this template use File | Settings | File Templates.
 */
public class AlarmUtility {
    private static final String TAG = AlarmUtility.class.getSimpleName();
    private static final int ALARM_REQUEST_CODE = R.id.alarmRequestCode;

    public static final String IDENTIFIER_ALARM_MESSAGE = "alarm_message";

    private static PendingIntent createPendingIntentWithInfo(Context context, int requestCode, String message) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(IDENTIFIER_ALARM_MESSAGE, message);

        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void scheduleAlarm(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(IDENTIFIER_ALARM_MESSAGE, "dummy message");

        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 30);
        Log.e(TAG, "registered new alarm at time " + cal.getTime().toString());

        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

    }

    public static void clearAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        PendingIntent displayIntent = createPendingIntentWithInfo(context, ALARM_REQUEST_CODE, null);
        alarmManager.cancel(displayIntent);
    }
}
