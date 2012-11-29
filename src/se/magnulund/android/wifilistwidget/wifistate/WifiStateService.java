package se.magnulund.android.wifilistwidget.wifistate;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.util.Log;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanReceiver;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanService;

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

        ComponentName wifiStateReceiver = new ComponentName(context, WifiStateReceiver.class);
        int wifiStateStatus = context.getPackageManager().getComponentEnabledSetting(wifiStateReceiver);

        ComponentName wifiScanReceiver = new ComponentName(context, WifiScanReceiver.class);
        int wifiScanStatus = context.getPackageManager().getComponentEnabledSetting(wifiScanReceiver);

        if (intent.getBooleanExtra("stop_services", false)) {
            disableComponent(context, wifiStateStatus, wifiStateReceiver);
            disableComponent(context, wifiScanStatus, wifiScanReceiver);
        } else {
            if (wifiStateStatus != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                context.getPackageManager().setComponentEnabledSetting(wifiStateReceiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                Log.e(TAG, "Enabling " + wifiStateReceiver.getShortClassName());
            }


            int wifiState = intent.getIntExtra("wifi_state", -1);

            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.e(TAG, "WIFI_STATE_ENABLED");
                    enableComponent(context, wifiScanStatus, wifiScanReceiver);
                    WifiManager wifiManager = ( WifiManager )getSystemService(WIFI_SERVICE);
                    wifiManager.startScan();
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    enableComponent(context, wifiScanStatus, wifiScanReceiver);
                    Log.e(TAG, "WIFI_STATE_ENABLING");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    Log.e(TAG, "WIFI_STATE_DISABLING");
                    disableComponent(context, wifiScanStatus, wifiScanReceiver);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.e(TAG, "WIFI_STATE_DISABLED");
                    disableComponent(context, wifiScanStatus, wifiScanReceiver);

                    // Clear list of APs
                    intent.setClass(context, WifiScanService.class);
                    context.startService(intent);
                    break;
                default:
                    break;
            }
        }
    }

    private void enableComponent(Context context, int status, ComponentName component) {
        //Log.e(TAG, " Check if Enabling " + component.getClassName() + " : " + (status > PackageManager.COMPONENT_ENABLED_STATE_ENABLED));
        if (status > PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            Log.e(TAG, "Enabling " + component.getShortClassName());
        }
    }

    private void disableComponent(Context context, int status, ComponentName component) {
        //Log.e(TAG, " Check if Disabling " + component.getClassName() + " : " + (status < PackageManager.COMPONENT_ENABLED_STATE_DISABLED));
        if (status < PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Log.e(TAG, "Disabling " + component.getShortClassName());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        Log.e(TAG, "stopped");
    }


}
