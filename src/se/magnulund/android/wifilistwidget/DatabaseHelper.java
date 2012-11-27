package se.magnulund.android.wifilistwidget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 27/11/2012
 * Time: 22:40
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

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
    public static final String NETWORK_ID = "network_id";
    public static final String CAPABILITIES = "capabilities";
    public static final String FREQUENCY = "frequency";
    public static final String LEVEL = "level";


    //---for database use---
    private static final String DATABASE_NAME = "wifiListWidgetDB";
    public static final String DATABASE_TABLE = "wifiscan";
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE +
                    " (" + _ID + " integer primary key autoincrement, "
                    + BSSID + " text not null, " + SSID + " text not null, " + NETWORK_ID + " int not null, "
                    + CAPABILITIES + " text not null, " + FREQUENCY + " int not null, "
                    + LEVEL + " int not null);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG, "DBHELPER onCreate");
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        Log.w("Content provider database",
                "Upgrading database from version " +
                        oldVersion + " to " + newVersion +
                        ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }
}
