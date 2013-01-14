package se.magnulund.android.wifilistwidget.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created with IntelliJ IDEA. User: Gustav Date: 28/11/2012 Time: 18:12 To
 * change this template use File | Settings | File Templates.
 */
public class SettingsActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		MainSettingsFragment mainSettingsFragment = MainSettingsFragment
				.newInstance(extras
						.getBoolean(Preferences.DEVICE_HAS_MOBILE_NETWORK));
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, mainSettingsFragment).commit();
	}
}