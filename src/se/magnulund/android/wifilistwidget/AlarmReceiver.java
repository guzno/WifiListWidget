package se.magnulund.android.wifilistwidget;

import android.appwidget.AppWidgetManager;
import android.content.*;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import se.magnulund.android.wifilistwidget.utils.AlarmUtility;
import se.magnulund.android.wifilistwidget.widget.WifiWidgetProvider;

/**
 * Created with IntelliJ IDEA.
 * User: erikeelde
 * Date: 5/12/2012
 * Time: 24:15
 * To change this template use File | Settings | File Templates.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent receivedIntent) {
        int alarmType = receivedIntent.getIntExtra(AlarmUtility.ALARM_IDENTIFIER, -1);

        if (alarmType == AlarmUtility.ALARM_TYPE_SCAN_DELAY
                && receivedIntent.getBooleanExtra(AlarmUtility.REENABLE_SCANNING, false) ) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(AlarmUtility.SCANNING_ENABLED, true);
                editor.commit();
        }

        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, WifiWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            for (int i = 0; i < appWidgetIds.length; ++i) {
                RemoteViews rv = WifiWidgetProvider.getRemoteViews(context, appWidgetIds[i]);
                appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            switch (alarmType) {
                case AlarmUtility.ALARM_TYPE_BACKOFF: {
                    int attempt = receivedIntent.getIntExtra(AlarmUtility.IDENTIFIER_ALARM_ATTEMPT, -1);
                    AlarmUtility.scheduleAlarmWithBackoff(context, ++attempt);
                    break;
                }
                case AlarmUtility.ALARM_TYPE_SCAN_DELAY: {
                    if (!receivedIntent.getBooleanExtra(AlarmUtility.REENABLE_SCANNING, true)){
                        AlarmUtility.scheduleScanDelayAlarm(context, false);
                    }
                    break;
                }
            }
        }
    }
}
