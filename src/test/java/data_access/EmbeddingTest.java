package data_access;

import static org.junit.jupiter.api.Assertions.*;

import entity.*;

import org.junit.jupiter.api.Test;

import use_case.discovery.EmbeddingProvider;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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
        List<String> calls = new ArrayList<>();
        EmbeddingProvider fake = recordingProvider(calls);
        Photo photo = photo("x", "image");
        CachedEmbeddingProvider cache = new CachedEmbeddingProvider(fake);

        assertSame(cache.embed(photo), cache.embed(photo));
        assertEquals("fake", cache.modelId());
        assertEquals(List.of("image"), calls);

        new CachedEmbeddingProvider(fake).embed(photo);
        assertEquals(List.of("image", "image"), calls);
    }

    @Test
    void reusesRecentlyViewedImagesAndRecomputesEvictedImages() throws Exception {
        List<String> calls = new ArrayList<>();
        CachedEmbeddingProvider cache = new CachedEmbeddingProvider(recordingProvider(calls), 2);
        Photo a = photo("a", "image-a");
        Photo b = photo("b", "image-b");
        Photo c = photo("c", "image-c");

        FeatureVector first = cache.embed(a);
        FeatureVector second = cache.embed(b);
        assertSame(first, cache.embed(a));
        cache.embed(c);
        assertSame(first, cache.embed(a));
        assertNotSame(second, cache.embed(b));

        assertEquals(List.of("image-a", "image-b", "image-c", "image-b"), calls);
    }

    @Test
    void recalculatesWhenTheImageUrlChangesForTheSamePhotoId() throws Exception {
        List<String> calls = new ArrayList<>();
        CachedEmbeddingProvider cache = new CachedEmbeddingProvider(recordingProvider(calls));
        FeatureVector first = cache.embed(photo("same-id", "old-image"));

        FeatureVector changed = cache.embed(photo("same-id", "new-image"));
        assertNotSame(first, changed);
        assertSame(changed, cache.embed(photo("same-id", "new-image")));
        assertEquals(List.of("old-image", "new-image"), calls);
    }

    private EmbeddingProvider recordingProvider(List<String> calls) {
        return new EmbeddingProvider() {
            @Override
            public String modelId() {
                return "fake";
            }

            @Override
            public FeatureVector embed(Photo photo) {
                calls.add(photo.imageUrl());
                return new FeatureVector(new double[] {1});
            }
        };
    }

    private Photo photo(String id, String imageUrl) {
        return new Photo(id, id, imageUrl, "page", "artist", "profile", "download");
    }
}
