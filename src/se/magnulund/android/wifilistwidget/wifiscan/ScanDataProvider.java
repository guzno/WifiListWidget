package se.magnulund.android.wifilistwidget.wifiscan;

import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ScanDataProvider extends ContentProvider {


    public static final String TAG = "ScanDataProvider";

    public static final String PROVIDER_NAME =
            "se.magnulund.provider.ScanData";

    public static final Uri CONTENT_URI =
            Uri.parse("content://" + PROVIDER_NAME + "/wifi_networks");

    public static final Uri CONTENT_URI_NO_NOTIFY =
            Uri.parse("content://" + PROVIDER_NAME + "/wifi_networks/no_notify");

    private static final int WIFI_NETWORKS = 1;
    private static final int WIFI_ID = 2;
    private static final int WIFI_NETWORKS_NO_NOTIFY = 3;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "wifi_networks", WIFI_NETWORKS);
        uriMatcher.addURI(PROVIDER_NAME, "wifi_networks/#", WIFI_ID);
        uriMatcher.addURI(PROVIDER_NAME, "wifi_networks/no_notify", WIFI_NETWORKS_NO_NOTIFY);
    }

    private WifiScanDatabase dbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new WifiScanDatabase(context);
        return (dbHelper != null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        //checkIfTableExistsAndCreateTable();
        sqlBuilder.setTables(WifiScanDatabase.DATABASE_TABLE);

        if (uriMatcher.match(uri) == WIFI_ID)
            //---if getting a particular wifi---
            sqlBuilder.appendWhere(
                    WifiScanDatabase._ID + " = " + uri.getPathSegments().get(1));

        if (sortOrder == null || sortOrder.equals(""))
            sortOrder = WifiScanDatabase.LEVEL;

        SQLiteDatabase scanDataDB = dbHelper.getWritableDatabase();

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
        switch (uriMatcher.match(uri)) {
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
        SQLiteDatabase scanDataDB = dbHelper.getWritableDatabase();

        //---add a new wifi---
        long rowID = scanDataDB.insert(
                WifiScanDatabase.DATABASE_TABLE, "", contentValues);

        //---if added successfully---
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        SQLiteDatabase scanDataDB = dbHelper.getWritableDatabase();

        int uriMatch = uriMatcher.match(arg0);
        int count = 0;
        switch (uriMatch) {
            case WIFI_NETWORKS | WIFI_NETWORKS_NO_NOTIFY:
                count = scanDataDB.delete(
                        WifiScanDatabase.DATABASE_TABLE,
                        arg1,
                        arg2);
                break;
            case WIFI_ID:
                String id = arg0.getPathSegments().get(1);
                count = scanDataDB.delete(
                        WifiScanDatabase.DATABASE_TABLE,
                        WifiScanDatabase._ID + " = " + id +
                                (!TextUtils.isEmpty(arg1) ? " AND (" +
                                        arg1 + ')' : ""),
                        arg2);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown URI " + arg0);
        }
        if (uriMatch != WIFI_NETWORKS_NO_NOTIFY) {
            getContext().getContentResolver().notifyChange(arg0, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase scanDataDB = dbHelper.getWritableDatabase();
        int uriMatch = uriMatcher.match(uri);
        int count = 0;
        switch (uriMatch) {
            case WIFI_NETWORKS | WIFI_NETWORKS_NO_NOTIFY:
                count = scanDataDB.update(
                        WifiScanDatabase.DATABASE_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case WIFI_ID:
                count = scanDataDB.update(
                        WifiScanDatabase.DATABASE_TABLE,
                        values,
                        WifiScanDatabase._ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" +
                                        selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown URI " + uri);
        }
        if (uriMatch != WIFI_NETWORKS_NO_NOTIFY) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
}
