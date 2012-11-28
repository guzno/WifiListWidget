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
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.util.Date;
import java.util.List;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

    public static final String PREFERENCES_NAME = "wifi_list_widget_settings";
    public static final String PREFS_INITIALIZED = "initialized";
    public static final String PREFS_MERGE_APS = "merge_access_points";

    private ListView wifiList;
    private List<WifiConfiguration> wifiConfigurations;
    SimpleCursorAdapter wifiAdapter;
    private WifiManager wifiManager;

    static final String[] WIFI_NETWORKS_SSID_PROJECTION = new String[]{
            DatabaseHelper._ID,
            DatabaseHelper.SSID,
            DatabaseHelper.LEVEL,
            DatabaseHelper.NETWORK_ID
    };

    private TextView headerView;

    private Boolean hasMobileNetwork = false;

    private Boolean mobileHotSpotActive = false;

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        wifiList = (ListView) findViewById(R.id.wifi_list);

        getLoaderManager().initLoader(0, null, this);
        wifiAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null,
                new String[]{DatabaseHelper.SSID, DatabaseHelper.LEVEL},
                new int[]{android.R.id.text1, android.R.id.text2}, 0);

        preferences = getSharedPreferences(PREFERENCES_NAME, 0);
        preferencesEditor = preferences.edit();
        initializeSettingsIfNeeded();

        if (headerView == null) {
            headerView = new TextView(MainActivity.this);
            int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            headerView.setPadding(padding, padding, padding, padding);
            wifiList.addHeaderView(headerView);
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        for ( NetworkInfo networkInfo : connectivityManager.getAllNetworkInfo()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                hasMobileNetwork = true;
                Log.e(TAG, "This device has a mobile connection");
                break;
            }
        }

        if ( hasMobileNetwork == false ) {
            Log.e(TAG, "This device has NO mobile connection");
        }

        wifiList.setAdapter(wifiAdapter);

        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor o = (Cursor) adapterView.getItemAtPosition(i);

                int networkId = o.getInt(o.getColumnIndex(DatabaseHelper.NETWORK_ID));

                wifiManager.disconnect();
                wifiManager.enableNetwork(networkId, true);
                wifiManager.reconnect();
                /*
                List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
                for( WifiConfiguration wifiConfiguration : wifiConfigurationList ) {
                    Log.e(TAG, "komperin: " + wifiConfiguration.SSID + " " + networkSSID);
                    if(wifiConfiguration.SSID != null && wifiConfiguration.SSID.equals("\"" + networkSSID + "\"")) {

                        wifiManager.disconnect();
                        wifiManager.enableNetwork(wifiConfiguration.networkId, true);
                        wifiManager.reconnect();

                        break;
                    }
                }
                */
            }
        });
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
        return new CursorLoader(this, uri, WIFI_NETWORKS_SSID_PROJECTION, null, null, DatabaseHelper.LEVEL + " DESC");
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        wifiAdapter.swapCursor(data);

        headerView.setText("Lest sken: " + new Date().toString());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        wifiAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);

        //MenuItem hotspotTogle = menu.findItem(R.id.hotspot_toggle);
        //hotspotTogle.setChecked(IS_HOTSPOT_ENABLED());

        MenuItem hotspotToggle = menu.findItem(R.id.hotspot_toggle);
        hotspotToggle.setChecked(false);
        hotspotToggle.setIcon((false) ? R.drawable.hotspot_active : R.drawable.hotspot_inactive );
        hotspotToggle.setTitle((false) ? R.string.hotspot_active : R.string.hotspot_inactive);


        return super.onCreateOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.hotspot_toggle: {
                Log.e(TAG, "If someone knew how to commit, things would happen now!!!");
                Boolean hotSpotActive = !item.isChecked();
                item.setChecked(hotSpotActive);
                item.setIcon((hotSpotActive) ? R.drawable.hotspot_active : R.drawable.hotspot_inactive );
                item.setTitle((hotSpotActive) ? R.string.hotspot_active : R.string.hotspot_inactive);
                return true;
            }
            case R.id.menu_settings: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void initializeSettingsIfNeeded() {
        if (!preferences.getBoolean(PREFS_INITIALIZED, false)){
            preferencesEditor.putBoolean(PREFS_INITIALIZED, true);
            preferencesEditor.putBoolean(PREFS_MERGE_APS, false);
            preferencesEditor.commit();
            preferencesEditor.clear();
        }
    }
}
