package App.application;

import App.domain.AuthRepository;
import App.domain.User;

public class RegisterUseCase {
    public enum Result {
        SUCCESS,
        INVALID_USERNAME,
        INVALID_EMAIL,
        INVALID_PASSWORD,
        EMAIL_ALREADY_EXISTS
    }

    private final AuthRepository repository;

    public RegisterUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public Result execute(
            String username,
            String email,
            String password,
            String confirmation
    ) {
        if (username == null || username.trim().length() < 3) {
            return Result.INVALID_USERNAME;
        }

        if (email == null ||
                !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return Result.INVALID_EMAIL;
        }

        if (password == null || password.length() < 8) {
            return Result.INVALID_PASSWORD;
        }

        if (!password.equals(confirmation)) {
            return Result.INVALID_PASSWORD;
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (repository.findByEmail(normalizedEmail).isPresent()) {
            return Result.EMAIL_ALREADY_EXISTS;
        }

        repository.save(new User(username.trim(), normalizedEmail, password));
        return Result.SUCCESS;
    }
}
