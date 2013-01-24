package se.magnulund.android.wifilistwidget;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import se.magnulund.android.wifilistwidget.models.FilteredScanResult;
import se.magnulund.android.wifilistwidget.utils.MyUtil;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: erikeelde Date: 8/1/2013 Time: 22:29 To
 * change this template use File | Settings | File Templates.
 */
public class WifiFilteredScanResultsAdapter extends BaseAdapter {
    private static final String TAG = "WifiFilteredScanResultsAdapter";
    private ArrayList<FilteredScanResult> filterScanResults;

    private Context context;

    public WifiFilteredScanResultsAdapter(Context context) {
        this.context = context;
        filterScanResults = FilteredScanResult.getFilteredScanResults(context, null);
    }

    @Override
    public int getCount() {
        return filterScanResults.size();
    }

    @Override
    public Object getItem(int position) {
        return filterScanResults.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View filterScanResultView;

        FilteredScanResult filteredScanResult = (FilteredScanResult) getItem(position);

        ScanResult scanResult = filteredScanResult.getScanResult();

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            filterScanResultView = inflater.inflate(R.layout.wifi_list_item,
                    parent, false);

            WifiFilteredScanResultsViewHolder holder = new WifiFilteredScanResultsViewHolder();
            holder.ssid = (TextView) filterScanResultView
                    .findViewById(R.id.ssid);
            holder.bssid = (TextView) filterScanResultView
                    .findViewById(R.id.bssid);
            holder.level = (TextView) filterScanResultView
                    .findViewById(R.id.level);
            holder.signalStrength = (ImageView) filterScanResultView
                    .findViewById(R.id.signal_strength);

            filterScanResultView.setTag(R.id.holder_tag, holder);

        } else {
            filterScanResultView = convertView;

        }

        WifiFilteredScanResultsViewHolder holder = (WifiFilteredScanResultsViewHolder) filterScanResultView
                .getTag(R.id.holder_tag);

        holder.ssid.setText(scanResult.SSID);

        holder.bssid.setText(scanResult.BSSID);

        holder.level.setText(scanResult.level + "");

        holder.signalStrength.setImageResource(MyUtil.getSignalStrengthIcon(
                MyUtil.getSignalStrength(scanResult.level),
                filteredScanResult.isCurrentConnection()));

        return filterScanResultView;
    }

    class WifiFilteredScanResultsViewHolder {
        TextView ssid;
        TextView bssid;
        TextView level;
        ImageView signalStrength;
    }
}
