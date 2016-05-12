package org.irenical.ist.cnv.cloudprime;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.irenical.jindy.ConfigNotFoundException;

import com.google.gson.Gson;

public class ApiHandler extends HttpHandler {

    private static final String USAGE = "Usage:\n/api/node\n/api/job";

    private static final Config config = ConfigFactory.getConfig();

    public void boot() throws Exception {
    }

    public void shutdown() {
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        String[] path = getPath(request.getRequestURI());
        if (path.length > 1) {
            switch (path[1]) {
            case "node":
                node(response.getWriter());
                return;
            case "job":
                job(response.getWriter());
                return;
            case "config":
                config(request, response);
                return;
            }
        }
        response.getWriter().append(USAGE);
    }

    private void config(Request request, Response response) throws IOException, ConfigNotFoundException {
        Gson gson = new Gson();
        if (Method.POST.equals(request.getMethod())) {
            Map<String, String[]> params = request.getParameterMap();
            for (String k : params.keySet()) {
                String[] values = params.get(k);
                Object value = null;
                if (values != null && values.length != 0) {
                    value = values.length == 1 ? values[0] : values;
                }
                config.setProperty(k, value);
            }
            LoadBalancer.reload();
            response.sendRedirect("/");
        } else {
            Map<String, String> props = new HashMap<>();
            for (String k : config.getKeys("cloudprime")) {
                props.put(k, config.getString(k));
            }
            response.getWriter().write(gson.toJson(props));
        }
    }

    private String[] getPath(String uri) {
        if (uri.charAt(0) == '/') {
            uri = uri.substring(1);
        }
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri.length() == 0 ? new String[] {} : uri.split("/");
    }

    private void node(Writer writer) throws IOException {
        List<CloudprimeNode> nodes = NodeController.getInstance().list(false);
        Gson gson = new Gson();
        writer.write(gson.toJson(nodes));
    }

    private void job(Writer writer) throws IOException {
        List<Job> jobs = JobController.getInstance().list();
        Gson gson = new Gson();
        writer.write(gson.toJson(jobs));
    }

}
