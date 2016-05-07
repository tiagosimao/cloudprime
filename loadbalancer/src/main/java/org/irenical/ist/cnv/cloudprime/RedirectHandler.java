package org.irenical.ist.cnv.cloudprime;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import okhttp3.OkHttpClient;

public class RedirectHandler extends HttpHandler {

    private static final int MIN_NODES = 2;

    private static final int MAX_NODES = 4;

    private static final int MAX_WORK_PER_NODE = 2;

    private static final long EC2_SYNC_POLL_MILLIS = 1000 * 20;

    private static final long MAX_NODE_INACTIVITY_MILLIS = 1000 * 60;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final Map<String, List<String>> runningJobs = new ConcurrentHashMap<>();

    private final Map<String, Long> nodeActivity = new ConcurrentHashMap<>();

    private final OkHttpClient http = new OkHttpClient().newBuilder().connectTimeout(0, TimeUnit.MILLISECONDS).readTimeout(0, TimeUnit.MILLISECONDS).build();

    private List<CloudprimeNode> nodes = Collections.emptyList();

    private boolean running = false;

    private Runnable ec2Sync;

    @Override
    public void destroy() {
        running = false;
        super.destroy();
    }

    @Override
    public void start() {
        try {
            super.start();
            running = true;
            List<CloudprimeNode> got = EC2Controller.getInstance().listNodes();
            nodes = Collections.unmodifiableList(new LinkedList<>(got));
            System.out.println(got.size() + " nodes currently running (min: " + MIN_NODES + ", max: " + MAX_NODES + ")");
            while (got.size() < MIN_NODES) {
                Optional<CloudprimeNode> node = EC2Controller.getInstance().createNode();
                if (node.isPresent()) {
                    got = EC2Controller.getInstance().listNodes();
                } else {
                    throw new Exception("Unable to start needed EC2 node");
                }
            }

            ec2Sync = () -> {
                try {
                    if (running) {
                        List<CloudprimeNode> allNodes = EC2Controller.getInstance().listNodes();
                        nodes = Collections.unmodifiableList(new LinkedList<>(allNodes));
                        if (allNodes.size() > MIN_NODES) {
                            for (CloudprimeNode node : allNodes) {
                                List<String> jobs = runningJobs.get(node.getId());
                                if (node.isReady() && (jobs == null || jobs.isEmpty())) {
                                    Long last = nodeActivity.get(node);
                                    if (last == null || System.currentTimeMillis() - last > MAX_NODE_INACTIVITY_MILLIS) {
                                        EC2Controller.getInstance().destroyNode(node.getId());
                                        break; // only destroys one node at a
                                               // time
                                    }
                                }
                            }
                        }
                        executor.schedule(ec2Sync, EC2_SYNC_POLL_MILLIS, TimeUnit.MILLISECONDS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            executor.schedule(ec2Sync, EC2_SYNC_POLL_MILLIS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        String number = request.getParameter("n");
        System.out.println("Attending request: " + number);
        Optional<CloudprimeNode> node = findAdequateNode(number);
        if (node.isPresent()) {
            String got = relay(node.get(), number);
            if (got != null) {
                response.getWriter().append(got);
            } else {
                response.sendError(500, "Backend error");
            }
        } else {
            response.sendError(503, "No nodes available");
        }
    }

    private String relay(CloudprimeNode node, String number) throws IOException {
        System.out.println("Relaying " + number + " to node " + node.getId());
        List<String> jobs = runningJobs.get(node.getId());
        if (jobs == null) {
            jobs = new CopyOnWriteArrayList<>();
            runningJobs.put(node.getId(), jobs);
        }
        jobs.add(number);
        okhttp3.Response response = null;
        try {
            okhttp3.Request hr = new okhttp3.Request.Builder().url("http://" + node.getPublicAddress() + ":8080/f.html?n=" + number).build();
            response = http.newCall(hr).execute();
            if (response.isSuccessful()) {
                nodeActivity.put(node.getId(), System.currentTimeMillis());
                return new String(response.body().bytes());
            } else {
                return null;
            }
        } finally {
            jobs.remove(number);
            if (response != null) {
                response.body().close();
            }
        }
    }

    private Optional<CloudprimeNode> findAdequateNode(String number) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        try {
            List<CloudprimeNode> all = new LinkedList<>(nodes);
            CloudprimeNode bestFit = null;
            for (CloudprimeNode node : all) {
                if (isAdequate(node)) {
                    bestFit = bestFit(bestFit, node);
                    System.out.println("Best fit for now: " + bestFit);
                }
            }
            if (bestFit != null) {
                return Optional.of(bestFit);
            }
            if (all.size() >= MAX_NODES) {
                return Optional.empty();
            } else {
                return EC2Controller.getInstance().createNode();
            }
        } finally {
            System.out.println("algo took " + (System.currentTimeMillis() - start));
        }
    }

    private boolean isAdequate(CloudprimeNode node) {
        List<String> jobs = runningJobs.get(node.getId());
        return jobs == null || jobs.isEmpty();
    }

    private CloudprimeNode bestFit(CloudprimeNode a, CloudprimeNode b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        List<String> jobsA = runningJobs.get(a.getId());
        List<String> jobsB = runningJobs.get(b.getId());
        long expectedWeightA = expectedWeight(jobsA);
        long expectedWeightB = expectedWeight(jobsB);
        if (expectedWeightA > expectedWeightB) {
            return a;
        } else {
            return b;
        }
    }

    private long expectedWeight(List<String> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return 0;
        } else {
            return new LinkedList<>(jobs).stream().map(n -> expectedWeight(n)).reduce((w1, w2) -> w1 + w2).get();
        }
    }

    private long expectedWeight(String n) {
        BigInteger i = new BigInteger(n);
        Optional<BigInteger> got = EC2Controller.getInstance().getNumberCost(i);
        if (got.isPresent()) {
            return got.get().longValue();
        } else {
            return guess(n);
        }
    }

    private long guess(String n) {
        return n.length();
    }

}
