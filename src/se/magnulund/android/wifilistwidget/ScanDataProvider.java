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

public class ScanDataProvider extends ContentProvider {


    public static final String TAG = "ScanDataProvider";

    public static final String PROVIDER_NAME =
            "se.magnulund.provider.ScanData";

    public static final Uri CONTENT_URI =
            Uri.parse("content://"+ PROVIDER_NAME + "/wifi_networks");
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


    //---for database use---
    private SQLiteDatabase scanDataDB;
    private static final String DATABASE_NAME = "WifiListWidgetDB";
    private static final String DATABASE_TABLE = "wifi_scan_data";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE +
                    " (_id integer primary key autoincrement, "
                    + "bssid text not null, ssid text not null, "
                    + "capabilities text not null, frequency int not null, "
                    + "level int not null);";

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            Log.e(TAG, "DBHELP constructed");
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            Log.e(TAG, "DBHELP create method");
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            Log.w("Content provider database",
                    "Upgrading database from version " +
                            oldVersion + " to " + newVersion +
                            ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS wifi_scan_data");
            onCreate(db);
        }
    }

    private static final int WIFI_NETWORKS = 1;
    private static final int WIFI_ID = 2;

    private static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "wifi_networks", WIFI_NETWORKS);
        uriMatcher.addURI(PROVIDER_NAME, "wifi_networks/#", WIFI_ID);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        scanDataDB = dbHelper.getWritableDatabase();
        Log.e(TAG, "Created = "+(scanDataDB != null));
        return (scanDataDB != null);
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
            sortOrder = LEVEL;

        Cursor cursor = sqlBuilder.query(
                scanDataDB,
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
            case WIFI_NETWORKS:
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
        long rowID = scanDataDB.insert(
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
            case WIFI_NETWORKS:
                count = scanDataDB.delete(
                        DATABASE_TABLE,
                        arg1,
                        arg2);
                break;
            case WIFI_ID:
                String id = arg0.getPathSegments().get(1);
                count = scanDataDB.delete(
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
            case WIFI_NETWORKS:
                count = scanDataDB.update(
                        DATABASE_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case WIFI_ID:
                count = scanDataDB.update(
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
