package se.magnulund.android.wifilistwidget.wifistate;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import se.magnulund.android.wifilistwidget.utils.ComponentManager;
import se.magnulund.android.wifilistwidget.widget.WifiWidgetProvider;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanReceiver;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanService;

public class WifiStateService extends IntentService {
    private static final String TAG = "WifiStateService";
    public static final Object wifiManagerLock = new Object();

    public WifiStateService() {
        super("WifiListWidget_WifiStateService");
        //Log.e(TAG, "Constructed");
    }

    private WifiManager wifiManager;

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        if (intent.getBooleanExtra("stop_services", false)) {
            ComponentManager.disableComponent(context, WifiStateReceiver.class);
            ComponentManager.disableComponent(context, WifiScanReceiver.class);
        } else {

            ComponentManager.enableComponent(context, WifiStateReceiver.class);

            int wifiState = intent.getIntExtra(WifiStateReceiver.WIFI_STATE, -1);
            if (wifiState == -1) {
                if (wifiManager == null) {
                    wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                }
                wifiState = wifiManager.getWifiState();
            }

            synchronized (WifiStateService.wifiManagerLock) {

                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.e(TAG, "WIFI_STATE_ENABLED");
                        ComponentManager.enableComponent(context, WifiScanReceiver.class);
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        Log.e(TAG, "WIFI_STATE_ENABLING");
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        Log.e(TAG, "WIFI_STATE_DISABLING");
                        ComponentManager.disableComponent(context, WifiScanReceiver.class);
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.e(TAG, "WIFI_STATE_DISABLED");
                        ComponentManager.disableComponent(context, WifiScanReceiver.class);

                        // Clear list of APs
                        intent.setClass(context, WifiScanService.class);
                        context.startService(intent);
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        break;
                    default:
                        throw new UnsupportedOperationException("that's no numbar" + wifiState);
                }

                Log.e(TAG, "Wifi state changed, checking if widget is active");

                WifiWidgetProvider.updateWidgets(context, WifiWidgetProvider.UPDATE_WIFI_STATE_CHANGED, wifiState);

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        //Log.e(TAG, "stopped");
    }


}
