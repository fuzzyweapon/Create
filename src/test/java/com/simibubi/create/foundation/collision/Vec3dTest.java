package com.simibubi.create.foundation.collision;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static java.lang.Math.sqrt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class Vec3dTest {
    private static final String METHOD_NOT_IMMUTABLE = "the method under test is not immutable";
    private static double SQRT_TWELVE = sqrt(12.0D);

    @Test
    void length() {
        Vec3d v = new Vec3d(2.0D, 2.0D, 2.0D);
        assertEquals(SQRT_TWELVE, v.length());
    }

    @Test
    void lengthSquared() {
        Vec3d v = new Vec3d(2.0D, 2.0D, 2.0D);
        assertEquals(12.0D, v.lengthSquared());
    }

    @Test
    void magnitude() {
        Vec3d v = new Vec3d(2.0D, 2.0D, 2.0D);
        assertEquals(SQRT_TWELVE, v.magnitude());
    }

    @Test
    void add() {
        Vec3d v1 = new Vec3d(2.0D, 2.0D, 2.0D);
        Vec3d v2 = new Vec3d(2.0D, 2.0D, 2.0D);
        Vec3d vExpected = new Vec3d(4.0D, 4.0D, 4.0D);

        assertEquals(vExpected, v1.add(v2));
        assertEquals(v2, v1, METHOD_NOT_IMMUTABLE);
    }

    @Test
    void subtract_this_vec3d_from_itself() {
        Vec3d v1, v2, vExpected;
        v1 = new Vec3d(2.0D, 2.0D, 2.0D);
        v2 = new Vec3d(2.0D, 2.0D, 2.0D);
        vExpected = new Vec3d(0.0D, 0.0D, 0.0D);

        assertEquals(vExpected, v1.subtract(v1));
        assertEquals(v2, v1, METHOD_NOT_IMMUTABLE);
    }

    @Test
    void subtract() {
        Vec3d v1, v2, vExpected;
        v1 = new Vec3d(2.0D, 2.0D, 2.0D);
        v2 = new Vec3d(2.0D, 2.0D, 2.0D);
        vExpected = new Vec3d(0.0D, 0.0D, 0.0D);

        assertEquals(vExpected, v1.subtract(v2));
        assertEquals(v2, v1, METHOD_NOT_IMMUTABLE);
    }

    @Test
    void dotProduct_unit_vector_with_self() {
        Vec3d v1 = new Vec3d(1.0D, 0.0D, 0.0D);
        assertEquals(1.0D, v1.dotProduct(v1));
    }

    @Test
    void dotProduct_non_unit_vector_with_self() {
        Vec3d v1 = new Vec3d(2.0D, 2.0D, 2.0D);
        assertEquals(12.0D, v1.dotProduct(v1));
    }

    @Test
    void dotProduct() {
        Vec3d v1, v2;
        v1 = new Vec3d(2.0D, 2.0D, 2.0D);
        v2 = new Vec3d(3.0D, 2.0D, 2.0D);

        assertEquals(14.0D, v1.dotProduct(v2));
    }

    @Test
    void crossProduct() {
        Vec3d v1, v2, vExpected;
        v1 = new Vec3d(1.0D, 1.0D, 1.0D);
        v2 = new Vec3d(1.0D, 1.0D, 1.0D);
        vExpected = new Vec3d(0.0D, 0.0D, 0.0D);

        assertEquals(vExpected, v1.crossProduct(v2));
        assertEquals(v2, v1, METHOD_NOT_IMMUTABLE);
    }

    @Test
    void scale() {
        double scalar = 2.0D;
        Vec3d v1, v2, vExpected;
        v1 = new Vec3d(2.0D, 2.0D, 2.0D);
        v2 = new Vec3d(2.0D, 2.0D, 2.0D);
        vExpected = new Vec3d(4.0D, 4.0D, 4.0D);

        assertEquals(vExpected, v1.scale(scalar));
        assertEquals(v2, v1, METHOD_NOT_IMMUTABLE);
    }

    @Test
    void angle() {
        Vec3d v1, v2, v3;
        v1 = new Vec3d(1.0D, 0.0D, 0.0D);
        v2 = new Vec3d(0.0D, 1.0D, 0.0D);
        v3 = new Vec3d(1.0D, 0.0D, 0.0D);


        assertEquals(0.5D * Math.PI, v1.angle(v2));
        assertEquals(v3, v1, METHOD_NOT_IMMUTABLE);
    }

    private static Stream<Arguments> provideVec3dsForEquality() {
        double oneThird = 1.0D / 3.0D;
        double oneThirdEps = oneThird + MathHelper.ABSOLUTE_EPSILOND;
        double oneThirdHalfEps = oneThirdEps - (MathHelper.ABSOLUTE_EPSILOND / 2);

        return Stream.of(
                Arguments.of(
                        // input
                        new Vec3d(1.0D, 1.0D, 1.0D),
                        // expected
                        new Vec3d(1.0D, 1.0D, 1.0D)
                ),
                Arguments.of(
                        new Vec3d(-1.0D, -1.0D, -1.0D),
                        new Vec3d(-1.0D, -1.0D, -1.0D)
                ),
                // floating point errors
                Arguments.of(
                        new Vec3d(oneThird, oneThird, oneThird),
                        new Vec3d(oneThirdEps, oneThird, oneThird)
                ),
                Arguments.of(
                        new Vec3d(oneThird, oneThird, oneThird),
                        new Vec3d(oneThird, oneThirdEps, oneThird)
                ),
                Arguments.of(
                        new Vec3d(oneThird, oneThird, oneThird),
                        new Vec3d(oneThird, oneThird, oneThirdEps)
                ),

                Arguments.of(
                        new Vec3d(oneThird, oneThird, oneThird),
                        new Vec3d(oneThirdHalfEps, oneThird, oneThird)
                ),
                Arguments.of(
                        new Vec3d(oneThird, oneThird, oneThird),
                        new Vec3d(oneThird, oneThirdHalfEps, oneThird)
                ),
                Arguments.of(
                        new Vec3d(oneThird, oneThird, oneThird),
                        new Vec3d(oneThird, oneThird, oneThirdHalfEps)
                )
        );
    }

    private static Stream<Arguments> provideVec3dsForNotEquality() {
        double oneThird = 1.0D / 3.0D;
        double oneThirdEps = oneThird + MathHelper.ABSOLUTE_EPSILOND;
        double oneThirdHalfEps = oneThirdEps + (MathHelper.ABSOLUTE_EPSILOND / 2);

        return Stream.of(
                // negative coordinates
                Arguments.of(
                        // input
                        new Vec3d(1.0D, 1.0D, 1.0D),
                        // expected
                        new Vec3d(-1.0D, 1.0D, 1.0D)
                ),
                Arguments.of(
                        new Vec3d(1.0D, 1.0D, 1.0D),
                        new Vec3d(1.0D, -1.0D, 1.0D)
                ),
                Arguments.of(
                        new Vec3d(1.0D, 1.0D, 1.0D),
                        new Vec3d(1.0D, 1.0D, -1.0D)
                ),
                // whole number coordinates
                Arguments.of(
                        new Vec3d(1.0D, 1.0D, 1.0D),
                        new Vec3d(2.0D, 1.0D, 1.0D)
                ),
                Arguments.of(
                        new Vec3d(1.0D, 1.0D, 1.0D),
                        new Vec3d(1.0D, 2.0D, 1.0D)
                ),
                Arguments.of(
                        new Vec3d(1.0D, 1.0D, 1.0D),
                        new Vec3d(1.0D, 1.0D, 2.0D)
                ),
                // floating point errors
                Arguments.of(
                        new Vec3d(oneThird, oneThird, oneThird),
                        new Vec3d(oneThirdHalfEps, 1.0D, 1.0D)
                ),
                Arguments.of(
                        new Vec3d(oneThird, oneThird, oneThird),
                        new Vec3d(1.0D, oneThirdHalfEps, 1.0D)
                ),
                Arguments.of(
                        new Vec3d(oneThird, oneThird, oneThird),
                        new Vec3d(1.0D, 1.0D, oneThirdHalfEps)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("com.simibubi.create.foundation.collision.Vec3dTest#provideVec3dsForEquality")
    void equality(Vec3d v1, Vec3d v2) {
        assertEquals(v1, v2);
    }

    @ParameterizedTest
    @MethodSource("com.simibubi.create.foundation.collision.Vec3dTest#provideVec3dsForNotEquality")
    void notEquality(Vec3d v1, Vec3d v2) {
        assertNotEquals(v1, v2);
    }

    @Test
    void normalize() {
        Vec3d v1, v2, vExpected;
        v1 = new Vec3d(2.0D, 0.0D, 0.0D);
        v2 = new Vec3d(2.0D, 0.0D, 0.0D);
        vExpected = new Vec3d(1.0D, 0.0D, 0.0D);

        assertEquals(vExpected, v1.normalize());
        assertEquals(v2, v1, METHOD_NOT_IMMUTABLE);
    }

    @Test
    void normalize_passed_vec3d_into_this_vec3d() {
        Vec3d v1, v2, v3, vExpected;
        v1 = new Vec3d(2.0D, 3.0D, 2.0D);
        v2 = new Vec3d(0.0D, 5.0D, 0.0D);
        v3 = new Vec3d(2.0D, 3.0D, 2.0D);
        vExpected = new Vec3d(0.0D, 1.0D, 0.0D);

        assertEquals(vExpected, v1.normalize(v2));
        assertEquals(v3, v1, METHOD_NOT_IMMUTABLE);
    }
}