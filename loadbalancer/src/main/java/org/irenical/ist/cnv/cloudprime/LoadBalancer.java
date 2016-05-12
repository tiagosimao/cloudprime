package org.irenical.ist.cnv.cloudprime;

import java.io.IOException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;
import org.irenical.jindy.ConfigNotFoundException;

public class LoadBalancer {
    
    public static class Property {
        
        static String PORT = "cloudprime.lb.port";
        
        static String MAX_REQUESTS = "cloudprime.lb.maxrequests";
        
        static String MIN_NODES = "cloudprime.lb.min.nodes";

        static String MAX_NODES = "cloudprime.lb.max.nodes";
        
        static String CONSUMER_THREAD_COUNT = "cloudprime.lb.consumer.threadcount";

        static String EC2_SYNC_POLL_MILLIS = "cloudprime.lb.ec2.sync.poll.millis";

        static String MAX_NODE_INACTIVITY_MILLIS = "cloudprime.lb.max.node.inactivity.millis";
        
        static String JOB_CHEAP_THRESHOLD = "cloudprime.lb.job.cheap.threshold";
        
        static String JOB_AVERAGE_THRESHOLD = "cloudprime.lb.job.average.threshold";
        
    }
    
    public static class Default {
        
        static int PORT = 8000;
        
        static int MAX_REQUESTS = 100;
        
        static int MIN_NODES = 0;

        static int MAX_NODES = 10;
        
        static int CONSUMER_THREAD_COUNT = 10;

        static int EC2_SYNC_POLL_MILLIS = 1000 * 20;

        static int MAX_NODE_INACTIVITY_MILLIS = 1000 * 60;
        
        static int JOB_CHEAP_THRESHOLD = 10000000;
        
        static int JOB_AVERAGE_THRESHOLD = 1000000000;
        
    }
    
    public volatile static boolean RUNNING = false;

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
        NetworkListener networkListener = new NetworkListener("LoadBalancer", "0.0.0.0", config.getInt(Property.PORT,Default.PORT));
        networkListener.getTransport().setServerConnectionBackLog(config.getInt(Property.MAX_REQUESTS,Default.MAX_REQUESTS));
        networkListener.getTransport().getWorkerThreadPoolConfig().setCorePoolSize((config.getInt(Property.MAX_REQUESTS,Default.MAX_REQUESTS)));
        networkListener.getTransport().getWorkerThreadPoolConfig().setQueueLimit((config.getInt(Property.MAX_REQUESTS,Default.MAX_REQUESTS)));
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
