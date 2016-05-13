package org.irenical.ist.cnv.cloudprime.stats;

import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Metric {
    
    private static Logger mLog;
    private static int mActiveThreads = 0;
    private static Map<Long, Metric> mStats = new ConcurrentHashMap<Long, Metric>();
    private static Map<String, Metric> mStatsByNumber = new ConcurrentHashMap<String, Metric>();
    
    volatile BigInteger number;
    volatile long instructionsCounter;
    volatile long methodCounter;
    private Metric(){
    }
    public void setInstructionsCounter(long instructionsCounter) {
        this.instructionsCounter = instructionsCounter;
    }
    public void setMethodCounter(long methodCounter) {
        this.methodCounter = methodCounter;
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
    public void setNumber(BigInteger number) {
        this.number = number;
    }
    

    static {
        mLog = Logger.getLogger(Metric.class.getName());
        try {
            mLog.addHandler(new FileHandler("metrics.log"));
        } catch (IOException e) {
            mLog.log(Level.SEVERE, e.getMessage());
        }
    }

    public static synchronized void start(BigInteger bigInteger) {
        mActiveThreads++;
        Metric stat = new Metric();
        stat.setNumber(bigInteger);
        mStats.put(Thread.currentThread().getId(), stat);
        mStatsByNumber.put(bigInteger.toString(), stat);
        mLog.log(Level.INFO, String.format("Factoring: %d (%d/%d active threads)", bigInteger, mActiveThreads, Thread.activeCount()));
    }

    public static synchronized void end() {
        mActiveThreads--;
        Metric stat = mStats.remove(Thread.currentThread().getId());
        mStatsByNumber.remove(stat.number.toString());
        DynamoController.getInstance().asyncReport(stat.number, stat.instructionsCounter, stat.methodCounter);
    }

    public static synchronized void reportInstructions(int instructions) {
        Metric stat = mStats.get(Thread.currentThread().getId());
        stat.instructionsCounter += instructions;
    }

    public static synchronized void reportMethods(String methodName) {
        Metric stat = mStats.get(Thread.currentThread().getId());
        stat.methodCounter += 1;
    }

    public static int getActiveThreadCount() {
        return mActiveThreads;
    }

    public static List<Metric> getOngoing() {
        return new LinkedList<Metric>(mStatsByNumber.values());
    }

}
