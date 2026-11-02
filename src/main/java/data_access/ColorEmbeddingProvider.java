package data_access;

import entity.FeatureVector;
import entity.Photo;

import use_case.discovery.EmbeddingProvider;

import java.awt.image.BufferedImage;
import java.io.IOException;

public final class ColorEmbeddingProvider implements EmbeddingProvider {

    private final ImageLoader images;

    public ColorEmbeddingProvider(ImageLoader images) {
        this.images = images;
    }

    @Override
    public String modelId() {
        return "rgb-histogram-64-v1";
    }

    @Override
    public FeatureVector embed(Photo photo) throws IOException {
        return extract(images.load(photo.imageUrl()));
    }

    public static FeatureVector extract(BufferedImage image) {
        double[] histogram = new double[64];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16 & 255) / 64, g = (rgb >> 8 & 255) / 64, b = (rgb & 255) / 64;
                histogram[16 * r + 4 * g + b]++;
            }
        }
        return new FeatureVector(histogram);
    }
}
