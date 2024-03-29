package org.irenical.ist.cnv.cloudprime;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CloudprimeNode {
    
    private String id;

    private String publicAddress;

    private String privateAddress;

    private boolean ready;
    
    private Date launchTime;
    
    private long lastActivity = System.currentTimeMillis();
    
    private long lastError;
    
    private List<CloudprimeJob> jobs = new CopyOnWriteArrayList<>();

    public void setPrivateAddress(String privateAddress) {
        this.privateAddress = privateAddress;
    }

    public void setPublicAddress(String publicAddress) {
        this.publicAddress = publicAddress;
    }

    public String getPrivateAddress() {
        return privateAddress;
    }

    public String getPublicAddress() {
        return publicAddress;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public void setLaunchTime(Date launchTime) {
        this.launchTime = launchTime;
    }
    
    public Date getLaunchTime() {
        return launchTime;
    }
    
    public List<CloudprimeJob> getJobs() {
        return jobs;
    }
    
    public long getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public void setLastError(long lastError) {
        this.lastError = lastError;
    }
    
    public long getLastError() {
        return lastError;
    }
    
    @Override
    public String toString() {
        String result = id + "[";
        for(CloudprimeJob job : new LinkedList<CloudprimeJob>(getJobs())){
            result += job + ", ";
        }
        return result + "]";
    }
    
}
