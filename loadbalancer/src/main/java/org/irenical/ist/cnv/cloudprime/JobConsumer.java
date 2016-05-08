package org.irenical.ist.cnv.cloudprime;

import java.math.BigInteger;
import java.util.Optional;

public class JobConsumer implements Runnable {

    @Override
    public void run() {
        while (LoadBalancer.RUNNING) {
            Job job = null;
            try {
                job = JobController.getInstance().popJob();
                preProcess(job);
                NodeController.getInstance().runJob(job);
                JobController.getInstance().resolveJob(job);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (job != null) {
                        JobController.getInstance().putbackJob(job);
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void preProcess(Job job) {
        job.setCost(expectedWeight(job.getNumber()));
    }

    private long expectedWeight(String n) {
        BigInteger i = new BigInteger(n);
        Optional<BigInteger> got = EC2Controller.getInstance().getNumberCost(i);
        if (got.isPresent()) {
            return got.get().longValue();
        } else {
            return Long.MAX_VALUE;
        }
    }

}
