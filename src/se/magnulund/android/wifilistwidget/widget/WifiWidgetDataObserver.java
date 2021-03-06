package se.magnulund.android.wifilistwidget.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;
import se.magnulund.android.wifilistwidget.R;

/**
 * Created with IntelliJ IDEA. User: Gustav Date: 29/11/2012 Time: 23:41 To
 * change this template use File | Settings | File Templates.
 */
class WifiWidgetDataObserver extends ContentObserver {

	private static final String TAG = "WifiWidgetDataObserver";

	private AppWidgetManager mAppWidgetManager;
	private ComponentName mComponentName;

	WifiWidgetDataObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
		super(h);
		mAppWidgetManager = mgr;
		mComponentName = cn;
	}

	@Override
	public void onChange(boolean selfChange) {
		// The data has changed, so notify the widget_listview that the
		// collection view needs to be updated.
		// In response, the factory's onDataSetChanged() will be called which
		// will requery the
		// cursor for the new data.
		Log.e(TAG, "Widget data changed");
		mAppWidgetManager.notifyAppWidgetViewDataChanged(
				mAppWidgetManager.getAppWidgetIds(mComponentName),
				R.id.widget_listview);
	}
}
