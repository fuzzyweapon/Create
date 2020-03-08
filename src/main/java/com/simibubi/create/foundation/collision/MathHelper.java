package com.simibubi.create.foundation.collision;

import static java.lang.Double.isNaN;

public final class MathHelper {

  static final double ABSOLUTE_EPSILON = 1.0E-6;

  private MathHelper() {
  }

  static boolean isEpsilonEqual(double d1, double d2) {
    double difference;

    difference = d1 - d2;
    return isNaN(difference) || (difference < 0 ? -difference : difference) <= ABSOLUTE_EPSILON;
  }

}
