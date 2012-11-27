package se.magnulund.android.wifilistwidget;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.List;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

    private ListView wifiList;
    private List<WifiConfiguration> wifiConfigurations;
    SimpleCursorAdapter wifiAdapter;
    private WifiManager wifiManager;

    static final String[] WIFI_NETWORKS_SSID_PROJECTION = new String[] {
        ScanDataProvider._ID,
        ScanDataProvider.SSID,
        ScanDataProvider.LEVEL
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        wifiList = (ListView) findViewById(R.id.wifi_list);

        /*
        getLoaderManager().initLoader(0, null, this);
        wifiAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null,
                new String[] { ScanDataProvider.SSID, ScanDataProvider.LEVEL },
                new int[] { android.R.id.text1, android.R.id.text2 }, 0);
        wifiList.setAdapter(wifiAdapter);
        */

        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        //wifiConfigurations = wifiManager.getConfiguredNetworks();
    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.

        Intent intent = new Intent(this, WifiStateService.class);
        intent.putExtra("wifi_state", wifiManager.getWifiState());
        startService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.

        Intent intent = new Intent(this, WifiStateService.class);
        intent.putExtra("stop_services", true);
        startService(intent);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Uri uri = ScanDataProvider.CONTENT_URI;
        return new CursorLoader(this, uri, WIFI_NETWORKS_SSID_PROJECTION, null, null, ScanDataProvider.LEVEL + " DESC"  );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        wifiAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        wifiAdapter.swapCursor(null);
    }


}
