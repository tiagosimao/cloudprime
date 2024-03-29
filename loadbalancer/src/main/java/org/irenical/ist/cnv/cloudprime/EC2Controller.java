package org.irenical.ist.cnv.cloudprime;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.util.Base64;
import com.amazonaws.util.IOUtils;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class EC2Controller {

    private static final long WAIT_FOR_EC2_READY_POLL_MILLIS = 1000 * 10;

    private final OkHttpClient http;

    private AmazonEC2Client ec2;

    private DynamoDB dynamo;

    private EC2Controller() {
        ec2 = new AmazonEC2Client();
        ec2.setRegion(Region.getRegion(Regions.EU_WEST_1));

        AmazonDynamoDBClient dynamoC = new AmazonDynamoDBClient();
        dynamoC.setRegion(Region.getRegion(Regions.EU_WEST_1));
        dynamo = new DynamoDB(dynamoC);

        http = new OkHttpClient();
    }

    private static EC2Controller instance;

    public static synchronized EC2Controller getInstance() {
        if (instance == null) {
            instance = new EC2Controller();
        }
        return instance;
    }

    public List<CloudprimeNode> getAllNodes() {
        List<CloudprimeNode> all = new LinkedList<>();
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        Filter filter1 = new Filter("tag:Name", Arrays.asList("cloudprime-node"));
        Filter filter2 = new Filter("instance-state-code", Arrays.asList("0", "16"));
        DescribeInstancesResult result = ec2.describeInstances(request.withFilters(filter1, filter2));
        List<Reservation> reservations = result.getReservations();

        for (Reservation r : reservations) {
            for (Instance i : r.getInstances()) {
                try {
                    Optional<CloudprimeNode> got = getNode(i.getInstanceId());
                    if (got.isPresent()) {
                        CloudprimeNode node = got.get();
                        all.add(node);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return all;
    }

    public synchronized void updateNodes(Map<String, CloudprimeNode> current) {
        Set<String> running = new HashSet<>();
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        Filter filter1 = new Filter("tag:Name", Arrays.asList("cloudprime-node"));
        Filter filter2 = new Filter("instance-state-code", Arrays.asList("16"));
        DescribeInstancesResult result = ec2.describeInstances(request.withFilters(filter1, filter2));
        List<Reservation> reservations = result.getReservations();

        for (Reservation r : reservations) {
            for (Instance i : r.getInstances()) {
                try {
                    Optional<CloudprimeNode> got = getNode(i.getInstanceId());
                    if (got.isPresent()) {
                        CloudprimeNode node = got.get();
                        if (node.isReady()) {
                            running.add(node.getId());
                            CloudprimeNode old = current.get(node.getId());
                            if (old == null) {
                                current.put(node.getId(), node);
                            } else {
                                old.setLaunchTime(node.getLaunchTime());
                                old.setPrivateAddress(node.getPrivateAddress());
                                old.setPublicAddress(node.getPublicAddress());
                                old.setReady(node.isReady());
                                updateJobs(node,old);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // removing terminated nodes
        for (String oldId : new HashSet<>(current.keySet())) {
            if (!running.contains(oldId)) {
                current.remove(oldId);
            }
        }
    }

    private void updateJobs(CloudprimeNode from, CloudprimeNode to) {
        List<CloudprimeJob> oldJobs = to.getJobs();
        List<CloudprimeJob> newJobs = from.getJobs();
        for(CloudprimeJob newJob : newJobs){
            for(CloudprimeJob oldJob : oldJobs){
                if(newJob.getNumber().equals(oldJob.getNumber())){
                    oldJob.setElapsed(newJob.getElapsed());
                }
            }
        }
    }

    public void createNode() throws IOException, InterruptedException {

        Instance result = null;

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

        runInstancesRequest.withImageId("ami-e1398992").withKeyName("cloudprime-node").withInstanceType("t2.micro");
        runInstancesRequest.withMinCount(1).withMaxCount(1).withKeyName("tiagosimao-aws");
        runInstancesRequest.withSecurityGroupIds("sg-5e50563a");

        IamInstanceProfileSpecification iamInstanceProfile = new IamInstanceProfileSpecification();
        iamInstanceProfile.withName("cloudprime");
        runInstancesRequest.withIamInstanceProfile(iamInstanceProfile);

        runInstancesRequest.setUserData(loadUserData());

        System.out.println("Running new instance");
        RunInstancesResult runInstances = ec2.runInstances(runInstancesRequest);

        List<Instance> instances = runInstances.getReservation().getInstances();

        for (Instance ec2Instance : instances) {
            CreateTagsRequest createTagsRequest = new CreateTagsRequest();
            createTagsRequest.withResources(ec2Instance.getInstanceId()).withTags(new Tag("Name", "cloudprime-node"));
            ec2.createTags(createTagsRequest);
            result = ec2Instance;
            break;
        }
        waitUntilNodeReady(result.getInstanceId());
    }

    public void destroyNode(String id) {
        TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest();
        terminateRequest.withInstanceIds(id);
        System.out.println("Terminating node " + id);
        ec2.terminateInstances(terminateRequest);
    }

    private void waitUntilNodeReady(String id) throws InterruptedException {
        Optional<CloudprimeNode> node = null;
        boolean isReady = false;
        do {
            System.out.println("New EC2 instance not ready yet, waiting...");
            Thread.sleep(WAIT_FOR_EC2_READY_POLL_MILLIS);
            node = getNode(id);
            isReady = node.isPresent() ? node.get().isReady() : false;
        } while (!isReady);
        System.out.println("New EC2 instance is ready!");
    }

    private Optional<CloudprimeNode> getNode(String id) throws InterruptedException {
        CloudprimeNode node = null;
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        request.setInstanceIds(Arrays.asList(id));
        DescribeInstancesResult result = ec2.describeInstances(request);
        List<Reservation> reservations = result.getReservations();
        for (Reservation r : reservations) {
            for (Instance i : r.getInstances()) {
                node = new CloudprimeNode();
                node.setId(i.getInstanceId());
                node.setLaunchTime(i.getLaunchTime());
                node.setPublicAddress(i.getPublicDnsName());
                node.setPrivateAddress(i.getPrivateDnsName());
                node.setReady(false);
                if (16 == i.getState().getCode()) {
                    ResponseBody body = null;
                    try {
                        Request hr = new Request.Builder().url("http://" + i.getPublicDnsName() + ":8080/status").build();
                        Response response = http.newCall(hr).execute();
                        body = response.body();
                        String got = new String(body.bytes(), "UTF-8");
                        Gson gson = new Gson();
                        CNodeStatus status = gson.fromJson(got, CNodeStatus.class);
                        updateJobs(node, status);
                        node.setReady(response.isSuccessful());
                    } catch (SocketTimeoutException | ConnectException e) {
                        System.out.println("No HTTP connection avaliable for node " + id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (body != null) {
                            body.close();
                        }
                    }
                } else if (0 != i.getState().getCode()) {
                    throw new InterruptedException();
                }
            }
        }
        return Optional.ofNullable(node);
    }

    private void updateJobs(CloudprimeNode node, CNodeStatus status) {
        if (status != null) {
            List<JobProgress> progresses = status.getProgress();
            if (progresses != null) {
                for (JobProgress progress : progresses) {
                    CloudprimeJob job = new CloudprimeJob();
                    job.setNumber(progress.getNumber());
                    job.setElapsed(progress.getInstructionsCounter());
                    node.getJobs().add(job);
                }
            }
        }
    }

    private String loadUserData() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("userdata");
        return Base64.encodeAsString(IOUtils.toByteArray(is));
    }

    public Optional<BigInteger> getNumberCost(BigInteger number) {
        Table table = dynamo.getTable("cloudprime-number");
        Item got = table.getItem("id", number);
        BigInteger is = got == null ? null : got.getBigInteger("cost");
        if (is == null) {
            System.out.println("Can't remember how much this cost: " + number);
        } else {
            System.out.println("I remember how much this cost: " + number + ". It was " + is.toString());
        }
        return Optional.ofNullable(is);
    }

}
