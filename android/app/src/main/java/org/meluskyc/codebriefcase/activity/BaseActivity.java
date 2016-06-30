package org.meluskyc.codebriefcase.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.server.AppWebService;

public class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * BroadcastReceiver to manage starting and stopping AppWebService.
     * -start when the device has a valid IP address
     * -stop otherwise
     */
    private WifiReceiver wifiReceiver;
    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (((WifiManager) context.getSystemService(context.WIFI_SERVICE))
                    .getConnectionInfo()
                    .getIpAddress() != 0) {

                AppWebService.start(context);
            }
            else {
                AppWebService.stop(context);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getString(getString(R.string.pref_theme_key),
                getString(R.string.pref_theme_default)).equals("default")) {
            setTheme(R.style.AppTheme_Default);
        }
        else {
            setTheme(R.style.AppTheme_Dark);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_theme_key))) {
            recreate();
        }
        if (key.equals(getString(R.string.pref_offline_mode_key))) {
            if (!sharedPreferences.getBoolean(getString(R.string.pref_offline_mode_key), false)) {
                registerWifiReceiver();
                AppWebService.start(this);
            }
            else {
                unregisterWifiReceiver();
                AppWebService.stop(this);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        if (!sharedPreferences.getBoolean(getString(R.string.pref_offline_mode_key), false)) {
            registerWifiReceiver();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        unregisterWifiReceiver();
    }

    /**
     * Create and register a new WifiReceiver.
     */
    protected void registerWifiReceiver() {
        if (wifiReceiver == null) {
            wifiReceiver = new WifiReceiver();
            registerReceiver(wifiReceiver,
                    new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        }
    }

    /**
     * Unregister the WifiReceiver and set to null.
     */
    protected void unregisterWifiReceiver() {
        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver);
            wifiReceiver = null;
        }
    }
}
