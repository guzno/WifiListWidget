package se.magnulund.android.wifilistwidget.utils;

import se.magnulund.android.wifilistwidget.R;

/**
 * Created with IntelliJ IDEA. User: erikeelde Date: 8/1/2013 Time: 22:41 To
 * change this template use File | Settings | File Templates.
 */
public class MyUtil {
	private static final String TAG = "MyUtil";

	public static final int WIFI_SIGNAL_THRESHOLD_BEST = -50;
	public static final int WIFI_SIGNAL_THRESHOLD_GOOD = -65;
	public static final int WIFI_SIGNAL_THRESHOLD_OK = -75;

	public static final int WIFI_SIGNAL_BEST = 4;
	public static final int WIFI_SIGNAL_GOOD = 3;
	public static final int WIFI_SIGNAL_OK = 2;
	public static final int WIFI_SIGNAL_POOR = 1;

	public static int getSignalStrength(int level) {

		int signalStrenth;
		if (level >= WIFI_SIGNAL_THRESHOLD_BEST) {
			signalStrenth = WIFI_SIGNAL_BEST;

		} else if (level >= WIFI_SIGNAL_THRESHOLD_GOOD) {
			signalStrenth = WIFI_SIGNAL_GOOD;

		} else if (level >= WIFI_SIGNAL_THRESHOLD_OK) {
			signalStrenth = WIFI_SIGNAL_OK;

		} else {
			signalStrenth = WIFI_SIGNAL_POOR;

		}
		return signalStrenth;
	}

	public static int getSignalStrengthIcon(int signalStrength,
			Boolean connected) {
		int icon;
		if (connected) {
			switch (signalStrength) {
			case WIFI_SIGNAL_BEST:
				icon = R.drawable.ic_signal_strength_best_connected;
				break;
			case WIFI_SIGNAL_GOOD:
				icon = R.drawable.ic_signal_strength_good_connected;
				break;
			case WIFI_SIGNAL_OK:
				icon = R.drawable.ic_signal_strength_ok_connected;
				break;
			case WIFI_SIGNAL_POOR:
				icon = R.drawable.ic_signal_strength_poor_connected;
				break;
			default:
				icon = R.drawable.ic_signal_strength_poor_connected;
				break;
			}
		} else {
			switch (signalStrength) {
			case WIFI_SIGNAL_BEST:
				icon = R.drawable.ic_signal_strength_best;
				break;
			case WIFI_SIGNAL_GOOD:
				icon = R.drawable.ic_signal_strength_good;
				break;
			case WIFI_SIGNAL_OK:
				icon = R.drawable.ic_signal_strength_ok;
				break;
			case WIFI_SIGNAL_POOR:
				icon = R.drawable.ic_signal_strength_poor;
				break;
			default:
				icon = R.drawable.ic_signal_strength_poor;
				break;
			}
		}

		return icon;
	}
}
