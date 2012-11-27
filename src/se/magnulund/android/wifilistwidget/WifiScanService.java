package se.magnulund.android.wifilistwidget;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class WifiScanService extends IntentService {

    private static final String TAG = "WifiScanService";

    public WifiScanService() {
        super("WifiListWidget_WifiScanService");
        Log.e(TAG, "Constructed");
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

        List<ScanResult> scanResults = wifiManager.getScanResults();

        getContentResolver().delete(ScanDataProvider.CONTENT_URI, null, null);

        ContentValues values = new ContentValues();
        ScanResult scanResult;

        HashMap<String, WifiConfiguration> wifiConfigurations = new HashMap<String, WifiConfiguration>();
        for (WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
            wifiConfigurations.put(wifiConfiguration.SSID, wifiConfiguration);
        }
        Iterator<ScanResult> iterator = scanResults.iterator();
        while (iterator.hasNext()) {
            scanResult = iterator.next();
            WifiConfiguration wifiConfiguration = wifiConfigurations.get(scanResult.SSID);
            if (wifiConfiguration != null) {
                values.put(DatabaseHelper.BSSID, scanResult.BSSID);
                values.put(DatabaseHelper.SSID, scanResult.SSID);
                values.put(DatabaseHelper.NETWORK_ID, wifiConfiguration.networkId);
                values.put(DatabaseHelper.CAPABILITIES, scanResult.capabilities);
                values.put(DatabaseHelper.FREQUENCY, scanResult.frequency);
                values.put(DatabaseHelper.LEVEL, scanResult.level);
                getContentResolver().insert(ScanDataProvider.CONTENT_URI, values);

            }
        }

        Log.e(TAG, "new wifi scan results");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        Log.e(TAG, "stopped");
    }


}
