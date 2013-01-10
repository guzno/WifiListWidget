package se.magnulund.android.wifilistwidget.supplicantstate;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 09/01/2013
 * Time: 00:16
 * To change this template use File | Settings | File Templates.
 */
public class SupplicantStateService extends IntentService {

    private static final String TAG = "SupplicantStateService";

    public SupplicantStateService() {
        super("wifilistwidget_SupplicantStateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
        /*Log.e(TAG, "Supplicant state: "+supplicantState.toString());
        String state = "";
        switch (supplicantState) {
            case ASSOCIATED: {
                state = "ASSOCIATED";
                break;
            }
            case ASSOCIATING: {
                state = "ASSOCIATING";
                break;
            }
            case AUTHENTICATING: {
                state = "AUTHENTICATING";
                break;
            }
            case COMPLETED: {
                state = "COMPLETED";
                break;
            }
            case DISCONNECTED: {
                state = "DISCONNECTED";
                break;
            }
            case DORMANT: {
                state = "DORMANT";
                break;
            }
            case FOUR_WAY_HANDSHAKE: {
                state = "FOUR_WAY_HANDSHAKE";
                break;
            }
            case GROUP_HANDSHAKE: {
                state = "GROUP_HANDSHAKE";
                break;
            }
            case INACTIVE: {
                state = "INACTIVE";
                break;
            }
            case INTERFACE_DISABLED: {
                state = "INTERFACE_DISABLED";
                break;
            }
            case INVALID: {
                state = "INVALID";
                break;
            }
            case SCANNING: {
                state = "SCANNING";
                break;
            }
            case UNINITIALIZED: {
                state = "UNINITIALIZED";
                break;
            }

        }*/
    }
}
