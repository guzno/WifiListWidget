package se.magnulund.android.wifilistwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 22/11/2012
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */
public class WifiNetworkConfigReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, WifiNetworkConfigService.class);

        context.startService(intent);
    }
}
