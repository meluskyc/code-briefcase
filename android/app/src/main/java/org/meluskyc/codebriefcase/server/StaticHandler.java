package org.meluskyc.codebriefcase.server;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class StaticHandler extends AppRouter.DefaultHandler {

    @Override
    public String getText() {
        throw new IllegalStateException();
    }
    @Override
    public String getMimeType() {
        throw new IllegalStateException();
    }

    public NanoHTTPD.Response get(AppRouter.UriResource uriResource,
                                  Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String uri = session.getUri();
        uri = (uri.equals("/")) ? AppServer.PATH_HOME_PAGE :
                AppServer.normalizeUri(AppServer.PATH_WEBROOT + uri);
        return AppServer.serveStaticFile(uri, session);
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }
}
