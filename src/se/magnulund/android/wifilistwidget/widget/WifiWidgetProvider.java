package se.magnulund.android.wifilistwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import se.magnulund.android.wifilistwidget.R;
import se.magnulund.android.wifilistwidget.wifiscan.ScanDataProvider;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanDatabase;
import se.magnulund.android.wifilistwidget.wifistate.WifiStateService;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 29/11/2012
 * Time: 23:39
 * To change this template use File | Settings | File Templates.
 */

/**
 * The weather widget's AppWidgetProvider.
 */
public class WifiWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WifiWidgetProvider";

    public static final String WIDGET_ACTIVE = "widget_active";

    public static String CLICK_ACTION = "se.magnulund.android.wifilistwidget.widget.CLICK";
    public static String REFRESH_ACTION = "se.magnulund.android.wifilistwidget.widget.REFRESH";
    public static String EXTRA_CITY_ID = "se.magnulund.android.wifilistwidget.widget.city";

    private static HandlerThread sWorkerThread;
    private static Handler sWorkerQueue;
    private static WifiWidgetDataObserver sDataObserver;

    public WifiWidgetProvider() {
        // Start the worker thread
        sWorkerThread = new HandlerThread("WifiWidgetProvider-worker");
        sWorkerThread.start();
        sWorkerQueue = new Handler(sWorkerThread.getLooper());
    }

    @Override
    public void onEnabled(Context context) {
        // Register for external updates to the data to trigger an update of the widget.  When using
        // content providers, the data is often updated via a background service, or in response to
        // user interaction in the main app.  To ensure that the widget always reflects the current
        // state of the data, we must listen for changes and update ourselves accordingly.
        Intent intent = new Intent(context, WifiStateService.class);
        context.startService(intent);
        final ContentResolver r = context.getContentResolver();
        if (sDataObserver == null) {
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, WifiWidgetProvider.class);
            sDataObserver = new WifiWidgetDataObserver(mgr, cn, sWorkerQueue);
            r.registerContentObserver(ScanDataProvider.CONTENT_URI, true, sDataObserver);
        }
        setWidgetActive(context, true);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent intent = new Intent(context, WifiStateService.class);
        intent.putExtra("stop_services", true);
        context.startService(intent);
        setWidgetActive(context, false);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();

        WifiManager wifiManager = (WifiManager) ctx.getSystemService(WifiWidgetService.WIFI_SERVICE);
        Log.e(TAG, "click?");
        if (action.equals(CLICK_ACTION)) {
            // Show a toast

            final int networkId = intent.getIntExtra(WifiScanDatabase.NETWORK_ID, -1);
            if ( networkId > -1 ){
                wifiManager.disconnect();
                wifiManager.enableNetwork(networkId, true);
                wifiManager.reconnect();
            }
        }

        super.onReceive(ctx, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        Log.e(TAG, "UPDATE");
        for (int i = 0; i < appWidgetIds.length; ++i) {
            // Specify the service to provide data for the collection widget.  Note that we need to
            // embed the appWidgetId via the data otherwise it will be ignored.
            final Intent intent = new Intent(context, WifiWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
            rv.setRemoteAdapter(R.id.widget_listview, intent);

            Log.e(TAG, "UPDATE in loop");

            // Set the empty view to be displayed if the collection is empty.  It must be a sibling
            // view of the collection view.
            rv.setEmptyView(R.id.widget_listview, R.id.empty_view);

            // Bind a click listener template for the contents of the weather list.  Note that we
            // need to update the intent's data if we set an extra, since the extras will be
            // ignored otherwise.

            final Intent onClickIntent = new Intent(context, WifiWidgetProvider.class);
            onClickIntent.setAction(WifiWidgetProvider.CLICK_ACTION);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0,
                    onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_listview, onClickPendingIntent);

            // Bind the click intent for the refresh button on the widget
            /*
            final Intent refreshIntent = new Intent(context, WifiWidgetProvider.class);
            refreshIntent.setAction(WifiWidgetProvider.REFRESH_ACTION);
            final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0,
                    refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.refresh, refreshPendingIntent);
            */

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void setWidgetActive(Context context, Boolean active){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(WIDGET_ACTIVE, active);
        edit.commit();
    }
}
