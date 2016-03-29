package org.irenical.ist.cnv.cloudprime.stats;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stats {

  private static final Logger LOG = LoggerFactory.getLogger(Stats.class);

  private static final long REPORT_INTERVAL_MILLIS = 1000;

  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  private static final Runnable reporter = new Runnable() {

    @Override
    public void run() {
      try {
        long now = System.currentTimeMillis();
        if (now - stamp > REPORT_INTERVAL_MILLIS) {
          report(stamp, now);
          stamp = now;
        }
      } catch (Exception e) {
        LOG.error("Reporter iteration failed", e);
      }
    }
  };

  private static volatile long stamp = 0;

  private static volatile AtomicLong instructionCount = new AtomicLong(0);
  private static volatile long b_count = 0;
  private static volatile long m_count = 0;

  public static void start(){
    executor.scheduleAtFixedRate(reporter, 1000, 1000, TimeUnit.MILLISECONDS);
  }
  
  public static void stop(){
    executor.shutdown();
  }

  private static void report(long start, long end) {
    long inst = instructionCount.getAndSet(0);
    long speed = inst/(end-start);
    if(speed>0) {
      LOG.info("Average speed: " + speed + " instructions per millisecond");
    }
  }

  public static synchronized void printICount(String foo) {
    LOG.error(instructionCount.get() + " instructions in " + b_count + " basic blocks were executed in " + m_count + " methods.");
  }

  public static synchronized void count(int incr) {
    instructionCount.incrementAndGet();
    b_count++;
  }

  public static synchronized void mcount(int incr) {
    m_count++;
  }

  public static Usage getStats() {
    Usage result = new Usage();
    result.setIntructionCount(instructionCount.get());
    result.setMethodCount(m_count);
    result.setBlockCount(b_count);
    return result;
  }

}
