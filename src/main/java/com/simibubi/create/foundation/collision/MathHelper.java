package com.simibubi.create.foundation.collision;

import static java.lang.Double.isNaN;

public class MathHelper {
    static double ABSOLUTE_EPSILOND = 1E-6;

    static boolean epsilonEquals(double d1, double d2) {
        return epsilonEquals(d1, d2, ABSOLUTE_EPSILOND);
    }

    private static boolean epsilonEquals(double d1, double d2, double epsilon) {
        double difference;

        difference = d1 - d2;
        if (isNaN(difference)) return true;
        return (difference < 0 ? -difference : difference) <= epsilon;
    }
}
