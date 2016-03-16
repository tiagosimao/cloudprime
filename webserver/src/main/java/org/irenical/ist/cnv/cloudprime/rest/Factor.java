package org.irenical.ist.cnv.cloudprime.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.irenical.ist.cnv.cloudprime.logic.Factorer;

@Path("factor/{number}")
public class Factor {

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response postNumber(@PathParam("number") String number) {
    return Response.ok(Factorer.factor(number)).build();
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getNumber(@PathParam("number") String number) {
    return Response.ok(Factorer.factor(number)).build();
  }

}
