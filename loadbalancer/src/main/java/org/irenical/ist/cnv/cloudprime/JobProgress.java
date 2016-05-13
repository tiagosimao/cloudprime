package org.irenical.ist.cnv.cloudprime;

import java.math.BigInteger;

public class JobProgress {

    private BigInteger number;
    private long instructionsCounter;
    private long methodCounter;

    public void setInstructionsCounter(long instructionsCounter) {
        this.instructionsCounter = instructionsCounter;
    }

    public void setMethodCounter(long methodCounter) {
        this.methodCounter = methodCounter;
    }

    public void setNumber(BigInteger number) {
        this.number = number;
    }

    public long getInstructionsCounter() {
        return instructionsCounter;
    }

    public long getMethodCounter() {
        return methodCounter;
    }

    public BigInteger getNumber() {
        return number;
    }

}
