package App.application;

import App.domain.AuthRepository;

public class RecoverAccountUseCase {
    private final AuthRepository repository;

    public RecoverAccountUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    // This only checks whether the account exists. It does not send email
    // or change a password.
    public boolean execute(String email) {
        if (email == null ||
                !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return false;
        }

        return repository.findByEmail(email.trim()).isPresent();
    }
}
