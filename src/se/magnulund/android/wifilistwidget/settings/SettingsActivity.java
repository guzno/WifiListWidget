package se.magnulund.android.wifilistwidget.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import se.magnulund.android.wifilistwidget.MainActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 28/11/2012
 * Time: 18:12
 * To change this template use File | Settings | File Templates.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        SettingsFragment settingsFragment = SettingsFragment.newInstance(extras.getBoolean(MainActivity.DEVICE_HAS_MOBILE_NETWORK));
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();
    }
}