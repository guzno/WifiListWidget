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
import se.magnulund.android.wifilistwidget.settings.Preferences;
import se.magnulund.android.wifilistwidget.widget.WifiWidgetProvider;
import se.magnulund.android.wifilistwidget.wifistate.WifiStateService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WifiScanService extends IntentService {

    private static final String TAG = "WifiScanService";

    public WifiScanService() {
        super("WifiListWidget_WifiScanService");
        //Log.e(TAG, "Constructed");
    }

    //private WifiManager wifiManager;

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
        WifiWidgetProvider.updateWidgets(getApplicationContext(), WifiWidgetProvider.UPDATE_WIFI_SCAN_RESULTS, null);
        Log.e(TAG, "new wifi scan results");

        /*
        if (wifiManager == null) {
            wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        }

        if (wifiManager == null) {
            // once when enabling hotspot we received a null wifimanager - should handle more gracefully.
            Log.e(TAG, "once when enabling hotspot we received a null WifiManager - should handle more gracefully.", new Exception("null WifiManager"));
            return;
        }

        synchronized (WifiStateService.wifiManagerLock) {
            List<ScanResult> scanResults = wifiManager.getScanResults();

            //getContentResolver().delete(ScanDataProvider.CONTENT_URI_NO_NOTIFY, null, null);

            if (intent.getBooleanExtra("new_scan_results", false)) {
                //Log.e(TAG, "here/1");
                /*
                HashMap<String, WifiConfiguration> wifiConfigurations = new HashMap<String, WifiConfiguration>();
                for (WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
                    wifiConfigurations.put(wifiConfiguration.SSID, wifiConfiguration);
                }
                //Log.e(TAG, "here/2");

                WifiInfo currentConnection = wifiManager.getConnectionInfo();
                String currentBSSID = "";
                if (currentConnection != null) {
                    currentBSSID = currentConnection.getBSSID();
                }

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Boolean mergeAPs = preferences.getBoolean(Preferences.MERGE_ACCESS_POINTS, false);

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
                        //ContentValues values = new ContentValues();
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

            }
        }
        */

    }
}
