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

public class SecurityHandler extends AppRouter.DefaultHandler {
    private static final String URI_CONNECT = "/connect";
    private static final String URI_CONNECT_IMAGE = "/assets/images/connect(.*).png";

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

    public NanoHTTPD.Response get(AppRouter.UriResource uriResource, Map<String, String> urlParams,
                                  NanoHTTPD.IHTTPSession session) {
        String uri = session.getUri();
        if (uri.equals(URI_CONNECT)) {
            if (showIncomingAlert(session)) {
                return NanoHTTPD.newFixedLengthResponse(getStatus(),
                        "application/json", "{ \"result\":\"accept\" }");
            }
            else {
                return NanoHTTPD.newFixedLengthResponse(getStatus(),
                        "application/json", "{ \"result\":\"reject\" }");
            }
        }
        else if (uri.equals(URI_CONNECT_IMAGE)) {
            return AppServer.serveStaticFile(AppServer.normalizeUri(
                    AppServer.PATH_WEBROOT + session.getUri()), session);
        }
        else {
            return AppServer.serveStaticFile(AppServer.PATH_CONNECT_PAGE, session);
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
        String clientIp = AppServer.getClientIpAddress();
        final AlertDialog.Builder incomingConnectionBuilder
                = AppServer.getIncomingConnectionBuilder();
        final Context context = AppServer.getContext();

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
                                    AppServer.setClientIpAddress(ip);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(context.getString(R.string.reject), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AppServer.setClientIpAddress("reject");
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
                            AppServer.setClientIpAddress("reject");
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

                    handler.postDelayed(runnable, 30000);

                }
            });
            while (TextUtils.isEmpty(AppServer.getClientIpAddress())) { }
            if (AppServer.getClientIpAddress().equals("rejected")) {
                AppServer.setClientIpAddress("");
                return false;
            }
            else {
                AppWebService.status(context);
                return true;
            }
        }
        else {
            AppServer.setClientIpAddress("");
            return false;
        }
    }
}