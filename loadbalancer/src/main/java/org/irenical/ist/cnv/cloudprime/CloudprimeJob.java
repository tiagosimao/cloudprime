package org.irenical.ist.cnv.cloudprime;

import java.math.BigInteger;

public class CloudprimeJob {

    private BigInteger number;

    private long cost = -1;

    private String result;

    private boolean ongoing;
    
    public void setCost(long cost) {
        this.cost = cost;
    }

    public long getCost() {
        return cost;
    }

    public void setNumber(BigInteger number) {
        this.number = number;
    }

    public BigInteger getNumber() {
        return number;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
    }

    public boolean isOngoing() {
        return ongoing;
    }
    
}
