package org.irenical.ist.cnv.cloudprime.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.irenical.ist.cnv.cloudprime.stats.Metric;

@Path("status")
public class Status {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStats() {
        int working = Metric.getActiveThreadCount();
        int all = Runtime.getRuntime().availableProcessors();
        Map<String, String> status = new HashMap<String,String>();
        status.put("totalcapacity", Integer.toString(all));
        status.put("usedcapacity", Integer.toString(working));
        return Response.ok(status).build();
    }

}
