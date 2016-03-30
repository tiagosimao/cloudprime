package org.irenical.ist.cnv.cloudprime.stats;

@Deprecated
public class Usage {

  private long methodCount;

  private long intructionCount;
  
  private long blockCount;

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
  
  public void setBlockCount(long blockCount) {
    this.blockCount = blockCount;
  }
  
  public long getBlockCount() {
    return blockCount;
  }

}
