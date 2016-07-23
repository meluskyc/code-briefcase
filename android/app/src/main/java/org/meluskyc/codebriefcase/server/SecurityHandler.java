package org.meluskyc.codebriefcase.server;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.WindowManager;

import org.meluskyc.codebriefcase.R;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class SecurityHandler extends WebRouter.DefaultHandler {
    private static final String URI_CONNECT = "/connect";
    private static final String URI_CONNECT_IMAGE = "/assets/images/connect(.*).png";
    private static final String REJECTED_IP = "reject";

    /**
     * time to keep the dialog open
     * default is 30 seconds
      */
    public static final int DIALOG_TIMEOUT = 30000;


    @Override
    public String getText() {
        throw new IllegalStateException();
    }

    @Override
    public String getMimeType() {
        throw new IllegalStateException();
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }

    public NanoHTTPD.Response get(WebRouter.UriResource uriResource, Map<String, String> urlParams,
                                  NanoHTTPD.IHTTPSession session) {
        String uri = session.getUri();
        if (uri.equals(URI_CONNECT)) {
            if (showIncomingAlert(session)) {
                return NanoHTTPD.newFixedLengthResponse(getStatus(),
                        "application/json", "{ \"result\":\"accept\" }");
            } else {
                return NanoHTTPD.newFixedLengthResponse(getStatus(),
                        "application/json", "{ \"result\":\"reject\" }");
            }
        } else if (uri.matches(URI_CONNECT_IMAGE)) {
            return WebServer.serveStaticFile(WebServer.normalizeUri(
                    WebServer.PATH_WEBROOT + uri), session);
        } else {
            return WebServer.serveStaticFile(WebServer.PATH_CONNECT_PAGE, session);
        }
    }

    /**
     * Show an {@link AlertDialog} that prompts the user to
     * accept a connection to the web app.
     *
     * @param session
     * @return true if accepted
     */
    public synchronized boolean showIncomingAlert(final NanoHTTPD.IHTTPSession session) {
        String clientIp = WebServer.getClientIpAddress();
        final AlertDialog.Builder incomingConnectionBuilder
                = WebServer.getDialogBuilder();
        final Context context = WebServer.getContext();

        if (TextUtils.isEmpty(clientIp)) {
            final Handler handler = new Handler(Looper.getMainLooper());
            final String ip = session.getRemoteIpAddress();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    incomingConnectionBuilder
                            .setTitle(context.getString(R.string.wants_to_connect, ip))
                            .setMessage("")
                            .setCancelable(false)
                            .setPositiveButton(context.getString(R.string.accept), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    WebServer.setClientIpAddress(ip);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(context.getString(R.string.reject), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    WebServer.setClientIpAddress(REJECTED_IP);
                                    dialog.dismiss();
                                }
                            });
                    final AlertDialog dialog = incomingConnectionBuilder.create();
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();

                    final Handler handler  = new Handler();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            WebServer.setClientIpAddress(REJECTED_IP);
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }
                    };

                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            handler.removeCallbacks(runnable);
                        }
                    });

                    handler.postDelayed(runnable, DIALOG_TIMEOUT);

                }
            });
            while (TextUtils.isEmpty(WebServer.getClientIpAddress())) { }
            if (WebServer.getClientIpAddress().equals(REJECTED_IP)) {
                WebServer.setClientIpAddress(null);
                return false;
            } else {
                WebService.status(context);
                return true;
            }
        } else {
            WebServer.setClientIpAddress(null);
            return false;
        }
    }
}