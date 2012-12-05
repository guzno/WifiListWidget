package se.magnulund.android.wifilistwidget.wifiscan;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import se.magnulund.android.wifilistwidget.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WifiScanService extends IntentService {

    private static final String TAG = "WifiScanService";

    public static final String KEY_PREF_MERGE_ACCESS_POINTS = "merge_access_points";

    public static final int WIFI_SIGNAL_THRESHOLD_BEST = -50;
    public static final int WIFI_SIGNAL_THRESHOLD_GOOD = -65;
    public static final int WIFI_SIGNAL_THRESHOLD_OK = -75;

    public static final int WIFI_SIGNAL_BEST = 4;
    public static final int WIFI_SIGNAL_GOOD = 3;
    public static final int WIFI_SIGNAL_OK = 2;
    public static final int WIFI_SIGNAL_POOR = 1;

    public WifiScanService() {
        super("WifiListWidget_WifiScanService");
        //Log.e(TAG, "Constructed");
    }

    /*
     ScanResult:
       public String	BSSID	The address of the access point.
       public String	SSID	The network name.
       public String	capabilities	Describes the authentication, key management, and encryption schemes supported by the access point.
       public int	frequency	The frequency in MHz of the channel over which the client is communicating with the access point.
       public int	level	The detected signal level in dBm.
    */

    @Override
    protected void onHandleIntent(Intent intent) {

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (wifiManager == null) {
            // once when enabling hotspot we received a null wifimanager - should handle more gracefully.
            Log.e(TAG, "once when enabling hotspot we received a null WifiManager - should handle more gracefully.", new Exception("null WifiManager"));
            return;
        }

        List<ScanResult> scanResults = wifiManager.getScanResults();

        getContentResolver().delete(ScanDataProvider.CONTENT_URI_NO_NOTIFY, null, null);

        if (intent.getBooleanExtra("new_scan_results", false)) {
            ContentValues values = new ContentValues();

            HashMap<String, WifiConfiguration> wifiConfigurations = new HashMap<String, WifiConfiguration>();
            for (WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
                wifiConfigurations.put(wifiConfiguration.SSID, wifiConfiguration);
            }

            WifiInfo currentConnection = wifiManager.getConnectionInfo();
            String currentBSSID = "";
            if (currentConnection != null) {
                currentBSSID = currentConnection.getBSSID();
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean mergeAPs = preferences.getBoolean(KEY_PREF_MERGE_ACCESS_POINTS, false);

            if (mergeAPs) {
                HashMap<String, ScanResult> SSIDs;
                SSIDs = new HashMap<String, ScanResult>();
                for (ScanResult scanResult : scanResults) {
                    ScanResult otherAP = SSIDs.get(scanResult.SSID);
                    if (otherAP == null || scanResult.level > otherAP.level) {
                        SSIDs.put(scanResult.SSID, scanResult);
                    }
                }
                scanResults = new ArrayList<ScanResult>(SSIDs.values());
            }

            int connected = 0;

            for (ScanResult scanResult : scanResults) {
                WifiConfiguration wifiConfiguration = wifiConfigurations.get("\"" + scanResult.SSID + "\"");

                if (wifiConfiguration != null) {
                    values.put(WifiScanDatabase.BSSID, scanResult.BSSID);
                    values.put(WifiScanDatabase.SSID, scanResult.SSID);
                    values.put(WifiScanDatabase.NETWORK_ID, wifiConfiguration.networkId);
                    values.put(WifiScanDatabase.CAPABILITIES, scanResult.capabilities);
                    values.put(WifiScanDatabase.FREQUENCY, scanResult.frequency);
                    values.put(WifiScanDatabase.LEVEL, scanResult.level);
                    values.put(WifiScanDatabase.SIGNALSTRENGTH, getSignalStrength(scanResult.level));
                    connected = (currentBSSID.equals(scanResult.BSSID)) ? 1 : 0;
                    values.put(WifiScanDatabase.CONNECTED, connected);
                    getContentResolver().insert(ScanDataProvider.CONTENT_URI, values);
                }
            }

            Log.e(TAG, "new wifi scan results");
        }
    }

    private int getSignalStrength(int level) {

        int signalStrenth;
        if (level >= WIFI_SIGNAL_THRESHOLD_BEST) {
            signalStrenth = WIFI_SIGNAL_BEST;

        } else if (level >= WIFI_SIGNAL_THRESHOLD_GOOD) {
            signalStrenth = WIFI_SIGNAL_GOOD;

        } else if (level >= WIFI_SIGNAL_THRESHOLD_OK) {
            signalStrenth = WIFI_SIGNAL_OK;

        } else {
            signalStrenth = WIFI_SIGNAL_POOR;

        }
        return signalStrenth;
    }

    public static int getSignalStrengthIcon(int signalStrength, Boolean connected) {
        int icon;
        if (connected) {
            switch (signalStrength) {
                case WifiScanService.WIFI_SIGNAL_BEST:
                    icon = R.drawable.ic_signal_strength_best_connected;
                    break;
                case WifiScanService.WIFI_SIGNAL_GOOD:
                    icon = R.drawable.ic_signal_strength_good_connected;
                    break;
                case WifiScanService.WIFI_SIGNAL_OK:
                    icon = R.drawable.ic_signal_strength_ok_connected;
                    break;
                case WifiScanService.WIFI_SIGNAL_POOR:
                    icon = R.drawable.ic_signal_strength_poor_connected;
                    break;
                default:
                    icon = R.drawable.ic_signal_strength_poor_connected;
                    break;
            }
        } else {
            switch (signalStrength) {
                case WifiScanService.WIFI_SIGNAL_BEST:
                    icon = R.drawable.ic_signal_strength_best;
                    break;
                case WifiScanService.WIFI_SIGNAL_GOOD:
                    icon = R.drawable.ic_signal_strength_good;
                    break;
                case WifiScanService.WIFI_SIGNAL_OK:
                    icon = R.drawable.ic_signal_strength_ok;
                    break;
                case WifiScanService.WIFI_SIGNAL_POOR:
                    icon = R.drawable.ic_signal_strength_poor;
                    break;
                default:
                    icon = R.drawable.ic_signal_strength_poor;
                    break;
            }
        }

        return icon;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        //Log.e(TAG, "stopped");
    }


}
