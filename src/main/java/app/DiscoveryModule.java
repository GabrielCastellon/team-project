package app;

import data_access.*;
import entity.User;
import interface_adapter.discovery.DiscoveryController;
import interface_adapter.save_pin.SavePinController;
import interface_adapter.save_pin.SavePinViewModel;
import use_case.discovery.DiscoveryService;
import use_case.discovery.EmbeddingProvider;
import use_case.save_pin.SavePinInteractor;
import view.DiscoveryView;
import view.SwingTaskRunner;
import java.nio.file.Path;
import java.util.Map;
import javax.swing.JPanel;
import interface_adapter.save_pin.SavePinPresenter;
import entity.SessionManager;
import entity.InMemorySessionManager;

/** Wires Kevin's module only; the shell remains usable without discovery configuration. */
public final class DiscoveryModule {
    private DiscoveryModule() { }

    public static JPanel createPanel() {
        return createPanel(System.getenv());
    }

    //overloaded panel creation method
    public static JPanel createPanel(SessionManager sessionManager) {
        return createPanel(System.getenv(), sessionManager);
    }

    static JPanel createPanel(Map<String, String> configuration) {
        return createPanel(configuration, new InMemorySessionManager());
    }

    // Explicit configuration keeps composition tests independent of machine credentials.
    static JPanel createPanel(Map<String, String> configuration, SessionManager sessionManager) {
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

            //where pins are stored
            JsonPinDataAccessObject pins = new JsonPinDataAccessObject(Path.of("data",
                    "pins.json"));
            //save pin interactor logic
            SavePinInteractor savePinInteractor = new SavePinInteractor(pins, savePinPresenter);
            //button calls controller with user ID and selected photo
            SavePinController savePinController = new SavePinController(savePinInteractor);

            //.setSaveAction() stores action; when card builds each card save button appears
            //case of signed out, presenter reports message through viewmodel that view listens to
            view.setSaveAction(photo ->
            {
                User user = sessionManager.getCurrentUser();
                if (user == null) {
                    savePinPresenter.prepareFailView("Sign in to save a photo");
                } else {
                    savePinController.execute(user.getId(), photo);
                }
            });

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
