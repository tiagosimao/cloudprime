package org.irenical.ist.cnv.cloudprime.util;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;

public class AWSCompact {
    private static final String INSTANCE_TYPE = "t2.micro";

    private final AmazonEC2Client mAmazon;

    public AWSCompact() {
        /* The ProfileCredentialsProvider will return your [default]
        * credential profile by reading from the credentials file located at
        * (~/.aws/credentials).
        */
        AWSCredentials credentials = null;
        credentials = new ProfileCredentialsProvider().getCredentials();

        mAmazon = new AmazonEC2Client(credentials);
        mAmazon.setEndpoint("ec2.eu-west-1.amazonaws.com");
    }

    public List<Instance> startServers(final int nInstances, String imageID, String keyName, String securityGroup) {

        if (nInstances <= 0)
            throw new IllegalArgumentException("Value needs to be greater than zero.");

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

        runInstancesRequest.withImageId(imageID)
                .withInstanceType(INSTANCE_TYPE)
                .withMinCount(nInstances)
                .withMaxCount(nInstances)
                .withKeyName(keyName)
                .withSecurityGroups(securityGroup);

        RunInstancesResult result = mAmazon.runInstances(runInstancesRequest);

        return result.getReservation().getInstances();
    }


    public void terminateServer(String instanceID) {
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(instanceID);
        mAmazon.terminateInstances(termInstanceReq);
    }

    public List<Instance> getInstances() {
        DescribeInstancesResult describeInstancesRequest = mAmazon.describeInstances();

        List<Reservation> reservations = describeInstancesRequest.getReservations();
        List<Instance> instances = new ArrayList<>();

        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }

        return instances;
    }
}