package se.magnulund.android.wifilistwidget.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import se.magnulund.android.wifilistwidget.R;
import se.magnulund.android.wifilistwidget.settings.Preferences;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanDatabase;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 29/11/2012
 * Time: 22:24
 * To change this template use File | Settings | File Templates.
 */
public class WifiWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public static final int WIFI_SIGNAL_THRESHOLD_BEST = -50;
    public static final int WIFI_SIGNAL_THRESHOLD_GOOD = -65;
    public static final int WIFI_SIGNAL_THRESHOLD_OK = -75;

    public static final int WIFI_SIGNAL_BEST = 4;
    public static final int WIFI_SIGNAL_GOOD = 3;
    public static final int WIFI_SIGNAL_OK = 2;
    public static final int WIFI_SIGNAL_POOR = 1;

    private Context mContext;
    ArrayList<FilteredScanResult> filterScanResults = new ArrayList<FilteredScanResult>();
    String currentBSSID = null;
    //private int mAppWidgetId;

    public WifiWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        //mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return filterScanResults.size();
    }

    public RemoteViews getViewAt(int position) {
        FilteredScanResult filteredScanResult = filterScanResults.get(position);
        ScanResult scanResult = filteredScanResult.getScanResult();
        WifiConfiguration wifiConfiguration = filteredScanResult.getWifiConfiguration();

        /*
        //ContentValues values = new ContentValues();
        values.put(WifiScanDatabase.BSSID, scanResult.BSSID);
        values.put(WifiScanDatabase.SSID, scanResult.SSID);
        values.put(WifiScanDatabase.NETWORK_ID, wifiConfiguration.networkId);
        values.put(WifiScanDatabase.CAPABILITIES, scanResult.capabilities);
        values.put(WifiScanDatabase.FREQUENCY, scanResult.frequency);
        values.put(WifiScanDatabase.LEVEL, scanResult.level);
        values.put(WifiScanDatabase.SIGNALSTRENGTH, getSignalStrength(scanResult.level));
        connected = (currentBSSID.equals(scanResult.BSSID)) ? 1 : 0;
        values.put(WifiScanDatabase.CONNECTED, connected);
        getContentResolver().insert(ScanDataProvider.CONTENT_URI, values);
        */

        // Get the data for this position from the content provider
        String ssid = scanResult.SSID;
        String bssid = scanResult.BSSID;
        boolean connected = scanResult.BSSID.equals(currentBSSID);
        int level = scanResult.level;
        int networkID = wifiConfiguration.networkId;

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_wifi_list_item);
        rv.setTextViewText(R.id.widget_ssid, ssid);

        rv.setTextViewText(R.id.widget_bssid, bssid);

        rv.setImageViewResource(R.id.widget_signal_strength, getSignalStrengthIcon(getSignalStrength(level), connected));

        rv.setTextViewText(R.id.widget_level, "" + level + "");

        // Set the click intent so that we can handle it and show a toast message
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();
        extras.putInt(WifiScanDatabase.NETWORK_ID, networkID);
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        return rv;
    }

    public RemoteViews getLoadingView() {
        // We aren't going to return a default loading view in this sample
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        List<ScanResult> scanResults = wifiManager.getScanResults();

        HashMap<String, WifiConfiguration> wifiConfigurations = new HashMap<String, WifiConfiguration>();
        for (WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
            wifiConfigurations.put(wifiConfiguration.SSID, wifiConfiguration);
        }

        WifiInfo currentConnection = wifiManager.getConnectionInfo();
        if (currentConnection != null) {
            currentBSSID = currentConnection.getBSSID();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Boolean mergeAPs = preferences.getBoolean(Preferences.MERGE_ACCESS_POINTS, false);

        if (mergeAPs) {
            HashMap<String, ScanResult> SSIDs;
            SSIDs = new HashMap<String, ScanResult>();
            for (ScanResult scanResult : scanResults) {
                ScanResult otherAP = SSIDs.get(scanResult.SSID);
                if (otherAP == null || scanResult.level > otherAP.level) {
                    SSIDs.put(scanResult.SSID, scanResult);
                }
            }
            scanResults = new ArrayList<ScanResult>(SSIDs.values());
        }

        int connected = 0;

        //
        filterScanResults.clear();

        for (ScanResult scanResult : scanResults) {
            if (wifiConfigurations.containsKey("\"" + scanResult.SSID + "\"")) {
                WifiConfiguration wifiConfiguration = wifiConfigurations.get("\"" + scanResult.SSID + "\"");

                filterScanResults.add(new FilteredScanResult(scanResult, wifiConfiguration));
            }

        }

    }

    private class FilteredScanResult
    {
        ScanResult scanResult;
        WifiConfiguration wifiConfiguration;

        public FilteredScanResult(ScanResult scanResult, WifiConfiguration wifiConfiguration) {
            this.scanResult = scanResult;
            this.wifiConfiguration = wifiConfiguration;
        }

        public ScanResult getScanResult() {
            return scanResult;
        }

        public WifiConfiguration getWifiConfiguration() {
            return wifiConfiguration;
        }
    }

    private int getSignalStrength(int level) {

        int signalStrenth;
        if (level >= WIFI_SIGNAL_THRESHOLD_BEST) {
            signalStrenth = WIFI_SIGNAL_BEST;

        } else if (level >= WIFI_SIGNAL_THRESHOLD_GOOD) {
            signalStrenth = WIFI_SIGNAL_GOOD;

        } else if (level >= WIFI_SIGNAL_THRESHOLD_OK) {
            signalStrenth = WIFI_SIGNAL_OK;

        } else {
            signalStrenth = WIFI_SIGNAL_POOR;

        }
        return signalStrenth;
    }

    public static int getSignalStrengthIcon(int signalStrength, Boolean connected) {
        int icon;
        if (connected) {
            switch (signalStrength) {
                case WIFI_SIGNAL_BEST:
                    icon = R.drawable.ic_signal_strength_best_connected;
                    break;
                case WIFI_SIGNAL_GOOD:
                    icon = R.drawable.ic_signal_strength_good_connected;
                    break;
                case WIFI_SIGNAL_OK:
                    icon = R.drawable.ic_signal_strength_ok_connected;
                    break;
                case WIFI_SIGNAL_POOR:
                    icon = R.drawable.ic_signal_strength_poor_connected;
                    break;
                default:
                    icon = R.drawable.ic_signal_strength_poor_connected;
                    break;
            }
        } else {
            switch (signalStrength) {
                case WIFI_SIGNAL_BEST:
                    icon = R.drawable.ic_signal_strength_best;
                    break;
                case WIFI_SIGNAL_GOOD:
                    icon = R.drawable.ic_signal_strength_good;
                    break;
                case WIFI_SIGNAL_OK:
                    icon = R.drawable.ic_signal_strength_ok;
                    break;
                case WIFI_SIGNAL_POOR:
                    icon = R.drawable.ic_signal_strength_poor;
                    break;
                default:
                    icon = R.drawable.ic_signal_strength_poor;
                    break;
            }
        }

        return icon;
    }

}
