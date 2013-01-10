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

    public static final String FIRST_RUN_CHECK = "first_run_check";

    @Override
    protected void onHandleIntent(Intent intent) {

        boolean firstRun = intent.getBooleanExtra(FIRST_RUN_CHECK, false);

        int networkType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);

        if (networkType == ConnectivityManager.TYPE_WIFI || firstRun) {
            Context context = getApplicationContext();

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();

            if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) == true) {
                editor.putBoolean(Preferences.WALLED_GARDEN_CHECK_DONE, false);
                editor.commit();
                WifiWidgetProvider.updateWidgets(context, WifiWidgetProvider.UPDATE_CONNECTION_LOST, null);
            } else if (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable()) {

                boolean walledGardenChecked = preferences.getBoolean(Preferences.WALLED_GARDEN_CHECK_DONE, false);
                boolean isWalledGarden = preferences.getBoolean(Preferences.WALLED_GARDEN_CONNECTION, false);

                boolean checkIfLoggedIn = ( walledGardenChecked && isWalledGarden );

                if ( walledGardenChecked == false || checkIfLoggedIn ) {

                    isWalledGardenConnection(editor);
                    editor.putBoolean(Preferences.WALLED_GARDEN_CHECK_DONE, true);

                }
                WifiWidgetProvider.updateWidgets(context, WifiWidgetProvider.UPDATE_CONNECTION_CHANGE, null);
            }

        }
    }

    private static final String WALLED_GARDEN_URL = "clients3.google.com/generate_204";
    private static final String LANDING_PAGE_URL = "www.google.com";
    private static final int WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000;

    private static void isWalledGardenConnection(SharedPreferences.Editor editor) {
        HttpURLConnection urlConnection = null;
        boolean walledGarden = false;
        String redirectURL = "";
        try {
            URL url = new URL("http://"+WALLED_GARDEN_URL); // "http://clients3.google.com/generate_204"
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            // We got a valid response, but not from the real google
            walledGarden = urlConnection.getResponseCode() != 204;
        } catch (MalformedURLException e) {
            Log.e(TAG, "Bad url exception: "
                    + e);
        } catch (IOException e) {
            Log.e(TAG, "Walled garden check - probably not a portal: exception "
                    + e);
        } finally {
            if (urlConnection != null) {
                if (walledGarden) {
                    redirectURL = urlConnection.getHeaderField("Location");
                    if ( redirectURL == null ) {
                        redirectURL = "http://"+LANDING_PAGE_URL;
                    } else {
                        redirectURL = redirectURL.replace(WALLED_GARDEN_URL, LANDING_PAGE_URL);
                        Log.e(TAG, "walled in to: " + redirectURL);
                    }
                }
                editor.putBoolean(Preferences.WALLED_GARDEN_CONNECTION, walledGarden);
                editor.putString(Preferences.WALLED_GARDEN_REDIRECT_URL, redirectURL);
                editor.commit();
                urlConnection.disconnect();
            }
        }
    }
}
