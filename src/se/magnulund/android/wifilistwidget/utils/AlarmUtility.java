package se.magnulund.android.wifilistwidget.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import se.magnulund.android.wifilistwidget.AlarmReceiver;
import se.magnulund.android.wifilistwidget.MainActivity;
import se.magnulund.android.wifilistwidget.R;

import java.util.Calendar;

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

    public static final String IDENTIFIER_ALARM_ATTEMPT = "alarm_attempt";
    public static final String ALARM_IDENTIFIER = "alarm_identifier";
    public static final String UPDATE_INFO = "update_info";

    public static final String SCANNING_ENABLED = "scanning_enabled";

    public static final int DISABLE_SCANNING = 0;
    public static final int RE_ENABLE_SCANNING = 1;

    public static final String LAST_WIFI_STATE = "last_wifi_state";

    public static final int ALARM_TYPE_BACKOFF = 1;
    public static final int ALARM_TYPE_SCAN_DELAY = 2;
    public static final int ALARM_TYPE_WIFI_STATE = 3;


    private static PendingIntent createPendingIntentWithInfo(Context context, int requestCode, int attempt) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(IDENTIFIER_ALARM_ATTEMPT, attempt);

        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void scheduleAlarm(Context context, int alarmType) {
        switch (alarmType){
            case ALARM_TYPE_BACKOFF: {
                scheduleAlarmWithBackoff(context, 1);
                break;
            }
            case ALARM_TYPE_SCAN_DELAY: {
                 scheduleScanDelayAlarm(context, DISABLE_SCANNING);
            }
            default:
                break;
        }

    }

    public static void scheduleAlarmWithBackoff(Context context, int attempt) {
        if (attempt > 5) {
            Log.e(TAG, "alarm reached max retries - bailing out");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(IDENTIFIER_ALARM_ATTEMPT, attempt);
        intent.putExtra(ALARM_IDENTIFIER, ALARM_TYPE_BACKOFF);

        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, 500*attempt);
        Log.e(TAG, "registered new alarm(" + attempt + ") at time " + cal.getTime().toString());

        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
    }

    public static void scheduleWifiStateChecker(Context context, int wifiState, int attempt) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if ( attempt > 15 ) {
            Log.e(TAG, "Stopping wifi state alarm to conserve resources...");
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(LAST_WIFI_STATE, wifiState);
        intent.putExtra(ALARM_IDENTIFIER, ALARM_TYPE_WIFI_STATE);
        intent.putExtra(IDENTIFIER_ALARM_ATTEMPT, attempt);

        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        int delay = ( attempt > 10 ) ? 1000 : 300;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, delay);
        Log.e(TAG, "registered wifi state alarm (" + attempt + ") at time " + cal.getTime().toString());
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
    }

    public static void scheduleScanDelayAlarm(Context context, int alarmAction) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(ALARM_IDENTIFIER, ALARM_TYPE_SCAN_DELAY);
        intent.putExtra(UPDATE_INFO, alarmAction);

        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        int delay;
        switch (alarmAction) {
            case DISABLE_SCANNING:
                delay = 100;
                break;
            case RE_ENABLE_SCANNING:
                delay = 5000;
                break;
            default:
                delay = 100;
                break;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, delay);
        Log.e(TAG, "registered Scan delay alarm at time " + cal.getTime().toString());
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
    }
}
