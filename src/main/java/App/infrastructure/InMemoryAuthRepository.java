package App.infrastructure;

import App.domain.AuthRepository;
import App.domain.User;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class InMemoryAuthRepository implements AuthRepository {
    private final Map<String, User> users = new HashMap<>();

    public InMemoryAuthRepository() {
        // Demo account from the original prototype.
        save(new User("DemoUser", "demo@example.com", "Pass1234"));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                users.get(email.trim().toLowerCase(Locale.ROOT))
        );
    }

    @Override
    public void save(User user) {
        users.put(
                user.getEmail().trim().toLowerCase(Locale.ROOT),
                user
        );
    }
}
