package se.magnulund.android.wifilistwidget.supplicantstate;

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
public class SupplicantStateReceiver extends BroadcastReceiver {

    private static final String TAG = SupplicantStateReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.e(TAG, "SUPP_STATE_CHANGED");
        intent.setClass(context, SupplicantStateService.class);
        context.startService(intent);
    }
}
