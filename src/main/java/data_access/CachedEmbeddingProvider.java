package data_access;

import entity.FeatureVector;
import entity.Photo;

import use_case.discovery.EmbeddingProvider;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CachedEmbeddingProvider implements EmbeddingProvider {

    private final EmbeddingProvider delegate;

    private final Map<String, FeatureVector> cache =
            new LinkedHashMap<String, FeatureVector>(64, .75f, true) {

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, FeatureVector> entry) {
                    return size() > 1000;
                }
            };

    public CachedEmbeddingProvider(EmbeddingProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public String modelId() {
        return delegate.modelId();
    }

    @Override
    public synchronized FeatureVector embed(Photo photo) throws IOException {
        String key = photo.id() + "\n" + photo.imageUrl();
        if (!cache.containsKey(key)) {
            cache.put(key, delegate.embed(photo));
        }
        return cache.get(key);
    }
}
