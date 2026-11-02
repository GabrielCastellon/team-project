package app;

import data_access.*;

import interface_adapter.discovery.DiscoveryController;

import use_case.discovery.DiscoveryService;
import use_case.discovery.EmbeddingProvider;

import view.DiscoveryView;

import java.nio.file.Path;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class DiscoveryApplication {

    private DiscoveryApplication() {}

    public static void main(String[] args) {
        try {
            ImageLoader images = new ImageLoader();
            String modelPath = System.getenv("CLIP_MODEL_PATH");
            EmbeddingProvider provider;
            if (modelPath == null || modelPath.isBlank()) {
                provider = new ColorEmbeddingProvider(images);
            } else {
                ClipEmbeddingProvider clip = new ClipEmbeddingProvider(Path.of(modelPath), images);
                provider = clip;
                Runtime.getRuntime()
                        .addShutdownHook(
                                new Thread(
                                        () -> {
                                            try {
                                                clip.close();
                                            } catch (Exception ex) {
                                                System.err.println("Could not close CLIP session");
                                            }
                                        }));
            }
            EmbeddingProvider embeddings = new CachedEmbeddingProvider(provider);
            String data =
                    System.getenv()
                            .getOrDefault("DISCOVERY_DATA_DIR", "data/discovery-preferences");
            DiscoveryService service =
                    new DiscoveryService(
                            new UnsplashCatalog(System.getenv("UNSPLASH_ACCESS_KEY")),
                            embeddings,
                            new JsonPreferenceStore(Path.of(data)));
            SwingUtilities.invokeLater(
                    () -> {
                        DiscoveryView view = new DiscoveryView(embeddings.modelId());
                        view.setController(new DiscoveryController(service, images::load, view));
                        JFrame frame = new JFrame("Image Discovery — Search & Recommendations");
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.setContentPane(view);
                        frame.pack();
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                    });
        } catch (Exception ex) {
            System.err.println("Startup failed: " + ex.getMessage());
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        null, ex.getMessage(), "Startup failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
