package org.meluskyc.codebriefcase.server;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

/**
 * Servlets that support a simple REST API for the web interface.
 *
 * The web interface is inaccessible until an {@link AlertDialog}
 * with the requesting IP address is accepted. Future requests from
 * that IP will be served the webapp until the app is closed
 * or the user disconnects from {@link org.meluskyc.codebriefcase.activity.WebActivity}.
 */
public class AppServer extends AppRouter {
    public static final String LOG_TAG = "AppServer";

    public static final String PATH_WEBROOT = "webapp";
    public static final String PATH_HOME_PAGE = "webapp/index.html";
    public static final String PATH_CONNECT_PAGE = "webapp/connect.html";


    public static final int PORT = 9999;

    private static Context context;
    private static String clientIpAddress = "";
    private static AlertDialog.Builder incomingConnectionBuilder;

    public AppServer(Context context, AlertDialog.Builder incomingConnectionBuilder) throws IOException {
        super(PORT);
        this.context = context;
        this.incomingConnectionBuilder = incomingConnectionBuilder;
        addMappings();
        start();
    }

    public static AlertDialog.Builder getIncomingConnectionBuilder() {
        return incomingConnectionBuilder;
    }

    public static String getClientIpAddress() {
        return clientIpAddress;
    }

    public static void setClientIpAddress(String clientIpAddress) {
        AppServer.clientIpAddress = clientIpAddress;
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
        addRoute(AppApiUriEnum.ITEMS.path,
                ItemsHandler.class, AppApiUriEnum.ITEMS);
        addRoute(AppApiUriEnum.ITEM_DISTINCT_TAGS.path,
                ItemsHandler.class, AppApiUriEnum.ITEM_DISTINCT_TAGS);
        addRoute(AppApiUriEnum.ITEM_BY_ID.path,
                ItemsHandler.class, AppApiUriEnum.ITEM_BY_ID);
        addRoute(AppApiUriEnum.TAGS.path,
                TagsHandler.class, AppApiUriEnum.TAGS);

        // everything else
        addRoute("/(.)*", StaticHandler.class);
    }

    @Override
    public Response serve(final IHTTPSession session) {
        if (session.getRemoteIpAddress().equals(clientIpAddress)) {
            return super.serve(session);
        }
        else {
            if (!TextUtils.isEmpty(clientIpAddress)) {
                return new ErrorHandlers.Error401UriHandler().get(null, null, session);
            }
            else {
                return new SecurityHandler().get(null, null, session);
            }
        }
    }


    public static NanoHTTPD.Response serveStaticFile(String uri, NanoHTTPD.IHTTPSession session) {
        InputStream inputStream;

        try {
            inputStream = context.getAssets().open(uri);
        }
        catch (IOException e) {
            return new AppRouter.Error404UriHandler().get(null, null, session);
        }

        return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, getMimeTypeForFile(uri), inputStream);
    }
}