package se.magnulund.android.wifilistwidget.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
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
    }
}
