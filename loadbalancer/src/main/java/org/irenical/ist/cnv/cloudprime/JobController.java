package org.irenical.ist.cnv.cloudprime;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class JobController {
    
    private final ExecutorService consumers = Executors.newFixedThreadPool(LoadBalancer.CONSUMER_THREAD_COUNT);

    private final BlockingQueue<Job> jobQueue = new LinkedBlockingQueue<>();
    
    private final List<Job> pending = new CopyOnWriteArrayList<Job>();

    private JobController() {
    }

    private static JobController instance;

    public static synchronized JobController getInstance() {
        if (instance == null) {
            instance = new JobController();
        }
        return instance;
    }
    
    public void start() {
        for(int i=0;i<LoadBalancer.CONSUMER_THREAD_COUNT;++i){
            consumers.submit(new JobConsumer());
        }
    }
    
    public void stop() {
        consumers.shutdown();
    }
    
    public List<Job> list(){
        List<Job> result = new LinkedList<>(jobQueue);
        result.addAll(pending);
        return result;
    }

    public Job submitJob(BigInteger number) throws InterruptedException {
        Job job = new Job();
        job.setNumber(number);
        synchronized (job) {
            jobQueue.put(job);
            System.out.println("new job queued: " + number);
            job.wait();
        }
        return job;
    }

    public Job popJob() throws InterruptedException {
        Job poped = jobQueue.take();
        synchronized (poped) {
            pending.add(poped);
            poped.setOngoing(true);
        }
        return poped;
    }
    
    public void putbackJob(Job job) throws InterruptedException {
        synchronized (job) {
            jobQueue.put(job);
            pending.remove(job);
            System.out.println("retrying job: " + job.getNumber());
        }
    }

    public void resolveJob(Job job) {
        synchronized (job) {
            pending.remove(job);
            job.setOngoing(false);
            job.notifyAll();
        }
    }

}
