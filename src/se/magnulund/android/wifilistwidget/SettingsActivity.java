package se.magnulund.android.wifilistwidget;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 28/11/2012
 * Time: 18:12
 * To change this template use File | Settings | File Templates.
 */
public class SettingsActivity extends Activity{

    private static final String TAG = "SettingsActivity";

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME, 0);
        preferencesEditor = preferences.edit();

        CheckBox mergeAPs = (CheckBox) findViewById(R.id.merge_access_points);

        mergeAPs.setChecked(preferences.getBoolean(MainActivity.PREFS_MERGE_APS, false));

    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch(view.getId()) {
            case R.id.merge_access_points:
                preferencesEditor.putBoolean(MainActivity.PREFS_MERGE_APS, checked);
                preferencesEditor.commit();
                preferencesEditor.clear();
            default: {

            }
        }
    }
}