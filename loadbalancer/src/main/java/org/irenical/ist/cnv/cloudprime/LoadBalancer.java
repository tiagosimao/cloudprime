package org.irenical.ist.cnv.cloudprime;

import java.io.IOException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.irenical.jindy.ConfigNotFoundException;

public class LoadBalancer {
    
    public static final String LB_PORT = "cloudprime.lb.port";
    
    public volatile static boolean RUNNING = false;

    public static final int MIN_NODES = 0;

    public static final int MAX_NODES = 4;

    public static final int MAX_JOBS_PER_NODE = 4;

    public static final int CONSUMER_THREAD_COUNT = 10;

    public static final long EC2_SYNC_POLL_MILLIS = 1000 * 20;

    public static final long MAX_NODE_INACTIVITY_MILLIS = 1000 * 60;

    private static final String HOST = "localhost";

    private static Config config;
    
    private static HttpServer httpServer;
    
    private static RedirectHandler redirect;
    
    private static ApiHandler api;
    
    private static ResourceHandler resource;
    
    public static void reload() throws IOException, ConfigNotFoundException {
        if(httpServer!=null){
            httpServer.shutdown(); 
        }
        httpServer=new HttpServer();
        NetworkListener networkListener = new NetworkListener("LoadBalancer", HOST, config.getMandatoryInt(LB_PORT));
        httpServer.addListener(networkListener);
        httpServer.getServerConfiguration().addHttpHandler(redirect, "/f.html");
        httpServer.getServerConfiguration().addHttpHandler(api, "/api");
        httpServer.getServerConfiguration().addHttpHandler(resource, "/*");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Stopping server..");
                httpServer.shutdown();
            }
        }, "shutdownHook"));
        httpServer.start();
    }

    public static void main(String... args) {
        try {
            
            config = ConfigFactory.getConfig();

            redirect = new RedirectHandler();
            api = new ApiHandler();
            resource = new ResourceHandler();

            redirect.boot();
            api.boot();
            resource.boot();

            reload();
            
            System.out.println("Press CTRL^C to exit..");
            Thread.currentThread().join();
            
            redirect.shutdown();
            api.shutdown();
            resource.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
