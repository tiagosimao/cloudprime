package org.irenical.ist.cnv.cloudprime;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;

public class LoadBalancer {
    private static final String HOST = "localhost";
    private static final int PORT = 8000;

    public static void main(String... args) {
        HttpServer httpServer = new HttpServer();

        NetworkListener networkListener = new NetworkListener("LoadBalancer", HOST, PORT);
        httpServer.addListener(networkListener);

        httpServer.getServerConfiguration().addHttpHandler(new RedirectHandler(), "/f.html");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Stopping server..");
                httpServer.shutdown();
            }
        }, "shutdownHook"));

        try {
            httpServer.start();
            System.out.println("Press CTRL^C to exit..");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
