package org.irenical.ist.cnv.cloudprime.stats;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Metric {
    private static int mActiveThreads = 0;
    private static Map<Long, Stat> mStats = new HashMap<Long, Stat>();

    public static synchronized void start(BigInteger bigInteger) {
        mActiveThreads++;
        mStats.put(Thread.currentThread().getId(), new Stat(bigInteger));
        System.out.println(String.format("Factoring: %d (%d/%d active threads)", bigInteger, mActiveThreads, Thread.activeCount()));
    }

    public static synchronized void end() {
        mActiveThreads--;
        Stat stat = mStats.get(Thread.currentThread().getId());
        System.out.println(String.format("Factorization of: %d took %d instructions and %d methods", stat.TargetNumber, stat.InstructionsCounter, stat.MethodCounter));

        // TODO: save the Stats information to a permanent database
    }

    public static synchronized void reportInstructions(int instructions) {
        Stat stat = mStats.get(Thread.currentThread().getId());
        stat.InstructionsCounter += instructions;
        // System.out.println(String.format("#%d Factorization of: %d taking %d instructions", Thread.currentThread().getId(), stat.TargetNumber, stat.InstructionsCounter));
    }

    public static synchronized void reportMethods(String methodName) {
        Stat stat = mStats.get(Thread.currentThread().getId());
        stat.MethodCounter += 1;
        // System.out.println(String.format("Factorization of: %d taking %d methods", stat.TargetNumber, stat.MethodCounter));
    }


    private static class Stat {
        final BigInteger TargetNumber;
        long InstructionsCounter;
        long MethodCounter;

        Stat(BigInteger target) {
            TargetNumber = target;
            InstructionsCounter = 0;
            MethodCounter = 0;
        }
    }
}
