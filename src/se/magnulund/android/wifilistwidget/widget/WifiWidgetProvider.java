package se.magnulund.android.wifilistwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import se.magnulund.android.wifilistwidget.MainActivity;
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
 * The weather widget_listview's AppWidgetProvider.
 */
public class WifiWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WifiWidgetProvider";

    public static final String WIDGET_ACTIVE = "widget_active";

    public static String CLICK_ACTION = "se.magnulund.android.wifilistwidget.widget_listview.CLICK";
    public static String WIFI_TOGGLE_ACTION = "se.magnulund.android.wifilistwidget.widget_listview.WIFI_TOGGLE";
    public static String HOTSPOT_TOGGLE_ACTION = "se.magnulund.android.wifilistwidget.widget_listview.HOTSPOT_TOGGLE";
    public static String WIFI_SCAN_ACTION = "se.magnulund.android.wifilistwidget.widget_listview.WIFI_SCAN";

    public static final int UPDATE_WIFI_STATE_CHANGED = 1;
    public static final int UPDATE_WIFI_SCAN_RESULTS = 2;

    private static final int WIDGET_TYPE_LISTVIEW = 1;
    private static final int WIDGET_TYPE_TOGGLE = 2;
    private static final int WIDGET_TYPE_PENDING = 3;

    //private static HandlerThread sWorkerThread;
    //private static Handler sWorkerQueue;
    //private static WifiWidgetDataObserver sDataObserver;

    public WifiWidgetProvider() {
        // Start the worker thread
        //sWorkerThread = new HandlerThread("WifiWidgetProvider-worker");
        //sWorkerThread.start();
        //sWorkerQueue = new Handler(sWorkerThread.getLooper());
    }

    @Override
    public void onEnabled(Context context) {
        // Register for external updates to the data to trigger an update of the widget_listview.  When using
        // content providers, the data is often updated via a background service, or in response to
        // user interaction in the main app.  To ensure that the widget_listview always reflects the current
        // state of the data, we must listen for changes and update ourselves accordingly.
        Intent intent = new Intent(context, WifiStateService.class);
        context.startService(intent);
        /*final ContentResolver r = context.getContentResolver();
        if (sDataObserver == null) {
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, WifiWidgetProvider.class);
            sDataObserver = new WifiWidgetDataObserver(mgr, cn, sWorkerQueue);
            r.registerContentObserver(ScanDataProvider.CONTENT_URI, true, sDataObserver);
        }*/
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

        Context appContext = context.getApplicationContext();

        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        Log.e(TAG, "Action: " + action);

        if (action.equals(CLICK_ACTION)) {

            final int networkId = intent.getIntExtra(WifiScanDatabase.NETWORK_ID, -1);
            if (networkId > -1) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(networkId, true);
                wifiManager.reconnect();

                AlarmUtility.scheduleAlarm(context, AlarmUtility.ALARM_TYPE_BACKOFF);
            }
        } else if (action.equals(WIFI_TOGGLE_ACTION)) {
            Log.e(TAG, "Wifi toggle pressed");

            wifiEnabled = !wifiManager.isWifiEnabled();

            wifiManager.setWifiEnabled(wifiEnabled);

            if (wifiEnabled == false) {
                context.getContentResolver().delete(ScanDataProvider.CONTENT_URI, null, null);
            }

            //AlarmUtility.scheduleAlarm(context, AlarmUtility.ALARM_TYPE_BACKOFF);
        } else if (action.equals(HOTSPOT_TOGGLE_ACTION)) {
            Log.e(TAG, "HotSpot toggle pressed");
            WifiApManager wifiApManager = new WifiApManager(appContext);

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

            AlarmUtility.scheduleAlarm(context, AlarmUtility.ALARM_TYPE_BACKOFF);

        } else if (action.equals(WIFI_SCAN_ACTION)) {
            wifiManager.startScan();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(AlarmUtility.SCANNING_ENABLED, false);
            editor.commit();
            //Log.e(TAG, "Scanning enabled1: " + preferences.getBoolean(AlarmUtility.SCANNING_ENABLED, true));
            AlarmUtility.scheduleAlarm(context, AlarmUtility.ALARM_TYPE_SCAN_DELAY);
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        if (preferences.getBoolean(MainActivity.MOBILE_NETWORK_CHECKED, false) == false) {
            Log.e(TAG, "MOBILE NETWORKS NOT CHECKED, CHECKING...");
            edit.putBoolean(MainActivity.MOBILE_NETWORK_CHECKED, true);
            edit.putBoolean(MainActivity.DEVICE_HAS_MOBILE_NETWORK, MainActivity.deviceHasMobileNetwork(context));
        }
        edit.putBoolean(WIDGET_ACTIVE, active);
        edit.commit();
    }

    public static RemoteViews getRemoteViews(Context context, int appWidgetID) {

        Context appContext = context.getApplicationContext();

        WifiApManager wifiApManager = new WifiApManager(appContext);
        WIFI_AP_STATE wifiApState = wifiApManager.getWifiApState();

        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiManager.getWifiState();

        int widgetType = getWidgetType(wifiApState, wifiState);

        final RemoteViews rv = getWidgetLayoutAndHeader(context, appContext, widgetType, wifiApState, wifiState);

        switch (widgetType) {
            case WIDGET_TYPE_LISTVIEW: {
                // Specify the service to provide data for the collection widget_listview.  Note that we need to
                // embed the appWidgetId via the data otherwise it will be ignored.
                final Intent intent = new Intent(context, WifiWidgetService.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
                intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
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
                break;
            }
            case WIDGET_TYPE_TOGGLE: {
                int toggleImg = 0;
                int toggleMsg = 0;
                final Intent toggleIntent = new Intent(context, WifiWidgetProvider.class);

                if (wifiApState == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED) {
                    toggleImg = R.drawable.ic_widget_ap_toggle;
                    toggleMsg = R.string.widget_toggle_msg_hotspot;
                    toggleIntent.setAction(WifiWidgetProvider.HOTSPOT_TOGGLE_ACTION);

                } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    toggleImg = R.drawable.ic_widget_wifi_toggle;
                    toggleMsg = R.string.widget_toggle_msg_wifi;
                    toggleIntent.setAction(WifiWidgetProvider.WIFI_TOGGLE_ACTION);
                }

                if (toggleImg > 0) {

                    rv.setImageViewResource(R.id.widget_main_toggle, toggleImg);
                    rv.setTextViewText(R.id.widget_main_toggle_text, context.getResources().getString(toggleMsg));

                    final PendingIntent togglePendingIntent = PendingIntent.getBroadcast(context, 0, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    rv.setOnClickPendingIntent(R.id.widget_main_toggle, togglePendingIntent);

                }

                break;
            }
            case WIDGET_TYPE_PENDING: {
                break;
            }
        }

        return rv;
    }

    private static int getWidgetType(WIFI_AP_STATE wifiApState, int wifiState) {
        int widgetType = 0;

        // CHECK WHICH LAYOUT TO USE
        switch (wifiApState) {
            case WIFI_AP_STATE_ENABLED: {
                widgetType = WIDGET_TYPE_TOGGLE;
                break;
            }
            case WIFI_AP_STATE_DISABLING:
            case WIFI_AP_STATE_ENABLING: {
                widgetType = WIDGET_TYPE_PENDING;
                break;
            }
            case WIFI_AP_STATE_DISABLED: {
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED: {
                        widgetType = WIDGET_TYPE_LISTVIEW;
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLING:
                    case WifiManager.WIFI_STATE_DISABLING: {
                        widgetType = WIDGET_TYPE_PENDING;
                        break;
                    }
                    case WifiManager.WIFI_STATE_DISABLED: {
                        widgetType = WIDGET_TYPE_TOGGLE;
                        break;
                    }
                }
                break;
            }
            default:
                widgetType = WIDGET_TYPE_PENDING;
                break;
        }
        Log.e(TAG, "WIDGET TYPE: " + widgetType);
        return widgetType;
    }

    private static RemoteViews getWidgetLayoutAndHeader(Context context, Context appContext, int widgetType, WIFI_AP_STATE wifiApState, int wifiState) {
        int headerApImg;
        int headerWifiImg;
        int headerScanImg;
        int widgetLayout;

        //boolean deviceHasAP = true;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        boolean deviceHasAP = preferences.getBoolean(MainActivity.DEVICE_HAS_MOBILE_NETWORK, true);

        // CHECK WHICH LAYOUT TO USE
        if (deviceHasAP) {
            switch (widgetType) {
                case WIDGET_TYPE_LISTVIEW: {
                    widgetLayout = R.layout.widget_listview;
                    break;
                }
                case WIDGET_TYPE_TOGGLE: {
                    widgetLayout = R.layout.widget_toggle;
                    break;
                }
                case WIDGET_TYPE_PENDING: {
                    widgetLayout = R.layout.widget_pending;
                    break;
                }
                default:
                    widgetLayout = R.layout.widget_pending;
                    break;
            }
        } else {
            switch (widgetType) {
                case WIDGET_TYPE_LISTVIEW: {
                    widgetLayout = R.layout.widget_listview_no_ap;
                    break;
                }
                case WIDGET_TYPE_TOGGLE: {
                    widgetLayout = R.layout.widget_toggle_no_ap;
                    break;
                }
                case WIDGET_TYPE_PENDING: {
                    widgetLayout = R.layout.widget_pending_no_ap;
                    break;
                }
                default:
                    widgetLayout = R.layout.widget_pending_no_ap;
                    break;
            }
        }


        final RemoteViews rv = new RemoteViews(context.getPackageName(), widgetLayout);

        boolean bindAPIntent = false;
        boolean bindWifiIntents = true;

        if (deviceHasAP) {
            switch (wifiApState) {
                case WIFI_AP_STATE_ENABLED: {
                    headerApImg = R.drawable.ic_menu_hotspot_active;
                    bindAPIntent = true;
                    bindWifiIntents = false;
                    break;
                }
                case WIFI_AP_STATE_DISABLING:
                case WIFI_AP_STATE_ENABLING: {
                    headerApImg = R.drawable.ic_menu_hotspot_pending;
                    bindWifiIntents = false;
                    break;
                }
                case WIFI_AP_STATE_DISABLED: {
                    headerApImg = R.drawable.ic_menu_hotspot_inactive;
                    bindAPIntent = true;
                    break;
                }
                default:
                    headerApImg = R.drawable.ic_menu_hotspot_pending;
            }

            rv.setImageViewResource(R.id.widget_hotspot_toggle, headerApImg);
            if (bindAPIntent) {
                final Intent hotSpotToggleIntent = new Intent(context, WifiWidgetProvider.class);
                hotSpotToggleIntent.setAction(WifiWidgetProvider.HOTSPOT_TOGGLE_ACTION);
                final PendingIntent hotSpotTogglePendingIntent = PendingIntent.getBroadcast(context, 0,
                        hotSpotToggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                rv.setOnClickPendingIntent(R.id.widget_hotspot_toggle, hotSpotTogglePendingIntent);
            } else {
                rv.setOnClickPendingIntent(R.id.widget_hotspot_toggle, null);
            }

        }

        boolean scanningEnabled = true;

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED: {
                headerWifiImg = R.drawable.ic_signal_strength_best_connected;
                headerScanImg = R.drawable.ic_wifi_scan_active;
                break;
            }
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_DISABLING: {
                headerWifiImg = R.drawable.ic_wifi_state_pending;
                headerScanImg = R.drawable.ic_wifi_scan_inactive;
                bindWifiIntents = false;
                break;
            }
            case WifiManager.WIFI_STATE_DISABLED: {
                headerWifiImg = R.drawable.ic_wifi_state_disabled;
                headerScanImg = R.drawable.ic_wifi_scan_inactive;
                scanningEnabled = false;
                break;
            }
            default:
                headerWifiImg = R.drawable.ic_wifi_state_pending;
                headerScanImg = R.drawable.ic_wifi_scan_inactive;
                bindWifiIntents = false;
                break;
        }
        //Log.e(TAG, "Scanning enabled 2: " + preferences.getBoolean(AlarmUtility.SCANNING_ENABLED, true));
        if (preferences.getBoolean(AlarmUtility.SCANNING_ENABLED, true) == false) {
            scanningEnabled = false;
            headerScanImg = R.drawable.ic_wifi_scan_inactive;
            //Log.e(TAG, "Scanning disabled!");
        }

        rv.setImageViewResource(R.id.widget_wifi_toggle, headerWifiImg);
        rv.setImageViewResource(R.id.widget_wifi_scan, headerScanImg);

        if (bindWifiIntents) {
            //Log.e(TAG, "Binding WIFI Intents");
            final Intent wifiToggleIntent = new Intent(context, WifiWidgetProvider.class);
            wifiToggleIntent.setAction(WifiWidgetProvider.WIFI_TOGGLE_ACTION);
            final PendingIntent wifiTogglePendingIntent = PendingIntent.getBroadcast(context, 0,
                    wifiToggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widget_wifi_toggle, wifiTogglePendingIntent);

            if (scanningEnabled) {
                final Intent wifiScanIntent = new Intent(context, WifiWidgetProvider.class);
                wifiScanIntent.setAction(WifiWidgetProvider.WIFI_SCAN_ACTION);
                final PendingIntent wifiScanPendingIntent = PendingIntent.getBroadcast(context, 0,
                        wifiScanIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                rv.setOnClickPendingIntent(R.id.widget_wifi_scan, wifiScanPendingIntent);
                //Log.e(TAG, "Scanning enabled!");
            } else {
                rv.setOnClickPendingIntent(R.id.widget_wifi_scan, null);
            }
        } else {
            rv.setOnClickPendingIntent(R.id.widget_wifi_toggle, null);
            rv.setOnClickPendingIntent(R.id.widget_wifi_scan, null);
        }


        return rv;
    }

    public static void updateWidgets(Context context, int updateType, int wifiState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (preferences.getBoolean(WifiWidgetProvider.WIDGET_ACTIVE, false) == true) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName widget = new ComponentName(context, WifiWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);

            for (int i = 0; i < appWidgetIds.length; ++i) {
                Log.e(TAG, "updating widget " + i);
                RemoteViews rv = WifiWidgetProvider.getRemoteViews(context, appWidgetIds[i]);
                appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
            }
            switch (updateType) {
                case UPDATE_WIFI_STATE_CHANGED: {
                    AlarmUtility.scheduleWifiStateChecker(context, wifiState);
                    break;
                }
                case UPDATE_WIFI_SCAN_RESULTS: {
                    Log.e(TAG, "notify new scanresults");
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview);
                    break;
                }
                default:
                    break;
            }


        }
    }
}
