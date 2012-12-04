package se.magnulund.android.wifilistwidget;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import se.magnulund.android.wifilistwidget.settings.SettingsActivity;
import se.magnulund.android.wifilistwidget.utils.NetworkUtils;
import se.magnulund.android.wifilistwidget.widget.WifiWidgetProvider;
import se.magnulund.android.wifilistwidget.wifiap.WifiApManager;
import se.magnulund.android.wifilistwidget.wifiscan.ScanDataProvider;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanDatabase;
import se.magnulund.android.wifilistwidget.wifistate.WifiStateService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

    public static final String PREFS_SHOW_HOTSPOT_TOGGLE = "show_hotspot_toggle";
    public static final String DEVICE_HAS_MOBILE_NETWORK = "device_has_mobile_networks";

    private ListView wifiList;
    private List<WifiConfiguration> wifiConfigurations;
    SimpleCursorAdapter wifiAdapter;
    private WifiManager wifiManager;

    private TextView headerView;

    private Boolean hasMobileNetwork = false;

    private Boolean mobileHotSpotActive = false;

    SharedPreferences preferences;
    WifiApManager wifiApManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        wifiList = (ListView) findViewById(R.id.wifi_list);

        getLoaderManager().initLoader(0, null, this);
        wifiAdapter = new WifiCursorAdapter(this,
                R.layout.wifi_list_item, null,
                new String[]{WifiScanDatabase.SSID, WifiScanDatabase.BSSID, WifiScanDatabase.SIGNALSTRENGTH, WifiScanDatabase.LEVEL},
                new int[]{R.id.ssid, R.id.bssid, R.id.signal_strength, R.id.level}, 0);

        if (headerView == null) {
            headerView = new TextView(MainActivity.this);
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            headerView.setPadding(padding, padding, padding, padding);
            wifiList.addHeaderView(headerView);
        }

        hasMobileNetwork = NetworkUtils.hasMobileNetwork(this);

        if ( hasMobileNetwork ){
            wifiApManager = new WifiApManager(MainActivity.this);
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        for (NetworkInfo networkInfo : connectivityManager.getAllNetworkInfo()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                hasMobileNetwork = true;
                wifiApManager = new WifiApManager(MainActivity.this);
                break;
            }
        }

        wifiList.setAdapter(wifiAdapter);

        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor o = (Cursor) adapterView.getItemAtPosition(i);
                if (o != null) {
                    int networkId = o.getInt(o.getColumnIndex(WifiScanDatabase.NETWORK_ID));

                    wifiManager.disconnect();
                    wifiManager.enableNetwork(networkId, true);
                    wifiManager.reconnect();
                }
            }
        });
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        if (isWidgetActive() == false) {
            Log.e(TAG, "Widget not active - starting service");
            Intent intent = new Intent(this, WifiStateService.class);
            startService(intent);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        if (isWidgetActive() == false) {
            Log.e(TAG, "Widget not active - stopping service");
            Intent intent = new Intent(this, WifiStateService.class);
            intent.putExtra("stop_services", true);
            startService(intent);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Uri uri = ScanDataProvider.CONTENT_URI;
        return new CursorLoader(this, uri, WifiScanDatabase.WIFI_NETWORKS_SSID_PROJECTION, null, null, WifiScanDatabase.LEVEL + " DESC");
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        wifiAdapter.swapCursor(data);
        if (mobileHotSpotActive == false) {
            SimpleDateFormat dateFormat = new SimpleDateFormat();
            headerView.setText("Last scan: " + dateFormat.format(new Date()));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        wifiAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);

        MenuItem hotspotToggle = menu.findItem(R.id.hotspot_toggle);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (hasMobileNetwork && preferences.getBoolean(PREFS_SHOW_HOTSPOT_TOGGLE, true)) {
            mobileHotSpotActive = wifiApManager.isWifiApEnabled();
            hotspotToggle.setChecked(mobileHotSpotActive);
            hotspotToggle.setIcon((mobileHotSpotActive) ? R.drawable.ic_menu_hotspot_active : R.drawable.ic_menu_hotspot_inactive);
            hotspotToggle.setTitle((mobileHotSpotActive) ? R.string.hotspot_active : R.string.hotspot_inactive);
        } else {
            hotspotToggle.setVisible(false);
            hotspotToggle.setEnabled(false);
        }

        return super.onCreateOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.hotspot_toggle: {
                mobileHotSpotActive = !item.isChecked();

                if (mobileHotSpotActive) { //(wifiApManager.isWifiApEnabled() == false) {
                    wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), true);
                    getContentResolver().delete(ScanDataProvider.CONTENT_URI, null, null);
                    headerView.setText("Scan disabled when hotspot is active.");
                } else {
                    wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), false);
                    wifiApManager.setWifiEnabled(true);
                    headerView.setText("Scanning...");
                    wifiManager.startScan();
                }

                item.setChecked(mobileHotSpotActive);
                item.setIcon((mobileHotSpotActive) ? R.drawable.ic_menu_hotspot_active : R.drawable.ic_menu_hotspot_inactive);
                item.setTitle((mobileHotSpotActive) ? R.string.hotspot_active : R.string.hotspot_inactive);
                return true;
            }
            case R.id.rescan: {
                if (!mobileHotSpotActive && wifiManager.isWifiEnabled()) {
                    wifiManager.startScan();
                    headerView.setText("Scanning...");
                    return true;
                }
            }
            case R.id.menu_wifi_settings: {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(intent);
                return true;
            }
            case R.id.menu_settings: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtra(DEVICE_HAS_MOBILE_NETWORK, hasMobileNetwork);
                startActivity(intent);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private Boolean isWidgetActive() {
        return preferences.getBoolean(WifiWidgetProvider.WIDGET_ACTIVE, false);
    }
}
