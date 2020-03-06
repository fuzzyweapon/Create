package com.simibubi.create.foundation.collision;


import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.Objects;

/**
 * Represents a {@link Quaternion} (Hamilton's hypercomplex numbers).
 * <p>
 * We use Quaternions for smooth rotations, cleaner code, and more efficient algorithms compared to
 * matrices and Euler angles.  There are lots of generic benefits, but unique ones that push Create
 * to use them are the ability to smoothly interpolate when a rigid body is undergoing rotation and
 * translation (as well as simultaneously), and that it is much easier and computationally efficient
 * for accumulating angular velocity.
 */
class Quaternion {

  static final Quaternion ZERO = new Quaternion(0.0D, 0.0D, 0.0D, 0.0D);
  // See Padé approximant.  This value is tuned to doubles.
  private static final double HALF_ULP_THRESHOLD = 2.107342e-08;
  // Scalar component (w)
  private final double w;
  // Vector component
  private final double x;
  private final double y;
  private final double z;

  /**
   * Instantiates a new unit Quaternion.
   */
  Quaternion() {
    w = 1.0D;
    x = 0.0D;
    y = 0.0D;
    z = 0.0D;
  }

  /**
   * Instantiates a new Quaternion.
   *
   * @param scalar the scalar
   * @param x the x component of {@code this} {@link Quaternion}
   * @param y the y component of {@code this} {@link Quaternion}
   * @param z the z component of {@code this} {@link Quaternion}
   */
  Quaternion(double scalar, double x, double y, double z) {
    w = scalar;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Instantiates a new Quaternion.
   *
   * @param scalar the scalar component of {@code this} {@link Quaternion}
   * @param vector the vector component of {@code this} {@link Quaternion}
   */
  Quaternion(double scalar, Vec3d vector) {
    w = scalar;
    x = vector.getX();
    y = vector.getY();
    z = vector.getZ();
  }

  /**
   * Instantiates a new Quaternion from a vector.
   * <p>
   * Aka, expressing a vector as a Quaternion.
   *
   * @param vector the vector
   */
  Quaternion(Vec3d vector) {
    w = 1.0D;
    x = vector.getX();
    y = vector.getY();
    z = vector.getZ();
  }

  static Quaternion realQuaternion(double scalar) {
    return new Quaternion(scalar, 0.0D, 0.0D, 0.0D);
  }

  private static Quaternion eulerAnglesToQuaternion(double roll, double pitch, double yaw) {
    double cos_roll, cos_pitch, cos_yaw, sin_roll, sin_pitch, sin_yaw;
    cos_roll = cos(0.5D * roll);
    cos_pitch = cos(0.5D * pitch);
    cos_yaw = cos(0.5D * yaw);
    sin_roll = sin(0.5D * roll);
    sin_pitch = sin(0.5D * pitch);
    sin_yaw = sin(0.5D * yaw);

    double cpcy = cos_pitch * cos_yaw;
    double spsy = sin_pitch * sin_yaw;

    return new Quaternion(
        (cos_roll * cpcy) + (sin_roll * spsy),
        (sin_roll * cpcy) - (cos_roll * spsy),
        (cos_roll * sin_pitch * cos_yaw) + (sin_roll * cos_pitch * sin_yaw),
        (cos_roll * cos_pitch * sin_yaw) - (sin_roll * sin_pitch * cos_yaw)
    );
  }

  /**
   * Normalizes {@code this} {@link Quaternion}.
   * <p>
   * Mutates the quaternion argument.
   *
   * @param real the {@link Quaternion} to be normalized
   * @return real the normalized {@link Quaternion}
   * @see #normalized()
   */
  static Quaternion normalized(Quaternion real) {
    return real.normalized();
  }

  static Quaternion rotationQuaternion(Vec3d rotationAxis, double degrees) {
    // degrees are oriented toward reducing cognitive load of using the method
    // normally this should be radians/2 for total equation and consumers get half of the degrees expected
    double radians = Math.toRadians(degrees);
    return new Quaternion(cos(radians), rotationAxis.multiply(sin(radians)));
  }

  private static Quaternion abs(Quaternion quaternion) {
    return new Quaternion(Math.abs(quaternion.w), Math.abs(quaternion.x), Math.abs(quaternion.y),
        Math.abs(quaternion.z));
  }

  /**
   * Get the scalar component of {@code this} {@link Quaternion}.
   *
   * @return {@link #w} the scalar component
   */
  final double getW() {
    return w;
  }

  /**
   * Get the {@link #x} component of {@code this} {@link Quaternion}'s vector component.
   *
   * @return {@link #x} vector component
   */
  final double getX() {
    return x;
  }

  /**
   * Get the {@link #y} component of {@code this} {@link Quaternion}'s vector component.
   *
   * @return {@link #y} vector component
   */
  final double getY() {
    return y;
  }

  /**
   * Get the {@link #z} component of {@code this} {@link Quaternion}'s vector component.
   *
   * @return {@link #z} vector component
   */
  final double getZ() {
    return z;
  }

  /**
   * Normalizes {@code this} {@link Quaternion}.
   * <p>
   * This implementation uses Padé approximant first, but if the steps are too large, it falls back
   * to the standard, but less efficient method that contains a {@link Math#sqrt(double a)}.
   * <p>
   * This makes it efficient enough to call to reduce magnitude and phase errors such as drift
   * introduced by floating point errors.
   * <p>
   * Note that normalization of a quaternion maintains its orientation but reduces its magnitude to
   * 1.0.
   *
   * @return {@link Quaternion} {@code new} normalized {@link Quaternion}
   */
  final Quaternion normalized() {
    double magnitudeSquared = lengthSquared();
    Quaternion tempQuaternion;

    // Requires small steps to stay within 0-2 or the more computationally expensive Math.sqrt is called.
    if (Math.abs(1.0D - magnitudeSquared) < HALF_ULP_THRESHOLD) {
      tempQuaternion = this.multiply(2.0D / (1.0D + magnitudeSquared));
    } else {
      tempQuaternion = this.multiply(1.0D / sqrt(magnitudeSquared));
    }

    return new Quaternion(tempQuaternion.getW(), -tempQuaternion.getX(), -tempQuaternion.getY(),
        -tempQuaternion.getZ());
  }

  final double lengthSquared() {
    return w * w + x * x + y * y + z * z;
  }

  /**
   * Multiply {@code this} {@link Quaternion} by scalar.
   *
   * @param factor the factor to multiply {@code this} {@link Quaternion} by
   * @return {@link Quaternion} a {@code new} {@link Quaternion} product
   */
  final Quaternion multiply(double factor) {
    return new Quaternion(w * factor, x * factor, y * factor, z * factor);
  }

  /**
   * Finds the inverse of a normalized {@link Quaternion} (aka unit quaternion).
   * <p>
   * Pre-condition:  {@code this} {@link Quaternion} is already normalized.
   *
   * @return {@link Quaternion} new {@link Quaternion} inverse
   */
  final Quaternion inverse() {
    return conjugate().multiply(1.0D / lengthSquared());
  }

  /**
   * Finds the length of {@code this} {@link Quaternion}.
   *
   * @return length length of {@code this} {@link Quaternion}
   * @see #magnitude()
   */
  final double length() {
    return magnitude();
  }

  /**
   * Finds the magnitude of {@code this} {@link Quaternion}.
   *
   * @return magnitude magnitude of {@code this} {@link Quaternion}
   */
  final double magnitude() {
    // |q| = sqrt(w2 + v·v)
    // Given the dot product of a vector with itself does not change magnitude of the vector, the dot product will
    // be the square of its magnitude.
    return sqrt(w * w + x * x + y * y + z * z);
  }

  /**
   * Add {@link Quaternion} to {@code this} {@link Quaternion}.
   *
   * @param quaternion {@link Quaternion} to add to {@code this} {@link Quaternion}
   * @return {@link Quaternion} a {@code new} {@link Quaternion}
   */
  final Quaternion add(Quaternion quaternion) {
    return new Quaternion(
        w + quaternion.getW(),
        x + quaternion.getX(),
        y + quaternion.getY(),
        z + quaternion.getZ()
    );
  }

  /**
   * Subtract {@link Quaternion} from {@code this} {@link Quaternion}.
   *
   * @param quaternion {@link Quaternion} to subtract from {@code this} {@link Quaternion}
   * @return {@link Quaternion} a {@code new} {@link Quaternion}
   */
  final Quaternion subtract(Quaternion quaternion) {
    return new Quaternion(
        w - quaternion.getW(),
        x - quaternion.getX(),
        y - quaternion.getY(),
        z - quaternion.getZ()
    );
  }

  /**
   * Multiplies {@code this} {@link Quaternion} with another {@link Quaternion}.
   *
   * @param quaternion the {@link Quaternion} to multiply with {@code this} {@link Quaternion}
   * @return {@link Quaternion} a {@code new} {@link Quaternion} product
   */
  final Quaternion multiply(Quaternion quaternion) {
    double w1 = w;
    double x1 = x;
    double y1 = y;
    double z1 = z;

    double w2 = quaternion.getW();
    double x2 = quaternion.getX();
    double y2 = quaternion.getY();
    double z2 = quaternion.getZ();

    // cross product - dot product...
    // (w1 * w2) - ([x1, y1, z1]·[x2, y2, z2])
    return new Quaternion(
        (w1 * w2) - ((x1 * x2) + (y1 * y2) + (z1 * z2)),
        (w1 * x2) + (w2 * x1) + (y1 * z2) - (z1 * y2),
        (w1 * y2) + (w2 * y1) - (x1 * z2) + (z1 * x2),
        (w1 * z2) + (w2 * z1) + (x1 * y2) - (y1 * x2)
    );
  }

  /**
   * Dot product of two {@link Quaternion}s.
   *
   * @param quaternion the quaternion
   * @return {@code double} the dot product of the two {@link Quaternion}s
   */
  double dotProduct(Quaternion quaternion) {
    return w * quaternion.getW() + (x * quaternion.getX()) + (y * quaternion.getY()) + (z
        * quaternion.getZ());
  }

  /**
   * Finds the conjugate of a {@link Quaternion}.
   *
   * @return {@link Quaternion} new {@link Quaternion} conjugate
   */
  final Quaternion conjugate() {
    return new Quaternion(w, -x, -y, -z);
  }

  public boolean equals(Object o) {
    try {
      Quaternion q = (Quaternion) o;
      return this.equals(q);
    } catch (NullPointerException | ClassCastException e1) {
      return false;
    }
  }

  public boolean equals(Quaternion quaternion) {
    try {
      if (!epsilonEquals(quaternion)) {
        return (w == quaternion.w && x == quaternion.x && y == quaternion.y && z == quaternion.z);
      } else {
        return true;
      }
    } catch (NullPointerException e1) {
      return false;
    }
  }

  private boolean epsilonEquals(Quaternion quaternion) {
    return epsilonEquals(quaternion, MathHelper.ABSOLUTE_EPSILOND);
  }

  private boolean epsilonEquals(Quaternion quaternion, double epsilon) {
    if (!MathHelper.epsilonEquals(w, quaternion.w)) {
      return false;
    }
    if (!MathHelper.epsilonEquals(x, quaternion.x)) {
      return false;
    }
    if (!MathHelper.epsilonEquals(y, quaternion.y)) {
      return false;
    }
    return MathHelper.epsilonEquals(z, quaternion.z);
  }

  private Quaternion relativeError(Quaternion quaternion) {
    return abs(this.subtract(quaternion).multiply(-1.0D).add(1.0D));
  }

  private Quaternion add(double scalar) {
    return new Quaternion(w + scalar, x + scalar, y + scalar, z + scalar);
  }

  @Override
  public int hashCode() {
    return Objects.hash(w, x, y, z);
  }

  @Override
  public String toString() {
    return "Quaternion{" +
        "w=" + w +
        ", x=" + x +
        ", y=" + y +
        ", z=" + z +
        '}';
  }

  private Quaternion floor() {
    return new Quaternion(Math.floor(w), Math.floor(x), Math.floor(y), Math.floor(z));
  }

  boolean isReal() {
    return w != 0.0D && (x == 0.0D && y == 0.0D && z == 0.0D);
  }

  boolean isPure() {
    return w == 0.0D && (x != 0.0D || y != 0.0D || z != 0.0D);
  }

  /**
   * Determines if {@code this} is a unit {@link Quaternion}.
   * <p>
   * Unit {@link Quaternion}s are identity {@link Quaternion}s.
   *
   * @return the boolean
   */
  boolean isUnit() {
    boolean isVectorUnit = (x == 0.0D && y == 0.0D && Math.abs(z) == 1.0D) ||
        (x == 0.0D && Math.abs(y) == 1.0D && z == 0.0D) ||
        (Math.abs(x) == 1.0D && y == 0.0D && z == 0.0D);

    if (w == 0.0D && isVectorUnit) {
      return true;
    }
    return Math.abs(w) == 1.0D && x == 0.0D && y == 0.0D && z == 0.0D;
  }

  /**
   * Rotate a {@link Vec3d} by {@code this} {@link Quaternion}.
   *
   * @param vector the vector to be rotated
   * @return {@link Vec3d} a {@code new} rotated {@link Vec3d}
   */
  Vec3d rotate(Vec3d vector) {
    Quaternion normalized = this.normalized();
    Quaternion vectorQuaternion = new Quaternion(0, vector.getX(), vector.getY(), vector.getZ());
    Quaternion newPureQuaternion = normalized.multiply(vectorQuaternion)
        .multiply(normalized.inverse());
    return new Vec3d(newPureQuaternion.getX(), newPureQuaternion.getY(), newPureQuaternion.getZ());
  }

  private Vec3d multiply(Vec3d part) {
    Quaternion result = new Quaternion()
        .multiply(new Quaternion(0, part.getX(), part.getY(), part.getZ()));
    return new Vec3d(result.getX(), result.getY(), result.getZ());
  }
}
