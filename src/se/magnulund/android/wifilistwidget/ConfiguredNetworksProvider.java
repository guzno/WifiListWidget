package se.magnulund.android.wifilistwidget;

import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ConfiguredNetworksProvider extends ContentProvider {

    public static final String TAG = "ConfiguredNetworksProvider";

    public static final String PROVIDER_NAME =
            "se.magnulund.provider.ConfiguredNetworks";

    public static final Uri CONTENT_URI =
            Uri.parse("content://"+ PROVIDER_NAME + "/configured_networks");


    /*
   WifiConfiguration:  Skit i dom inom parentes?
       public String	BSSID	When set, this network configuration entry should only be used when associating with the AP having the specified BSSID.
       public String	SSID	The network's SSID.
       (public BitSet	allowedAuthAlgorithms	The set of authentication protocols supported by this configuration.)
       (public BitSet	allowedGroupCiphers	The set of group ciphers supported by this configuration.)
       (public BitSet	allowedKeyManagement	The set of key management protocols supported by this configuration.)
       (public BitSet	allowedPairwiseCiphers	The set of pairwise ciphers for WPA supported by this configuration.)
       (public BitSet	allowedProtocols	The set of security protocols supported by this configuration.)
       (public boolean	hiddenSSID	This is a network that does not broadcast its SSID, so an SSID-specific probe request must be used for scans.)
       public int	networkId	The ID number that the supplicant uses to identify this network configuration entry.
       (public String	preSharedKey	Pre-shared key for use with WPA-PSK.)
       (public int	priority	Priority determines the preference given to a network by wpa_supplicant when choosing an access point with which to associate.)
       (public int	status	The current status of this network configuration entry.)
       (public String[]	wepKeys	Up to four WEP keys.)
       (public int	wepTxKeyIndex	Default WEP key index, ranging from 0 to 3.)
    */
    public static final String _ID = "_id";
    public static final String BSSID = "bssid";
    public static final String SSID = "ssid";
    public static final String NETWORK_ID = "network_id";


    //---for database use---
    private SQLiteDatabase wifiConfigDB;
    private static final String DATABASE_NAME = "WifiListWidgetDB";
    private static final String DATABASE_TABLE = "wifi_config";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE +
                    " ("+_ID+" integer primary key autoincrement, "
                    +BSSID+" text not null, "+SSID+" text not null, "
                    +NETWORK_ID+" int not null);";

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            Log.w("Content provider database",
                    "Upgrading database from version " +
                            oldVersion + " to " + newVersion +
                            ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS wifi_config");
            onCreate(db);
        }
    }

    private static final int WIFI_CONFIGS = 1;
    private static final int WIFI_ID = 2;

    private static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "configured_networks", WIFI_CONFIGS);
        uriMatcher.addURI(PROVIDER_NAME, "configured_networks/#", WIFI_ID);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        wifiConfigDB = dbHelper.getWritableDatabase();
        return (wifiConfigDB != null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(DATABASE_TABLE);

        if (uriMatcher.match(uri) == WIFI_ID)
            //---if getting a particular wifi---
            sqlBuilder.appendWhere(
                    _ID + " = " + uri.getPathSegments().get(1));

        if (sortOrder==null || sortOrder.equals(""))
            sortOrder = NETWORK_ID;

        Cursor cursor = sqlBuilder.query(
                wifiConfigDB,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        //---register to watch a content URI for changes---
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            //---get all books---
            case WIFI_CONFIGS:
                return "vnd.android.cursor.dir/vnd.magnulund.ScanData ";
            //---get a particular book---
            case WIFI_ID:
                return "vnd.android.cursor.item/vnd.magnulund.ScanData ";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        //---add a new wifi---
        long rowID = wifiConfigDB.insert(
                DATABASE_TABLE, "", contentValues);

        //---if added successfully---
        if (rowID>0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        // arg0 = uri
        // arg1 = selection
        // arg2 = selectionArgs
        int count=0;
        switch (uriMatcher.match(arg0)){
            case WIFI_CONFIGS:
                count = wifiConfigDB.delete(
                        DATABASE_TABLE,
                        arg1,
                        arg2);
                break;
            case WIFI_ID:
                String id = arg0.getPathSegments().get(1);
                count = wifiConfigDB.delete(
                        DATABASE_TABLE,
                        _ID + " = " + id +
                                (!TextUtils.isEmpty(arg1) ? " AND (" +
                                        arg1 + ')' : ""),
                        arg2);
                break;
            default: throw new IllegalArgumentException(
                    "Unknown URI " + arg0);
        }
        getContext().getContentResolver().notifyChange(arg0, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case WIFI_CONFIGS:
                count = wifiConfigDB.update(
                        DATABASE_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case WIFI_ID:
                count = wifiConfigDB.update(
                        DATABASE_TABLE,
                        values,
                        _ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" +
                                        selection + ')' : ""),
                        selectionArgs);
                break;
            default: throw new IllegalArgumentException(
                    "Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
