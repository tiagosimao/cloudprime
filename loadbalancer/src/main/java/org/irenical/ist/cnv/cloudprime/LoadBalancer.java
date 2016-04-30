package org.irenical.ist.cnv.cloudprime;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;

public class LoadBalancer {
    private static final String HOST = "localhost";
    private static final int PORT = 80;

    public static void main(String... args) {
        HttpServer httpServer = new HttpServer();

        NetworkListener networkListener = new NetworkListener("LoadBalancer", HOST, PORT);
        httpServer.addListener(networkListener);

        httpServer.getServerConfiguration().addHttpHandler(new RedirectHandler(), "/status");
        httpServer.getServerConfiguration().addHttpHandler(new RedirectHandler(), "/f.html");

        try {
            httpServer.start();
            System.out.println("Press any key to stop the server...");
            System.in.read();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
