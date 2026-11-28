package entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemorySessionManagerTest {
    @Test void loginAndLogoutManageCurrentUser() {
        SessionManager session = new InMemorySessionManager();
        User user = new User("id-1", "matthew", "hash");

        assertFalse(session.isLoggedIn());
        session.setCurrentUser(user);
        assertTrue(session.isLoggedIn());
        assertEquals("id-1", session.getCurrentUser().getId());

        session.logout();
        assertFalse(session.isLoggedIn());
        assertNull(session.getCurrentUser());
    }
}
