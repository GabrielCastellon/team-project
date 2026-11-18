package data_access;

import static org.junit.jupiter.api.Assertions.*;

import entity.*;

import org.junit.jupiter.api.Test;

import use_case.discovery.EmbeddingProvider;

import java.awt.Color;
import java.awt.image.BufferedImage;

class EmbeddingTest {

    BufferedImage solid(Color color) {
        BufferedImage image = new BufferedImage(16, 8, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 16; x++) {
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    @Test
    void colorFeaturesDistinguishDifferentColors() {
        assertEquals(
                0,
                ColorEmbeddingProvider.extract(solid(Color.RED))
                        .cosine(ColorEmbeddingProvider.extract(solid(Color.BLUE))),
                1e-12);
        assertEquals(
                1,
                ColorEmbeddingProvider.extract(solid(Color.RED))
                        .cosine(ColorEmbeddingProvider.extract(solid(Color.RED))),
                1e-12);
    }

    @Test
    void clipPreprocessingUsesRgbChannelsAndNormalization() {
        float[] values = ClipEmbeddingProvider.preprocess(solid(Color.RED));
        assertEquals(3 * 224 * 224, values.length);
        assertEquals((1 - .48145466) / .26862954, values[0], 1e-5);
        assertEquals(-.4578275 / .26130258, values[224 * 224], 1e-5);
        assertEquals(-.40821073 / .27577711, values[2 * 224 * 224], 1e-5);
    }

    @Test
    void cachesFeaturesButNotAcrossModelInstances() throws Exception {
        int[] calls = {0};
        EmbeddingProvider fake =
                new EmbeddingProvider() {

                    public String modelId() {
                        return "fake";
                    }

                    public FeatureVector embed(Photo p) {
                        calls[0]++;
                        return new FeatureVector(new double[] {1});
                    }
                };
        Photo p = new Photo("x", "x", "image", "page", "artist", "profile", "download");
        CachedEmbeddingProvider cache = new CachedEmbeddingProvider(fake);
        cache.embed(p);
        cache.embed(p);
        assertEquals(1, calls[0]);
        new CachedEmbeddingProvider(fake).embed(p);
        assertEquals(2, calls[0]);
    }
}
