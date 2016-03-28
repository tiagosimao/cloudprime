package org.irenical.ist.cnv.cloudprime.rest;

import java.math.BigInteger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.irenical.ist.cnv.cloudprime.logic.IntFactorization;

@Path("f.html")
public class Factor {

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response postNumber(@QueryParam("n") String number) {
    return Response.ok(new IntFactorization().calcPrimeFactors(new BigInteger(number))).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getNumber(@QueryParam("n") String number) {
    return Response.ok(new IntFactorization().calcPrimeFactors(new BigInteger(number))).build();
  }

}
