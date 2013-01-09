package se.magnulund.android.wifilistwidget.supplicantchange;

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
public class SupplicantChangeReceiver extends BroadcastReceiver {

    private static final String TAG = SupplicantChangeReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "SUPP_CHANGED");
        intent.setClass(context, SupplicantChangeService.class);
        context.startService(intent);
    }
}
