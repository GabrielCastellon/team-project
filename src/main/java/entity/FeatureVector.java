package entity;

import java.util.Collection;

public final class FeatureVector {

    private final double[] values;

    public FeatureVector(double[] source) {
        if (source == null || source.length == 0) {
            throw new IllegalArgumentException("Empty vector");
        }
        double scale = 0;
        for (double x : source) {
            if (!Double.isFinite(x)) {
                throw new IllegalArgumentException("Non-finite vector");
            }
            scale = Math.max(scale, Math.abs(x));
        }
        if (scale == 0) {
            throw new IllegalArgumentException("Zero vector has no direction");
        }
        values = source.clone();
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            values[i] /= scale;
            sum += values[i] * values[i];
        }
        double norm = Math.sqrt(sum);
        for (int i = 0; i < values.length; i++) {
            values[i] /= norm;
        }
    }

    public double[] values() {
        return values.clone();
    }

    public double cosine(FeatureVector other) {
        requireSameSize(other);
        double dot = 0;
        for (int i = 0; i < values.length; i++) {
            dot += values[i] * other.values[i];
        }
        return Math.max(-1, Math.min(1, dot));
    }

    public FeatureVector mix(FeatureVector other, double weight) {
        requireSameSize(other);
        if (!Double.isFinite(weight) || weight < 0 || weight > 1) {
            throw new IllegalArgumentException("Weight must be between 0 and 1");
        }
        double[] result = new double[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = weight * values[i] + (1 - weight) * other.values[i];
        }
        return new FeatureVector(result);
    }

    public static FeatureVector average(Collection<FeatureVector> vectors) {
        if (vectors.isEmpty()) {
            throw new IllegalArgumentException("Save at least one preference image first");
        }
        FeatureVector first = vectors.iterator().next();
        double[] result = new double[first.values.length];
        for (FeatureVector vector : vectors) {
            first.requireSameSize(vector);
            for (int i = 0; i < result.length; i++) {
                result[i] += vector.values[i] / vectors.size();
            }
        }
        return new FeatureVector(result);
    }

    private void requireSameSize(FeatureVector other) {
        if (other == null || values.length != other.values.length) {
            throw new IllegalArgumentException("Incompatible vector dimensions");
        }
    }
}
