package app;

import data_access.FileAvatarImageLoader;
import data_access.FileSystemAvatarFileChecker;
import data_access.JsonProfileDataAccessObject;
import entity.SessionManager;
import entity.User;
import interface_adapter.profile.EditProfileController;
import interface_adapter.profile.EditProfilePresenter;
import interface_adapter.profile.ProfileViewModel;
import interface_adapter.profile.ViewProfileController;
import interface_adapter.profile.ViewProfilePresenter;
import use_case.edit_profile.EditProfileInteractor;
import use_case.view_profile.ViewProfileInteractor;
import view.ProfileView;

import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Wires Kieran's profile feature (View Profile + Edit Profile). Construction only, no logic. */
public final class ProfileModule {
    static final Path PROFILE_FILE = Path.of("data", "profiles.json");

    private ProfileModule() { }

    public static JPanel createPanel(SessionManager sessionManager) {
        return createPanel(sessionManager, PROFILE_FILE, backgroundExecutor());
    }

    // Composition seam: tests pass their own session, a temp file and a same-thread executor.
    static JPanel createPanel(SessionManager sessionManager, Path profileFile, Executor executor) {
        // one DAO object serves both use cases, each through its own narrow interface
        JsonProfileDataAccessObject profiles = new JsonProfileDataAccessObject(profileFile);
        ProfileViewModel viewModel = new ProfileViewModel();

        ViewProfileInteractor viewProfile =
                new ViewProfileInteractor(profiles, new ViewProfilePresenter(viewModel));
        EditProfileInteractor editProfile = new EditProfileInteractor(
                profiles, new FileSystemAvatarFileChecker(), new EditProfilePresenter(viewModel));

        // The only place Profile reads the session: controllers get just the id (null when signed out)
        Supplier<String> currentUserId = () -> {
            User user = sessionManager.getCurrentUser();
            return user == null ? null : user.getId();
        };

        ProfileView view = new ProfileView(
                new ViewProfileController(viewProfile, currentUserId, executor),
                new EditProfileController(editProfile, currentUserId, executor),
                viewModel,
                new FileAvatarImageLoader()::load);

        // Sign-in, account switch or logout: show the new user's profile (or the sign-in message).
        // The session may notify from any thread, and the view must only be touched on the EDT.
        sessionManager.addSessionListener(() -> SwingUtilities.invokeLater(view::reload));
        return view;
    }

    // Single thread: a load and a save never overlap. Daemon: it never keeps the app open after the window closes.
    private static Executor backgroundExecutor() {
        return Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "profile-io");
            thread.setDaemon(true);
            return thread;
        });
    }
}
