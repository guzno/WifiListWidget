package se.magnulund.android.wifilistwidget;

import android.app.Application;
import android.util.Log;
import com.crittercism.app.Crittercism;

/**
 * User: erikeelde Date: 12/6/12
 */
public class WifiListWidgetApplication extends Application {
	private static final String TAG = WifiListWidgetApplication.class
			.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "application created");

		Crittercism.init(getApplicationContext(), "50c084317e69a33c53000002");
	}

}
