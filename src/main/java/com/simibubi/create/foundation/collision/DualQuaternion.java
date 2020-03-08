package com.simibubi.create.foundation.collision;

import java.text.MessageFormat;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@code DualQuaternion}s are used to represent rotation and translation.
 * <p>
 * Quaternions are great!  However, dual-quaternions are better.
 *
 * @see Quaternion for more info.
 * @see <a href="https://www.3dgep.com/understanding-quaternions/">Quaternion Guide</a>
 * @see
 * <a href="https://cs.gmu.edu/~jmlien/teaching/cs451/uploads/Main/dual-quaternion.pdf">Dual-quaternion
 * Primer</a>
 */
public class DualQuaternion {

  public static final  DualQuaternion ZERO                               = new DualQuaternion(Quaternion.ZERO, Quaternion.ZERO);
  private static final double         FLOATING_POINT_PRECISION_TOLERANCE = 0.000001D;
  @Nullable
  private final        Double         w;
  @Nonnull
  private      final   Quaternion     real;
  private       final  Quaternion     dual;

  /**
   * Instantiates a new Dual quaternion.
   */
  public DualQuaternion() {
    real = new Quaternion();
    dual = new Quaternion(0.0D, 0.0D, 0.0D, 0.0D);
    w = null;
  }

  /**
   * Instantiates a new Dual quaternion.
   *
   * @param real the real component - rotation {@link Quaternion}
   * @param dual the dual component
   */
  private DualQuaternion(@Nonnull Quaternion real, Quaternion dual) {
    this.real = Quaternion.normalized(real);
    this.dual = dual;
    w = null;
  }

  /**
   * Instantiates a new Dual quaternion.
   *  @param quaternion the quaternion
   * @param vector the vector
   */
  private DualQuaternion(@Nonnull Quaternion quaternion, @Nonnull Vec3d vector) {
    real = Quaternion.normalized(quaternion);
    Quaternion realsCrossProduct = new Quaternion(0.0D, vector).multiply(real);
    dual = realsCrossProduct.multiply(0.5D);
    w = null;
  }

  @Nonnull
  private DualQuaternion multiply(double factor) {
    return new DualQuaternion(real.multiply(factor), dual.multiply(factor));
  }

  @Nonnull
  private DualQuaternion multiply(@Nonnull DualQuaternion dualQuaternion) {
    // Multiply left to right
    return new DualQuaternion(
        dualQuaternion.getReal().multiply(real),
        dualQuaternion.getDual().multiply(real).add(
            dualQuaternion.getReal().multiply(dual))
    );
  }

  @Nonnull
  private DualQuaternion add(@Nonnull DualQuaternion dualQuaternion) {
    return new DualQuaternion(real.add(dualQuaternion.getReal()), dual.add(
        dualQuaternion.getDual()));
  }

  @Nonnull
  private DualQuaternion normalize() {
    double mag = magnitude();
    assert (mag > DualQuaternion.FLOATING_POINT_PRECISION_TOLERANCE);

    double factor = 1.0 / mag;
    return new DualQuaternion(real.multiply(factor), dual.multiply(factor));
  }

  private double magnitude() {
    return real.dotProduct(real);
  }

  private double dotProduct(@Nonnull DualQuaternion quaternion) {
    return real.dotProduct(quaternion.getReal());
  }

  @Nonnull
  private DualQuaternion conjugate() {
    return new DualQuaternion(real.conjugate(), dual.conjugate());
  }

  /**
   * Determines the equality of {@code this} dual quaternion to another.
   *
   * @param o Object with which to determine equality
   *
   * @return boolean equality of two dual quaternions
   *
   * @throws ClassCastException if o is not a {@code Quaternion}
   */
  public boolean equals(Object o) {
    return (o instanceof DualQuaternion) && isEqual(((DualQuaternion) o));
  }

  /**
   *
   */
  private boolean isEqual(@Nonnull DualQuaternion dualQuaternion) {
    return real.equals(dualQuaternion.getReal()) && dual.equals(dualQuaternion.getDual());
  }

  @Nonnull
  private Vec3d extractTranslationVector() {
    Quaternion translation = (dual.multiply(2.0D)).multiply(real.conjugate());
    return new Vec3d(translation.getX(), translation.getY(), translation.getZ());
  }

  private Double getW() {
    return w;
  }

  @Override
  public int hashCode() {
    return Objects.hash(w, real, dual);
  }

  @Nonnull
  @Override
  public String toString() {
    return MessageFormat.format("DualQuaternion'{'w={0}, real={1}, dual={2}'}'", w, real, dual);
  }

  @Nonnull
  private Quaternion getReal() {
    return real;
  }

  private Quaternion getDual() {
    return dual;
  }

}