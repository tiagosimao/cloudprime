package org.irenical.ist.cnv.cloudprime.logic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Factorer {

  public static Collection<Long> factor(String number) throws NumberFormatException {
    Long n = Long.parseLong(number);
    Set<Long> result = new HashSet<>();
    long lasting = n;
    for (int i = 2; i <= lasting; ++i) {
      if (lasting % i == 0) {
        result.add((long) i);
        lasting /= i;
        --i;
      }
    }
    return result;
  }

}
