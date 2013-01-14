package se.magnulund.android.wifilistwidget.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import se.magnulund.android.wifilistwidget.MainActivity;
import se.magnulund.android.wifilistwidget.R;
import se.magnulund.android.wifilistwidget.settings.MainSettingsFragment;
import se.magnulund.android.wifilistwidget.settings.Preferences;
import se.magnulund.android.wifilistwidget.settings.WidgetSettingsFragment;

/**
 * Created with IntelliJ IDEA. User: erikeelde Date: 4/12/2012 Time: 22:47 To
 * change this template use File | Settings | File Templates.
 */
public class WifiWidgetConfigurationActivity extends Activity {
	private static final String TAG = "WifiWidgetConfigurationActivity";

	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context context = getApplicationContext();

		PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

		setResult(RESULT_CANCELED);

		setContentView(R.layout.wifi_widget_configuration_activity_layout);

		boolean hasMobileNetworks = MainActivity
				.deviceHasMobileNetwork(context);

		findViewById(R.id.save_button).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						Intent resultValue = new Intent();
						Log.e(TAG, "sending ID: " + appWidgetId);
						resultValue.putExtra(
								AppWidgetManager.EXTRA_APPWIDGET_ID,
								appWidgetId);
						setResult(RESULT_OK, resultValue);
						finish();
					}
				});

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		Log.e(TAG, "ID: " + appWidgetId);

		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			throw new RuntimeException(
					"I want to be instantiated by a widget_listview.");
		}

		WidgetSettingsFragment widgetSettingsFragment = WidgetSettingsFragment
				.newInstance(hasMobileNetworks, appWidgetId);
		getFragmentManager().beginTransaction()
				.add(R.id.settings_container, widgetSettingsFragment).commit();
	}
}
