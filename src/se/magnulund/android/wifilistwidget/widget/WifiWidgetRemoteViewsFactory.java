package se.magnulund.android.wifilistwidget.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import se.magnulund.android.wifilistwidget.R;
import se.magnulund.android.wifilistwidget.models.FilteredScanResult;
import se.magnulund.android.wifilistwidget.utils.MyUtil;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: Gustav Date: 29/11/2012 Time: 22:24 To
 * change this template use File | Settings | File Templates.
 */
public class WifiWidgetRemoteViewsFactory implements
        RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    ArrayList<FilteredScanResult> filterScanResults;

    // String currentBSSID = null;
    private int mAppWidgetId;

    public WifiWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId =
                intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
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
        WifiConfiguration wifiConfiguration = filteredScanResult
                .getWifiConfiguration();

        // Get the data for this position from the content provider
        String ssid = scanResult.SSID;
        String bssid = scanResult.BSSID;
        boolean connected = filteredScanResult.isCurrentConnection();
        int level = scanResult.level;
        int networkID = wifiConfiguration.networkId;
        String redirectURL = filteredScanResult.getRedirectURL();

        boolean useWalledGarden = connected && filteredScanResult.isWalledGarden();

        int clickType = (useWalledGarden) ? WifiWidgetProvider.ITEM_CLICK_REDIRECT : WifiWidgetProvider.ITEM_CLICK_CONNECT;

        int widgetTheme = filteredScanResult.getWidgetTheme();
        int listItemLayout;

        listItemLayout = getItemLayout(widgetTheme, useWalledGarden);

        RemoteViews rv = new RemoteViews(mContext.getPackageName(),
                listItemLayout);
        rv.setTextViewText(R.id.widget_ssid, ssid);

        rv.setTextViewText(R.id.widget_bssid, bssid);

        rv.setImageViewResource(R.id.widget_signal_strength, MyUtil
                .getSignalStrengthIcon(MyUtil.getSignalStrength(level),
                        connected));

        rv.setTextViewText(R.id.widget_level, "" + level + "");

        // Set the click intent so that we can handle it and show a toast
        // message
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();
        extras.putInt(WifiWidgetProvider.CLICK_TYPE, clickType);
        extras.putInt(WifiWidgetProvider.NETWORK_ID, networkID);
        extras.putString(WifiWidgetProvider.REDIRECT_URL, redirectURL);
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
        return 2;
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
        filterScanResults = FilteredScanResult.getFilteredScanResults(mContext, mAppWidgetId);
    }

    private int getItemLayout(int widgetTheme, boolean useWalledGarden) {
        int layout;
        switch (widgetTheme) {
            case WidgetRemoteViews.WIDGET_THEME_LIGHT: {
                if (useWalledGarden) {
                    layout = R.layout.widget_wifi_list_item_walled_garden_light;
                } else {

                    layout = R.layout.widget_wifi_list_item_light;
                }
                break;
            }
            case WidgetRemoteViews.WIDGET_THEME_DARK:
            default: {
                if (useWalledGarden) {
                    layout = R.layout.widget_wifi_list_item_walled_garden_dark;
                } else {

                    layout = R.layout.widget_wifi_list_item_dark;
                }
                break;
            }
        }
        return layout;
    }
}
