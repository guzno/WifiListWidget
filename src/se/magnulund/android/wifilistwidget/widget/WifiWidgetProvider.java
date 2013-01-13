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
import se.magnulund.android.wifilistwidget.connectivitychange.ConnectivityChangeService;
import se.magnulund.android.wifilistwidget.settings.Preferences;
import se.magnulund.android.wifilistwidget.utils.AlarmUtility;
import se.magnulund.android.wifilistwidget.utils.ComponentManager;
import se.magnulund.android.wifilistwidget.wifiap.WIFI_AP_STATE;
import se.magnulund.android.wifilistwidget.wifiap.WifiApManager;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanReceiver;
import se.magnulund.android.wifilistwidget.wifistate.WifiStateReceiver;
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
    public static final int UPDATE_ALARM_TYPE_BACKOFF = 3;
    public static final int UPDATE_ALARM_TYPE_SCAN_DELAY = 4;
    public static final int UPDATE_ALARM_TYPE_WIFI_STATE = 5;
    public static final int UPDATE_SYSTEM_INITIATED = 6;
    public static final int UPDATE_CONNECTION_CHANGE = 7;
    public static final int UPDATE_CONNECTION_LOST = 8;

    public static final String CLICK_TYPE = "click_type";
    public static final int ITEM_CLICK_CONNECT = 1;
    public static final int ITEM_CLICK_REDIRECT = 2;

    public static final String NETWORK_ID = "network_id";
    public static final String REDIRECT_URL = "redirect_url";

    private Boolean wifiEnabled;
    private Boolean mobileHotSpotActive;

    public WifiWidgetProvider() {
    }

    @Override
    public void onEnabled(Context context) {
        Intent intent = new Intent(context, WifiStateService.class);
        context.startService(intent);
        setWidgetActive(context, true);
    }

    private void setWidgetActive(Context context, Boolean active) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if (active) {

            if (preferences.contains(Preferences.DEVICE_HAS_MOBILE_NETWORK) == false) {
                Log.e(TAG, "MOBILE NETWORKS NOT CHECKED, CHECKING...");
                editor.putBoolean(Preferences.DEVICE_HAS_MOBILE_NETWORK, MainActivity.deviceHasMobileNetwork(context));
            }
            if (preferences.contains(Preferences.WALLED_GARDEN_CHECK_DONE) == false) {
                Intent intent = new Intent(context, ConnectivityChangeService.class);
                intent.putExtra(ConnectivityChangeService.FIRST_RUN_CHECK, true);
                context.startService(intent);
            }
        }
        editor.putBoolean(WIDGET_ACTIVE, active);
        editor.commit();
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        ComponentManager.disableComponent(context, WifiStateReceiver.class);
        ComponentManager.disableComponent(context, WifiScanReceiver.class);
        setWidgetActive(context, false);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        Context appContext = context.getApplicationContext();

        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        //Log.e(TAG, "Action: " + action);

        if (action.equals(CLICK_ACTION)) {
            switch (intent.getIntExtra(CLICK_TYPE, -1)) {
                case ITEM_CLICK_CONNECT: {
                    final int networkId = intent.getIntExtra(WifiWidgetProvider.NETWORK_ID, -1);
                    if (networkId > -1) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(networkId, true);
                        wifiManager.reconnect();

                        //AlarmUtility.scheduleAlarm(context, AlarmUtility.ALARM_TYPE_BACKOFF);
                    }

                    break;
                }
                case ITEM_CLICK_REDIRECT: {
                    String redirectUrl = intent.getStringExtra(REDIRECT_URL);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(redirectUrl));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                    break;
                }
                default: {
                    Log.e(TAG, "unknown click action");
                    break;
                }
            }
        } else if (action.equals(WIFI_TOGGLE_ACTION)) {

            wifiEnabled = !wifiManager.isWifiEnabled();

            wifiManager.setWifiEnabled(wifiEnabled);

        } else if (action.equals(HOTSPOT_TOGGLE_ACTION)) {
            WifiApManager wifiApManager = new WifiApManager(appContext);

            mobileHotSpotActive = wifiApManager.isWifiApEnabled();

            if (mobileHotSpotActive) {
                wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), false);
                wifiApManager.setWifiEnabled(true);
                wifiManager.startScan();
            } else {
                wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), true);
            }

            mobileHotSpotActive = !mobileHotSpotActive;

            AlarmUtility.scheduleAlarm(context, AlarmUtility.ALARM_TYPE_BACKOFF);

        } else if (action.equals(WIFI_SCAN_ACTION)) {
            wifiManager.startScan();
            AlarmUtility.scheduleAlarm(context, AlarmUtility.ALARM_TYPE_SCAN_DELAY);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        updateWidgets(context, UPDATE_SYSTEM_INITIATED, null);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidgets(Context context, int updateType, Integer updateInfo) {

        //Log.e(TAG, "Update type: " + updateType);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widget = new ComponentName(context, WifiWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);

        boolean updateListView = false;

        if (appWidgetIds.length > 0) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            SharedPreferences.Editor editor;

            // DO THINGS BEFORE UPDATING WIDGETS

            switch (updateType) {
                case UPDATE_WIFI_STATE_CHANGED: {
                    break;
                }
                case UPDATE_WIFI_SCAN_RESULTS: {
                    editor = preferences.edit();
                    editor.putBoolean(Preferences.SCANNING_ENABLED, true);
                    editor.commit();
                    updateListView = true;
                    break;
                }
                case UPDATE_ALARM_TYPE_BACKOFF: {
                    break;
                }
                case UPDATE_ALARM_TYPE_SCAN_DELAY: {
                    editor = preferences.edit();
                    if (updateInfo != null) {
                        switch (updateInfo) {
                            case AlarmUtility.DISABLE_SCANNING: {
                                editor.putBoolean(Preferences.SCANNING_ENABLED, false);
                                editor.commit();
                                break;
                            }
                            case AlarmUtility.RE_ENABLE_SCANNING: {
                                editor.putBoolean(Preferences.SCANNING_ENABLED, true);
                                editor.commit();
                                break;
                            }
                        }
                    }
                    break;
                }
                case UPDATE_ALARM_TYPE_WIFI_STATE: {
                    break;
                }
                case UPDATE_SYSTEM_INITIATED: {
                    updateListView = true;
                    break;
                }
                case UPDATE_CONNECTION_CHANGE: {
                    updateListView = true;
                    break;
                }
                case UPDATE_CONNECTION_LOST: {
                    break;
                }
                default:
                    Log.e(TAG, "Unknown update type: " + updateType);
                    return;
            }

            // UPDATE WIDGETS

            for (int appWidgetId : appWidgetIds) {
                WidgetRemoteViews widgetRemoteViews = new WidgetRemoteViews(context, appWidgetId);
                widgetRemoteViews.setupRemoteViews();
                RemoteViews rv = widgetRemoteViews.createRemoteViews();
                appWidgetManager.updateAppWidget(appWidgetId, rv);
                if (updateListView) {
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview);
                }
            }

            // DO THINGS AFTER UPDATING WIDGETS

            switch (updateType) {
                case UPDATE_WIFI_STATE_CHANGED: {
                    if (updateInfo == WifiManager.WIFI_STATE_DISABLING) {
                        AlarmUtility.scheduleWifiStateChecker(context, updateInfo, 1);
                    }
                    break;
                }
                case UPDATE_WIFI_SCAN_RESULTS: {
                    break;
                }
                case UPDATE_ALARM_TYPE_BACKOFF: {
                    break;
                }
                case UPDATE_ALARM_TYPE_SCAN_DELAY: {
                    break;
                }
                case UPDATE_ALARM_TYPE_WIFI_STATE: {
                    break;
                }
                case UPDATE_SYSTEM_INITIATED: {
                    break;
                }
                case UPDATE_CONNECTION_CHANGE: {
                    break;
                }
                case UPDATE_CONNECTION_LOST: {
                    break;
                }
                default:
                    break;
            }
        }
    }



}
