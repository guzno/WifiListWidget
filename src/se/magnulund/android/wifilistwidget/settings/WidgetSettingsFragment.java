package se.magnulund.android.wifilistwidget.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import se.magnulund.android.wifilistwidget.R;

/**
 * Created with IntelliJ IDEA. User: Gustav Date: 28/11/2012 Time: 23:19 To
 * change this template use File | Settings | File Templates.
 */
public class WidgetSettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.widget_preferences);

		int appWidgetID = getArguments().getInt(Preferences.APP_WIDGET_ID);

		Preference hotSpotToggle = findPreference(Preferences.SHOW_AP_BUTTON_);
		hotSpotToggle.setKey(hotSpotToggle.getKey() + appWidgetID);
		if (getArguments().getBoolean(Preferences.DEVICE_HAS_MOBILE_NETWORK) == false) {
			hotSpotToggle.setDefaultValue(false);
			hotSpotToggle.setEnabled(false);
			hotSpotToggle
					.setSummary("Only available on devices with mobile connections");
		}

		Preference mergeAPs = findPreference(Preferences.WIDGET_MERGE_AP_);
		mergeAPs.setKey(mergeAPs.getKey() + appWidgetID);

		Preference theme = findPreference(Preferences.THEME_);
		theme.setKey(theme.getKey() + appWidgetID);
	}

	public static WidgetSettingsFragment newInstance(boolean hasMobileNetworks,
			int appWidgetID) {
		WidgetSettingsFragment widgetSettingsFragment = new WidgetSettingsFragment();
		Bundle args = new Bundle();
		args.putBoolean(Preferences.DEVICE_HAS_MOBILE_NETWORK,
				hasMobileNetworks);
		args.putInt(Preferences.APP_WIDGET_ID, appWidgetID);
		widgetSettingsFragment.setArguments(args);

		return widgetSettingsFragment;
	}
}
