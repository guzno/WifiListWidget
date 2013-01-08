package se.magnulund.android.wifilistwidget.supplicantstate;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 09/01/2013
 * Time: 00:16
 * To change this template use File | Settings | File Templates.
 */
public class SupplicantStateService extends IntentService {

    public SupplicantStateService() {
        super("wifilistwidget_SupplicantStateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
