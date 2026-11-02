package entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class FeatureVectorTest {

    @Test
    void cosineAndDefensiveCopies() {
        double[] raw = {3, 4};
        FeatureVector a = new FeatureVector(raw);
        raw[0] = 999;
        assertEquals(1, a.cosine(new FeatureVector(new double[] {6, 8})), 1e-12);
        assertEquals(0, a.cosine(new FeatureVector(new double[] {-4, 3})), 1e-12);
        double[] copy = a.values();
        copy[0] = 100;
        assertEquals(.6, a.values()[0], 1e-12);
        assertEquals(-1, a.cosine(new FeatureVector(new double[] {-3, -4})), 1e-12);
    }

    @Test
    void averageNormalizesEachImageAndMixHonorsWeights() {
        FeatureVector a = new FeatureVector(new double[] {100, 0}),
                b = new FeatureVector(new double[] {0, 1});
        assertEquals(
                1,
                FeatureVector.average(List.of(a, b)).cosine(new FeatureVector(new double[] {1, 1})),
                1e-12);
        assertEquals(1, a.mix(b, .7).cosine(new FeatureVector(new double[] {.7, .3})), 1e-12);
        assertEquals(1, a.mix(b, 1).cosine(a), 1e-12);
        assertEquals(1, a.mix(b, 0).cosine(b), 1e-12);
    }

    @Test
    void rejectsInvalidInputsAndCancellation() {
        FeatureVector a = new FeatureVector(new double[] {1, 0});
        assertThrows(IllegalArgumentException.class, () -> new FeatureVector(new double[] {0, 0}));
        assertThrows(
                IllegalArgumentException.class, () -> new FeatureVector(new double[] {Double.NaN}));
        assertThrows(
                IllegalArgumentException.class,
                () -> new FeatureVector(new double[] {Double.POSITIVE_INFINITY}));
        assertThrows(
                IllegalArgumentException.class,
                () -> a.cosine(new FeatureVector(new double[] {1})));
        assertThrows(IllegalArgumentException.class, () -> a.mix(a, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> a.mix(a, 2));
        assertThrows(IllegalArgumentException.class, () -> FeatureVector.average(List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> a.mix(new FeatureVector(new double[] {-1, 0}), .5));
    }

    @Test
    void handlesVeryLargeAndSmallFiniteValues() {
        assertEquals(
                1,
                new FeatureVector(new double[] {Double.MAX_VALUE, Double.MAX_VALUE})
                        .cosine(
                                new FeatureVector(
                                        new double[] {Double.MIN_VALUE, Double.MIN_VALUE})),
                1e-12);
    }
}
