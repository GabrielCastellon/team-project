package use_case.signup;

import entity.User;
import org.junit.jupiter.api.Test;
import use_case.security.PasswordHasher;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SignupInteractorTest {
    private static class FakeUsers implements SignupUserDataAccessInterface {
        final Map<String, User> users = new HashMap<>();
        public boolean existsByName(String name) {
            return users.keySet().stream().anyMatch(k -> k.equalsIgnoreCase(name));
        }
        public void save(User user) { users.put(user.getUsername(), user); }
    }

    private static class CapturingPresenter implements SignupOutputBoundary {
        SignupOutputData output;
        public void present(SignupOutputData outputData) { output = outputData; }
    }

    @Test void signupSuccessStoresHashedPassword() {
        FakeUsers dao = new FakeUsers();
        CapturingPresenter presenter = new CapturingPresenter();
        new SignupInteractor(dao, presenter, new PasswordHasher())
                .execute(new SignupInputData("matthew", "secret", "secret"));

        assertTrue(presenter.output.isSuccess());
        assertEquals(1, dao.users.size());
        User saved = dao.users.get("matthew");
        assertNotEquals("secret", saved.getPasswordHash());
        assertTrue(new PasswordHasher().matches("secret", saved.getPasswordHash()));
        assertFalse(saved.getId().isEmpty());
    }

    @Test void duplicateUsernameIsRejected() {
        FakeUsers dao = new FakeUsers();
        dao.users.put("Matthew", new User("id-1", "Matthew", "hash"));
        CapturingPresenter presenter = new CapturingPresenter();

        new SignupInteractor(dao, presenter, new PasswordHasher())
                .execute(new SignupInputData("matthew", "secret", "secret"));

        assertFalse(presenter.output.isSuccess());
        assertEquals(1, dao.users.size());
    }

    @Test void passwordMismatchIsRejected() {
        FakeUsers dao = new FakeUsers();
        CapturingPresenter presenter = new CapturingPresenter();

        new SignupInteractor(dao, presenter, new PasswordHasher())
                .execute(new SignupInputData("matthew", "secret", "different"));

        assertFalse(presenter.output.isSuccess());
        assertTrue(dao.users.isEmpty());
    }

    @Test void emptyUsernameIsRejected() {
        FakeUsers dao = new FakeUsers();
        CapturingPresenter presenter = new CapturingPresenter();

        new SignupInteractor(dao, presenter, new PasswordHasher())
                .execute(new SignupInputData("  ", "secret", "secret"));

        assertFalse(presenter.output.isSuccess());
        assertTrue(dao.users.isEmpty());
    }
}
