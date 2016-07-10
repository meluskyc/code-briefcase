package org.meluskyc.codebriefcase.server;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract;
import org.meluskyc.codebriefcase.utils.AppUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class ItemsHandler extends AppRouter.DefaultHandler {
    private static final String LOG_TAG = "ItemsHandler";

    @Override
    public String getText() {
        throw new IllegalStateException();
    }

    @Override
    public String getMimeType() {
        return "application/json";
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }

        /*
        GET /api/items      all items
        GET /api/items/tag  distinct tags for items
        GET /api/items/:id  item by ID
         */

    @Override
    public NanoHTTPD.Response get(AppRouter.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        ContentResolver cr = AppServer.getContext().getContentResolver();
        Cursor c;
        String text = null;

        switch (uriResource.initParameter(AppApiUriEnum.class)) {
            case ITEMS:
                c = cr.query(CodeBriefcaseContract.Item.buildTagDirUri(),
                        new String[]{CodeBriefcaseContract.Qualified.ITEM_ID, CodeBriefcaseContract.Item.ITEM_TAG_PRIMARY, CodeBriefcaseContract.Item.ITEM_DESCRIPTION,
                                CodeBriefcaseContract.Item.ITEM_DATE_UPDATED, CodeBriefcaseContract.Item.ITEM_TAG_SECONDARY, CodeBriefcaseContract.Tag.TAG_COLOR, CodeBriefcaseContract.Item.ITEM_STARRED}, null, null,
                        CodeBriefcaseContract.Item.ITEM_DATE_UPDATED + " DESC");
                text = AppUtils.cur2Json(c).toString();
                break;
            case ITEM_DISTINCT_TAGS:
                c = cr.query(CodeBriefcaseContract.Item.buildTagDirUri(),
                        new String[]{AppUtils
                                .formatQueryDistinctParameter(CodeBriefcaseContract.Item.ITEM_TAG_PRIMARY)},
                        null, null, CodeBriefcaseContract.Item.ITEM_TAG_PRIMARY + " COLLATE NOCASE ASC");
                text = AppUtils.cur2Json(c).toString();
                break;
            case ITEM_BY_ID:
                long id;
                try {
                    id = Long.parseLong(urlParams.get("_id"));
                }
                catch(NumberFormatException e) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                            "text/plain", null);
                }

                c = cr.query(CodeBriefcaseContract.Item.buildTagItemUri(id),
                        new String[]{CodeBriefcaseContract.Qualified.ITEM_ID, CodeBriefcaseContract.Item.ITEM_TAG_PRIMARY,
                                CodeBriefcaseContract.Item.ITEM_DESCRIPTION, CodeBriefcaseContract.Item.ITEM_DATE_UPDATED,
                                CodeBriefcaseContract.Item.ITEM_TAG_SECONDARY, CodeBriefcaseContract.Item.ITEM_CONTENT,
                                CodeBriefcaseContract.Tag.TAG_ACE_MODE}, null, null, null);

                if (c.getCount() > 0) {
                    JSONArray arr = AppUtils.cur2Json(c);
                    try {
                        text = arr.getJSONObject(0).toString();
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Unable to parse JSON: " + e.getMessage());
                        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR,
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
    public NanoHTTPD.Response delete(AppRouter.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        ContentResolver cr = AppServer.getContext().getContentResolver();
        Cursor c;
        String text = null;

        switch (uriResource.initParameter(AppApiUriEnum.class)) {
            case ITEM_BY_ID:
                long id;
                try {
                    id = Long.parseLong(urlParams.get("_id"));
                }
                catch(NumberFormatException e) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                            "text/plain", null);
                }

                c = cr.query(CodeBriefcaseContract.Item.buildItemUri(id),
                        new String[]{CodeBriefcaseContract.Item.ITEM_DESCRIPTION, CodeBriefcaseContract.Item.ITEM_TAG_PRIMARY,
                                CodeBriefcaseContract.Item.ITEM_TAG_SECONDARY, CodeBriefcaseContract.Item.ITEM_CONTENT}, null, null, null);
                if (c.getCount() > 0) {
                    JSONArray arr = AppUtils.cur2Json(c);
                    try {
                        text = arr.getJSONObject(0).toString();
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Unable to parse JSON: " + e.getMessage());
                    }
                }
                else {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR,
                            "text/plain", null);
                }
                cr.delete(CodeBriefcaseContract.Item.buildItemUri(id), null, null);
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
    public NanoHTTPD.Response post(AppRouter.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        ContentResolver cr = AppServer.getContext().getContentResolver();
        Cursor c;
        byte[] buffer;
        int contentLength;
        String text = null;
        String bufferText, description, tag_primary;

        switch (uriResource.initParameter(AppApiUriEnum.class)) {
            case ITEMS:
                ContentValues values = new ContentValues();
                try {
                    contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
                    buffer = new byte[contentLength];
                    session.getInputStream().read(buffer, 0, contentLength);
                    bufferText = new String(buffer);
                    JSONObject item = new JSONObject(bufferText);

                    // set a description and tag if none were entered
                    description = item.getString(CodeBriefcaseContract.Item.ITEM_DESCRIPTION);
                    tag_primary = item.getString(CodeBriefcaseContract.Item.ITEM_TAG_PRIMARY);
                    description = TextUtils.isEmpty(description) ?
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                            : description;
                    tag_primary = (TextUtils.isEmpty(tag_primary) || tag_primary.equals("Tag"))
                            ? "Text" : tag_primary;

                    values.put(CodeBriefcaseContract.Item.ITEM_DESCRIPTION, description);
                    values.put(CodeBriefcaseContract.Item.ITEM_CONTENT, item.getString(CodeBriefcaseContract.Item.ITEM_CONTENT));
                    values.put(CodeBriefcaseContract.Item.ITEM_TAG_PRIMARY, tag_primary);
                    values.put(CodeBriefcaseContract.Item.ITEM_TAG_SECONDARY, item.getString(CodeBriefcaseContract.Item.ITEM_TAG_SECONDARY));
                    values.put(CodeBriefcaseContract.Item.ITEM_DATE_CREATED, System.currentTimeMillis());
                    values.put(CodeBriefcaseContract.Item.ITEM_DATE_UPDATED, System.currentTimeMillis());

                    Uri newRow = cr.insert(CodeBriefcaseContract.Item.CONTENT_URI, values);

                    c = cr.query(CodeBriefcaseContract.Item.buildItemUri(Long.parseLong(newRow.getLastPathSegment())),
                            new String[]{CodeBriefcaseContract.Item.ITEM_DESCRIPTION, CodeBriefcaseContract.Item.ITEM_TAG_PRIMARY,
                                    CodeBriefcaseContract.Item.ITEM_TAG_SECONDARY, CodeBriefcaseContract.Item.ITEM_CONTENT}, null, null, null);
                    text = AppUtils.cur2Json(c).getJSONObject(0).toString();
                } catch (IOException e) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", null);
                } catch (JSONException e) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", null);
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
    public NanoHTTPD.Response put(AppRouter.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        ContentResolver cr = AppServer.getContext().getContentResolver();
        Cursor c;
        byte[] buffer;
        String text = null;
        String bufferText = null;

        switch (uriResource.initParameter(AppApiUriEnum.class)) {
            case ITEM_BY_ID:
                ContentValues values = new ContentValues();
                try {
                    int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
                    buffer = new byte[contentLength];
                    session.getInputStream().read(buffer, 0, contentLength);
                    bufferText = new String(buffer);
                    JSONObject item = new JSONObject(bufferText);

                    long id = Long.parseLong(urlParams.get(CodeBriefcaseContract.Item.ITEM_ID));

                    if (item.has(CodeBriefcaseContract.Item.ITEM_STARRED)) {
                        values.put(CodeBriefcaseContract.Item.ITEM_STARRED, item.getString(CodeBriefcaseContract.Item.ITEM_STARRED));
                    }
                    else if (item.has(CodeBriefcaseContract.Item.ITEM_CONTENT)) {
                        values.put(CodeBriefcaseContract.Item.ITEM_CONTENT, item.getString(CodeBriefcaseContract.Item.ITEM_CONTENT));
                        values.put(CodeBriefcaseContract.Item.ITEM_DESCRIPTION, item.getString(CodeBriefcaseContract.Item.ITEM_DESCRIPTION));
                        values.put(CodeBriefcaseContract.Item.ITEM_TAG_PRIMARY, item.getString(CodeBriefcaseContract.Item.ITEM_TAG_PRIMARY));
                        values.put(CodeBriefcaseContract.Item.ITEM_TAG_SECONDARY, item.getString(CodeBriefcaseContract.Item.ITEM_TAG_SECONDARY));
                        values.put(CodeBriefcaseContract.Item.ITEM_DATE_UPDATED, System.currentTimeMillis());
                    }

                    cr.update(CodeBriefcaseContract.Item.buildItemUri(id), values, null, null);

                    c = cr.query(CodeBriefcaseContract.Item.buildItemUri(id),
                            new String[]{CodeBriefcaseContract.Item.ITEM_DESCRIPTION, CodeBriefcaseContract.Item.ITEM_TAG_PRIMARY,
                                    CodeBriefcaseContract.Item.ITEM_TAG_SECONDARY, CodeBriefcaseContract.Item.ITEM_CONTENT}, null, null, null);
                    text = AppUtils.cur2Json(c).getJSONObject(0).toString();
                } catch (IOException e) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", null);
                } catch (JSONException e) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", null);
                }
                break;
        }
        ByteArrayInputStream inp = new ByteArrayInputStream(text.getBytes());
        int size = text.getBytes().length;
        return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), inp, size);
    }
}