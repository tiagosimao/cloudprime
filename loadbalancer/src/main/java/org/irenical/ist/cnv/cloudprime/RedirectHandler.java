package org.irenical.ist.cnv.cloudprime;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;


public class RedirectHandler extends HttpHandler {
    @Override
    public void service(Request request, Response response) throws Exception {
        response.sendRedirect("https://www.google.pt/#q=" + request.getRequestURI());
    }
}
