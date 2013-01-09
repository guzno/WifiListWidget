package se.magnulund.android.wifilistwidget.connectivitychange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 09/01/2013
 * Time: 00:15
 * To change this template use File | Settings | File Templates.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private static final String TAG = ConnectivityChangeReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, ConnectivityChangeService.class);
        context.startService(intent);
    }
}
