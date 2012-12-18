package se.magnulund.android.wifilistwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import se.magnulund.android.wifilistwidget.utils.AlarmUtility;
import se.magnulund.android.wifilistwidget.widget.WifiWidgetProvider;
import se.magnulund.android.wifilistwidget.widget.WifiWidgetService;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: erikeelde
 * Date: 5/12/2012
 * Time: 24:15
 * To change this template use File | Settings | File Templates.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent receivedIntent) {
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, WifiWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            for (int i = 0; i < appWidgetIds.length; ++i) {
                RemoteViews rv = WifiWidgetProvider.getRemoteViews(context, appWidgetIds[i]);
                /*
                // Specify the service to provide data for the collection widget_listview.  Note that we need to
                // embed the appWidgetId via the data otherwise it will be ignored.
                final Intent intent = new Intent(context, WifiWidgetService.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
                intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
                final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_listview);
                rv.setRemoteAdapter(R.id.widget_listview, intent);

                // Set the empty view to be displayed if the collection is empty.  It must be a sibling
                // view of the collection view.
                rv.setEmptyView(R.id.widget_listview, R.id.empty_view);

                // Bind a click listener template for the contents of the weather list.  Note that we
                // need to update the intent's data if we set an extra, since the extras will be
                // ignored otherwise.

                final Intent onClickIntent = new Intent(context, WifiWidgetProvider.class);
                onClickIntent.setAction(WifiWidgetProvider.CLICK_ACTION);
                onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
                onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
                final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0,
                        onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                rv.setPendingIntentTemplate(R.id.widget_listview, onClickPendingIntent);

                // Bind the click intent for the refresh button on the widget_listview
                final Intent wifiToggleIntent = new Intent(context, WifiWidgetProvider.class);
                wifiToggleIntent.setAction(WifiWidgetProvider.WIFI_TOGGLE_ACTION);
                final PendingIntent wifiTogglePendingIntent = PendingIntent.getBroadcast(context, 0,
                        wifiToggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                rv.setOnClickPendingIntent(R.id.widget_wifi_toggle, wifiTogglePendingIntent);

                final Intent hotSpotToggleIntent = new Intent(context, WifiWidgetProvider.class);
                hotSpotToggleIntent.setAction(WifiWidgetProvider.HOTSPOT_TOGGLE_ACTION);
                final PendingIntent hotSpotTogglePendingIntent = PendingIntent.getBroadcast(context, 0,
                        hotSpotToggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                rv.setOnClickPendingIntent(R.id.widget_wifi_toggle, hotSpotTogglePendingIntent);
                */
                appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            int attempt = receivedIntent.getIntExtra(AlarmUtility.IDENTIFIER_ALARM_ATTEMPT, -1);
            AlarmUtility.scheduleAlarmWithBackoff(context, ++attempt);
        }
    }
}
