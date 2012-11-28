package se.magnulund.android.wifilistwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 20/11/2012
 * Time: 23:39
 * To change this template use File | Settings | File Templates.
 */
public class WifiStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

        intent.setClass(context, WifiStateService.class);
        intent.putExtra("wifi_state", wifiState);

        context.startService(intent);
    }


}
