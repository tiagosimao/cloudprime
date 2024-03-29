package org.irenical.ist.cnv.cloudprime;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.irenical.jindy.Config;
import org.irenical.jindy.ConfigFactory;

import okhttp3.OkHttpClient;

public class NodeController {

    private static final Config config = ConfigFactory.getConfig();

    private final OkHttpClient http = new OkHttpClient().newBuilder().connectTimeout(0, TimeUnit.MILLISECONDS).readTimeout(0, TimeUnit.MILLISECONDS).build();

    private final ExecutorService ec2Lancher = Executors.newSingleThreadExecutor();

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private Runnable ec2Sync;

    private final Map<String, CloudprimeNode> NODES = new ConcurrentHashMap<>();

    private boolean pendingEC2Creation = false;

    private NodeController() {
    }

    private static NodeController instance;

    public static synchronized NodeController getInstance() {
        if (instance == null) {
            instance = new NodeController();
        }
        return instance;
    }

    public void start() throws Exception {
        ec2Sync = () -> {
            try {
                if (LoadBalancer.RUNNING) {
                    int minNodes = config.getInt(LoadBalancer.Property.MIN_NODES, LoadBalancer.Default.MIN_NODES);
                    long maxInactivity = config.getInt(LoadBalancer.Property.MAX_NODE_INACTIVITY_MILLIS, LoadBalancer.Default.MAX_NODE_INACTIVITY_MILLIS);
                    EC2Controller.getInstance().updateNodes(NODES);
                    synchronized (NodeController.this) {
                        NodeController.this.notifyAll();
                    }
                    System.out.println("Updating running nodes");
                    for (CloudprimeNode n : NODES.values()) {
                        System.out.println(n);
                    }
                    if (NODES.size() > minNodes) {
                        for (CloudprimeNode node : NODES.values()) {
                            List<CloudprimeJob> jobs = node.getJobs();
                            if (node.isReady() && jobs.isEmpty()) {
                                long last = node.getLastActivity();
                                if (System.currentTimeMillis() - last > maxInactivity) {
                                    EC2Controller.getInstance().destroyNode(node.getId());
                                    break;
                                }
                            }
                        }
                    } else if (NODES.size() < minNodes) {
                        EC2Controller.getInstance().createNode();
                        EC2Controller.getInstance().updateNodes(NODES);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                executor.schedule(ec2Sync, config.getInt(LoadBalancer.Property.EC2_SYNC_POLL_MILLIS, LoadBalancer.Default.EC2_SYNC_POLL_MILLIS), TimeUnit.MILLISECONDS);
            }
        };
        executor.schedule(ec2Sync, 0, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executor.shutdown();
        ec2Lancher.shutdown();
    }

    public List<CloudprimeNode> list(boolean readyOnly) {
        return readyOnly ? new LinkedList<>(NODES.values()) : EC2Controller.getInstance().getAllNodes();
    }

    public void runJob(CloudprimeJob job) throws IOException, InterruptedException {
        CloudprimeNode node = findAdequateNode(job);
        String got = relay(node, job);
        job.setResult(got);
    }

    private CloudprimeNode findAdequateNode(CloudprimeJob job) throws IOException, InterruptedException {
        CloudprimeNode bestFit = null;
        while (bestFit == null) {
            for (CloudprimeNode node : new LinkedList<>(NODES.values())) {
                if (isAdequate(job,node)) {
                    bestFit = bestFit(bestFit, node);
                }
            }
            if (bestFit == null) {
                youRequireMoreVespeneGas();
            } else {
                return bestFit;
            }
        }
        throw new InterruptedException();
    }

    private synchronized void youRequireMoreVespeneGas() throws InterruptedException {
        if (NODES.size() < config.getInt(LoadBalancer.Property.MAX_NODES, LoadBalancer.Default.MAX_NODES) && !pendingEC2Creation) {
            pendingEC2Creation = true;
            ec2Lancher.execute(() -> {
                try {
                    EC2Controller.getInstance().createNode();
                    EC2Controller.getInstance().updateNodes(NODES);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    synchronized (NodeController.this) {
                        NodeController.this.notifyAll();
                    }
                    pendingEC2Creation = false;
                }
            });
        } else {
            wait();
        }
    }

    // the number of jobs should be irrelevant
    private boolean isAdequate(CloudprimeJob newJob, CloudprimeNode node) {
        long pending = expectedWeight(node.getJobs());
        long newCost = newJob.getCost();
        int cheap = config.getInt(LoadBalancer.Property.JOB_CHEAP_THRESHOLD, LoadBalancer.Default.JOB_CHEAP_THRESHOLD);
        int average = config.getInt(LoadBalancer.Property.JOB_AVERAGE_THRESHOLD, LoadBalancer.Default.JOB_AVERAGE_THRESHOLD);
        if (newCost == -1) {
            return pending < average && pending > -1;
        } 
        else if (newCost < cheap) {
            return true;
        }
        else if (pending == -1) {
            return false;
        } else {
            return newCost + pending < average;
        }
    }

    private CloudprimeNode bestFit(CloudprimeNode a, CloudprimeNode b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        List<CloudprimeJob> jobsA = a.getJobs();
        List<CloudprimeJob> jobsB = b.getJobs();
        long expectedWeightA = expectedWeight(jobsA);
        long expectedWeightB = expectedWeight(jobsB);
        if (expectedWeightA == expectedWeightB) {
            if (a.getLastError() == b.getLastError()) {
                if (a.getLastActivity() > b.getLastActivity()) {
                    return b;
                } else {
                    return a;
                }
            } else if (a.getLastError() > b.getLastError()) {
                return b;
            } else {
                return a;
            }
        } else if (expectedWeightA == -1) {
            return b;
        } else if (expectedWeightB == -1) {
            return a;
        } else if (expectedWeightA > expectedWeightB) {
            return b;
        } else {
            return a;
        }
    }

    private long expectedWeight(List<CloudprimeJob> jobs) {
        if (jobs.isEmpty()) {
            return 0;
        } else {
            long soFar = 0;
            for (CloudprimeJob job : jobs) {
                if (job.getCost() == -1) {
                    return -1;
                } else {
                    soFar += job.getCost() - job.getElapsed();
                }
            }
            return soFar;
        }
    }

    private String relay(CloudprimeNode node, CloudprimeJob job) throws IOException {
        System.out.println("Relaying " + job.getNumber() + " to node " + node.getId());
        okhttp3.Response response = null;
        try {
            node.getJobs().add(job);
            okhttp3.Request hr = new okhttp3.Request.Builder().url("http://" + node.getPublicAddress() + ":8080/f.html?n=" + job.getNumber()).build();
            response = http.newCall(hr).execute();
            if (response.isSuccessful()) {
                node.setLastActivity(System.currentTimeMillis());
                return new String(response.body().bytes());
            } else {
                return null;
            }
        } catch (Exception e) {
            node.setLastError(System.currentTimeMillis());
            throw e;
        } finally {
            node.getJobs().remove(job);
            if (response != null) {
                response.body().close();
            }
            synchronized (this) {
                notifyAll();
            }
        }
    }

}
