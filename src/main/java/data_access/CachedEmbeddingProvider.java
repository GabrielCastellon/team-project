package data_access;

import entity.FeatureVector;
import entity.Photo;

import use_case.discovery.EmbeddingProvider;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CachedEmbeddingProvider implements EmbeddingProvider {

    private static final int DEFAULT_CAPACITY = 1000;

    private final EmbeddingProvider delegate;

    private final int capacity;

    private final Map<ImageKey, FeatureVector> cache = new LinkedHashMap<>(16, .75f, true);

    public CachedEmbeddingProvider(EmbeddingProvider delegate) {
        this(delegate, DEFAULT_CAPACITY);
    }

    CachedEmbeddingProvider(EmbeddingProvider delegate, int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Cache capacity must be positive");
        }
        this.delegate = delegate;
        this.capacity = capacity;
    }

    @Override
    public String modelId() {
        return delegate.modelId();
    }

    @Override
    public synchronized FeatureVector embed(Photo photo) throws IOException {
        ImageKey key = new ImageKey(photo.id(), photo.imageUrl());
        FeatureVector cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        FeatureVector vector = delegate.embed(photo);
        cache.put(key, vector);
        if (cache.size() > capacity) {
            cache.remove(cache.keySet().iterator().next());
        }
        return vector;
    }

    private record ImageKey(String id, String url) { }
}
