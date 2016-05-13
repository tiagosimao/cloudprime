package org.irenical.ist.cnv.cloudprime;

import java.util.List;


public class CNodeStatus {
    
    private int totalcapacity;
    
    private int usedcapacity;
    
    private List<JobProgress> progress;
    
    public void setProgress(List<JobProgress> progress) {
        this.progress = progress;
    }
    
    public void setTotalcapacity(int totalcapacity) {
        this.totalcapacity = totalcapacity;
    }
    
    public void setUsedcapacity(int usedcapacity) {
        this.usedcapacity = usedcapacity;
    }
    
    public List<JobProgress> getProgress() {
        return progress;
    }
    
    public int getTotalcapacity() {
        return totalcapacity;
    }
    
    public int getUsedcapacity() {
        return usedcapacity;
    }
    
}
