package org.meluskyc.codebriefcase.server;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

/**
 * A simple web server using NanoHttpd.
 *
 * The web interface is inaccessible until an {@code AlertDialog} opened by
 * {@link SecurityHandler} with the requesting IP address is accepted. Future requests
 * from that IP will be served the webapp until the Android app is closed
 * or the user disconnects from {@link org.meluskyc.codebriefcase.activity.WebActivity}.
 */
public class WebServer extends WebRouter {
    public static final String PATH_WEBROOT = "webapp";
    public static final String PATH_HOME_PAGE = "webapp/index.html";
    public static final String PATH_CONNECT_PAGE = "webapp/assets/connect.html";

    /**
     * Port. Default is 9999.
     *
     * todo: make port a preference
     */
    public static final int PORT = 9999;

    private static Context context;
    private static String clientIpAddress = "";
    private static AlertDialog.Builder dialogBuilder;

    public WebServer(Context context, AlertDialog.Builder dialogBuilder) throws IOException {
        super(PORT);
        this.context = context;
        this.dialogBuilder = dialogBuilder;
        addMappings();
        start();
    }

    public static AlertDialog.Builder getDialogBuilder() {
        return dialogBuilder;
    }

    public static String getClientIpAddress() {
        return clientIpAddress;
    }

    public static void setClientIpAddress(String clientIpAddress) {
        WebServer.clientIpAddress = clientIpAddress;
    }

    public static Context getContext() {
        return context;
    }


    @Override
    public void addMappings() {
        super.addMappings();

        // remove the default routes
        removeRoute("/");
        removeRoute("/index.html");

        // API
        addRoute(ApiUriEnum.ITEMS.path,
                ItemsHandler.class, ApiUriEnum.ITEMS);
        addRoute(ApiUriEnum.ITEM_DISTINCT_TAGS.path,
                ItemsHandler.class, ApiUriEnum.ITEM_DISTINCT_TAGS);
        addRoute(ApiUriEnum.ITEM_BY_ID.path,
                ItemsHandler.class, ApiUriEnum.ITEM_BY_ID);
        addRoute(ApiUriEnum.TAGS.path,
                TagsHandler.class, ApiUriEnum.TAGS);

        // everything else
        addRoute("/(.)*", StaticHandler.class);
    }

    @Override
    public Response serve(final IHTTPSession session) {
        if (session.getRemoteIpAddress().equals(clientIpAddress)) {
            return super.serve(session);
        } else {
            if (!TextUtils.isEmpty(clientIpAddress)) {
                return new ErrorHandlers.UnauthorizedHandler().get(null, null, session);
            } else {
                return new SecurityHandler().get(null, null, session);
            }
        }
    }


    public static NanoHTTPD.Response serveStaticFile(String uri, NanoHTTPD.IHTTPSession session) {
        InputStream inputStream;

        try {
            inputStream = context.getAssets().open(uri);
        } catch (IOException e) {
            return new WebRouter.Error404UriHandler().get(null, null, session);
        }

        return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, getMimeTypeForFile(uri),
                inputStream);
    }
}