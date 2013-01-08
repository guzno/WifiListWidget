package se.magnulund.android.wifilistwidget.wifiscan;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import se.magnulund.android.wifilistwidget.R;
import se.magnulund.android.wifilistwidget.settings.Preferences;
import se.magnulund.android.wifilistwidget.widget.WifiWidgetProvider;
import se.magnulund.android.wifilistwidget.wifistate.WifiStateService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WifiScanService extends IntentService {

    private static final String TAG = "WifiScanService";

    public WifiScanService() {
        super("WifiListWidget_WifiScanService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WifiWidgetProvider.updateWidgets(getApplicationContext(), WifiWidgetProvider.UPDATE_WIFI_SCAN_RESULTS, null);
    }
}
