package se.magnulund.android.wifilistwidget;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.text.BreakIterator;
import java.util.List;

public class MainActivity extends Activity {

    private ListView wifiList;
    private List<WifiConfiguration> wifiConfigurations;
    ListAdapter listAdapter;
    private WifiManager wifiManager;
    private static Boolean WIFI_SCAN_RECEIVER_ACTIVE = false;

    WifiScanReceiver wifiScanReceiver;
    WifiStateReceiver wifiStateReceiver;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        wifiList = (ListView) findViewById(R.id.wifi_list);


        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        wifiConfigurations = wifiManager.getConfiguredNetworks();

        wifiStateReceiver = new WifiStateReceiver();
        registerReceiver(wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        if (wifiManager.isWifiEnabled()) {
            wifiScanReceiver = new WifiScanReceiver();
            registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            WIFI_SCAN_RECEIVER_ACTIVE = true;
        }
    }

    public void updateWifiList ( ) {
        List<ScanResult> scanResults = wifiManager.getScanResults();
        // Kanske ska göra lite skit här......
    }

    public void toggleWifiReceiver(int wifiState){
        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                if ( wifiScanReceiver == null) {
                    wifiScanReceiver = new WifiScanReceiver();
                }
                registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                WIFI_SCAN_RECEIVER_ACTIVE = true;
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                unregisterReceiver(wifiScanReceiver);
                WIFI_SCAN_RECEIVER_ACTIVE = false;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.

        unregisterReceiver(wifiStateReceiver);

        if (WIFI_SCAN_RECEIVER_ACTIVE) {
            unregisterReceiver(wifiScanReceiver);
        }
    }
}
