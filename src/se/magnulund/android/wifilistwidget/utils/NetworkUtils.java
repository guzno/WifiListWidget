package se.magnulund.android.wifilistwidget.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 04/12/2012
 * Time: 23:10
 * To change this template use File | Settings | File Templates.
 */
public class NetworkUtils {

    public static boolean hasMobileNetwork(Context context) {
        ConnectivityManager connectivityManager = (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        for (NetworkInfo networkInfo : connectivityManager.getAllNetworkInfo()) {
            if (networkInfo.getType() == android.net.ConnectivityManager.TYPE_MOBILE) {
                return true;
            }
        }
        return false;
    }

}
