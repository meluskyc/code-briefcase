package org.meluskyc.codebriefcase.activity;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.server.WiFiReceiver;

public class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private WiFiReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        wifiReceiver = new WiFiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(wifiReceiver);
    }
}
