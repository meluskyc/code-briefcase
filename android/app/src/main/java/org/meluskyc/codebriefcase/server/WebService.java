package org.meluskyc.codebriefcase.server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.util.Log;

import org.meluskyc.codebriefcase.R;

import java.io.IOException;

/**
 * {@link Service} class to manage starting and stopping the web
 * server.
 */
public class WebService extends Service {

    /**
     * Listen for this {@code Intent} action to receive server status updates
     */
    public static final String ACTION_STATUS_BROADCAST
            = "org.meluskyc.codebriefcase.STATUS_BROADCAST";

    /**
     * Client IP address sent with broadcasts of {@link WebService#ACTION_STATUS_BROADCAST}
     */
    public static final String EXTRA_CLIENT_IP = "clientIp";

    /**
     * Server is currently offline. Sent as {@link WebService#EXTRA_CLIENT_IP}.
     */
    public static final String STATUS_SERVER_OFFLINE = "offline";

    private static final String ACTION_START = "org.meluskyc.codebriefcase.START";
    private static final String ACTION_STOP = "org.meluskyc.codebriefcase.STOP";
    private static final String ACTION_STATUS = "org.meluskyc.codebriefcase.STATUS";
    private static final String ACTION_DISCONNECT = "org.meluskyc.codebriefcase.DISCONNECT";

    private WebServer server;

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
            } else if (action.equals(ACTION_STOP)) {
                stop();
            } else if (action.equals(ACTION_STATUS)) {
                status();
            } else if (action.equals(ACTION_DISCONNECT)) {
                disconnect();
            }
        }
        return START_STICKY;
    }

    /**
     * Disconnect from the current client.
     */
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

    /**
     * Start the web server.
     */
    private void start() {
        if (server == null || !server.isAlive()) {
            try {
                AlertDialog.Builder incomingConnectionBuilder = new AlertDialog.Builder
                        (new ContextThemeWrapper(this, R.style.AppTheme_Dark));
                server = new WebServer(this, incomingConnectionBuilder);
                status();
            } catch (IOException e) {
                Log.e("WebService", "Unable to start server.");
            }
        }
    }

    /**
     * Broadcast an {@link Intent} with the server's status.
     */
    private void status() {
        Intent statusIntent = new Intent(ACTION_STATUS_BROADCAST);
        if (server != null) {
            String ip = server.getClientIpAddress();
            if (!TextUtils.isEmpty(ip)) {
                statusIntent.putExtra(EXTRA_CLIENT_IP, ip);
            } else {
                statusIntent.putExtra(EXTRA_CLIENT_IP, "");
            }

        } else {
            statusIntent.putExtra(EXTRA_CLIENT_IP, STATUS_SERVER_OFFLINE);
        }
        sendBroadcast(statusIntent);
    }

    /**
     * Stop the web server and broadcast the status.
     */
    private void stop() {
        if (server != null && server.isAlive()) {
            server.stop();
            server = null;
        }
        status();
        stopSelf();
    }

    /**
     * Start the {@link Service} with an {@link Intent} to start the web server.
     * @param context
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, WebService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    /**
     * Start the {@link Service} with an {@link Intent} to stop the web server.
     * @param context
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context, WebService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    /**
     * Start the {@link Service} with an {@link Intent} to disconnect from the
     * connected client.
     * @param context
     */
    public static void disconnect(Context context) {
        Intent intent = new Intent(context, WebService.class);
        intent.setAction(ACTION_DISCONNECT);
        context.startService(intent);
    }

    /**
     * Start the {@link Service} with an {@link Intent} to broadcast the
     * server's status with {@code Intent} action
     * {@link WebService#ACTION_STATUS_BROADCAST}
     * @param context
     */
    public static void status(Context context) {
        Intent intent = new Intent(context, WebService.class);
        intent.setAction(ACTION_STATUS);
        context.startService(intent);
    }

}
