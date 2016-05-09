package org.irenical.ist.cnv.cloudprime;

import java.io.InputStream;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.amazonaws.util.IOUtils;

public class ResourceHandler extends HttpHandler {

    @Override
    public void service(Request request, Response response) throws Exception {
        String path = request.getRequestURI();
        String resource = getResource(path);
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
            response.getWriter().write(IOUtils.toString(is));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(404);
        }
    }

    private String getResource(String path) {
        switch (path) {
        case "/cloudprime.js":
            return "cloudprime.js";
        default:
            return "index.html";
        }
    }

}
