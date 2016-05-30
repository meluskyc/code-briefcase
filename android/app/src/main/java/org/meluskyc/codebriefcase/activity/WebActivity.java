package org.meluskyc.codebriefcase.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.server.AppServlets;
import org.meluskyc.codebriefcase.server.AppWebService;

public class WebActivity extends BaseActivity {

    private BroadcastReceiver statusReceiver = new BroadcastReceiver() {
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
    };

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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("org.meluskyc.codebriefcase.STATUS_UPDATE");
        registerReceiver(statusReceiver, intentFilter);
        AppWebService.status(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(statusReceiver);
    }

    public void disconnect(View view) {
        AppWebService.disconnect(this);
    }
}
