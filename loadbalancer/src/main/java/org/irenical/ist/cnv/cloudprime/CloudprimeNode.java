package org.irenical.ist.cnv.cloudprime;

import java.util.Date;

public class CloudprimeNode {
    
    private String id;

    private String publicAddress;

    private String privateAddress;

    private boolean ready;
    
    private Date launchTime;

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
    
    @Override
    public String toString() {
        return id + "|" + publicAddress + "|" + privateAddress;
    }

}
