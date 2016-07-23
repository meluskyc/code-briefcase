package org.meluskyc.codebriefcase.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.server.WebServer;
import org.meluskyc.codebriefcase.server.WebService;
import org.meluskyc.codebriefcase.utils.AppUtils;

/**
 * Activity to display information about the web interface.
 */
public class WebActivity extends BaseActivity {

    /**
     * {@code BroadcastReceiver} to display messages from {@link WebService}.
     */
    private StatusReceiver statusReceiver;
    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int deviceIp = ((WifiManager) getSystemService(WIFI_SERVICE))
                    .getConnectionInfo().getIpAddress();
            if (deviceIp != 0) {
                String clientIp = intent.getStringExtra(WebService.EXTRA_CLIENT_IP);
                if (clientIp.equals("")) {
                    ((TextView) findViewById(R.id.web_text_help))
                            .setText(getString(R.string.to_use_the_web,
                                    AppUtils.formatIpAddress(deviceIp) + ":" + WebServer.PORT));
                    findViewById(R.id.web_btn_disconnect).setVisibility(View.INVISIBLE);
                } else {
                    ((TextView) findViewById(R.id.web_text_help)).setText(
                            getString(R.string.connected_to_ip, clientIp));
                    findViewById(R.id.web_btn_disconnect).setVisibility(View.VISIBLE);
                }
            } else {
                ((TextView) findViewById(R.id.web_text_help)).setText(R.string.to_enable_the_web);
                findViewById(R.id.web_btn_disconnect).setVisibility(View.INVISIBLE);
            }
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.web_btn_disconnect:
                    disconnect();
                    break;
                case R.id.web_text_disable_offline:
                    disableOfflineMode();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(getString(R.string.web));

        findViewById(R.id.web_text_disable_offline).setOnClickListener(onClickListener);
        findViewById(R.id.web_btn_disconnect).setOnClickListener(onClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean(getString(R.string.pref_offline_mode_key), false)) {
            registerStatusReceiver();
        } else {
            ((TextView)findViewById(R.id.web_text_help)).setText(R.string.offline_mode_is_on);
            findViewById(R.id.web_text_disable_offline).setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterStatusReceiver();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_theme_key))) {
            recreate();
        }
        if (key.equals(getString(R.string.pref_offline_mode_key))) {
            if (sharedPreferences.getBoolean(getString(R.string.pref_offline_mode_key), false)) {
                unregisterWifiReceiver();
                unregisterStatusReceiver();
                ((TextView)findViewById(R.id.web_text_help)).setText(R.string.offline_mode_is_on);
                findViewById(R.id.web_text_disable_offline).setVisibility(View.VISIBLE);
                WebService.stop(this);
            } else {
                registerWifiReceiver();
                registerStatusReceiver();
                findViewById(R.id.web_text_disable_offline).setVisibility(View.GONE);
                WebService.start(this);
            }
        }
    }

    /**
     * Disconnect from the current client.
     */
    public void disconnect() {
        WebService.disconnect(this);
    }

    public void disableOfflineMode() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor	= sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_offline_mode_key), false);
        editor.commit();
    }

    /**
     * Create and register a new {@code StatusReceiver}.
     */
    private void registerStatusReceiver() {
        if (statusReceiver == null) {
            statusReceiver = new StatusReceiver();
            registerReceiver(statusReceiver,
                    new IntentFilter(WebService.ACTION_STATUS_BROADCAST));
            WebService.status(this);
        }
    }

    /**
     * Unregister the {@code StatusReceiver} and set to null.
     */
    private void unregisterStatusReceiver() {
        if (statusReceiver != null) {
            unregisterReceiver(statusReceiver);
            statusReceiver = null;
        }
    }
}
