package se.magnulund.android.wifilistwidget.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import se.magnulund.android.wifilistwidget.R;
import se.magnulund.android.wifilistwidget.wifiscan.ScanDataProvider;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanDatabase;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanService;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 29/11/2012
 * Time: 22:24
 * To change this template use File | Settings | File Templates.
 */
public class WifiWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor mCursor;
    private int mAppWidgetId;

    public WifiWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called immediately after
        // onCreate(), we do nothing here.
    }

    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }


    public int getCount() {
        return mCursor.getCount();
    }

    public RemoteViews getViewAt(int position) {
        // Get the data for this position from the content provider
        String ssid = "";
        String bssid = "";
        int signalStrength = 0;
        boolean connected = false;
        int level = 0;
        int networkID = 0;
        if (mCursor.moveToPosition(position)) {
            int columnIndex = mCursor.getColumnIndex(WifiScanDatabase.SSID);
            ssid = mCursor.getString(columnIndex);

            columnIndex = mCursor.getColumnIndex(
                    WifiScanDatabase.LEVEL);
            level = mCursor.getInt(columnIndex);

            columnIndex = mCursor.getColumnIndex(
                    WifiScanDatabase.NETWORK_ID);
            networkID = mCursor.getInt(columnIndex);

            columnIndex = mCursor.getColumnIndex(
                    WifiScanDatabase.BSSID);
            bssid = mCursor.getString(columnIndex);

            columnIndex = mCursor.getColumnIndex(
                    WifiScanDatabase.SIGNALSTRENGTH);
            signalStrength = mCursor.getInt(columnIndex);

            columnIndex = mCursor.getColumnIndex(WifiScanDatabase.CONNECTED);
            connected = (mCursor.getInt(columnIndex) == 1);
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_wifi_list_item);
        rv.setTextViewText(R.id.widget_ssid, ssid);

        rv.setTextViewText(R.id.widget_bssid, bssid);

        rv.setImageViewResource(R.id.widget_signal_strength, WifiScanService.getSignalStrengthIcon(signalStrength, connected));

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

    public int getViewTypeCount() {
        // Technically, we have two types of views (the dark and light background views)
        return 2;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // Refresh the cursor
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(ScanDataProvider.CONTENT_URI, WifiScanDatabase.WIFI_NETWORKS_SSID_PROJECTION, null, null, WifiScanDatabase.LEVEL + " DESC");
    }

}
