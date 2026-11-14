package App.application;

import App.domain.AuthRepository;
import App.domain.User;

import java.util.Optional;

public class LoginUseCase {
    private final AuthRepository repository;

    public LoginUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public Optional<User> execute(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }

        return repository.findByEmail(email)
                .filter(user -> user.passwordMatches(password));
    }
}
