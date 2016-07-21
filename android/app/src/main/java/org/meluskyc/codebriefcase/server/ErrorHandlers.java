package org.meluskyc.codebriefcase.server;

import fi.iki.elonen.NanoHTTPD;

public class ErrorHandlers {

    public static class UnauthorizedHandler extends WebRouter.DefaultHandler {

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

    public static class InternalServerErrorHandler extends WebRouter.DefaultHandler {

        public String getText() {
            return "<html><body><h3>Error 500: internal server error.</h3></body></html>";
        }

        @Override
        public String getMimeType() {
            return "text/html";
        }

        @Override
        public NanoHTTPD.Response.IStatus getStatus() {
            return NanoHTTPD.Response.Status.INTERNAL_ERROR;
        }
    }

    public static class BadRequestHandler extends WebRouter.DefaultHandler {

        public String getText() {
            return "<html><body><h3>Error 400: bad request.</h3></body></html>";
        }

        @Override
        public String getMimeType() {
            return "text/html";
        }

        @Override
        public NanoHTTPD.Response.IStatus getStatus() {
            return NanoHTTPD.Response.Status.BAD_REQUEST;
        }
    }

}
