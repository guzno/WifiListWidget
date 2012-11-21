package se.magnulund.android.wifilistwidget;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 21/11/2012
 * Time: 10:32
 * To change this template use File | Settings | File Templates.
 */
public class WifiStateService extends IntentService {

    private static final String TAG = "WifiStateService";

    public WifiStateService() {
        super("WifiListWidget_WifiUpdateService");
        Log.e(TAG, "Constructed");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // hittade på: http://stackoverflow.com/questions/6529276/android-how-to-unregister-a-receiver-created-in-the-manifest
        Context context = getApplicationContext();
        if (intent.getBooleanExtra("stop_service", false)) {
            ComponentName wifiScanReceiver = new ComponentName(context, WifiScanReceiver.class);
            context.getPackageManager().setComponentEnabledSetting(wifiScanReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            ComponentName wifiStateReceiver = new ComponentName(context, WifiStateReceiver.class);
            context.getPackageManager().setComponentEnabledSetting(wifiStateReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } else {
            int wifiState = intent.getIntExtra("wifi_state", -1);
            ComponentName component = new ComponentName(context, WifiScanReceiver.class);
            int status = context.getPackageManager().getComponentEnabledSetting(component);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.e(TAG, "WIFI_STATE_ENABLED");
                    if (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                        context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                        Log.e(TAG, "Enabling WifiScanReceiver");
                    }
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    Log.e(TAG, "WIFI_STATE_ENABLING");
                    Log.e(TAG, "Enabling WifiScanReceiver");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    Log.e(TAG, "WIFI_STATE_DISABLING");
                    Log.e(TAG, "Disabling WifiScanReceiver");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.e(TAG, "WIFI_STATE_DISABLED");
                    if (status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                        context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                        Log.e(TAG, "Disabling WifiScanReceiver");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        Log.e(TAG, "stopped");
    }
}
