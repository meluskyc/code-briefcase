package org.meluskyc.codebriefcase.server;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class StaticHandler extends WebRouter.DefaultHandler {

    @Override
    public String getText() {
        throw new IllegalStateException();
    }
    @Override
    public String getMimeType() {
        throw new IllegalStateException();
    }

    public NanoHTTPD.Response get(WebRouter.UriResource uriResource,
                                  Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String uri = session.getUri();
        uri = (uri.equals("/")) ? WebServer.PATH_HOME_PAGE :
                WebServer.normalizeUri(WebServer.PATH_WEBROOT + uri);
        return WebServer.serveStaticFile(uri, session);
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }
}
