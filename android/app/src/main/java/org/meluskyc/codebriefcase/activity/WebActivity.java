package org.meluskyc.codebriefcase.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.server.AppServlets;
import org.meluskyc.codebriefcase.server.AppWebService;

public class WebActivity extends BaseActivity {

    private StatusReceiver statusReceiver;

    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String serverIp = intent.getStringExtra("serverIp");

            if (serverIp.equals(AppWebService.STATUS_OFFLINE)) {
                ((TextView) findViewById(R.id.web_text_help)).setText(R.string.to_enable_the_web);
                findViewById(R.id.web_btn_disconnect).setVisibility(View.INVISIBLE);
            }
            else {
                String clientIp = intent.getStringExtra("clientIp");
                if (clientIp.equals("")) {
                    ((TextView) findViewById(R.id.web_text_help)).setText(getString(R.string.to_use_the_web,
                            serverIp + ":" + AppServlets.PORT));
                    findViewById(R.id.web_btn_disconnect).setVisibility(View.INVISIBLE);
                }
                else {
                    ((TextView) findViewById(R.id.web_text_help)).setText(getString(R.string.connected_to_ip, clientIp));
                    findViewById(R.id.web_btn_disconnect).setVisibility(View.VISIBLE);
                }
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(getString(R.string.web));
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean(getString(R.string.pref_offline_mode_key), false)) {
            registerStatusReceiver();
        }
        else {
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
            if (!sharedPreferences.getBoolean(getString(R.string.pref_offline_mode_key), false)) {
                registerWifiReceiver();
                registerStatusReceiver();
                findViewById(R.id.web_text_disable_offline).setVisibility(View.GONE);
                AppWebService.start(this);
            }
            else {
                unregisterWifiReceiver();
                unregisterStatusReceiver();
                ((TextView)findViewById(R.id.web_text_help)).setText(R.string.offline_mode_is_on);
                findViewById(R.id.web_text_disable_offline).setVisibility(View.VISIBLE);
                AppWebService.stop(this);
            }
        }
    }

    public void disconnect(View view) {
        AppWebService.disconnect(this);
    }

    public void disableOfflineMode(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor	= sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_offline_mode_key), false);
        editor.commit();
    }

    private void registerStatusReceiver() {
        if (statusReceiver == null) {
            statusReceiver = new StatusReceiver();
            registerReceiver(statusReceiver,
                    new IntentFilter("org.meluskyc.codebriefcase.STATUS_UPDATE"));
            AppWebService.status(this);
        }
    }

    private void unregisterStatusReceiver() {
        if (statusReceiver != null) {
            unregisterReceiver(statusReceiver);
            statusReceiver = null;
        }
    }
}
