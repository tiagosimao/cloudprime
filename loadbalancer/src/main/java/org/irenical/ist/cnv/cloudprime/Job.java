package org.irenical.ist.cnv.cloudprime;

import java.math.BigInteger;

public class Job {

    private BigInteger number;

    private Long cost;

    private String result;

    private boolean ongoing;

    public void setCost(Long cost) {
        this.cost = cost;
    }

    public Long getCost() {
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
