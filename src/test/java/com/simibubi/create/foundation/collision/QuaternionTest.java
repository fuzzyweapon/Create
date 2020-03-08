package com.simibubi.create.foundation.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class QuaternionTest {

  private static final String METHOD_NOT_IMMUTABLE = "the method under test is not immutable";

  private static Stream<Arguments> provideQuaternionsForIsPure() {
    return Stream.of(
        // input
        Arguments.of(new Quaternion(0.0D, 2.0D, 2.0D, 2.0D)),
        Arguments.of(new Quaternion(0.0D, -2.0D, 2.0D, 2.0D)),
        Arguments.of(new Quaternion(0.0D, 2.0D, 2.25D, 2.0D)),
        Arguments.of(new Quaternion(0.0D, 0.0D, 2.0D, 2.0D))
                    );
  }

  private static Stream<Arguments> provideQuaternionsForIsNotPure() {
    return Stream.of(
        // input
        Arguments.of(new Quaternion(1.0D, 0.0D, 0.0D, 0.0D)),
        Arguments.of(new Quaternion(0.25D, -2.0D, 2.0D, 2.0D)),
        Arguments.of(new Quaternion(-1.0D, 0.0D, 0.0D, 0.0D))
                    );
  }

  private static Stream<Arguments> provideQuaternionsForEquality() {
    double oneThird = 1.0D / 3.0D;
    double oneThirdEps = oneThird + MathHelper.ABSOLUTE_EPSILON;
    double oneThirdHalfEps = oneThirdEps - (MathHelper.ABSOLUTE_EPSILON / 2);

    return Stream.of(
        Arguments.of(
            // input
            new Quaternion(1.0D, 1.0D, 1.0D, 1.0D),
            // expected
            new Quaternion(1.0D, 1.0D, 1.0D, 1.0D)
                    ),
        Arguments.of(
            new Quaternion(-1.0D, -1.0D, -1.0D, -1.0D),
            new Quaternion(-1.0D, -1.0D, -1.0D, -1.0D)
                    ),
        // floating-point errors
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(oneThirdEps, oneThird, oneThird, oneThird)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(oneThird, oneThirdEps, oneThird, oneThird)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(oneThird, oneThird, oneThirdEps, oneThird)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(oneThird, oneThird, oneThird, oneThirdEps)
                    ),

        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(oneThirdHalfEps, oneThird, oneThird, oneThird)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(oneThird, oneThirdHalfEps, oneThird, oneThird)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(oneThird, oneThird, oneThirdHalfEps, oneThird)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(oneThird, oneThird, oneThird, oneThirdHalfEps)
                    )
                    );
  }

  private static Stream<Arguments> provideQuaternionsForNotEquality() {
    double oneThird = 1.0D / 3.0D;
    double oneThirdEps = oneThird + MathHelper.ABSOLUTE_EPSILON;
    double oneThirdHalfEps = oneThird + (MathHelper.ABSOLUTE_EPSILON / 2);
    return Stream.of(
        Arguments.of(
            // input
            new Quaternion(1.0D, 1.0D, 1.0D, 1.0D),
            // expected
            new Quaternion(2.0D, 1.0D, 1.0D, 1.0D)
                    ),
        Arguments.of(
            new Quaternion(1.0D, 1.0D, 1.0D, 1.0D),
            new Quaternion(1.0D, 2.0D, 1.0D, 1.0D)
                    ),
        Arguments.of(
            new Quaternion(1.0D, 1.0D, 1.0D, 1.0D),
            new Quaternion(1.0D, 1.0D, 2.0D, 1.0D)
                    ),
        Arguments.of(
            new Quaternion(1.0D, 1.0D, 1.0D, 1.0D),
            new Quaternion(1.0D, 1.0D, 1.0D, 2.0D)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(oneThirdEps, 1.0D, 1.0D, 1.0D)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(1.0D, oneThirdEps, 1.0D, 1.0D)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(1.0D, 1.0D, oneThirdEps, 1.0D)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(1.0D, 1.0D, 1.0D, oneThirdEps)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(oneThirdHalfEps, 1.0D, 1.0D, 1.0D)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(1.0D, oneThirdHalfEps, 1.0D, 1.0D)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(1.0D, 1.0D, oneThirdHalfEps, 1.0D)
                    ),
        Arguments.of(
            new Quaternion(oneThird, oneThird, oneThird, oneThird),
            new Quaternion(1.0D, 1.0D, 1.0D, oneThirdHalfEps)
                    )
                    );
  }

  private static Stream<Arguments> provideInputsForRotate() {
    return Stream.of(
        Arguments.of(
            // input
            new Vec3d(0.0D, 0.0D, 2.0D),
            // input
            new Quaternion(0.0D, 0.0D, 1.0D, 0.0D),
            // expected
            new Vec3d(0.0D, 0.0D, -2.0D)
                    ),

        Arguments.of(
            new Vec3d(0.0D, 0.0D, 2.0D),
            new Quaternion(0.0D, 0.0D, 0.0D, 1.0D),
            new Vec3d(0.0D, 0.0D, 2.0D)
                    ),

        Arguments.of(
            new Vec3d(2.0D, 2.0D, 2.0D),
            new Quaternion(0.0D, -1.0D, 0.0D, 1.0D),
            new Vec3d(-2.0D, -2.0D, -2.0D)
                    ),

        Arguments.of(
            new Vec3d(2.0D, 2.0D, 2.0D),
            new Quaternion(0.0, 1.5D, 0.0D, 0.5D),
            new Vec3d(2.8D, -2.0D, -0.4D)
                    )
                    );
  }

  private static Stream<Arguments> provideInputsForToRotationMatrix() {
    double sinAndCos45 = 0.5D * Math.sqrt(2.0D);
    double twoSinAndCos45 = 2.0D * sinAndCos45;

    return Stream.of(
        Arguments.of(
            // input rotationAxis
            new Vec3d(1.0D, 1.0D, 1.0D),
            // input degrees
            90.0D,
            // expected
            new Quaternion(0.0D, 1.0D, 1.0D, 1.0D)
                    ),
        Arguments.of(
            // input rotationAxis
            new Vec3d(1.0D, 1.0D, 1.0D),
            // input degrees
            180.0D,
            // expected
            new Quaternion(-1.0D, 0.0D, 0.0D, 0.0D)
                    ),
        Arguments.of(
            // input rotationAxis
            new Vec3d(1.0D, 1.0D, 1.0D),
            // input degrees
            45.0D,
            // expected
            new Quaternion(sinAndCos45, sinAndCos45, sinAndCos45, sinAndCos45)
                    ),
        Arguments.of(
            // input rotationAxis
            new Vec3d(2.0D, 2.0D, 2.0D),
            // input degrees
            45.0D,
            // expected
            new Quaternion(sinAndCos45, twoSinAndCos45, twoSinAndCos45, twoSinAndCos45)
                    )
                    );
  }

  private static Stream<Arguments> provideInputsForRotationQuaternion() {
    double sinAndCos45 = 0.5D * Math.sqrt(2.0D);
    double twoSinAndCos45 = 2.0D * sinAndCos45;

    return Stream.of(
        Arguments.of(
            // input rotationAxis
            new Vec3d(1.0D, 1.0D, 1.0D),
            // input degrees
            90.0D,
            // expected
            new Quaternion(0.0D, 1.0D, 1.0D, 1.0D)
                    ),
        Arguments.of(
            // input rotationAxis
            new Vec3d(1.0D, 1.0D, 1.0D),
            // input degrees
            180.0D,
            // expected
            new Quaternion(-1.0D, 0.0D, 0.0D, 0.0D)
                    ),
        Arguments.of(
            // input rotationAxis
            new Vec3d(1.0D, 1.0D, 1.0D),
            // input degrees
            45.0D,
            // expected
            new Quaternion(sinAndCos45, sinAndCos45, sinAndCos45, sinAndCos45)
                    ),
        Arguments.of(
            // input rotationAxis
            new Vec3d(2.0D, 2.0D, 2.0D),
            // input degrees
            45.0D,
            // expected
            new Quaternion(sinAndCos45, twoSinAndCos45, twoSinAndCos45, twoSinAndCos45)
                    )
                    );
  }

  @Test
  void normalize() {
    Quaternion q1, q2, qExpected;
    q1 = new Quaternion(-1.0D, -1.0D, -1.0D, -1.0D);
    q2 = new Quaternion(-1.0D, -1.0D, -1.0D, -1.0D);
    qExpected = new Quaternion(-0.5D, 0.5D, 0.5D, 0.5D);

    assertEquals(qExpected, q1.normalized());
    assertEquals(q2, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void normalize_static() {
    Quaternion q1, q2, qExpected;
    q1 = new Quaternion(-1.0D, -1.0D, -1.0D, -1.0D);
    q2 = new Quaternion(-1.0D, -1.0D, -1.0D, -1.0D);
    qExpected = new Quaternion(-0.5D, 0.5D, 0.5D, 0.5D);

    assertEquals(qExpected, Quaternion.normalized(q1));
    assertEquals(q2, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void isReal() {
    Quaternion qReal = new Quaternion(2.0D, 0.0D, 0.0D, 0.0D);
    Quaternion qNotReal = new Quaternion(2.0D, 1.0D, 1.0D, 1.0D);

    assertTrue(qReal.isReal());
    assertFalse(qNotReal.isReal());
  }

  @Test
  void multiply() {
    Quaternion q1, q2, q3, qExpected;
    q1 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    q2 = new Quaternion(2.0D, 2.0D, 2.0D, 2.0D);
    q3 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    qExpected = new Quaternion(-4.0D, 4.0D, 4.0D, 4.0D);

    assertEquals(qExpected, q1.multiply(q2));
    assertEquals(q3, q1, METHOD_NOT_IMMUTABLE);
  }

  @ParameterizedTest
  @MethodSource("com.simibubi.create.foundation.collision"
                + ".QuaternionTest#provideQuaternionsForIsPure")
  void isPure(Quaternion quaternion) {
    assertTrue(quaternion.isPure());
  }

  @ParameterizedTest
  @MethodSource("com.simibubi.create.foundation.collision"
                + ".QuaternionTest#provideQuaternionsForIsNotPure")
  void isNotPure(Quaternion quaternion) {
    assertFalse(quaternion.isPure());
  }

  @Test
  void multiply_two_reals() {
    Quaternion q1, q2, q3, qExpected;
    q1 = new Quaternion(2.0D, 0.0D, 0.0D, 0.0D);
    q2 = new Quaternion(3.0D, 0.0D, 0.0D, 0.0D);
    q3 = new Quaternion(2.0D, 0.0D, 0.0D, 0.0D);
    qExpected = new Quaternion(6.0D, 0.0D, 0.0D, 0.0D);

    assertEquals(qExpected, q1.multiply(q2));
    assertEquals(q3, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void conjugate() {
    Quaternion q1, q2, qExpected;
    q1 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    q2 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    qExpected = new Quaternion(1.0D, -1.0D, -1.0D, -1.0D);

    assertEquals(qExpected, q1.conjugate());
    assertEquals(q2, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void add() {
    Quaternion q1, q2, q3, qExpected;
    q1 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    q2 = new Quaternion(2.0D, 2.0D, 2.0D, 2.0D);
    q3 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    qExpected = new Quaternion(3.0D, 3.0D, 3.0D, 3.0D);

    assertEquals(qExpected, q1.add(q2));
    assertEquals(q3, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void multiply_scalar() {
    Quaternion q1, q2, qExpected;
    double scalar = 2.0D;
    q1 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    q2 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    qExpected = new Quaternion(2.0D, 2.0D, 2.0D, 2.0D);

    assertEquals(qExpected, q1.multiply(scalar));
    assertEquals(q2, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void dotProduct() {
    Quaternion q1 = new Quaternion(2.0D, 2.0D, 2.0D, 2.0D);
    double actual = q1.dotProduct(q1);

    assertEquals(16.0D, actual);
    assert (actual > 0.0D);
  }

  @Test
  void dotProduct_negative() {
    Quaternion q1, q2;
    q1 = new Quaternion(2.0D, 2.0D, 2.0D, 2.0D);
    q2 = new Quaternion(-2.0D, -2.0D, -2.0D, -2.0D);
    double actual = q1.dotProduct(q2);

    assertEquals(-16.0D, actual);
  }

  @Test
  void multiply_two_pures2() {
    Quaternion q1, q2, q3, qExpected;
    q1 = new Quaternion(0.0D, 2.0D, 1.0D, 2.0D);
    q2 = new Quaternion(0.0D, 1.0D, 1.0D, 1.0D);
    q3 = new Quaternion(0.0D, 2.0D, 1.0D, 2.0D);
    qExpected = new Quaternion(-5.0D, -1.0D, 0.0D, 1.0D);

    assertEquals(qExpected, q1.multiply(q2));
    assertEquals(q3, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void multiply_two_pures() {
    Quaternion q1, q2, q3, qExpected;
    q1 = new Quaternion(0.0D, 2.0D, 2.0D, 2.0D);
    q2 = new Quaternion(0.0D, 3.0D, 5.0D, 10.0D);
    q3 = new Quaternion(0.0D, 2.0D, 2.0D, 2.0D);
    qExpected = new Quaternion(-36.0D, 10.0D, -14.0D, 4.0D);

    assertEquals(qExpected, q1.multiply(q2));
    assertEquals(q3, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void inverse() {
    Quaternion q1, q2, qExpected;
    q1 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    q2 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    qExpected = new Quaternion(0.25D, -0.25D, -0.25D, -0.25D);

    assertEquals(qExpected, q1.inverse());
    assertEquals(q2, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void magnitude() {
    Quaternion q1, q2;
    q1 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    q2 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    double dExpected = 2.0D;

    assertEquals(dExpected, q1.magnitude());
    assertEquals(q2, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void length() {
    Quaternion q1, q2;
    q1 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    q2 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    double dExpected = 2.0D;

    assertEquals(dExpected, q1.magnitude());
    assertEquals(q2, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void lengthSquared() {
    Quaternion q1, q2;
    q1 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    q2 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    double dExpected = 4.0D;

    assertEquals(dExpected, q1.lengthSquared());
    assertEquals(q2, q1, METHOD_NOT_IMMUTABLE);
  }

  @Test
  void subtract() {
    Quaternion q1, q2, q3, qExpected;
    q1 = new Quaternion(2.0D, 2.0D, 2.0D, 2.0D);
    q2 = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);
    q3 = new Quaternion(2.0D, 2.0D, 2.0D, 2.0D);
    qExpected = new Quaternion(1.0D, 1.0D, 1.0D, 1.0D);

    assertEquals(qExpected, q1.subtract(q2));
    assertEquals(q3, q1, METHOD_NOT_IMMUTABLE);
  }

  @ParameterizedTest
  @MethodSource("com.simibubi.create.foundation.collision"
                + ".QuaternionTest#provideQuaternionsForEquality")
  void equality(Quaternion quaternion, Quaternion qExpected) {
    assertEquals(qExpected, quaternion);
  }

  @ParameterizedTest
  @MethodSource("com.simibubi.create.foundation.collision"
                + ".QuaternionTest#provideQuaternionsForNotEquality")
  void notEquality(Quaternion quaternion, Quaternion qExpected) {
    assertNotEquals(qExpected, quaternion);
  }

  @Test
  void realQuaternion_from_scalar() {
    double scalar = 2.0D;
    Quaternion qExpected = new Quaternion(scalar, 0.0D, 0.0D, 0.0D);

    assertEquals(qExpected, Quaternion.realQuaternion(scalar));
  }

  @Test
  void zero_constant() {
    assertEquals(Quaternion.ZERO, new Quaternion(0.0D, 0.0D, 0.0D, 0.0D));
  }

  @Test
  void unitVectorDominant() {
    Quaternion unitQ = new Quaternion(0.0D, 0.0D, 0.0D, 1.0D);
    assertTrue(unitQ.isUnit());
  }

  @Test
  void isUnitScalarDominant() {
    Quaternion unitQ = new Quaternion(1.0D, 0.0D, 0.0D, 0.0D);
    assertTrue(unitQ.isUnit());
  }

  @Test
  void isUnit_negatives() {
    Quaternion unitNegScalarQ, unitNegVectorQ;
    unitNegScalarQ = new Quaternion(-1.0D, 0.0D, 0.0D, 0.0D);
    unitNegVectorQ = new Quaternion(0.0D, 0.0D, -1.0D, 0.0D);

    assertTrue(unitNegScalarQ.isUnit());
    assertTrue(unitNegVectorQ.isUnit());
  }

  @Test
  void isNotUnit_scalar_and_vector() {
    Quaternion unitQ = new Quaternion(1.0D, 0.0D, 0.0D, 1.0D);

    assertFalse(unitQ.isUnit());
  }

  @Test
  void isNotUnit_vector() {
    Quaternion nonUnitVectorQ = new Quaternion(0.0D, 0.0D, 2.0D, -1.0D);
    assertFalse(nonUnitVectorQ.isUnit());
  }

  @Test
  void isNotUnit_scalar() {
    Quaternion nonUnitScalarQ = new Quaternion(2.0D, 0.0D, 0.0D, 0.0D);
    assertFalse(nonUnitScalarQ.isUnit());
  }

  @ParameterizedTest
  @MethodSource("com.simibubi.create.foundation.collision"
                + ".QuaternionTest#provideInputsForRotationQuaternion")
  void rotationQuaternion(Vec3d rotationAxis, double degrees, Quaternion qExpected) {
    assertEquals(qExpected, Quaternion.rotationQuaternion(rotationAxis, degrees));
  }

  @ParameterizedTest
  @MethodSource("com.simibubi.create.foundation.collision.QuaternionTest#provideInputsForRotate")
  void rotate(Vec3d vector, Quaternion rotationQ, Vec3d expected) {
    Vec3d vCopy = new Vec3d(vector.getX(), vector.getY(), vector.getZ());
    assertEquals(expected, rotationQ.rotate(vector));
    assertEquals(vCopy, vector, METHOD_NOT_IMMUTABLE);
  }

}