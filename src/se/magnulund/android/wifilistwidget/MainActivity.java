package se.magnulund.android.wifilistwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
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
import android.widget.TextView;
import android.widget.Toast;
import se.magnulund.android.wifilistwidget.settings.Preferences;
import se.magnulund.android.wifilistwidget.settings.SettingsActivity;
import se.magnulund.android.wifilistwidget.utils.NetworkUtils;
import se.magnulund.android.wifilistwidget.widget.WidgetRemoteViews;
import se.magnulund.android.wifilistwidget.widget.WifiWidgetProvider;
import se.magnulund.android.wifilistwidget.wifiap.WifiApManager;
import se.magnulund.android.wifilistwidget.wifistate.WifiStateService;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private ListView wifiList;
    WifiFilteredScanResultsAdapter wifiAdapter;
    private WifiManager wifiManager;

    private TextView headerView;

    private Boolean hasMobileNetwork = false;

    private Boolean mobileHotSpotActive = false;

    SharedPreferences preferences;
    WifiApManager wifiApManager;

    // push
    private NfcAdapter mAdapter;
    private NdefMessage mMessage;

    // fetch
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this,
                R.xml.preferences, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = getWidgetTheme(Integer.parseInt(preferences.getString(Preferences.THEME, "1")));

        this.setTheme(theme);

        setContentView(R.layout.main);

        wifiList = (ListView) findViewById(R.id.wifi_list);

        wifiAdapter = new WifiFilteredScanResultsAdapter(this);

        if (headerView == null) {
            headerView = new TextView(MainActivity.this);
            int padding = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 16, getResources()
                    .getDisplayMetrics());
            headerView.setPadding(padding, padding, padding, padding);
            wifiList.addHeaderView(headerView);
        }

        hasMobileNetwork = NetworkUtils.hasMobileNetwork(this);

        if (hasMobileNetwork) {
            wifiApManager = new WifiApManager(MainActivity.this);
        }

        hasMobileNetwork = preferences.getBoolean(
                Preferences.DEVICE_HAS_MOBILE_NETWORK,
                deviceHasMobileNetwork(MainActivity.this));
        if (hasMobileNetwork) {
            wifiApManager = new WifiApManager(MainActivity.this);
        }

        wifiList.setAdapter(wifiAdapter);

        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int i, long l) {
                Toast.makeText(MainActivity.this,
                        "Clicks need to be reimplemented.. *<|:(",
                        Toast.LENGTH_SHORT).show();
            }
        });

        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
             /*
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mAdapter != null) { // this device has nfc
            // push

            wifiList.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Create an NDEF message a URL

                    mMessage = new NdefMessage(NdefRecord.createUri("http://www.android.com"));
                    mAdapter.setNdefPushMessage(mMessage, MainActivity.this);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Beam that sheed");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Naep", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAdapter.setNdefPushMessage(null, MainActivity.this);
                            return;
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return false;
                }
            });



            // fetch

            // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
            // will fill in the intent with the details of the discovered tag before delivering to
            // this activity.
            mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

            // Setup an intent filter for all MIME based dispatches
            IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try {
                ndef.addDataType("* /*");
            } catch (IntentFilter.MalformedMimeTypeException e) {
                throw new RuntimeException("fail", e);
            }
            mFilters = new IntentFilter[]{ndef,};

            // Setup a tech list for all NfcF tags
            mTechLists = new String[][]{new String[]{NfcF.class.getName()}};

        }
        */
    }


    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (isWidgetActive() == false) {
            Log.e(TAG, "Widget not active - starting service");
            Intent intent = new Intent(this, WifiStateService.class);
            startService(intent);
        }
        /*
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (preferences.contains(Preferences.DEVICE_HAS_MOBILE_NETWORK) == false) {
            SharedPreferences.Editor edit = preferences.edit();
            edit.putBoolean(Preferences.DEVICE_HAS_MOBILE_NETWORK,
                    hasMobileNetwork);
            edit.commit();
        }
        if (isWidgetActive() == false) {
            Log.e(TAG, "Widget not active - stopping service");
            Intent intent = new Intent(this, WifiStateService.class);
            intent.putExtra("stop_services", true);
            startService(intent);
        }

        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    public static boolean deviceHasMobileNetwork(Context context) {
        boolean mobileNetwork = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        for (NetworkInfo networkInfo : connectivityManager.getAllNetworkInfo()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                mobileNetwork = true;
                break;
            }
        }
        return mobileNetwork;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);

        MenuItem hotspotToggle = menu.findItem(R.id.hotspot_toggle);

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        if (hasMobileNetwork
                && preferences.getBoolean(Preferences.SHOW_MAIN_HOTSPOT_TOGGLE,
                true)) {
            mobileHotSpotActive = wifiApManager.isWifiApEnabled();
            hotspotToggle.setChecked(mobileHotSpotActive);
            hotspotToggle
                    .setIcon((mobileHotSpotActive) ? R.drawable.ic_menu_hotspot_pending
                            : R.drawable.ic_menu_hotspot_inactive);
            hotspotToggle
                    .setTitle((mobileHotSpotActive) ? R.string.hotspot_active
                            : R.string.hotspot_inactive);
        } else {
            hotspotToggle.setVisible(false);
            hotspotToggle.setEnabled(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.hotspot_toggle: {
                mobileHotSpotActive = !item.isChecked();

                if (mobileHotSpotActive) { // (wifiApManager.isWifiApEnabled() ==
                    // false) {
                    wifiApManager.setWifiApEnabled(
                            wifiApManager.getWifiApConfiguration(), true);
                    headerView.setText("Scan disabled when hotspot is active.");
                } else {
                    wifiApManager.setWifiApEnabled(
                            wifiApManager.getWifiApConfiguration(), false);
                    wifiApManager.setWifiEnabled(true);
                    headerView.setText("Scanning...");
                    wifiManager.startScan();
                }

                item.setChecked(mobileHotSpotActive);
                item.setIcon((mobileHotSpotActive) ? R.drawable.ic_menu_hotspot_pending
                        : R.drawable.ic_menu_hotspot_inactive);
                item.setTitle((mobileHotSpotActive) ? R.string.hotspot_active
                        : R.string.hotspot_inactive);
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
                Intent intent = new Intent(MainActivity.this,
                        SettingsActivity.class);
                intent.putExtra(Preferences.DEVICE_HAS_MOBILE_NETWORK,
                        hasMobileNetwork);
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

    public int getWidgetTheme(int widgetTheme) {
        int theme;
        switch (widgetTheme) {
            case WidgetRemoteViews.WIDGET_THEME_DARK: {
                theme = R.style.APListTheme_Dark;
                break;
            }
            case WidgetRemoteViews.WIDGET_THEME_LIGHT: {
                theme = R.style.APListTheme_Light;
                break;
            }
            default: {
                theme = R.style.APListTheme_Dark;
                break;
            }
        }
        return theme;
    }
}
