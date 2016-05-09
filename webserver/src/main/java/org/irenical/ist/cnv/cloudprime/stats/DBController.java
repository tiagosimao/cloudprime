package org.irenical.ist.cnv.cloudprime.stats;

import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

public class DBController {
    
    private final ExecutorService exec = Executors.newCachedThreadPool();

    private DynamoDB dynamo;

    private DBController() {
        AmazonDynamoDBClient dynamoC = new AmazonDynamoDBClient();
        dynamoC.setRegion(Region.getRegion(Regions.EU_WEST_1));
        dynamo = new DynamoDB(dynamoC);
    }

    private static DBController instance;

    public static synchronized DBController getInstance() {
        if (instance == null) {
            instance = new DBController();
        }
        return instance;
    }

    public void asyncReport(final BigInteger number, final long instructions, final long methods) {
        exec.execute(new Runnable() {
            
            @Override
            public void run() {
                try{
                    Table table = dynamo.getTable("cloudprime-number");
                    Item item = new Item();
                    item.with("id", number);
                    item.with("cost", instructions);
                    item.with("depth", methods);
                    table.putItem(item);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

}
