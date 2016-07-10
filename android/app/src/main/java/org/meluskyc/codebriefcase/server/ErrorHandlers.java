package org.meluskyc.codebriefcase.server;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by c on 7/10/16.
 */
public class ErrorHandlers {

    public static class Error401UriHandler extends AppRouter.DefaultHandler {

        public String getText() {
            return "<html><body><h3>Error 401: unauthorized.</h3></body></html>";
        }

        @Override
        public String getMimeType() {
            return "text/html";
        }

        @Override
        public NanoHTTPD.Response.IStatus getStatus() {
            return NanoHTTPD.Response.Status.UNAUTHORIZED;
        }
    }
}
