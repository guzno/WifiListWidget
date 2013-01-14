package se.magnulund.android.wifilistwidget.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import se.magnulund.android.wifilistwidget.R;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 28/11/2012
 * Time: 23:19
 * To change this template use File | Settings | File Templates.
 */
public class MainSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        if (getArguments().getBoolean(Preferences.DEVICE_HAS_MOBILE_NETWORK) == false) {
            Preference hotSpotToggle = findPreference(Preferences.SHOW_MAIN_HOTSPOT_TOGGLE);
            hotSpotToggle.setEnabled(false);
            hotSpotToggle.setSummary("Only available on devices with mobile connections");
        }

    }

    public static MainSettingsFragment newInstance(Boolean hasMobileNetworks) {
        Log.e("TJOSAN:", "mobilenet"+hasMobileNetworks);
        MainSettingsFragment mainSettingsFragment = new MainSettingsFragment();
        Bundle args = new Bundle();
        args.putBoolean(Preferences.DEVICE_HAS_MOBILE_NETWORK, hasMobileNetworks);
        mainSettingsFragment.setArguments(args);

        return mainSettingsFragment;
    }
}
