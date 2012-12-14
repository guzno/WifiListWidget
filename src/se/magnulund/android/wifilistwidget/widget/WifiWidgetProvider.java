package se.magnulund.android.wifilistwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import se.magnulund.android.wifilistwidget.R;
import se.magnulund.android.wifilistwidget.utils.AlarmUtility;
import se.magnulund.android.wifilistwidget.wifiap.WIFI_AP_STATE;
import se.magnulund.android.wifilistwidget.wifiap.WifiApManager;
import se.magnulund.android.wifilistwidget.wifiscan.ScanDataProvider;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanDatabase;
import se.magnulund.android.wifilistwidget.wifistate.WifiStateService;

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
    public static String WIFI_TOGGLE_ACTION = "se.magnulund.android.wifilistwidget.widget.WIFI_TOGGLE";
    public static String HOTSPOT_TOGGLE_ACTION = "se.magnulund.android.wifilistwidget.widget.HOTSPOT_TOGGLE";

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

    private Boolean wifiEnabled;
    private Boolean mobileHotSpotActive;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Log.e(TAG, "Action: " + action);

        if (action.equals(CLICK_ACTION)) {

            final int networkId = intent.getIntExtra(WifiScanDatabase.NETWORK_ID, -1);
            if (networkId > -1) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(networkId, true);
                wifiManager.reconnect();

                AlarmUtility.scheduleAlarm(context);
            }
        } else if (action.equals(WIFI_TOGGLE_ACTION)) {
            Log.e(TAG, "Wifi toggle pressed");

            wifiEnabled = !wifiManager.isWifiEnabled();

            wifiManager.setWifiEnabled(wifiEnabled);

            if (wifiEnabled == false) {
                context.getContentResolver().delete(ScanDataProvider.CONTENT_URI, null, null);
            }

            AlarmUtility.scheduleAlarm(context);
        } else if (action.equals(HOTSPOT_TOGGLE_ACTION)) {
            Log.e(TAG, "HotSpot toggle pressed");
            WifiApManager wifiApManager = new WifiApManager(context);

            mobileHotSpotActive = wifiApManager.isWifiApEnabled();

            if (mobileHotSpotActive) {
                wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), false);
                wifiApManager.setWifiEnabled(true);
                wifiManager.startScan();
            } else {
                context.getContentResolver().delete(ScanDataProvider.CONTENT_URI, null, null);
                wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), true);
            }

            mobileHotSpotActive = !mobileHotSpotActive;

            AlarmUtility.scheduleAlarm(context);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {
            RemoteViews rv = getRemoteViews(context, appWidgetIds[i]);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void setWidgetActive(Context context, Boolean active) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(WIDGET_ACTIVE, active);
        edit.commit();
    }

    public static RemoteViews getRemoteViews(Context context, int appWidgetID) {
        // Specify the service to provide data for the collection widget.  Note that we need to
        // embed the appWidgetId via the data otherwise it will be ignored.
        final Intent intent = new Intent(context, WifiWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
        rv.setRemoteAdapter(R.id.widget_listview, intent);

        rv.setEmptyView(R.id.widget_listview, R.id.empty_view);

        // LIST ITEM CLICK

        final Intent onClickIntent = new Intent(context, WifiWidgetProvider.class);
        onClickIntent.setAction(WifiWidgetProvider.CLICK_ACTION);
        onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
        onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0,
                onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.widget_listview, onClickPendingIntent);

        // WIFI TOOGLE

        final Intent wifiToggleIntent = new Intent(context, WifiWidgetProvider.class);
        wifiToggleIntent.setAction(WifiWidgetProvider.WIFI_TOGGLE_ACTION);
        final PendingIntent wifiTogglePendingIntent = PendingIntent.getBroadcast(context, 0,
                wifiToggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_wifi_toggle, wifiTogglePendingIntent);

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();


        int wifiIconID = (wifiEnabled) ? R.drawable.ic_signal_strength_best_connected : R.drawable.ic_signal_strength_best;
        rv.setImageViewResource(R.id.widget_wifi_toggle, wifiIconID);

        // HOTSPOT TOGGLE

        final Intent hotSpotToggleIntent = new Intent(context, WifiWidgetProvider.class);
        hotSpotToggleIntent.setAction(WifiWidgetProvider.HOTSPOT_TOGGLE_ACTION);
        final PendingIntent hotSpotTogglePendingIntent = PendingIntent.getBroadcast(context, 0,
                hotSpotToggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_hotspot_toggle, hotSpotTogglePendingIntent);

        WifiApManager wifiApManager = new WifiApManager(context);
        boolean mobileHotSpotActive = wifiApManager.isWifiApEnabled();

        int hotSpotIconID = (mobileHotSpotActive) ? R.drawable.ic_menu_hotspot_active : R.drawable.ic_menu_hotspot_inactive;
        rv.setImageViewResource(R.id.widget_hotspot_toggle, hotSpotIconID);

        return rv;
    }

    public static boolean areWidgetsIdle(Context context) {

        boolean idle;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        int wifiState = wifiManager.getWifiState();

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_DISABLING:
            case WifiManager.WIFI_STATE_UNKNOWN:
                idle = false;
                break;
            default: {
                WifiApManager wifiApManager = new WifiApManager(context);
                WIFI_AP_STATE wifiApState = wifiApManager.getWifiApState();

                switch (wifiApState) {
                    case WIFI_AP_STATE_ENABLING:
                    case WIFI_AP_STATE_DISABLING:
                        idle = false;
                        break;
                    default:
                        idle = true;
                }
            }
        }

        return idle;
    }
}
