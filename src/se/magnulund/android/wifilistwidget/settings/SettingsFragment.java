package se.magnulund.android.wifilistwidget.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import se.magnulund.android.wifilistwidget.MainActivity;
import se.magnulund.android.wifilistwidget.R;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 28/11/2012
 * Time: 23:19
 * To change this template use File | Settings | File Templates.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        if (getArguments().getBoolean(MainActivity.DEVICE_HAS_MOBILE_NETWORK) == false) {
            Preference hotSpotToggle = findPreference(MainActivity.PREFS_SHOW_HOTSPOT_TOGGLE);
            hotSpotToggle.setEnabled(false);
            hotSpotToggle.setSummary("Only available on devices with mobile connections");
        }

    }

    public static SettingsFragment newInstance(Boolean hasMobileNetworks) {
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putBoolean(MainActivity.DEVICE_HAS_MOBILE_NETWORK, hasMobileNetworks);
        settingsFragment.setArguments(args);

        return settingsFragment;
    }
}
