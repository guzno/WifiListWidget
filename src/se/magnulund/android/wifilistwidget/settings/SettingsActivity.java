package se.magnulund.android.wifilistwidget.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

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
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}