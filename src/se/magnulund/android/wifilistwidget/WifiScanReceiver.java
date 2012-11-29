package se.magnulund.android.wifilistwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 20/11/2012
 * Time: 23:43
 * To change this template use File | Settings | File Templates.
 */
public class WifiScanReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        intent.setClass(context, WifiScanService.class);
        intent.putExtra("new_scan_results", true);
        context.startService(intent);
    }
}
