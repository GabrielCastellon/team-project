
package app;

import app.presentation.AuthPanel;
import data_access.JsonStorage;
import data_access.JsonUserDataAccessObject;
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

import java.nio.file.Paths;

public final class AuthenticationModule {

    private AuthenticationModule() {
    }

    public static AuthPanel createPanel(SessionManager sessionManager) {

        JsonStorage jsonStorage = new JsonStorage();

        JsonUserDataAccessObject users =
                new JsonUserDataAccessObject(
                        jsonStorage,
                        Paths.get("data", "users.json")
                );

        PasswordHasher passwordHasher = new PasswordHasher();

        LoginViewModel loginViewModel = new LoginViewModel();

        LoginPresenter loginPresenter =
                new LoginPresenter(loginViewModel, sessionManager);

        LoginInteractor loginInteractor =
                new LoginInteractor(users, loginPresenter, passwordHasher);

        LoginController loginController =
                new LoginController(loginInteractor);

        SignupViewModel signupViewModel = new SignupViewModel();

        SignupPresenter signupPresenter =
                new SignupPresenter(signupViewModel);

        SignupInteractor signupInteractor =
                new SignupInteractor(users, signupPresenter, passwordHasher);

        SignupController signupController =
                new SignupController(signupInteractor);

        return new AuthPanel(
                loginController,
                signupController,
                loginViewModel,
                signupViewModel,
                sessionManager
        );
    }
}

