package app;

import java.awt.event.HierarchyEvent;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.JPanel;

import data_access.JsonPinDataAccessObject;
import entity.InMemorySessionManager;
import entity.SessionManager;
import entity.User;
import interface_adapter.view_pins.ViewPinsController;
import interface_adapter.view_pins.ViewPinsPresenter;
import use_case.view_pins.ViewPinsInteractor;
import view.MainView;
import view.PinsView;
import view.PlaceholderView;
import data_access.ImageLoader;

/** Composition root. Replace only your feature's factory when integrating a use case. */
public final class ApplicationFactory {
    private final Supplier<JPanel> discoveryFactory;
    private final SessionManager sessionManager;
    private static final Path DEFAULT_PINS_FILE = Path.of("data","pins.json");
    private final Path pinsFile;


    public ApplicationFactory() {
        this(new InMemorySessionManager(), DEFAULT_PINS_FILE);
    }

    // Composition seam for an isolated shell integration test with real discovery wiring.
    ApplicationFactory(SessionManager sessionManager, Path pinsFile) {
        this.sessionManager = sessionManager;
        this.pinsFile = pinsFile;
        this.discoveryFactory = () -> DiscoveryModule.createPanel(sessionManager);
    }

    ApplicationFactory(Supplier<JPanel> discoveryFactory) {
        this.discoveryFactory = java.util.Objects.requireNonNull(discoveryFactory);
        this.sessionManager = new InMemorySessionManager();
        this.pinsFile = DEFAULT_PINS_FILE;
    }

    public MainView createView() {
        Map<String, Supplier<JPanel>> features = new LinkedHashMap<>();
        features.put("Accounts",
                () -> AuthenticationModule.createPanel(sessionManager));
        features.put("Discovery", discoveryFactory);
        features.put("Pins", this::createPinsPanel);
        features.put("Boards",
                () -> BoardsModule.createPanel(sessionManager));
        features.put("Profile", () -> new PlaceholderView("Profile", "Kieran and Matthew"));
        return new MainView(features);
    }

    private JPanel createPinsPanel() {
        //demonstrated control flow
        ImageLoader images = new ImageLoader();
        /*when view asks for a thumbnail, image.load(url) is called; constructor injection;
        factory chooses concrete loader whilst view receives the interface, keeping DA indepedent
        of Swing.
         */
        PinsView view = new PinsView(images::load);
        ViewPinsPresenter presenter = new ViewPinsPresenter(view);
        JsonPinDataAccessObject storage = new JsonPinDataAccessObject(pinsFile);
        ViewPinsInteractor interactor = new ViewPinsInteractor(storage, presenter);
        ViewPinsController controller = new ViewPinsController(interactor);

        //listener runs lookup anytime screen becomes visible
        view.addHierarchyListener(event -> {
            boolean showingChanged = (event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0;
            if (showingChanged && view.isShowing()) {
                User user = sessionManager.getCurrentUser();
                if (user == null) {
                    view.showError("Sign in to view your saved pins");
                } else {
                    controller.execute(user.getId()); //reads current collection following use case
                }
            }
        });
        return view;
    }
}
