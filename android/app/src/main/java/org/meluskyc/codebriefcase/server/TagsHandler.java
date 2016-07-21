package org.meluskyc.codebriefcase.server;

import android.content.ContentResolver;
import android.database.Cursor;

import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Tag;
import org.meluskyc.codebriefcase.utils.AppUtils;

import java.io.ByteArrayInputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class TagsHandler extends WebRouter.DefaultHandler {
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
    GET /api/tags      all tags
     */
    @Override
    public NanoHTTPD.Response get(WebRouter.UriResource uriResource, Map<String, String> urlParams,
                                  NanoHTTPD.IHTTPSession session) {
        ContentResolver cr = WebServer.getContext().getContentResolver();
        Cursor c = null;
        String text = null;

        c = cr.query(Tag.CONTENT_URI,
                new String[]{Tag.TAG_NAME}, null, null,
                Tag.TAG_NAME + " ASC");
        text = AppUtils.cur2Json(c).toString();

        ByteArrayInputStream inp = new ByteArrayInputStream(text.getBytes());
        int size = text.getBytes().length;
        return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), inp, size);
    }
}
