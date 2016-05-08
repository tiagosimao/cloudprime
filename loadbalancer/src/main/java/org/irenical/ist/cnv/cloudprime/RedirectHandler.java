package org.irenical.ist.cnv.cloudprime;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

public class RedirectHandler extends HttpHandler {

    @Override
    public void destroy() {
        super.destroy();
        LoadBalancer.RUNNING = false;
        JobController.getInstance().stop();
        NodeController.getInstance().stop();
    }

    @Override
    public void start() {
        try {
            super.start();
            LoadBalancer.RUNNING = true;
            NodeController.getInstance().start();
            JobController.getInstance().start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        String number = request.getParameter("n");
        System.out.println("new request: " + number);
        Job job = JobController.getInstance().submitJob(number);
        if (job.getResult() != null) {
            response.getWriter().append(job.getResult());
        } else {
            response.sendError(500, "Backend error");
        }
    }

    

}
