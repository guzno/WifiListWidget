package se.magnulund.android.wifilistwidget;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 22/11/2012
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */
public class WifiNetworkConfigService extends IntentService {

    private static final String TAG = "WifiNetworkConfigService";

    public WifiNetworkConfigService() {
        super("WifiListWidget_WifiNetworkConfigService");
        Log.e(TAG, "Constructed");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        List<WifiConfiguration> wifiConfigs = wifiManager.getConfiguredNetworks();

        getContentResolver().delete(ConfiguredNetworksProvider.CONTENT_URI, null, null);

        ContentValues values = new ContentValues();
        WifiConfiguration wifiConfig;

        Iterator<WifiConfiguration> iterator = wifiConfigs.iterator();
        while (iterator.hasNext()) {
            wifiConfig = iterator.next();
            values.put(ConfiguredNetworksProvider.BSSID, wifiConfig.BSSID);
            values.put(ConfiguredNetworksProvider.SSID, wifiConfig.SSID);
            values.put(ConfiguredNetworksProvider.NETWORK_ID, wifiConfig.networkId);
            getContentResolver().insert(ConfiguredNetworksProvider.CONTENT_URI, values);
        }

        Log.e(TAG, "new wifi configs");

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        Log.e(TAG, "stopped");
    }
}
