package App;

import App.presentation.AuthFrame;
import data_access.JsonStorage;
import data_access.JsonUserDataAccessObject;
import entity.InMemorySessionManager;
import entity.SessionManager;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginViewModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupPresenter;
import interface_adapter.signup.SignupViewModel;
import use_case.login.LoginInteractor;
import use_case.security.PasswordHasher;
import use_case.signup.SignupInteractor;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Paths;

/** Application composition root: creates dependencies and launches Photofy. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            launchApplication();
        });
    }

    private static void launchApplication() {
        JsonStorage jsonStorage = new JsonStorage();
        JsonUserDataAccessObject users = new JsonUserDataAccessObject(
                jsonStorage, Paths.get("data", "users.json"));

        SessionManager sessionManager = new InMemorySessionManager();
        PasswordHasher passwordHasher = new PasswordHasher();

        LoginViewModel loginViewModel = new LoginViewModel();
        LoginPresenter loginPresenter =
                new LoginPresenter(loginViewModel, sessionManager);
        LoginInteractor loginInteractor =
                new LoginInteractor(users, loginPresenter, passwordHasher);
        LoginController loginController = new LoginController(loginInteractor);

        SignupViewModel signupViewModel = new SignupViewModel();
        SignupPresenter signupPresenter = new SignupPresenter(signupViewModel);
        SignupInteractor signupInteractor =
                new SignupInteractor(users, signupPresenter, passwordHasher);
        SignupController signupController =
                new SignupController(signupInteractor);

        AuthFrame frame = new AuthFrame(
                loginController,
                signupController,
                loginViewModel,
                signupViewModel,
                sessionManager
        );
        frame.setVisible(true);
    }
}
