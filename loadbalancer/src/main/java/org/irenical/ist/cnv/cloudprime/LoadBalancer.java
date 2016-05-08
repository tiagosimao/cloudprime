package org.irenical.ist.cnv.cloudprime;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;

public class LoadBalancer {
    
    public volatile static boolean RUNNING = false;
    
    public static final int MIN_NODES = 2;

    public static final int MAX_NODES = 4;
    
    public static final int MAX_JOBS_PER_NODE = 4;
    
    public static final int CONSUMER_THREAD_COUNT = 10;

    public static final long EC2_SYNC_POLL_MILLIS = 1000 * 20;

    public static final long MAX_NODE_INACTIVITY_MILLIS = 1000 * 60;
    
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
