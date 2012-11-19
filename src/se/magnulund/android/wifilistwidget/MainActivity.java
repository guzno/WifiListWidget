package se.magnulund.android.wifilistwidget;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends Activity {

    private ListView wifiList;
    List<ScanResult> scanResults;
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

        Intent i = registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));


    }

    private BroadcastReceiver wifiScanReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {

            scanResults = wifiManager.getScanResults();
        }
    };
}
