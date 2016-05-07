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
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(UriBuilder.fromUri("http://0.0.0.0").port(8080).build(), rc);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                server.shutdown();
            }
        }, "Server shutdown hook"));
        server.start();
    }

}
