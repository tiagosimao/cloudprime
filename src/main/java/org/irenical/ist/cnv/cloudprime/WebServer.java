package org.irenical.ist.cnv.cloudprime;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

public class WebServer {

  public static void main(String[] args) throws Exception {
    ResourceConfig rc = new ResourceConfig();
    rc.property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
    rc.register(JacksonFeature.class);
    rc.packages(true, "org.irenical.ist.cnv.cloudprime.rest");
    HttpServer server = GrizzlyHttpServerFactory.createHttpServer(UriBuilder.fromUri("http://localhost").port(8080).path("factor").build(), rc);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdown(), "Server shutdown hook"));
    server.start();
  }

}
