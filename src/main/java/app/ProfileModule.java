package app;

import data_access.FileSystemAvatarFileChecker;
import data_access.JsonProfileDataAccessObject;
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
import javax.swing.JPanel;

/** Wires Kieran's profile feature (View Profile + Edit Profile). Construction only, no logic. */
public final class ProfileModule {
    // Placeholder until Matthew's login provides the signed-in user; this is the only place to change.
    static final String USER_ID = "demo-user";

    static final Path PROFILE_FILE = Path.of("data", "profiles.json");

    private ProfileModule() { }

    public static JPanel createPanel() {
        return createPanel(PROFILE_FILE, backgroundExecutor());
    }

    // Composition seam: tests pass a temp file and a same-thread executor.
    static JPanel createPanel(Path profileFile, Executor executor) {
        // one DAO object serves both use cases, each through its own narrow interface
        JsonProfileDataAccessObject profiles = new JsonProfileDataAccessObject(profileFile);
        ProfileViewModel viewModel = new ProfileViewModel();

        ViewProfileInteractor viewProfile =
                new ViewProfileInteractor(profiles, new ViewProfilePresenter(viewModel));
        EditProfileInteractor editProfile = new EditProfileInteractor(
                profiles, new FileSystemAvatarFileChecker(), new EditProfilePresenter(viewModel));

        return new ProfileView(
                new ViewProfileController(viewProfile, USER_ID, executor),
                new EditProfileController(editProfile, USER_ID, executor),
                viewModel);
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
