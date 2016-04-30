package org.irenical.ist.cnv.cloudprime.rest;

import java.math.BigInteger;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.irenical.ist.cnv.cloudprime.logic.IntFactorization;
import org.irenical.ist.cnv.cloudprime.stats.Metric;

@Path("f.html")
public class Factor {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void asyncGet(@Suspended final AsyncResponse asyncResponse, @QueryParam("n") final String number) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                BigInteger integer = new BigInteger(number);
                Metric.start(integer);
                ArrayList<BigInteger> res = new IntFactorization().calcPrimeFactors(integer);
                Metric.end();
                asyncResponse.resume(res);
            }
        }).start();
    }
}
