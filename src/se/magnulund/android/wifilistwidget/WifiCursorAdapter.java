package se.magnulund.android.wifilistwidget;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanDatabase;
import se.magnulund.android.wifilistwidget.wifiscan.WifiScanService;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 29/11/2012
 * Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public class WifiCursorAdapter extends SimpleCursorAdapter {

    private Context context;

    private int layout;

    public WifiCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
        this.layout = layout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        Cursor c = getCursor();

        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layout, parent, false);

        int columnIndex = c.getColumnIndex(WifiScanDatabase.SSID);

        String string = c.getString(columnIndex);

        TextView textView = (TextView) v.findViewById(R.id.ssid);
        if (textView != null) {
            textView.setText(string);
        }

        columnIndex = c.getColumnIndex(WifiScanDatabase.BSSID);

        string = c.getString(columnIndex);

        textView = (TextView) v.findViewById(R.id.bssid);
        if (textView != null) {
            textView.setText(string);
        }

        columnIndex = c.getColumnIndex(WifiScanDatabase.LEVEL);

        string = c.getString(columnIndex);

        textView = (TextView) v.findViewById(R.id.level);
        if (textView != null) {
            textView.setText(string);
        }

        columnIndex = c.getColumnIndex(WifiScanDatabase.SIGNALSTRENGTH);
        int signalStrength = c.getInt(columnIndex);
        columnIndex = c.getColumnIndex(WifiScanDatabase.CONNECTED);
        boolean connected = (c.getInt(columnIndex) == 1);

        ImageView imageView = (ImageView) v.findViewById(R.id.signal_strength);
        imageView.setImageResource(WifiScanService.getSignalStrengthIcon(signalStrength, connected));

        return v;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {

        int columnIndex = c.getColumnIndex(WifiScanDatabase.SSID);

        String string = c.getString(columnIndex);

        TextView textView = (TextView) v.findViewById(R.id.ssid);
        if (textView != null) {
            textView.setText(string);
        }

        columnIndex = c.getColumnIndex(WifiScanDatabase.BSSID);

        string = c.getString(columnIndex);

        textView = (TextView) v.findViewById(R.id.bssid);
        if (textView != null) {
            textView.setText(string);
        }

        columnIndex = c.getColumnIndex(WifiScanDatabase.LEVEL);

        string = c.getString(columnIndex);

        textView = (TextView) v.findViewById(R.id.level);
        if (textView != null) {
            textView.setText(string);
        }

        columnIndex = c.getColumnIndex(WifiScanDatabase.SIGNALSTRENGTH);
        int signalStrength = c.getInt(columnIndex);
        columnIndex = c.getColumnIndex(WifiScanDatabase.CONNECTED);
        boolean connected = (c.getInt(columnIndex) == 1);

        ImageView imageView = (ImageView) v.findViewById(R.id.signal_strength);
        imageView.setImageResource(WifiScanService.getSignalStrengthIcon(signalStrength, connected));
    }
}

