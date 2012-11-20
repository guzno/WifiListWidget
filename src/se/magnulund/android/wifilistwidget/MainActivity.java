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

import java.util.List;

public class MainActivity extends Activity {

    private ListView wifiList;
    List<WifiConfiguration> wifiConfigurations;
    ListAdapter listAdapter;
    private WifiManager wifiManager;

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

        Intent intent = registerReceiver(wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        if (wifiManager.isWifiEnabled()) {
            Intent i = registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
    }

    private void updateWifiList ( ) {
        List<ScanResult> scanResults = wifiManager.getScanResults();
        // Kanske ska göra lite skit här......
    }

    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            updateWifiList();
        }
    };


    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLING:
                    Intent i = registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    unregisterReceiver(wifiScanReceiver);
                    break;
                default:
                    break;

            }
        }
    };
}
