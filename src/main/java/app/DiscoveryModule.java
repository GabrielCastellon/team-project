package app;

import data_access.CachedEmbeddingProvider;
import data_access.ClipEmbeddingProvider;
import data_access.ColorEmbeddingProvider;
import data_access.ImageLoader;
import data_access.JsonPreferenceStore;
import data_access.UnsplashCatalog;
import interface_adapter.discovery.DiscoveryController;
import interface_adapter.save_pin.SavePinViewModel;
import use_case.discovery.DiscoveryService;
import use_case.discovery.EmbeddingProvider;
import view.DiscoveryView;
import view.SwingTaskRunner;
import java.nio.file.Path;
import java.util.Map;
import javax.swing.JPanel;
import interface_adapter.save_pin.SavePinPresenter;

/** Wires Kevin's module only; the shell remains usable without discovery configuration. */
public final class DiscoveryModule {
    private DiscoveryModule() { }

    public static JPanel createPanel() {
        return createPanel(System.getenv());
    }

    // Explicit configuration keeps composition tests independent of machine credentials.
    static JPanel createPanel(Map<String, String> configuration) {
        ClipEmbeddingProvider clip = null;
        try {
            // Validate required configuration before allocating optional model resources.
            UnsplashCatalog catalog = new UnsplashCatalog(configuration.get("UNSPLASH_ACCESS_KEY"));
            ImageLoader images = new ImageLoader();
            String modelPath = configuration.get("CLIP_MODEL_PATH");
            EmbeddingProvider provider;
            if (modelPath == null || modelPath.isBlank()) {
                provider = new ColorEmbeddingProvider(images);
            } else {
                clip = new ClipEmbeddingProvider(Path.of(modelPath), images);
                provider = clip;
            }
            EmbeddingProvider embeddings = new CachedEmbeddingProvider(provider);
            String directory = configuration
                    .getOrDefault("DISCOVERY_DATA_DIR", "data/discovery-preferences");
            DiscoveryService service = new DiscoveryService(
                    catalog, embeddings, new JsonPreferenceStore(Path.of(directory)));

            SavePinViewModel savePinViewModel = new SavePinViewModel();
            DiscoveryView view = new DiscoveryView(embeddings.modelId(), savePinViewModel);

            SavePinPresenter savePinPresenter = new SavePinPresenter(savePinViewModel);
            //.setSaveAction() stores action; when card builds each card save button appears
            //case of signed out, presenter reports message through viewmodel that view listens to
            view.setSaveAction(photo -> savePinPresenter.prepareFailView(
                    "Sign in to save a photo"));

            view.setController(new DiscoveryController(
                    service, images::load, view, new SwingTaskRunner<>()));

            if (clip != null) {
                ClipEmbeddingProvider ownedClip = clip;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> close(ownedClip)));
            }
            return view;
        } catch (Exception exception) {
            close(clip);
            throw new IllegalStateException(exception.getMessage(), exception);
        }
    }

    private static void close(ClipEmbeddingProvider clip) {
        if (clip != null) {
            try {
                clip.close();
            } catch (Exception exception) {
                System.err.println("Could not close CLIP session");
            }
        }
    }
}
