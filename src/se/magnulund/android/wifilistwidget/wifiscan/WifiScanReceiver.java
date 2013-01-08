package se.magnulund.android.wifilistwidget.wifiscan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 20/11/2012
 * Time: 23:43
 * To change this template use File | Settings | File Templates.
 */
public class WifiScanReceiver extends BroadcastReceiver {
    private static final String TAG = WifiScanReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "SCAN RECEIVED");

        intent.setClass(context, WifiScanService.class);
        context.startService(intent);
    }
}
