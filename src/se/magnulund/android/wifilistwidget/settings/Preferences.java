package se.magnulund.android.wifilistwidget.settings;

/**
 * Created with IntelliJ IDEA. User: Gustav Date: 27/12/2012 Time: 00:25 To
 * change this template use File | Settings | File Templates.
 */
public class Preferences {

	// GLOBAL APP PREFERENCES
	public static final String SHOW_MAIN_HOTSPOT_TOGGLE = "show_main_hotspot_toggle";
	public static final String DEVICE_HAS_MOBILE_NETWORK = "device_has_mobile_networks";
	public static final String SCANNING_ENABLED = "scanning_enabled";
    public static final String THEME = "theme";
	public static final String MERGE_ACCESS_POINTS = "merge_access_points";

	public static final String WALLED_GARDEN_CHECK_DONE = "walled_garden_check_done";
	public static final String WALLED_GARDEN_CONNECTION = "walled_garden_connection";
	public static final String WALLED_GARDEN_REDIRECT_URL = "walled_garden_redirect_url";

	// WIDGET SPECIFIC PREFERENCES, SUFFIXED BY WIDGET ID

	public static final String APP_WIDGET_ID = "app_widget_id";

	public static final String WIDGET_MERGE_AP_ = "merge_access_points_widget_";
	public static final String SHOW_AP_BUTTON_ = "show_hotspot_toggle_widget_";
	public static final String THEME_ = "theme_widget_";
}
