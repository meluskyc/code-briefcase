package org.meluskyc.codebriefcase.server;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Item;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Qualified;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Tag;
import org.meluskyc.codebriefcase.utils.AppUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;


public class AppServlets extends AppRouter {

    private static final String LOG_TAG = "AppServlets";
    public static final int PORT = 9999;
    private static final String WEBROOT = "webapp";
    private static final String HOME_PAGE = "webapp/index.html";
    private static final String CONNECT_PAGE = "webapp/connect.html";
    private static Context context;
    private String clientIpAddress = "";

    private static final int ITEMS = 1;
    private static final int ITEM_DISTINCT_TAGS = 2;
    private static final int ITEM_BY_ID = 3;
    private static final int TAGS = 4;
    private static final int ACCEPT = 5;
    private static final int REJECT = 6;
    private static final int IMAGE = 7;
    private static final int REQUEST = 8;

    private AlertDialog.Builder incomingConnectionBuilder;

    public AppServlets(Context context, AlertDialog.Builder incomingConnectionBuilder) throws IOException {
        super(PORT);
        this.context = context;
        this.incomingConnectionBuilder = incomingConnectionBuilder;
        addMappings();
        start();
    }

    public String getClientIpAddress() {
        return clientIpAddress;
    }

    public void setClientIpAddress(String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
    }

    public static class StaticHandler extends DefaultHandler {
        @Override
        public String getText() {
            throw new IllegalStateException();
        }
        @Override
        public String getMimeType() {
            throw new IllegalStateException();
        }

        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            String uri = session.getUri();
            uri = (uri.equals("/")) ? HOME_PAGE : normalizeUri(WEBROOT + uri);
            return serveStaticFile(uri, session);
        }

        @Override
        public Response.IStatus getStatus() {
            return Response.Status.OK;
        }

    }

    public static class ItemsHandler extends DefaultHandler {
        @Override
        public String getText() {
            throw new IllegalStateException();
        }

        @Override
        public String getMimeType() {
            return "application/json";
        }

        @Override
        public Response.IStatus getStatus() {
            return Response.Status.OK;
        }

        /*
        GET /api/items      all items
        GET /api/items/tag  distinct tags for items
        GET /api/items/:id  item by ID
         */

        @Override
        public NanoHTTPD.Response get(UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            ContentResolver cr = context.getContentResolver();
            Cursor c;
            String text = null;

            switch (uriResource.initParameter(Integer.class)) {
                case ITEMS:
                    c = cr.query(Item.buildTagDirUri(),
                            new String[]{Qualified.ITEM_ID, Item.ITEM_TAG_PRIMARY, Item.ITEM_DESCRIPTION,
                                    Item.ITEM_DATE_UPDATED, Item.ITEM_TAG_SECONDARY, Tag.TAG_COLOR, Item.ITEM_STARRED}, null, null,
                            Item.ITEM_DATE_UPDATED + " DESC");
                    text = cur2Json(c).toString();
                    break;
                case ITEM_DISTINCT_TAGS:
                    c = cr.query(Item.buildTagDirUri(),
                            new String[]{AppUtils
                                    .formatQueryDistinctParameter(Item.ITEM_TAG_PRIMARY)},
                            null, null, Item.ITEM_TAG_PRIMARY + " COLLATE NOCASE ASC");
                    text = cur2Json(c).toString();
                    break;
                case ITEM_BY_ID:
                    long id;
                    try {
                        id = Long.parseLong(urlParams.get("_id"));
                    }
                    catch(NumberFormatException e) {
                        return NanoHTTPD.newFixedLengthResponse(Response.Status.BAD_REQUEST,
                                "text/plain", null);
                    }

                    c = cr.query(Item.buildTagItemUri(id),
                            new String[]{Qualified.ITEM_ID, Item.ITEM_TAG_PRIMARY,
                                    Item.ITEM_DESCRIPTION, Item.ITEM_DATE_UPDATED,
                                    Item.ITEM_TAG_SECONDARY, Item.ITEM_CONTENT,
                                    Tag.TAG_ACE_MODE}, null, null, null);

                    if (c.getCount() > 0) {
                        JSONArray arr = cur2Json(c);
                        try {
                            text = arr.getJSONObject(0).toString();
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "Unable to parse JSON: " + e.getMessage());
                            return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR,
                                    "text/plain", null);
                        }
                    }
                    break;
            }

            ByteArrayInputStream inp = new ByteArrayInputStream(text.getBytes());
            int size = text.getBytes().length;
            return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), inp, size);
        }

        /*
        DELETE /api/items/:id      delete item by ID
         */

        @Override
        public NanoHTTPD.Response delete(UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            ContentResolver cr = context.getContentResolver();
            Cursor c;
            String text = null;

            switch (uriResource.initParameter(Integer.class)) {
                case ITEM_BY_ID:
                    long id;
                    try {
                        id = Long.parseLong(urlParams.get("_id"));
                    }
                    catch(NumberFormatException e) {
                        return NanoHTTPD.newFixedLengthResponse(Response.Status.BAD_REQUEST,
                                "text/plain", null);
                    }

                    c = cr.query(Item.buildItemUri(id),
                            new String[]{Item.ITEM_DESCRIPTION, Item.ITEM_TAG_PRIMARY,
                                    Item.ITEM_TAG_SECONDARY, Item.ITEM_CONTENT}, null, null, null);
                    if (c.getCount() > 0) {
                        JSONArray arr = cur2Json(c);
                        try {
                            text = arr.getJSONObject(0).toString();
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "Unable to parse JSON: " + e.getMessage());
                        }
                    }
                    else {
                        return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR,
                                "text/plain", null);
                    }
                    cr.delete(Item.buildItemUri(id), null, null);
                    break;
            }
            ByteArrayInputStream inp = new ByteArrayInputStream(text.getBytes());
            int size = text.getBytes().length;
            return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), inp, size);
        }

        /*
        POST /api/items/      add an item
         */

        @Override
        public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            ContentResolver cr = context.getContentResolver();
            Cursor c;
            byte[] buffer;
            int contentLength;
            String text = null;
            String bufferText, description, tag_primary;

            switch (uriResource.initParameter(Integer.class)) {
                case ITEMS:
                    ContentValues values = new ContentValues();
                    try {
                        contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
                        buffer = new byte[contentLength];
                        session.getInputStream().read(buffer, 0, contentLength);
                        bufferText = new String(buffer);
                        JSONObject item = new JSONObject(bufferText);

                        // set a description and tag if none were entered
                        description = item.getString(Item.ITEM_DESCRIPTION);
                        tag_primary = item.getString(Item.ITEM_TAG_PRIMARY);
                        description = TextUtils.isEmpty(description) ?
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                                : description;
                        tag_primary = (TextUtils.isEmpty(tag_primary) || tag_primary.equals("Tag"))
                                ? "Text" : tag_primary;

                        values.put(Item.ITEM_DESCRIPTION, description);
                        values.put(Item.ITEM_CONTENT, item.getString(Item.ITEM_CONTENT));
                        values.put(Item.ITEM_TAG_PRIMARY, tag_primary);
                        values.put(Item.ITEM_TAG_SECONDARY, item.getString(Item.ITEM_TAG_SECONDARY));
                        values.put(Item.ITEM_DATE_CREATED, System.currentTimeMillis());
                        values.put(Item.ITEM_DATE_UPDATED, System.currentTimeMillis());

                        Uri newRow = cr.insert(Item.CONTENT_URI, values);

                        c = cr.query(Item.buildItemUri(Long.parseLong(newRow.getLastPathSegment())),
                                new String[]{Item.ITEM_DESCRIPTION, Item.ITEM_TAG_PRIMARY,
                                        Item.ITEM_TAG_SECONDARY, Item.ITEM_CONTENT}, null, null, null);
                        text = cur2Json(c).getJSONObject(0).toString();
                    } catch (IOException e) {
                        return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", null);
                    } catch (JSONException e) {
                        return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", null);
                    }
                    break;
            }
            ByteArrayInputStream inp = new ByteArrayInputStream(text.getBytes());
            int size = text.getBytes().length;
            return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), inp, size);
        }

        /*
        PUT /api/items/:id      update item by ID
         */

        @Override
        public Response put(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            ContentResolver cr = context.getContentResolver();
            Cursor c;
            byte[] buffer;
            String text = null;
            String bufferText = null;

            switch (uriResource.initParameter(Integer.class)) {
                case ITEM_BY_ID:
                    ContentValues values = new ContentValues();
                    try {
                        int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
                        buffer = new byte[contentLength];
                        session.getInputStream().read(buffer, 0, contentLength);
                        bufferText = new String(buffer);
                        JSONObject item = new JSONObject(bufferText);

                        long id = Long.parseLong(urlParams.get(Item.ITEM_ID));

                        if (item.has(Item.ITEM_STARRED)) {
                            values.put(Item.ITEM_STARRED, item.getString(Item.ITEM_STARRED));
                        }
                        else if (item.has(Item.ITEM_CONTENT)) {
                            values.put(Item.ITEM_CONTENT, item.getString(Item.ITEM_CONTENT));
                            values.put(Item.ITEM_DESCRIPTION, item.getString(Item.ITEM_DESCRIPTION));
                            values.put(Item.ITEM_TAG_PRIMARY, item.getString(Item.ITEM_TAG_PRIMARY));
                            values.put(Item.ITEM_TAG_SECONDARY, item.getString(Item.ITEM_TAG_SECONDARY));
                            values.put(Item.ITEM_DATE_UPDATED, System.currentTimeMillis());
                        }

                        cr.update(Item.buildItemUri(id), values, null, null);

                        c = cr.query(Item.buildItemUri(id),
                                new String[]{Item.ITEM_DESCRIPTION, Item.ITEM_TAG_PRIMARY,
                                        Item.ITEM_TAG_SECONDARY, Item.ITEM_CONTENT}, null, null, null);
                        text = cur2Json(c).getJSONObject(0).toString();
                    } catch (IOException e) {
                        return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", null);
                    } catch (JSONException e) {
                        return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", null);
                    }
                    break;
            }
            ByteArrayInputStream inp = new ByteArrayInputStream(text.getBytes());
            int size = text.getBytes().length;
            return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), inp, size);
        }
    }

    public static class TagsHandler extends DefaultHandler {
        @Override
        public String getText() {
            throw new IllegalStateException();
        }

        @Override
        public String getMimeType() {
            return "application/json";
        }

        @Override
        public Response.IStatus getStatus() {
            return Response.Status.OK;
        }

        /*
        GET /api/tags      all tags
         */
        @Override
        public NanoHTTPD.Response get(UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            ContentResolver cr = context.getContentResolver();
            Cursor c = null;
            String text = null;

            switch (uriResource.initParameter(Integer.class)) {
                case TAGS:
                    c = cr.query(Tag.CONTENT_URI,
                            new String[]{Tag.TAG_NAME}, null, null, Tag.TAG_NAME + " ASC");
                    text = cur2Json(c).toString();
                    break;
            }

            ByteArrayInputStream inp = new ByteArrayInputStream(text.getBytes());
            int size = text.getBytes().length;
            return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), inp, size);
        }
    }

    public static class Error401UriHandler extends DefaultHandler {

        public String getText() {
            return "<html><body><h3>Error 401: unauthorized.</h3></body></html>";
        }

        @Override
        public String getMimeType() {
            return "text/html";
        }

        @Override
        public Response.IStatus getStatus() {
            return Response.Status.UNAUTHORIZED;
        }
    }

    public static class SecurityHandler extends DefaultHandler {

        @Override
        public String getText() {
            throw new IllegalStateException();
        }

        @Override
        public String getMimeType() {
            throw new IllegalStateException();
        }

        @Override
        public Response.IStatus getStatus() {
            return Response.Status.OK;
        }

        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            String text, mimeType;
            text = mimeType = "";

            switch (uriResource.initParameter(Integer.class)) {
                case REQUEST:
                    return serveStaticFile(CONNECT_PAGE, session);
                case ACCEPT:
                    mimeType = "application/json";
                    text = "{ \"result\":\"accept\" }";
                    break;
                case IMAGE:
                    return serveStaticFile(normalizeUri(WEBROOT + session.getUri()), session);
                case REJECT:
                    mimeType = "application/json";
                    text = "{ \"result\":\"reject\" }";
                    break;
            }
            return NanoHTTPD.newFixedLengthResponse(getStatus(), mimeType, text);
        }
    }

    private static Response serveStaticFile(String uri, IHTTPSession session) {
        InputStream inputStream;

        try {
            inputStream = context.getAssets().open(uri);
        }
        catch (IOException e) {
            return new Error404UriHandler().get(null, null, session);
        }

        return NanoHTTPD.newChunkedResponse(Response.Status.OK, getMimeTypeForFile(uri), inputStream);
    }

    // http://stackoverflow.com/questions/13070791/android-cursor-to-jsonarray
    private static JSONArray cur2Json(Cursor cursor) {
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            final int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            int i;
            for (  i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    String getcol = cursor.getColumnName(i);
                    String getstr = cursor.getString(i);

                    try {
                        rowObject.put(getcol, getstr);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Unable to serialize cursor");
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        return resultSet;
    }

    @Override
    public void addMappings() {
        super.addMappings();

        // remove the default routes
        removeRoute("/");
        removeRoute("/index.html");

        // API
        addRoute("/api/items", ItemsHandler.class, ITEMS);
        addRoute("/api/items/tags", ItemsHandler.class, ITEM_DISTINCT_TAGS);
        addRoute("/api/items/:_id", ItemsHandler.class, ITEM_BY_ID);
        addRoute("/api/tags", TagsHandler.class, TAGS);

        // everything else
        addRoute("/(.)*", StaticHandler.class);
    }


    public synchronized Response showIncomingAlert(final IHTTPSession session) {
        if (TextUtils.isEmpty(clientIpAddress)) {
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
                                    clientIpAddress = ip;
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(context.getString(R.string.reject), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    clientIpAddress = "reject";
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
                            clientIpAddress = "reject";
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
            while (TextUtils.isEmpty(clientIpAddress)) { }
            if (clientIpAddress.equals(ip)) {
                AppWebService.status(context);
                return new SecurityHandler().get(new UriResource(null, 0, null, ACCEPT), null, session);
            }
            else {
                clientIpAddress = "";
                return new SecurityHandler().get(new UriResource(null, 0, null, REJECT), null, session);
            }
        }
        else {
            // a previous request was accepted - return reject
            return new SecurityHandler().get(new UriResource(null, 0, null, REJECT), null, session);
        }
    }


    @Override
    public Response serve(final IHTTPSession session) {
        if (session.getRemoteIpAddress().equals(clientIpAddress)) {
            return super.serve(session);
        }
        else {
            if (!TextUtils.isEmpty(clientIpAddress)) {
                // already have a client - send a 401
                return new Error401UriHandler().get(null, null, session);
            }
            else {
                final String uri = session.getUri();
                if (uri.equals("/connect")) {
                    return showIncomingAlert(session);
                }
                else if (uri.matches("/assets/images/connect(.*).png")) {
                    return new SecurityHandler().get(new UriResource(null, 0, null, IMAGE), null, session);
                }
                else {
                    // send the request page
                    return new SecurityHandler().get(new UriResource(null, 0, null, REQUEST), null, session);
                }
            }
        }
    }
}