package se.magnulund.android.wifilistwidget.wifistate;

import android.content.*;
import android.net.wifi.WifiManager;
import android.os.IBinder;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 20/11/2012
 * Time: 23:39
 * To change this template use File | Settings | File Templates.
 */
public class WifiStateReceiver extends BroadcastReceiver {
    public static final String LULCAKE = "wifi_state";

    @Override
    public void onReceive(Context context, Intent intent) {
        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

        intent.setClass(context, WifiStateService.class);
        intent.putExtra(WifiStateReceiver.LULCAKE, wifiState);

        context.startService(intent);
    }
}
