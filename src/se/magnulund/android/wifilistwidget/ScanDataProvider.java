package se.magnulund.android.wifilistwidget;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 21/11/2012
 * Time: 22:39
 * To change this template use File | Settings | File Templates.
 */
public class ScanDataProvider extends ContentProvider {


    public static final String TAG = "ScanDataProvider";

    public static final String PROVIDER_NAME =
            "se.magnulund.android.wifilistwidget.ScanData";

    public static final Uri CONTENT_URI =
            Uri.parse("content://"+ PROVIDER_NAME + "/scandata");
    /*

   ScanResult:
       public String	BSSID	The address of the access point.
       public String	SSID	The network name.
       public String	capabilities	Describes the authentication, key management, and encryption schemes supported by the access point.
       public int	frequency	The frequency in MHz of the channel over which the client is communicating with the access point.
       public int	level	The detected signal level in dBm.
    */

    public static final String _ID = "_id";
    public static final String BSSID = "bssid";
    public static final String SSID = "ssid";
    public static final String CAPABILITIES = "capabilities";
    public static final String FREQUENCY = "frequency";
    public static final String LEVEL = "level";

    @Override
    public boolean onCreate() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getType(Uri uri) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
