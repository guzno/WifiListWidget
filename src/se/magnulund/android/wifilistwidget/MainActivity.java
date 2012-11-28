package se.magnulund.android.wifilistwidget;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        wifiList = (ListView) findViewById(R.id.wifi_list);
        /*Button button = (Button) findViewById(R.id.cakeface);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiApManager wifiApManager = new WifiApManager(MainActivity.this);
                if (wifiApManager.isWifiApEnabled() == false) {
                    wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), true);
                } else {
                    wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), false);
                }
            }
        });
        */

        getLoaderManager().initLoader(0, null, this);
        wifiAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null,
                new String[]{DatabaseHelper.SSID, DatabaseHelper.LEVEL},
                new int[]{android.R.id.text1, android.R.id.text2}, 0);

        if (headerView == null) {
            headerView = new TextView(MainActivity.this);
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            headerView.setPadding(padding, padding, padding, padding);
            wifiList.addHeaderView(headerView);
        }

        wifiList.setAdapter(wifiAdapter);

        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor o = (Cursor) adapterView.getItemAtPosition(i);

                int networkId = o.getInt(o.getColumnIndex(DatabaseHelper.NETWORK_ID));

                WifiApManager wifiApManager = new WifiApManager(MainActivity.this);
                if (wifiApManager.isWifiApEnabled() == true) {
                    wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), false);
                }

                if (wifiManager.isWifiEnabled() == false) {
                    wifiManager.setWifiEnabled(true);
                }

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


    private Boolean isMobileDataEnabled() {
        Object connectivityService = getSystemService(CONNECTIVITY_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) connectivityService;

        try {
            Class<?> c = Class.forName(cm.getClass().getName());
            Method m = c.getDeclaredMethod("getMobileDataEnabled");
            m.setAccessible(true);
            return (Boolean) m.invoke(cm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);

        if (1 == 2) {
            MenuItem menuItem = menu.findItem(R.id.menu_hotspot);
            menuItem.setVisible(false);
            menuItem.setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_hotspot: {
                WifiApManager wifiApManager = new WifiApManager(MainActivity.this);

                if (wifiApManager.isWifiApEnabled() == false) {
                    wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), true);
                } else {
                    wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), false);
                }
            }
            default: {
                return super.onMenuItemSelected(featureId, item);
            }
        }
    }
}
