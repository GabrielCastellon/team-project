package entity;

/** Simple in-memory session implementation; the session is not persisted. */
public final class InMemorySessionManager implements SessionManager {
    private User currentUser;

    @Override public synchronized User getCurrentUser() { return currentUser; }

    @Override public synchronized void setCurrentUser(User user) {
        if (user == null) throw new IllegalArgumentException("user cannot be null");
        currentUser = user;
    }

    @Override public synchronized boolean isLoggedIn() { return currentUser != null; }

    @Override public synchronized void logout() { currentUser = null; }
}
