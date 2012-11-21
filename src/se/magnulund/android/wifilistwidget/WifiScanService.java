package se.magnulund.android.wifilistwidget;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 21/11/2012
 * Time: 11:15
 * To change this template use File | Settings | File Templates.
 */
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

        Iterator<ScanResult> iterator = scanResults.iterator();
        while ( iterator.hasNext() ) {
            scanResult = iterator.next();
            values.put(ScanDataProvider.BSSID, scanResult.BSSID);
            values.put(ScanDataProvider.SSID, scanResult.SSID);
            values.put(ScanDataProvider.CAPABILITIES, scanResult.capabilities);
            values.put(ScanDataProvider.FREQUENCY, scanResult.frequency);
            values.put(ScanDataProvider.LEVEL, scanResult.level);
            getContentResolver().insert(ScanDataProvider.CONTENT_URI, values);
        }

        Log.e(TAG, "new wifi scan results");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        Log.e(TAG, "stopped");
    }
}
