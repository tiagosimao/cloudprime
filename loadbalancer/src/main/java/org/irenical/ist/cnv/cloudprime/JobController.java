package org.irenical.ist.cnv.cloudprime;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;

public class JobController {

    private static final Config config = ConfigFactory.getConfig();

    private final BlockingQueue<CloudprimeJob> jobQueue = new LinkedBlockingQueue<>();

    private final List<CloudprimeJob> pending = new CopyOnWriteArrayList<CloudprimeJob>();

    private ExecutorService executor = null;

    private List<JobConsumer> consumers = null;

    private JobController() {
    }

    private static JobController instance;

    public static synchronized JobController getInstance() {
        if (instance == null) {
            instance = new JobController();
        }
        return instance;
    }

    public void reload() {
        int threadCount = config.getInt(LoadBalancer.Property.CONSUMER_THREAD_COUNT, LoadBalancer.Default.CONSUMER_THREAD_COUNT);
        ExecutorService oldExecutor = executor;
        List<JobConsumer> oldConsumers = consumers;

        executor = Executors.newFixedThreadPool(threadCount);
        consumers = new CopyOnWriteArrayList<>();
        for (int i = 0; i < threadCount; ++i) {
            JobConsumer consumer = new JobConsumer();
            consumer.setRunning(true);
            consumers.add(consumer);
            executor.submit(consumer);
        }

        if (oldConsumers != null) {
            for (JobConsumer c : oldConsumers) {
                c.setRunning(false);
            }
        }
        if (oldExecutor != null) {
            oldExecutor.shutdown();
        }
    }

    public void stop() {
        for (JobConsumer c : consumers) {
            c.setRunning(false);
        }
        executor.shutdown();
    }

    public List<CloudprimeJob> list() {
        List<CloudprimeJob> result = new LinkedList<>(jobQueue);
        result.addAll(pending);
        return result;
    }

    public CloudprimeJob submitJob(BigInteger number) throws InterruptedException {
        CloudprimeJob job = new CloudprimeJob();
        job.setNumber(number);
        synchronized (job) {
            jobQueue.put(job);
            System.out.println("new job queued: " + number);
            job.wait();
        }
        return job;
    }

    public CloudprimeJob popJob() throws InterruptedException {
        CloudprimeJob poped = jobQueue.take();
        synchronized (poped) {
            pending.add(poped);
            poped.setOngoing(true);
        }
        return poped;
    }

    public void putbackJob(CloudprimeJob job) throws InterruptedException {
        synchronized (job) {
            jobQueue.put(job);
            pending.remove(job);
            System.out.println("retrying job: " + job.getNumber());
        }
    }

    public void resolveJob(CloudprimeJob job) {
        synchronized (job) {
            pending.remove(job);
            job.setOngoing(false);
            job.notifyAll();
        }
    }

}
