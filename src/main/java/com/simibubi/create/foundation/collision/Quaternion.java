package com.simibubi.create.foundation.collision;


import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.text.MessageFormat;
import java.util.Objects;
import javax.annotation.Nonnull;


/**
 * Represents a {@code Quaternion} (Hamilton's hypercomplex numbers)
 * <p>
 * We use quaternions for smooth rotations, cleaner code, and more efficient algorithms compared to
 * matrices and Euler angles.  There are lots of generic benefits, but unique ones that push Create
 * to use them are the ability to smoothly interpolate when a rigid body is undergoing rotation and
 * translation (as well as simultaneously), and that it is much easier and computationally efficient
 * for accumulating angular velocity.
 */
class Quaternion {

  static final         Quaternion ZERO               = new Quaternion(0.0D, 0.0D, 0.0D, 0.0D);
  // See Padé approximant.  This value is tuned to doubles.
  private static final double     HALF_ULP_THRESHOLD = 2.107342e-08;
  private static final double     HALF_ONE_DOUBLE    = 0.5D;
  // Scalar component (w)
  private final        double     w;
  // Vector component
  private final        double     x;
  private final        double     y;
  private final        double     z;

  /**
   * Instantiates a {@code new} unit quaternion.
   */
  Quaternion() {
    w = 1.0D;
    x = 0.0D;
    y = 0.0D;
    z = 0.0D;
  }

  /**
   * Instantiates a {@code new} Quaternion.
   *
   * @param scalar the scalar component of {@code this} quaternion
   * @param x the x component of {@code this} quaternion
   * @param y the y component of {@code this} quaternion
   * @param z the z component of {@code this} quaternion
   */
  Quaternion(double scalar, double x, double y, double z) {
    w = scalar;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Instantiates a {@code new} Quaternion.
   *
   * @param scalar the scalar component of {@code this} quaternion
   * @param vector the vector component of {@code this} quaternion
   */
  Quaternion(double scalar, @Nonnull Vec3d vector) {
    w = scalar;
    x = vector.getX();
    y = vector.getY();
    z = vector.getZ();
  }

  /**
   * Instantiates a {@code new} Quaternion from a vector.
   * <p>
   * Aka, expressing a vector as a quaternion.
   *
   * @param vector the vector
   */
  private Quaternion(@Nonnull Vec3d vector) {
    w = 1.0D;
    x = vector.getX();
    y = vector.getY();
    z = vector.getZ();
  }

  @Nonnull
  static Quaternion realQuaternion(double scalar) {
    return new Quaternion(scalar, 0.0D, 0.0D, 0.0D);
  }

  @Nonnull
  private static Quaternion eulerAnglesToQuaternion(
      double roll, double pitch, double yaw
                                                   ) {
    double cosRoll, cosPitch, cosYaw, sinRoll, sinPitch, sinYaw;
    cosRoll = Math.cos(HALF_ONE_DOUBLE * roll);
    cosPitch = Math.cos(HALF_ONE_DOUBLE * pitch);
    cosYaw = Math.cos(HALF_ONE_DOUBLE * yaw);
    sinRoll = sin(HALF_ONE_DOUBLE * roll);
    sinPitch = sin(HALF_ONE_DOUBLE * pitch);
    sinYaw = sin(HALF_ONE_DOUBLE * yaw);

    double cosPitchCosYaw = cosPitch * cosYaw;
    double sinPitchSinYaw = sinPitch * sinYaw;

    return new Quaternion(
        (cosRoll * cosPitchCosYaw) + (sinRoll * sinPitchSinYaw),
        (sinRoll * cosPitchCosYaw) - (cosRoll * sinPitchSinYaw),
        (cosRoll * sinPitch * cosYaw) + (sinRoll * cosPitch * sinYaw),
        (cosRoll * cosPitch * sinYaw) - (sinRoll * sinPitch * cosYaw)
    );
  }

  /**
   * Normalizes {@code this} quaternion.
   * <p>
   * Mutates the quaternion argument.
   *
   * @param quaternion the  to be normalized
   *
   * @return a {@code new} normalized
   *
   * @see #normalized()
   */
  @Nonnull
  static Quaternion normalized(@Nonnull Quaternion quaternion) {
    return quaternion.normalized();
  }

  /**
   * Normalizes {@code this} quaternion.
   * <p>
   * This implementation uses Padé approximant first, but if the steps are too large, it falls back
   * to the standard, but less efficient method that contains a {@link Math#sqrt(double a)}.
   * <p>
   * This makes it efficient enough to call to reduce magnitude and phase errors such as drift
   * introduced by floating-point errors.
   * <p>
   * Note that normalization of a quaternion maintains its orientation but reduces its magnitude to
   * 1.0.
   *
   * @return {@code new} normalized Quaternion
   */
  @Nonnull
  final Quaternion normalized() {
    double magnitudeSquared = lengthSquared();
    Quaternion tempQuaternion;

    // Requires small steps to stay within 0-2 or the more computationally expensive Math.sqrt is
    //called.
    tempQuaternion = Math.abs(1.0D - magnitudeSquared) < Quaternion.HALF_ULP_THRESHOLD ? multiply(
        2.0D / (1.0D + magnitudeSquared)) : multiply(1.0D / sqrt(magnitudeSquared));

    return new Quaternion(tempQuaternion.getW(), -tempQuaternion.getX(), -tempQuaternion.getY(),
                          -tempQuaternion.getZ()
    );
  }

  double lengthSquared() {
    return magnitudeSquared();
  }

  /**
   * Multiply {@code this} quaternion by a scalar.
   *
   * @param factor the factor to multiply {@code this} quaternion by
   *
   * @return a {@code new} Quaternion
   */
  @Nonnull
  final Quaternion multiply(double factor) {
    return new Quaternion(getW() * factor, getX() * factor, getY() * factor, getZ() * factor);
  }

  /**
   * Get the scalar component of {@code this} quaternion.
   *
   * @return {@link #w} the scalar component
   */
  final double getW() {
    return w;
  }

  /**
   * Get the {@link #x} component of {@code this} quaternion's vector component.
   *
   * @return {@link #x} vector component
   */
  final double getX() {
    return x;
  }

  /**
   * Get the {@link #y} component of {@code this} quaternion's vector component.
   *
   * @return {@link #y} vector component
   */
  final double getY() {
    return y;
  }

  /**
   * Get the {@link #z} component of {@code this} quaternion's vector component.
   *
   * @return {@link #z} vector component
   */
  final double getZ() {
    return z;
  }

  private double magnitudeSquared() {
    return getW() * getW() + getX() * getX() + getY() * getY() + getZ() * getZ();
  }

  @Nonnull
  static Quaternion rotationQuaternion(@Nonnull Vec3d rotationAxis, double degrees) {
    // degrees are oriented toward reducing cognitive load of using the method
    // normally this should be radians/2 for total equation and consumers get half of the degrees
    // expected
    double radians = Math.toRadians(degrees);
    Vec3d vectorComponent = rotationAxis.multiply(sin(radians));
    return new Quaternion(Math.cos(radians), vectorComponent);
  }

  @Nonnull
  private static Vec3d multiply(@Nonnull Vec3d part) {
    Quaternion identityQuaternion = new Quaternion();
    Quaternion pureQuaternion = new Quaternion(0, part.getX(), part.getY(), part.getZ());
    Quaternion result = identityQuaternion.multiply(pureQuaternion);
    return new Vec3d(result.getX(), result.getY(), result.getZ());
  }

  /**
   * Get the cross-product {@code this} quaternion with another quaternion.
   * <p>
   * Aka {@code this} quaternion X quaternion
   *
   * @param quaternion the quaternion to cross with {@code this} quaternion
   *
   * @return the {@code new} Quaternion
   */
  @Nonnull
  final Quaternion multiply(@Nonnull Quaternion quaternion) {
    double w1 = getW();
    double x1 = getX();
    double y1 = getY();
    double z1 = getZ();

    double w2 = quaternion.getW();
    double x2 = quaternion.getX();
    double y2 = quaternion.getY();
    double z2 = quaternion.getZ();

    // cross product - dot product...
    // (w1 * w2) - ([x1, y1, z1]·[x2, y2, z2])
    double scalar = (w1 * w2) - ((x1 * x2) + (y1 * y2) + (z1 * z2));
    double x3 = (w1 * x2) + (w2 * x1) + (y1 * z2) - (z1 * y2);
    double y3 = (w1 * y2) + (w2 * y1) - (x1 * z2) + (z1 * x2);
    double z3 = (w1 * z2) + (w2 * z1) + (x1 * y2) - (y1 * x2);
    return new Quaternion(scalar, x3, y3, z3);
  }

  /**
   * Finds the inverse of a normalized quaternion (aka a unit quaternion).
   * <p>
   * Pre-condition:  {@code this} quaternion is already normalized.
   *
   * @return {@code new} inverse
   */
  @Nonnull
  final Quaternion inverse() {
    Quaternion conjugate = conjugate();
    double lengthSquared = lengthSquared();
    return conjugate.multiply(1.0D / lengthSquared);
  }

  /**
   * Finds the length of {@code this} quaternion.
   *
   * @return length length of {@code this} quaternion
   *
   * @see #magnitude()
   */
  final double length() {
    return magnitude();
  }

  /**
   * Finds the magnitude of {@code this} quaternion.
   *
   * @return magnitude magnitude of {@code this} quaternion
   */
  final double magnitude() {
    // |q| = sqrt(w2 + v·v)
    // Given the dot product of a vector with itself does not change magnitude of the vector, the
    // dot product will
    // be the square of its magnitude.
    return sqrt(w * w + x * x + y * y + z * z);
  }

  /**
   * Add another quaternion to {@code this} quaternion.
   *
   * @param quaternion to add to {@code this} quaternion
   *
   * @return a {@code new}
   */
  @Nonnull
  final Quaternion add(@Nonnull Quaternion quaternion) {
    return new Quaternion(
        getW() + quaternion.getW(),
        getX() + quaternion.getX(),
        getY() + quaternion.getY(),
        getZ() + quaternion.getZ()
    );
  }

  /**
   * Dot product of two quaternions.
   *
   * @param quaternion the quaternion
   *
   * @return {@code double} the dot product of the two s
   */
  double dotProduct(@Nonnull Quaternion quaternion) {
    return getW() * quaternion.getW() + (getX() * quaternion.getX()) + (getY() * quaternion.getY())
           + (
               getZ()
               * quaternion.getZ()
           );
  }

  /**
   * Finds the conjugate of a {@code this} quaternion.
   *
   * @return {@code new} Quaternion that is the conjugate of {@code this} quaternion
   */
  @Nonnull
  Quaternion conjugate() {
    return new Quaternion(getW(), -getX(), -getY(), -getZ());
  }

  @Nonnull
  private Quaternion relativePrecisionError(@Nonnull Quaternion quaternion) {
    Quaternion subExpression = subtract(quaternion).multiply(-1.0D);
    Quaternion precisionError = subExpression.add(1.0D);
    return Quaternion.absoluteValue(precisionError);
  }

  /**
   * Subtract another quaternion from {@code this} quaternion.
   *
   * @param quaternion to subtract from {@code this} quaternion
   *
   * @return a {@code new}
   */
  @Nonnull
  Quaternion subtract(@Nonnull Quaternion quaternion) {
    return new Quaternion(
        getW() - quaternion.getW(),
        getX() - quaternion.getX(),
        getY() - quaternion.getY(),
        getZ() - quaternion.getZ()
    );
  }

  @Nonnull
  private Quaternion add(double scalar) {
    return new Quaternion(getW() + scalar, getX() + scalar, getY() + scalar, getZ() + scalar);
  }

  @Nonnull
  private static Quaternion absoluteValue(@Nonnull Quaternion quaternion) {
    return new Quaternion(Math.abs(quaternion.getW()), Math.abs(quaternion.getX()), Math.abs(
        quaternion.getY()),
                          Math.abs(quaternion.getZ())
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(getW(), getX(), getY(), getZ());
  }

  /**
   * Determines if {@code this} quaternion is equal to another quaternion.
   *
   * @param o Object with which to determine equality
   *
   * @return boolean equality of two quaternions
   *
   * @throws ClassCastException if o is not a {@code Quaternion}
   */
  public boolean equals(Object o) {
    return o instanceof Quaternion && isEqual(((Quaternion) o));
  }

  @Nonnull
  @Override
  public String toString() {
    return MessageFormat
               .format(
                   "Quaternion'{' w={0}, x={1}, y={2}, z={3}'}'", getW(), getX(), getY(), getZ());
  }

  @Nonnull
  private Quaternion floor() {
    return new Quaternion(Math.floor(getW()), Math.floor(getX()), Math.floor(getY()), Math.floor(
        getZ()));
  }

  boolean isReal() {
    return getW() != 0.0D && getX() == 0.0D && getY() == 0.0D && getZ() == 0.0D;
  }

  boolean isPure() {
    return getW() == 0.0D && !isZero();
  }

  private boolean isZero() {
    return equals(Quaternion.ZERO);
  }

  private boolean isEqual(@Nonnull Quaternion quaternion) {
    boolean isWEqual = ((Double) getW()).equals(quaternion.getW());
    boolean isXEqual = ((Double) getX()).equals(quaternion.getX());
    boolean isYEqual = ((Double) getY()).equals(quaternion.getY());
    boolean isZEqual = ((Double) getZ()).equals(quaternion.getZ());

    boolean isVecComponentEqual = isWEqual && isXEqual && isYEqual && isZEqual;
    return equalsEpsilon(quaternion) || isVecComponentEqual;
  }

  private boolean equalsEpsilon(@Nonnull Quaternion quaternion) {
    boolean isEqual = false;
    boolean isWEqual = MathHelper.isEpsilonEqual(getW(), quaternion.getW());
    if (isWEqual) {
      boolean isXEqual = MathHelper.isEpsilonEqual(getX(), quaternion.getX());
      if (isXEqual) {
        boolean isYEqual = MathHelper.isEpsilonEqual(getY(), quaternion.getY());
        if (isYEqual) {
          isEqual = MathHelper.isEpsilonEqual(getZ(), quaternion.getZ());
        }
      }
    }
    return isEqual;
  }

  /**
   * Determines if {@code this} quaternion is a unit quaternion (aka normalized) .
   * <p>
   * Unit quaternions are identity quaternions.
   *
   * @return boolean whether {@code this} quaternion is a unit quaternion
   */
  final boolean isUnit() {
    // Because the square root of 1 is 1, we can skip the expensive square root.
    return ((Double) lengthSquared()).equals(1.0D);
  }

  /**
   * Rotate a {@link Vec3d} by {@code this} quaternion.
   *
   * @param vector the vector to be rotated
   *
   * @return {@link Vec3d} a {@code new} rotated {@link Vec3d}
   */
  Vec3d rotate(Vec3d vector) {
    Quaternion normalized = normalized();
    Quaternion vectorQuaternion = new Quaternion(0, vector.getX(), vector.getY(), vector.getZ());
    Quaternion newPureQuaternion = normalized.multiply(vectorQuaternion)
                                             .multiply(normalized.inverse());
    return new Vec3d(newPureQuaternion.getX(), newPureQuaternion.getY(), newPureQuaternion.getZ());
  }

}
