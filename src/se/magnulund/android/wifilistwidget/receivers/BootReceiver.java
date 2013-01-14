package se.magnulund.android.wifilistwidget.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import se.magnulund.android.wifilistwidget.utils.AlarmUtility;

/**
 * Created with IntelliJ IDEA. User: erikeelde Date: 5/12/2012 Time: 24:14 To
 * change this template use File | Settings | File Templates.
 */
public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = BootReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			AlarmUtility
					.scheduleAlarm(context, AlarmUtility.ALARM_TYPE_BACKOFF);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}
}
