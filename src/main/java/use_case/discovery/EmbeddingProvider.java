package use_case.discovery;

import entity.FeatureVector;
import entity.Photo;

import java.io.IOException;

public interface EmbeddingProvider {

    String modelId();

    FeatureVector embed(Photo photo) throws IOException;
}
