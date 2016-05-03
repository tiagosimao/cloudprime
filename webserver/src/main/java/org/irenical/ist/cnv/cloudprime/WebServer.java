package org.irenical.ist.cnv.cloudprime;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

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
        if (args != null && args.length > 0) {
            registerNode(args[0]);
        }
        server.start();
    }

    private static void registerNode(String myId) {
        System.getProperties().put("aws.accessKeyId", "AKIAJ6HLHZ2T2CZ6BXAA");
        System.getProperties().put("aws.secretKey", "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");
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(new SystemPropertiesCredentialsProvider());
        client.setRegion(Region.getRegion(Regions.EU_WEST_1));
        DynamoDB db = new DynamoDB(client);
        Table nodes = db.getTable("cloudprime-node");
        Item me = new Item();
        me.withPrimaryKey("id", myId);
        me.withInt("load", 0);
        me.withInt("capacity", Runtime.getRuntime().availableProcessors());
        nodes.putItem(me);
    }

}
