package com.simibubi.create.foundation.collision;

import net.minecraft.dispenser.IPosition;

import java.util.Objects;

import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

/**
 * Represents an immutable, three dimensional vector with doubles.
 * <p>
 * If the vector represents a normal, it should be normalized.
 */
public final class Vec3d implements IPosition {
    /**
     * The constant ZERO.
     */
    public static final Vec3d ZERO = new Vec3d(0.0D, 0.0D, 0.0D);
    private final double x;
    private final double y;
    private final double z;

    /**
     * Instantiates a new {@link Vec3d}.
     *
     * @param x the x component
     * @param y the y component
     * @param z the z component
     */
    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Length squared double.
     *
     * @return the double
     */
    public final double lengthSquared() {
        return (x * x) + (y * y) + (z * z);
    }

    /**
     * Add a {@link Vec3d} to {@code this} {@link Vec3d}.
     *
     * @param vector the vector to add
     * @return {@link Vec3d} the {@code new} summed {@link Vec3d}
     */
    public final Vec3d add(Vec3d vector) {
        return add(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Add vec 3 d.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @return the vec 3 d
     */
    public final Vec3d add(double x, double y, double z) {
        return new Vec3d(this.x + x, this.y + y, this.z + z);
    }

    public final double getX() {
        return x;
    }

    @Override
    public final double getY() {
        return y;
    }

    @Override
    public final double getZ() {
        return z;
    }

    /**
     * Subtract a {@link Vec3d} from {@code this} {@link Vec3d}.
     *
     * @param vector the vector to subtract
     * @return {@link Vec3d} the {@code new} resulting {@link Vec3d}
     */
    public final Vec3d subtract(Vec3d vector) {
        return add(-vector.getX(), -vector.getY(), -vector.getZ());
    }

    /**
     * Cross product of two {@link Vec3d}s.
     *
     * @param vector the vector being crossed with {@code this} (i.e. {@code this X vector})
     * @return {@link Vec3d} the {@code new} cross product {@link Vec3d}
     */
    public final Vec3d crossProduct(Vec3d vector) {
        return new Vec3d(
                (y * vector.getZ()) - (z * vector.getY()),
                (z * vector.getX()) - (x * vector.getZ()),
                (x * vector.getY()) - y * vector.getX()
        );
    }

    /**
     * Scales a {@link Vec3d} by a factor.
     * <p>
     * Scaling is just a synonym for increasing the magnitude/length of a vector.
     *
     * @param factor the factor to scale by
     * @return {@link Vec3d} the {@code new} scaled {@link Vec3d}
     */
    public final Vec3d scale(double factor) {
        return new Vec3d(x * factor, y * factor, z * factor);
    }

    final Vec3d multiply(double factor) {
        return scale(factor);
    }

    /**
     * Angle double.
     *
     * @param vector the vector
     * @return the double
     */
    public final double angle(Vec3d vector) {
        double vDotProduct = this.dotProduct(vector) / (this.length() * vector.length());
        if (vDotProduct < -1.0D) vDotProduct = -1.0D;
        else if (vDotProduct > 1.0D) vDotProduct = 1.0D;
        return acos(vDotProduct);
    }

    /**
     * Dot product double of two {@link Vec3d}s.
     * <p>
     * — result means the two vectors are pointing away from each other (obtuse angles)
     * + result means the two vectors are pointing toward each other (acute angles)
     *
     * @param vector the vector
     * @return double the dot product of {@code this} {@link Vec3d} and {@code vector}
     */
    public final double dotProduct(Vec3d vector) {
        return (x * vector.getX()) + (y * vector.getY()) + (z * vector.getZ());
    }

    /**
     * Length of {@code this} {@link Vec3d}.
     *
     * @return double the length
     */
    public final double length() {
        return magnitude();
    }

    /**
     * Magnitude of {@code this} {@link Vec3d}.
     *
     * @return double the magnitude
     */
    public final double magnitude() {
        return sqrt(x * x + y * y + z * z);
    }

    /**
     * Normalize a {@link Vec3d}.
     *
     * @param vector the vector to normalize
     * @return {@link Vec3d} the {@code new} normalized {@link Vec3d}
     */
    public final Vec3d normalize(Vec3d vector) {
        double norm = 1.0D / vector.length();
        return new Vec3d(vector.x * norm, vector.y * norm, vector.z * norm);
    }

    /**
     * Normalize a {@link Vec3d}.
     *
     * @return {@link Vec3d} the {@code new} normalized {@link Vec3d}
     */
    public final Vec3d normalize() {
        double norm = 1.0D / length();
        return new Vec3d(x * norm, y * norm, z * norm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        try {
            Vec3d vector = (Vec3d) o;
            return equals(vector);
        } catch (NullPointerException | ClassCastException e1) {
            return false;
        }
    }

    public boolean equals(Vec3d vector) {
        try {
            if (!epsilonEquals(vector)) {
                return x == vector.x && y == vector.y && z == vector.z;
            } else
                return true;
        } catch (NullPointerException e1) {
            return false;
        }
    }

    private boolean epsilonEquals(Vec3d vector) {
        if (!MathHelper.epsilonEquals(x, vector.x)) return false;
        if (!MathHelper.epsilonEquals(y, vector.y)) return false;
        return MathHelper.epsilonEquals(z, vector.z);
    }

    @Override
    public String toString() {
        return "Vec3d{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
