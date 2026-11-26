package use_case.signup;

import entity.User;
import use_case.security.PasswordHasher;

import java.util.UUID;

public final class SignupInteractor implements SignupInputBoundary {
    private final SignupUserDataAccessInterface userDataAccess;
    private final SignupOutputBoundary presenter;
    private final PasswordHasher passwordHasher;

    public SignupInteractor(SignupUserDataAccessInterface userDataAccess,
                            SignupOutputBoundary presenter,
                            PasswordHasher passwordHasher) {
        this.userDataAccess = userDataAccess;
        this.presenter = presenter;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void execute(SignupInputData inputData) {
        String username = inputData == null || inputData.getUsername() == null
                ? "" : inputData.getUsername().trim();
        String password = inputData == null ? null : inputData.getPassword();
        String repeat = inputData == null ? null : inputData.getRepeatPassword();

        if (username.isEmpty()) {
            presenter.present(new SignupOutputData(false, username,
                    "Username cannot be empty."));
            return;
        }
        if (password == null || password.isEmpty()) {
            presenter.present(new SignupOutputData(false, username,
                    "Password cannot be empty."));
            return;
        }
        if (!password.equals(repeat)) {
            presenter.present(new SignupOutputData(false, username,
                    "Passwords do not match."));
            return;
        }
        if (userDataAccess.existsByName(username)) {
            presenter.present(new SignupOutputData(false, username,
                    "That username is already taken."));
            return;
        }

        User user = new User(UUID.randomUUID().toString(), username,
                passwordHasher.hash(password));
        userDataAccess.save(user);
        presenter.present(new SignupOutputData(true, username, null));
    }
}
