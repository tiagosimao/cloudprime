package org.irenical.ist.cnv.cloudprime;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import java.util.function.Consumer;

import BIT.highBIT.BasicBlock;
import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;

public class Instrumentator implements Consumer<File> {

  @SuppressWarnings("unchecked")
  @Override
  public void accept(File dir) {
    System.out.println("Reading " + dir);
    for (File file : dir.listFiles()) {
      if (file.isDirectory()) {
        accept(file);
      } else if (isValidFile(file)) {
        ClassInfo ci = new ClassInfo(file.getAbsolutePath());
        for (Routine routine : (Vector<Routine>) ci.getRoutines()) {
          routine.addBefore("org/irenical/ist/cnv/cloudprime/stats/Stats", "mcount", new Integer(1));

          for (Enumeration<BasicBlock> b = routine.getBasicBlocks().elements(); b.hasMoreElements();) {
            BasicBlock bb = b.nextElement();
            bb.addBefore("org/irenical/ist/cnv/cloudprime/stats/Stats", "count", new Integer(bb.size()));
          }
        }
        ci.addAfter("org/irenical/ist/cnv/cloudprime/stats/Stats", "printICount", ci.getClassName());
        ci.write(file.getAbsolutePath());
      }
    }
  }

  private boolean isValidFile(File file) {
    return file.getParent().endsWith("logic");
  }

}
