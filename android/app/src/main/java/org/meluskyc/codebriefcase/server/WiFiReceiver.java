package org.meluskyc.codebriefcase.server;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class WiFiReceiver extends BroadcastReceiver {
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
