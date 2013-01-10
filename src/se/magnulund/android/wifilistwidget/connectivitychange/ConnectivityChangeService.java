package se.magnulund.android.wifilistwidget.connectivitychange;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import se.magnulund.android.wifilistwidget.settings.Preferences;
import se.magnulund.android.wifilistwidget.widget.WifiWidgetProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 09/01/2013
 * Time: 00:16
 * To change this template use File | Settings | File Templates.
 */
public class ConnectivityChangeService extends IntentService {

    private static final String TAG = "ConnectivityChangeService";

    public ConnectivityChangeService() {
        super("wifilistwidget_ConnectivityChangeService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int networkType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);
        if (networkType == ConnectivityManager.TYPE_WIFI) {
            Log.e(TAG, "onWifi");
            Context context = getApplicationContext();

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();

            if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) == true) {
                Log.e(TAG, "Connectivity lost.");
                editor.putBoolean(Preferences.WALLED_GARDEN_CHECK_DONE, false);
                editor.commit();
                WifiWidgetProvider.updateWidgets(context, WifiWidgetProvider.UPDATE_CONNECTION_LOST, null);
            } else if (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable()) {
                Log.e(TAG, "WIFI NETWORK ALIVE AND KICKIN'");

                if (preferences.getBoolean(Preferences.WALLED_GARDEN_CHECK_DONE, false) == false) {
                    editor.putBoolean(Preferences.WALLED_GARDEN_CHECK_DONE, true);
                    boolean walledGarden = isWalledGardenConnection();
                    editor.putBoolean(Preferences.WALLED_GARDEN_CONNECTION, walledGarden);
                    Log.e(TAG, "walled in... : " + walledGarden);
                    editor.commit();
                }
                WifiWidgetProvider.updateWidgets(context, WifiWidgetProvider.UPDATE_CONNECTION_CHANGE, null);
            }

        }
    }

    private static final String WALLED_GARDEN_URL = "http://clients3.google.com/generate_204";
    private static final int WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000;

    private boolean isWalledGardenConnection() {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(WALLED_GARDEN_URL); // "http://clients3.google.com/generate_204"
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            // We got a valid response, but not from the real google
            return urlConnection.getResponseCode() != 204;
        } catch (MalformedURLException e) {
            Log.e(TAG, "Bad url exception: "
                    + e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Walled garden check - probably not a portal: exception "
                    + e);
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
