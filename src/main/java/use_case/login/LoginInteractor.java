package use_case.login;

import entity.User;
import use_case.security.PasswordHasher;

public final class LoginInteractor implements LoginInputBoundary {
    private final LoginUserDataAccessInterface userDataAccess;
    private final LoginOutputBoundary presenter;
    private final PasswordHasher passwordHasher;

    public LoginInteractor(LoginUserDataAccessInterface userDataAccess,
                           LoginOutputBoundary presenter,
                           PasswordHasher passwordHasher) {
        this.userDataAccess = userDataAccess;
        this.presenter = presenter;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void execute(LoginInputData inputData) {
        String username = inputData == null || inputData.getUsername() == null
                ? "" : inputData.getUsername().trim();
        String password = inputData == null ? null : inputData.getPassword();

        if (username.isEmpty() || password == null || password.isEmpty()
                || !userDataAccess.existsByName(username)) {
            presenter.present(new LoginOutputData(false, null,
                    "Invalid username or password."));
            return;
        }

        User user = userDataAccess.get(username);
        if (user == null || !passwordHasher.matches(password, user.getPasswordHash())) {
            presenter.present(new LoginOutputData(false, null,
                    "Invalid username or password."));
            return;
        }

        presenter.present(new LoginOutputData(true, user, null));
    }
}
