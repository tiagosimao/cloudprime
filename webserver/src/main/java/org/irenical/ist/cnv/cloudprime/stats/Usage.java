package org.irenical.ist.cnv.cloudprime.stats;

public class Usage {

  private long methodCount;

  private long intructionCount;

  public void setIntructionCount(long intructionCount) {
    this.intructionCount = intructionCount;
  }

  public void setMethodCount(long methodCount) {
    this.methodCount = methodCount;
  }

  public long getIntructionCount() {
    return intructionCount;
  }

  public long getMethodCount() {
    return methodCount;
  }

}
