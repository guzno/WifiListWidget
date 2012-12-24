package se.magnulund.android.wifilistwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
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
        boolean updateWidgets = true;
        boolean restartAlarm = false;
        int updateType;
        Integer updateInfo = null;

        switch (alarmType) {
            case AlarmUtility.ALARM_TYPE_BACKOFF: {
                restartAlarm = true;
                updateType = WifiWidgetProvider.UPDATE_ALARM_TYPE_BACKOFF;
            }
            case AlarmUtility.ALARM_TYPE_SCAN_DELAY: {
                updateType = WifiWidgetProvider.UPDATE_ALARM_TYPE_SCAN_DELAY;
                updateInfo = receivedIntent.getIntExtra(AlarmUtility.UPDATE_INFO, -1);
                if ( updateInfo == AlarmUtility.DISABLE_SCANNING ) {
                    restartAlarm = true;
                }
                break;
            }
            case AlarmUtility.ALARM_TYPE_WIFI_STATE: {
                updateType = WifiWidgetProvider.UPDATE_ALARM_TYPE_WIFI_STATE;
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.getWifiState() == receivedIntent.getIntExtra(AlarmUtility.LAST_WIFI_STATE, -1)) {
                    updateWidgets = false;
                    restartAlarm = true;
                }
                break;
            }
            default: {
                Log.e(TAG, "unknown alarm");
                return;
            }
        }

        try {
            if (updateWidgets) {
                WifiWidgetProvider.updateWidgets(context, updateType, updateInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (restartAlarm) {
                switch (alarmType) {
                    case AlarmUtility.ALARM_TYPE_BACKOFF: {
                        int attempt = receivedIntent.getIntExtra(AlarmUtility.IDENTIFIER_ALARM_ATTEMPT, -1);
                        AlarmUtility.scheduleAlarmWithBackoff(context, ++attempt);
                        break;
                    }
                    case AlarmUtility.ALARM_TYPE_SCAN_DELAY: {
                        AlarmUtility.scheduleScanDelayAlarm(context, AlarmUtility.RE_ENABLE_SCANNING);
                        break;
                    }
                    case AlarmUtility.ALARM_TYPE_WIFI_STATE: {
                        if (restartAlarm) {
                            int attempt = receivedIntent.getIntExtra(AlarmUtility.IDENTIFIER_ALARM_ATTEMPT, -1);
                            AlarmUtility.scheduleWifiStateChecker(context, receivedIntent.getIntExtra(AlarmUtility.LAST_WIFI_STATE, -1), ++attempt);
                        }
                        break;
                    }
                    default: {
                        Log.e(TAG, "unknown alarm");
                        break;
                    }
                }

            }
        }
    }
}
