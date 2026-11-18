package app;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.JPanel;
import view.MainView;
import view.PlaceholderView;

/** Composition root. Replace only your feature's factory when integrating a use case. */
public final class ApplicationFactory {
    private final Supplier<JPanel> discoveryFactory;

    public ApplicationFactory() {
        this(DiscoveryModule::createPanel);
    }

    // Composition seam for an isolated shell integration test with real discovery wiring.
    ApplicationFactory(Supplier<JPanel> discoveryFactory) {
        this.discoveryFactory = java.util.Objects.requireNonNull(discoveryFactory);
    }
    public MainView createView() {
        Map<String, Supplier<JPanel>> features = new LinkedHashMap<>();
        features.put("Accounts", () -> new PlaceholderView("Accounts", "Matthew"));
        features.put("Discovery", discoveryFactory);
        features.put("Pins", () -> new PlaceholderView("Pins", "Alex"));
        features.put("Boards", () -> new PlaceholderView("Boards", "Gabriel"));
        features.put("Profile", () -> new PlaceholderView("Profile", "Kieran and Matthew"));
        return new MainView(features);
    }
}
