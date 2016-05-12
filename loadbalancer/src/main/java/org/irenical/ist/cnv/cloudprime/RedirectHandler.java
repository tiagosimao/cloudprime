package org.irenical.ist.cnv.cloudprime;

import java.math.BigInteger;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

public class RedirectHandler extends HttpHandler {

    public void boot() throws Exception {
        LoadBalancer.RUNNING = true;
        NodeController.getInstance().start();
        JobController.getInstance().reload();
    }

    public void shutdown() {
        LoadBalancer.RUNNING = false;
        JobController.getInstance().stop();
        NodeController.getInstance().stop();
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        String input = request.getParameter("n");
        BigInteger number = toNumber(input);
        if (number != null) {
            System.out.println("new request: " + number);
            CloudprimeJob job = JobController.getInstance().submitJob(number);
            if (job.getResult() != null) {
                response.getWriter().append(job.getResult());
            } else {
                response.sendError(500, "Backend error");
            }
        } else {
            response.sendError(400, "Invalid input");
        }
    }

    private BigInteger toNumber(String number) {
        BigInteger i = null;
        if (number != null && !number.trim().isEmpty()) {
            try {
                i = new BigInteger(number);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return i;
    }

}
