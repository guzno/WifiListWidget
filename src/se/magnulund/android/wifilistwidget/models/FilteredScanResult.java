package se.magnulund.android.wifilistwidget.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import se.magnulund.android.wifilistwidget.settings.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: erikeelde Date: 8/1/2013 Time: 22:22 To
 * change this template use File | Settings | File Templates.
 */
public class FilteredScanResult implements Comparable<FilteredScanResult> {

    private static final String TAG = "FilteredScanResult";

    @Override
    public int compareTo(FilteredScanResult another) {
        if (isCurrentConnection()) {
            return -1;
        } else if (another.isCurrentConnection()) {
            return 1;
        } else {
            return another.scanResult.level - scanResult.level;
        }
    }

    ScanResult scanResult;
    WifiConfiguration wifiConfiguration;
    boolean isCurrentConnection = false;
    boolean isWalledGarden = false;
    String redirectURL = "";
    int widgetTheme = -1;

    public FilteredScanResult(ScanResult scanResult,
                              WifiConfiguration wifiConfiguration) {
        this.scanResult = scanResult;
        this.wifiConfiguration = wifiConfiguration;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public WifiConfiguration getWifiConfiguration() {
        return wifiConfiguration;
    }

    public void setCurrentConnection(boolean currentConnection) {
        isCurrentConnection = currentConnection;
    }

    public boolean isCurrentConnection() {
        return isCurrentConnection;
    }

    public void setWalledGarden(boolean walledGarden) {
        isWalledGarden = walledGarden;
    }

    public boolean isWalledGarden() {
        return isWalledGarden;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setWidgetTheme(int widgetTheme) {
        this.widgetTheme = widgetTheme;
    }

    public int getWidgetTheme() {
        return widgetTheme;
    }

    public static ArrayList<FilteredScanResult> getFilteredScanResults(
            Context context, Integer appWidgetId) {

        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);

        // init filtered results since the factory can be longlived and carry
        // results from past runs otherwise
        ArrayList<FilteredScanResult> filterScanResults = new ArrayList<FilteredScanResult>();

        List<ScanResult> scanResults = wifiManager.getScanResults();

        WifiInfo currentConnection = wifiManager.getConnectionInfo();

        HashMap<String, WifiConfiguration> wifiConfigurations = new HashMap<String, WifiConfiguration>();

        if (wifiManager.getConfiguredNetworks() != null) {

            for (WifiConfiguration wifiConfiguration : wifiManager
                    .getConfiguredNetworks()) {
                wifiConfigurations.put(wifiConfiguration.SSID,
                        wifiConfiguration);
            }

            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
            Boolean mergeAPs = preferences.getBoolean(
                    Preferences.MERGE_ACCESS_POINTS, false);
            Integer widgetTheme = null;
            if (appWidgetId != null) {
                widgetTheme = Integer.parseInt(preferences.getString(Preferences.THEME_ + appWidgetId, "-1"));
                Log.e(TAG, "Theme: "+widgetTheme);
                Log.e(TAG, "Widget: "+appWidgetId);
            }

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

            for (ScanResult scanResult : scanResults) {
                if (wifiConfigurations.containsKey("\"" + scanResult.SSID
                        + "\"")) {
                    WifiConfiguration wifiConfiguration = wifiConfigurations
                            .get("\"" + scanResult.SSID + "\"");

                    FilteredScanResult filteredScanResult = new FilteredScanResult(
                            scanResult, wifiConfiguration);

                    if (widgetTheme != null) {
                        filteredScanResult.setWidgetTheme(widgetTheme);
                    }

                    if (scanResult.BSSID.equals(currentConnection.getBSSID())) {
                        filteredScanResult.setCurrentConnection(true);
                        filteredScanResult.setWalledGarden(preferences
                                .getBoolean(
                                        Preferences.WALLED_GARDEN_CONNECTION,
                                        false));
                        filteredScanResult.setRedirectURL(preferences
                                .getString(
                                        Preferences.WALLED_GARDEN_REDIRECT_URL,
                                        ""));
                    }

                    filterScanResults.add(filteredScanResult);
                }

            }
        }

        Collections.sort(filterScanResults);

        return filterScanResults;
    }
}
