package org.meluskyc.codebriefcase.server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;

import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.utils.AppUtils;

import java.io.IOException;

public class AppWebService extends Service {

    private static final String ACTION_START = "org.meluskyc.codebriefcase.START";
    private static final String ACTION_STOP = "org.meluskyc.codebriefcase.STOP";
    private static final String ACTION_STATUS = "org.meluskyc.codebriefcase.STATUS";
    private static final String ACTION_DISCONNECT = "org.meluskyc.codebriefcase.DISCONNECT";
    public static final String STATUS_OFFLINE = "OFFLINE";

    private AppServlets server;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(ACTION_START)) {
                start();
            }
            else if (action.equals(ACTION_STOP)) {
                stop();
            }
            else if (action.equals(ACTION_STATUS)) {
                status();
            }
            else if (action.equals(ACTION_DISCONNECT)) {
                disconnect();
            }
        }
        return START_STICKY;
    }

    private void disconnect() {
        if (server != null) {
            server.setClientIpAddress("");
        }
        status();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    private void start() {
        if (server == null || !server.isAlive()) {
            try {
                AlertDialog.Builder incomingConnectionBuilder = new AlertDialog.Builder
                        (new ContextThemeWrapper(this, R.style.AppTheme_Dark));
                server = new AppServlets(this, incomingConnectionBuilder);
                status();
            } catch (IOException e) {
                Log.e("AppWebService", "Unable to start server.");
            }
        }
    }

    private void status() {
        Intent statusIntent = new Intent("org.meluskyc.codebriefcase.STATUS_UPDATE");
        int ip = ((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo().getIpAddress();
        if (ip != 0) {
            statusIntent.putExtra("serverIp", AppUtils.formatIpAddress(ip));
        }
        else {
            statusIntent.putExtra("serverIp", STATUS_OFFLINE);
        }
        if (server != null) {
            statusIntent.putExtra("clientIp", server.getClientIpAddress());
        }
        else {
            statusIntent.putExtra("clientIp", "");
        }
        sendBroadcast(statusIntent);
    }

    private void stop() {
        if (server != null && server.isAlive()) {
            server.stop();
            server = null;
        }
        status();
        stopSelf();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, AppWebService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, AppWebService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    public static void disconnect(Context context) {
        Intent intent = new Intent(context, AppWebService.class);
        intent.setAction(ACTION_DISCONNECT);
        context.startService(intent);
    }

    public static void status(Context context) {
        Intent intent = new Intent(context, AppWebService.class);
        intent.setAction(ACTION_STATUS);
        context.startService(intent);
    }

}