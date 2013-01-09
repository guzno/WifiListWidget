package se.magnulund.android.wifilistwidget.supplicantchange;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 09/01/2013
 * Time: 00:16
 * To change this template use File | Settings | File Templates.
 */
public class SupplicantChangeService extends IntentService {

    private static final String TAG = "SupplicantChangeService";

    public SupplicantChangeService() {
        super("wifilistwidget_SupplicantChangeService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG, "Supplicant connected: "+intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false));
    }
}
