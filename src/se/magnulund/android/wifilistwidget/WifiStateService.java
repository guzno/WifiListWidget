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

        ComponentName wifiScanReceiver = new ComponentName(context, WifiScanReceiver.class);
        int wifiScanStatus = context.getPackageManager().getComponentEnabledSetting(wifiScanReceiver);

        ComponentName wifiConfigReceiver = new ComponentName(context, WifiNetworkConfigReceiver.class);
        int wifiConfigStatus = context.getPackageManager().getComponentEnabledSetting(wifiConfigReceiver);

        if (intent.getBooleanExtra("stop_services", false)) {
            disableComponent(context, wifiScanStatus, wifiScanReceiver);
            disableComponent(context, wifiConfigStatus, wifiConfigReceiver);
        } else {

            int wifiState = intent.getIntExtra("wifi_state", -1);

            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.e(TAG, "WIFI_STATE_ENABLED");
                    enableComponent(context, wifiScanStatus, wifiScanReceiver);
                    enableComponent(context, wifiConfigStatus, wifiConfigReceiver);
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    enableComponent(context, wifiScanStatus, wifiScanReceiver);
                    enableComponent(context, wifiConfigStatus, wifiConfigReceiver);
                    Log.e(TAG, "WIFI_STATE_ENABLING");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    Log.e(TAG, "WIFI_STATE_DISABLING");
                    disableComponent(context, wifiScanStatus, wifiScanReceiver);
                    disableComponent(context, wifiConfigStatus, wifiConfigReceiver);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.e(TAG, "WIFI_STATE_DISABLED");
                    disableComponent(context, wifiScanStatus, wifiScanReceiver);
                    disableComponent(context, wifiConfigStatus, wifiConfigReceiver);
                    break;
                default:
                    break;
            }
        }
    }

    private void enableComponent(Context context, int status, ComponentName component){
        if (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            Log.e(TAG, "Enabling "+component.getClassName());
        }
    }

    private void disableComponent(Context context, int status, ComponentName component){
        if (status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Log.e(TAG, "Disabling "+component.getClassName());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        Log.e(TAG, "stopped");
    }
}
