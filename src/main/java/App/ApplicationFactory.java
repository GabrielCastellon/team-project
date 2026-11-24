package App;

import App.AuthenticationModule;
import entity.InMemorySessionManager;
import entity.SessionManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.JPanel;

import view.MainView;
import view.PlaceholderView;

/**
 * Composition root for the Photofy application.
 */
public final class ApplicationFactory {

    // One shared session for the entire application.
    private final SessionManager sessionManager =
            new InMemorySessionManager();

    public MainView createView() {

        Map<String, Supplier<JPanel>> features =
                new LinkedHashMap<>();

        // Matthew's authentication implementation
        features.put("Accounts",
                () -> AuthenticationModule.createPanel(sessionManager));

        features.put("Discovery",
                () -> new PlaceholderView("Discovery", "Kevin"));

        features.put("Pins",
                () -> new PlaceholderView("Pins", "Alex"));

        features.put("Boards",
                () -> new PlaceholderView("Boards", "Gabriel"));

        features.put("Profile",
                () -> new PlaceholderView("Profile", "Kieran and Matthew"));

        return new MainView(features);
    }

    // Allows other application features to access the same session.
    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
