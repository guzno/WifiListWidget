package se.magnulund.android.wifilistwidget.wifiscan;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import se.magnulund.android.wifilistwidget.connectivitychange.ConnectivityChangeService;
import se.magnulund.android.wifilistwidget.widget.WifiWidgetProvider;

public class WifiScanService extends IntentService {

	private static final String TAG = "WifiScanService";

	public WifiScanService() {
		super("WifiListWidget_WifiScanService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = getApplicationContext();
		ConnectivityChangeService.isWalledGardenConnection(context, null);
		WifiWidgetProvider.updateWidgets(getApplicationContext(),
				WifiWidgetProvider.UPDATE_WIFI_SCAN_RESULTS, null);
	}
}
