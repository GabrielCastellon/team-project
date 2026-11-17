package data_access;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import entity.FeatureVector;
import entity.Photo;

import use_case.discovery.EmbeddingProvider;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.Map;

public final class ClipEmbeddingProvider implements EmbeddingProvider, AutoCloseable {

    private static final int SIZE = 224;

    private static final double[] MEAN = {.48145466, .4578275, .40821073};

    private static final double[] STD = {.26862954, .26130258, .27577711};

    private final OrtEnvironment environment = OrtEnvironment.getEnvironment();

    private final OrtSession session;

    private final ImageLoader images;

    public ClipEmbeddingProvider(Path model, ImageLoader images) throws OrtException {
        this.images = images;
        try (OrtSession.SessionOptions options = new OrtSession.SessionOptions()) {
            session = environment.createSession(model.toString(), options);
        }
        if (!session.getInputNames().equals(java.util.Set.of("pixel_values"))
                || !session.getOutputNames().contains("image_embeds")) {
            session.close();
            throw new IllegalArgumentException(
                    "Expected image-only CLIP model: pixel_values -> image_embeds");
        }
    }

    @Override
    public String modelId() {
        return "clip-vit-base-patch32-onnx";
    }

    @Override
    public FeatureVector embed(Photo photo) throws IOException {
        try (OnnxTensor input =
                        OnnxTensor.createTensor(
                                environment,
                                FloatBuffer.wrap(preprocess(images.load(photo.imageUrl()))),
                                new long[] {1, 3, SIZE, SIZE});
                OrtSession.Result output = session.run(Map.of("pixel_values", input))) {
            Object raw =
                    output.get("image_embeds")
                            .orElseThrow(() -> new IOException("Model output missing"))
                            .getValue();
            if (!(raw instanceof float[][])) {
                throw new IOException("Expected CLIP output [1,512]");
            }
            float[][] batch = (float[][]) raw;
            if (batch.length != 1 || batch[0].length != 512) {
                throw new IOException("Expected CLIP output [1,512]");
            }
            double[] vector = new double[512];
            for (int i = 0; i < vector.length; i++) {
                vector[i] = batch[0][i];
            }
            return new FeatureVector(vector);
        } catch (OrtException ex) {
            throw new IOException("CLIP inference failed", ex);
        }
    }

    public static float[] preprocess(BufferedImage source) {
        double scale = SIZE / (double) Math.min(source.getWidth(), source.getHeight());
        int width = Math.max(SIZE, (int) (source.getWidth() * scale));
        int height = Math.max(SIZE, (int) (source.getHeight() * scale));
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        int left = (width - SIZE) / 2, top = (height - SIZE) / 2;
        float[] tensor = new float[3 * SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int rgb = resized.getRGB(left + x, top + y);
                for (int c = 0; c < 3; c++) {
                    int value = rgb >> (16 - 8 * c) & 255;
                    tensor[c * SIZE * SIZE + y * SIZE + x] =
                            (float) ((value / 255.0 - MEAN[c]) / STD[c]);
                }
            }
        }
        return tensor;
    }

    @Override
    public void close() throws OrtException {
        session.close();
    }
}
