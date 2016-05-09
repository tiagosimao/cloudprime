package org.irenical.ist.cnv.cloudprime;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.google.gson.Gson;

public class ApiHandler extends HttpHandler {
    
    private static final String USAGE = "Usage:\n/api/node\n/api/job";

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        String [] path = getPath(request.getRequestURI());
        if(path.length>1){
            switch (path[1]) {
            case "node":
                node(response.getWriter());
                return;
            case "job":
                job(response.getWriter());
                return;
            }
        }
        response.getWriter().append(USAGE);
    }
    
    private String [] getPath(String uri) {
        if(uri.charAt(0)=='/'){
            uri = uri.substring(1);
        }
        if(uri.endsWith("/")){
            uri = uri.substring(0,uri.length()-1);
        }
        return uri.length() == 0 ? new String[]{} : uri.split("/");
    }

    private void node(Writer writer) throws IOException {
        List<CloudprimeNode> nodes = NodeController.getInstance().list();
        Gson gson = new Gson();
        writer.write(gson.toJson(nodes));
    }

    private void job(Writer writer) throws IOException {
        List<Job> jobs = JobController.getInstance().list();
        Gson gson = new Gson();
        writer.write(gson.toJson(jobs));
    }

}
