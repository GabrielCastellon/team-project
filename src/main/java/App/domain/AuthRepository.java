package App.domain;

import java.util.Optional;

public interface AuthRepository {
    Optional<User> findByEmail(String email);

    void save(User user);
}
