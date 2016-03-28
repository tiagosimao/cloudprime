package org.irenical.ist.cnv.cloudprime.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stats {

  private static final Logger LOG = LoggerFactory.getLogger(Stats.class);

  private static long i_count = 0, b_count = 0, m_count = 0;

  public static synchronized void printICount(String foo) {
    LOG.error(i_count + " instructions in " + b_count + " basic blocks were executed in " + m_count + " methods.");
  }

  public static synchronized void count(int incr) {
    i_count += incr;
    b_count++;
  }

  public static synchronized void mcount(int incr) {
    m_count++;
  }

  public static Usage getStats() {
    Usage result = new Usage();
    result.setIntructionCount(i_count);
    result.setMethodCount(m_count);
    return result;
  }

}
