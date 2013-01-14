package se.magnulund.android.wifilistwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import se.magnulund.android.wifilistwidget.R;
import se.magnulund.android.wifilistwidget.settings.Preferences;
import se.magnulund.android.wifilistwidget.wifiap.WIFI_AP_STATE;
import se.magnulund.android.wifilistwidget.wifiap.WifiApManager;

/**
 * Created with IntelliJ IDEA. User: Gustav Date: 10/01/2013 Time: 23:27 To
 * change this template use File | Settings | File Templates.
 */
public class WidgetRemoteViews {
    private static final String TAG = "WidgetRemoteViews";

    //
    Context mContext;
    int appWidgetID;
    SharedPreferences preferences;
    int wifiState;
    WIFI_AP_STATE wifiApState;

    // Preferences
    int widgetTheme;
    boolean showAP;
    boolean scanningEnabled;

    // Parameters
    int widgetType;
    int widgetLayout;

    int headerApImg;
    int headerWifiImg;
    int headerScanImg;

    boolean bindWifiApIntent;
    boolean bindWifiIntent;
    boolean bindScanningIntent;

    // static stuff...

    private static final int WIDGET_TYPE_LISTVIEW = 1;
    private static final int WIDGET_TYPE_TOGGLE = 2;
    private static final int WIDGET_TYPE_PENDING = 3;

    public static final int WIDGET_THEME_DARK = 1;
    public static final int WIDGET_THEME_LIGHT = 2;

    private static final int BUTTON_STATE_ACTIVE = 1;
    private static final int BUTTON_STATE_PENDING = 2;
    private static final int BUTTON_STATE_INACTIVE = 3;

    public WidgetRemoteViews(Context context, int appWidgetID) {
        this.mContext = context.getApplicationContext();

        WifiApManager wifiApManager = new WifiApManager(mContext);
        this.wifiApState = wifiApManager.getWifiApState();

        WifiManager wifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        this.wifiState = wifiManager.getWifiState();

        this.preferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        this.appWidgetID = appWidgetID;
    }

    public void getPreferences() {
        widgetTheme = Integer.parseInt(preferences.getString(Preferences.THEME_
                + appWidgetID, "1"));
        showAP = (preferences.getBoolean(Preferences.SHOW_AP_BUTTON_
                + appWidgetID, true) && preferences.getBoolean(
                Preferences.DEVICE_HAS_MOBILE_NETWORK, true));
        scanningEnabled = (preferences.contains(Preferences.SCANNING_ENABLED) && preferences
                .getBoolean(Preferences.SCANNING_ENABLED, false));
    }

    public void setupRemoteViews() {
        getPreferences();

        getWidgetType();

        getWidgetTheme();

        getWidgetLayout();

        if (showAP) {
            getApResourceAndIntent();
        }

        getWifiResourcesAndIntents();
    }

    public RemoteViews createRemoteViews() {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(),
                widgetLayout);
        Log.e(TAG, "Creating remote views: " + (rv != null));
        // Set resources and bind intents to header of widget
        if (showAP) {
            rv.setImageViewResource(R.id.widget_hotspot_toggle, headerApImg);
            if (bindWifiApIntent) {
                final Intent hotSpotToggleIntent = new Intent(mContext,
                        WifiWidgetProvider.class);
                hotSpotToggleIntent
                        .setAction(WifiWidgetProvider.HOTSPOT_TOGGLE_ACTION);
                final PendingIntent hotSpotTogglePendingIntent = PendingIntent
                        .getBroadcast(mContext, 0, hotSpotToggleIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                rv.setOnClickPendingIntent(R.id.widget_hotspot_toggle,
                        hotSpotTogglePendingIntent);
            } else {
                rv.setOnClickPendingIntent(R.id.widget_hotspot_toggle, null);
            }
        }

        rv.setImageViewResource(R.id.widget_wifi_toggle, headerWifiImg);
        rv.setImageViewResource(R.id.widget_wifi_scan, headerScanImg);

        if (bindWifiIntent) {
            final Intent wifiToggleIntent = new Intent(mContext,
                    WifiWidgetProvider.class);
            wifiToggleIntent.setAction(WifiWidgetProvider.WIFI_TOGGLE_ACTION);
            final PendingIntent wifiTogglePendingIntent = PendingIntent
                    .getBroadcast(mContext, 0, wifiToggleIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widget_wifi_toggle,
                    wifiTogglePendingIntent);

            if (bindScanningIntent) {
                final Intent wifiScanIntent = new Intent(mContext,
                        WifiWidgetProvider.class);
                wifiScanIntent.setAction(WifiWidgetProvider.WIFI_SCAN_ACTION);
                final PendingIntent wifiScanPendingIntent = PendingIntent
                        .getBroadcast(mContext, 0, wifiScanIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                rv.setOnClickPendingIntent(R.id.widget_wifi_scan,
                        wifiScanPendingIntent);
            } else {
                rv.setOnClickPendingIntent(R.id.widget_wifi_scan, null);
            }
        } else {
            rv.setOnClickPendingIntent(R.id.widget_wifi_toggle, null);
            rv.setOnClickPendingIntent(R.id.widget_wifi_scan, null);
        }

        // Set resources and bind intents to body of widget
        switch (widgetType) {
            case WIDGET_TYPE_LISTVIEW: {
                final Intent intent = new Intent(mContext, WifiWidgetService.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
                intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
                rv.setRemoteAdapter(R.id.widget_listview, intent);

                rv.setEmptyView(R.id.widget_listview, R.id.empty_view);

                final Intent onClickIntent = new Intent(mContext,
                        WifiWidgetProvider.class);
                onClickIntent.setAction(WifiWidgetProvider.CLICK_ACTION);
                onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        appWidgetID);
                onClickIntent.setData(Uri.parse(onClickIntent
                        .toUri(Intent.URI_INTENT_SCHEME)));
                final PendingIntent onClickPendingIntent = PendingIntent
                        .getBroadcast(mContext, 0, onClickIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                rv.setPendingIntentTemplate(R.id.widget_listview,
                        onClickPendingIntent);
                break;
            }
            case WIDGET_TYPE_TOGGLE: {
                int toggleImg = 0;
                int toggleMsg = 0;
                final Intent toggleIntent = new Intent(mContext,
                        WifiWidgetProvider.class);

                if (wifiApState == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED) {
                    toggleImg = R.drawable.ic_widget_ap_toggle;
                    toggleMsg = R.string.widget_toggle_msg_hotspot;
                    toggleIntent
                            .setAction(WifiWidgetProvider.HOTSPOT_TOGGLE_ACTION);

                } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    toggleImg = R.drawable.ic_widget_wifi_toggle;
                    toggleMsg = R.string.widget_toggle_msg_wifi;
                    toggleIntent.setAction(WifiWidgetProvider.WIFI_TOGGLE_ACTION);
                }

                if (toggleImg > 0) {
                    rv.setImageViewResource(R.id.widget_main_toggle, toggleImg);
                    rv.setTextViewText(R.id.widget_main_toggle_text, mContext
                            .getResources().getString(toggleMsg));

                    final PendingIntent togglePendingIntent = PendingIntent
                            .getBroadcast(mContext, 0, toggleIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                    rv.setOnClickPendingIntent(R.id.widget_main_toggle,
                            togglePendingIntent);
                }

                break;
            }
            case WIDGET_TYPE_PENDING: {
                break;
            }
        }

        return rv;
    }

    public void getWidgetType() {
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
        this.widgetType = widgetType;
    }

    public void getWidgetTheme() {
        switch (widgetTheme) {
            case WIDGET_THEME_DARK: {
                mContext.setTheme(R.style.WidgetTheme_Dark);
                Log.e(TAG, "widget theme dark"+mContext.getTheme());
                break;
            }
            case WIDGET_THEME_LIGHT: {
                mContext.setTheme(R.style.WidgetTheme_Light);
                Log.e(TAG, "widget theme light"+mContext.getTheme());
                break;
            }
            default: {
                mContext.setTheme(R.style.WidgetTheme_Dark);
                Log.e(TAG, "widget theme dark (default)"+mContext.getTheme());
                break;
            }
        }
    }

    public void getWidgetLayout() {

        int widgetLayout;

        // CHECK WHICH LAYOUT TO USE
        if (showAP) {
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
        this.widgetLayout = widgetLayout;
    }

    public void getApResourceAndIntent() {
        int headerApButtonState;
        boolean bindWifiApIntent;

        if (showAP) {
            switch (wifiApState) {
                case WIFI_AP_STATE_ENABLED: {
                    headerApButtonState = BUTTON_STATE_ACTIVE;
                    bindWifiApIntent = true;
                    break;
                }
                case WIFI_AP_STATE_DISABLING:
                case WIFI_AP_STATE_ENABLING: {
                    headerApButtonState = BUTTON_STATE_PENDING;
                    bindWifiApIntent = false;
                    break;
                }
                case WIFI_AP_STATE_DISABLED: {
                    headerApButtonState = BUTTON_STATE_INACTIVE;
                    bindWifiApIntent = true;
                    break;
                }
                default:
                    headerApButtonState = BUTTON_STATE_PENDING;
                    bindWifiApIntent = false;
            }

        } else {
            headerApButtonState = BUTTON_STATE_INACTIVE;
            bindWifiApIntent = false;
        }

        this.headerApImg = getApIcon(headerApButtonState);
        this.bindWifiApIntent = bindWifiApIntent;
    }

    public int getApIcon(int buttonState) {
        switch (widgetTheme) {
            case WIDGET_THEME_LIGHT: {
                switch (buttonState) {
                    case BUTTON_STATE_ACTIVE: {
                        return R.drawable.ic_menu_hotspot_active;
                    }
                    case BUTTON_STATE_PENDING: {
                        return R.drawable.ic_menu_hotspot_pending;
                    }
                    case BUTTON_STATE_INACTIVE: {
                        return R.drawable.ic_menu_hotspot_inactive;
                    }
                    default: {
                        return R.drawable.ic_menu_hotspot_inactive;
                    }
                }
            }
            case WIDGET_THEME_DARK:
            default: {
                switch (buttonState) {
                    case BUTTON_STATE_ACTIVE: {
                        return R.drawable.ic_menu_hotspot_active;
                    }
                    case BUTTON_STATE_PENDING: {
                        return R.drawable.ic_menu_hotspot_pending;
                    }
                    case BUTTON_STATE_INACTIVE: {
                        return R.drawable.ic_menu_hotspot_inactive;
                    }
                    default: {
                        return R.drawable.ic_menu_hotspot_inactive;
                    }
                }
            }
        }
    }

    public void getWifiResourcesAndIntents() {
        int headerWifiButtonState;
        int headerScanButtonState;
        boolean bindScanningIntent;
        boolean bindWifiIntent;

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED: {
                headerWifiButtonState = BUTTON_STATE_ACTIVE;
                bindWifiIntent = true;
                if (scanningEnabled) {
                    headerScanButtonState = BUTTON_STATE_ACTIVE;
                    bindScanningIntent = true;
                } else {
                    headerScanButtonState = BUTTON_STATE_INACTIVE;
                    bindScanningIntent = false;
                }
                break;
            }
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_DISABLING: {
                headerWifiButtonState = BUTTON_STATE_PENDING;
                headerScanButtonState = BUTTON_STATE_INACTIVE;
                bindWifiIntent = false;
                bindScanningIntent = false;
                break;
            }
            case WifiManager.WIFI_STATE_DISABLED: {
                headerWifiButtonState = BUTTON_STATE_INACTIVE;
                headerScanButtonState = BUTTON_STATE_INACTIVE;
                bindWifiIntent = true;
                bindScanningIntent = false;
                break;
            }
            default:
                headerWifiButtonState = BUTTON_STATE_PENDING;
                headerScanButtonState = BUTTON_STATE_INACTIVE;
                bindWifiIntent = false;
                bindScanningIntent = false;
                break;
        }
        this.headerWifiImg = getWifiIcon(headerWifiButtonState);
        this.headerScanImg = getScanIcon(headerScanButtonState);
        this.bindWifiIntent = bindWifiIntent;
        this.bindScanningIntent = bindScanningIntent;
    }

    public int getWifiIcon(int buttonState) {
        switch (widgetTheme) {
            case WIDGET_THEME_LIGHT: {
                switch (buttonState) {
                    case BUTTON_STATE_ACTIVE: {
                        return R.drawable.ic_signal_strength_best_connected;
                    }
                    case BUTTON_STATE_PENDING: {
                        return R.drawable.ic_wifi_state_pending;
                    }
                    case BUTTON_STATE_INACTIVE: {
                        return R.drawable.ic_wifi_state_disabled;
                    }
                    default: {
                        return R.drawable.ic_wifi_state_disabled;
                    }
                }
            }
            case WIDGET_THEME_DARK:
            default: {
                switch (buttonState) {
                    case BUTTON_STATE_ACTIVE: {
                        return R.drawable.ic_signal_strength_best_connected;
                    }
                    case BUTTON_STATE_PENDING: {
                        return R.drawable.ic_wifi_state_pending;
                    }
                    case BUTTON_STATE_INACTIVE: {
                        return R.drawable.ic_wifi_state_disabled;
                    }
                    default: {
                        return R.drawable.ic_wifi_state_disabled;
                    }
                }
            }
        }
    }

    public int getScanIcon(int buttonState) {
        switch (widgetTheme) {
            case WIDGET_THEME_LIGHT: {
                switch (buttonState) {
                    case BUTTON_STATE_ACTIVE: {
                        return R.drawable.ic_wifi_scan_active;
                    }
                    case BUTTON_STATE_INACTIVE:
                    default: {
                        return R.drawable.ic_wifi_scan_inactive;
                    }
                }
            }
            case WIDGET_THEME_DARK:
            default: {
                switch (buttonState) {
                    case BUTTON_STATE_ACTIVE: {
                        return R.drawable.ic_wifi_scan_active;
                    }
                    case BUTTON_STATE_INACTIVE:
                    default: {
                        return R.drawable.ic_wifi_scan_inactive;
                    }
                }
            }
        }
    }
}
