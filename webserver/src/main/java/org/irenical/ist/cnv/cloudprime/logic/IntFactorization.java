package org.irenical.ist.cnv.cloudprime.logic;

import java.math.BigInteger;
import java.util.ArrayList;

public class IntFactorization {

  private BigInteger zero = new BigInteger("0");
  private BigInteger one = new BigInteger("1");
  private BigInteger divisor = new BigInteger("2");
  private ArrayList<BigInteger> factors = new ArrayList<BigInteger>();

  public ArrayList<BigInteger> calcPrimeFactors(BigInteger num) {

    if (num.compareTo(one) == 0) {
      return factors;
    }

    while (num.remainder(divisor).compareTo(zero) != 0) {
      divisor = divisor.add(one);
    }

    factors.add(divisor);
    return calcPrimeFactors(num.divide(divisor));
  }

}