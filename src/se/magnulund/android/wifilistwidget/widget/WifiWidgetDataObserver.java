package se.magnulund.android.wifilistwidget.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.database.ContentObserver;
import android.os.Handler;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 29/11/2012
 * Time: 23:41
 * To change this template use File | Settings | File Templates.
 */
class WifiWidgetDataObserver extends ContentObserver {
    private AppWidgetManager mAppWidgetManager;
    private ComponentName mComponentName;

    WifiWidgetDataObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
        super(h);
        mAppWidgetManager = mgr;
        mComponentName = cn;
    }

    @Override
    public void onChange(boolean selfChange) {
        // The data has changed, so notify the widget that the collection view needs to be updated.
        // In response, the factory's onDataSetChanged() will be called which will requery the
        // cursor for the new data.
        mAppWidgetManager.notifyAppWidgetViewDataChanged(
                mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.weather_list);
    }
}
