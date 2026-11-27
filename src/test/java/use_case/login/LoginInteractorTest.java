package use_case.login;

import entity.User;
import org.junit.jupiter.api.Test;
import use_case.security.PasswordHasher;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoginInteractorTest {
    private static class FakeUsers implements LoginUserDataAccessInterface {
        final Map<String, User> users = new HashMap<>();
        public boolean existsByName(String name) {
            return users.keySet().stream().anyMatch(k -> k.equalsIgnoreCase(name));
        }
        public User get(String name) {
            return users.values().stream()
                    .filter(u -> u.getUsername().equalsIgnoreCase(name))
                    .findFirst().orElse(null);
        }
    }

    private static class CapturingPresenter implements LoginOutputBoundary {
        LoginOutputData output;
        public void present(LoginOutputData outputData) { output = outputData; }
    }

    @Test void correctCredentialsSucceed() {
        PasswordHasher hasher = new PasswordHasher();
        FakeUsers dao = new FakeUsers();
        User user = new User("id-1", "matthew", hasher.hash("secret"));
        dao.users.put(user.getUsername(), user);
        CapturingPresenter presenter = new CapturingPresenter();

        new LoginInteractor(dao, presenter, hasher)
                .execute(new LoginInputData("MATTHEW", "secret"));

        assertTrue(presenter.output.isSuccess());
        assertEquals("id-1", presenter.output.getUser().getId());
    }

    @Test void wrongPasswordFails() {
        PasswordHasher hasher = new PasswordHasher();
        FakeUsers dao = new FakeUsers();
        dao.users.put("matthew", new User("id-1", "matthew", hasher.hash("secret")));
        CapturingPresenter presenter = new CapturingPresenter();

        new LoginInteractor(dao, presenter, hasher)
                .execute(new LoginInputData("matthew", "wrong"));

        assertFalse(presenter.output.isSuccess());
        assertNull(presenter.output.getUser());
    }

    @Test void unknownUsernameFails() {
        FakeUsers dao = new FakeUsers();
        CapturingPresenter presenter = new CapturingPresenter();

        new LoginInteractor(dao, presenter, new PasswordHasher())
                .execute(new LoginInputData("unknown", "secret"));

        assertFalse(presenter.output.isSuccess());
    }
}
